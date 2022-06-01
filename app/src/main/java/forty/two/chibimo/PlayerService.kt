package forty.two.chibimo

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import java.util.*

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
const val CHANNEL_ID = "chibimoPlayer"

class PlayerService: Service() {
	data class MyBinder(val playerService: PlayerService): Binder()

	private var isStarted = false
	private lateinit var mediaSession: MediaSessionCompat
	private lateinit var mediaStyle: MediaStyle
	private lateinit var builder: NotificationCompat.Builder

	private val player = StatefulMediaPlayer().apply {
		setAudioAttributes(
			AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				.setUsage(AudioAttributes.USAGE_MEDIA).build()
		)
		setOnCompletionListener {
			Toast.makeText(this@PlayerService, "completed $currentSong", Toast.LENGTH_SHORT).show()
			release()
		}
	}
	private lateinit var progressTask: TimerTask
	var progressCallback: ((Int, Int) -> Unit)? = null

	var currentSong = ""
		private set

	private var position: Int = 0
	private var duration: Int = 0

	override fun onBind(p0: Intent?): IBinder {
		return MyBinder(this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if(!isStarted) {
			startForeground(42, createNotification())
			isStarted = true
		}
		return START_NOT_STICKY
	}

	override fun onCreate() {
		getSystemService(NotificationManager::class.java)
			.createNotificationChannel(
				NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
			)
		super.onCreate()
	}

	fun play(song: String) {
		stop()
		val file = getMusicDir(this)?.child(song) ?: return

		try {
			with(player) {
				reset()
				setDataSource(this@PlayerService, file.uri)
				prepare()
				start()
			}

			currentSong = song
			duration = player.duration

			mediaSession.setMetadata(
				MediaMetadataCompat.Builder()
					.putString(MediaMetadata.METADATA_KEY_TITLE, currentSong)
					.putLong(MediaMetadata.METADATA_KEY_DURATION, duration.toLong())
					.build()
			)

			progressTask = object: TimerTask() {
				override fun run() {
					try {
						position = player.currentPosition
						updateProgress()
					} catch(e: Exception) {
						e.printStackTrace()
						hardStop()
					}
				}
			}
			Timer().scheduleAtFixedRate(progressTask, 0, 500)
		} catch(e: Exception) {
			Toast.makeText(this, getString(R.string.play_error, e.localizedMessage), Toast.LENGTH_LONG).show()
		}
	}

	fun playPause(): Boolean {
		if(player.state == MediaPlayerState.Started) {
			player.pause()
		} else if(player.state == MediaPlayerState.Paused) {
			player.start()
		}
		return player.isPlaying
	}

	fun seekTo(position: Int) {
		if(player.state == MediaPlayerState.Started || player.state == MediaPlayerState.Paused)
			player.seekTo(position)
	}

	private fun stop() {
		if(::progressTask.isInitialized && currentSong.isNotEmpty()) {
			progressTask.cancel()
		}
		currentSong = ""
	}

	fun hardStop() {
		stop()
		player.release()
		stopSelf()
	}

	private fun createNotification(): Notification {
		val intent = PendingIntent.getActivity(
			this, 42,
			Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
		)

		mediaSession = MediaSessionCompat(this, "PlayerService")
		mediaStyle = MediaStyle().setMediaSession(mediaSession.sessionToken)
		builder = NotificationCompat.Builder(this, CHANNEL_ID)
			.setStyle(mediaStyle)
			.setContentIntent(intent)
			.setSmallIcon(R.drawable.ic_baseline_library_music_24)
			.setOnlyAlertOnce(true)
			.setOngoing(true)
		return builder.build()
	}

	private fun updateProgress() {
		progressCallback?.let { it(position, duration) }
		mediaSession.setPlaybackState(
			PlaybackStateCompat.Builder()
				.setState(
					PlaybackState.STATE_PLAYING,
					position.toLong(),
					1f
				)
				.setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_SEEK_TO)
				.addCustomAction(
					PlaybackStateCompat.CustomAction.Builder("foo", "foo name", R.drawable.ic_baseline_pause_24).run {
						println("foo called")
						build()
					})
				.build()
		)

		with(NotificationManagerCompat.from(this)) {
			notify(
				42,
				builder
					.setProgress(duration, position, false)
					.build()
			)
		}
	}
}