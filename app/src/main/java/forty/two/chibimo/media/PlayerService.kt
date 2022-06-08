package forty.two.chibimo.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.preference.PreferenceManager
import forty.two.chibimo.R
import forty.two.chibimo.child
import forty.two.chibimo.net.EmoConnection
import forty.two.chibimo.net.EmoMsg
import forty.two.chibimo.ui.MainActivity
import forty.two.chibimo.ui.ToastController
import forty.two.chibimo.ui.getMusicDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
const val CHANNEL_ID = "chibimoPlayer"

class PlayerService: LifecycleService() {
	data class MyBinder(val playerService: PlayerService): Binder()

	private val toastController = ToastController(this)
	private val channel = Channel<EmoMsg>()

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
			getAndPlayNext()
		}
	}
	private lateinit var progressTask: TimerTask
	var progressCallback: ((Int, Int) -> Unit)? = null

	var currentSong = ""
		private set

	private var position: Int = 0
	private var duration: Int = 0

	fun withEmo(callback: suspend CoroutineScope.(Channel<EmoMsg>) -> Unit) {
		lifecycleScope.launch(Dispatchers.IO) {
			callback(channel)
		}
	}

	private fun alertConnecting() {
		toastController.show(R.string.connecting, Toast.LENGTH_LONG)
	}

	private fun alertConnected() {
		toastController.show(getString(R.string.connected), Toast.LENGTH_LONG)
	}

	private fun alertConnectionError(error: String) {
		toastController.show(getString(R.string.connection_error, error), Toast.LENGTH_LONG)
	}

	override fun onBind(intent: Intent): IBinder {
		super.onBind(intent)
		return MyBinder(this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		super.onStartCommand(intent, flags, startId)
		if(!isStarted) {
			startForeground(42, createNotification())
			isStarted = true

			val address = PreferenceManager.getDefaultSharedPreferences(this).getString("emoURL", "")
			if(address.isNullOrBlank()) {
				alertConnectionError(getString(R.string.not_set))
			} else {
				alertConnecting()

				withEmo {
					EmoConnection(channel, address, {
						alertConnected()
					}, {
						alertConnectionError("$address (${it.localizedMessage})")
					}).start()
				}
			}
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

	private fun complete(song: String, completion: Int) {
		Log.d("player", "completed $song at $completion")
		if(completion >= 80) withEmo { it.send(EmoMsg.Complete(song)) }
	}

	fun getAndPlayNext() {
		withEmo {
			it.send(EmoMsg.GetNext)
			play((it.receive() as EmoMsg.RespNext).next)
		}
	}

	fun play(song: String) {
		stop()
		val file = getMusicDir(this)?.child(song) ?: return

		try {
			with(player) {
				println(player.state)
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
						Log.e("PlayerService", "progress timer", e)
					}
				}
			}
			Timer().scheduleAtFixedRate(progressTask, 0, 500)
		} catch(e: Exception) {
			Log.e("PlayerService", "play()", e)
			toastController.show(getString(R.string.play_error, e.localizedMessage), Toast.LENGTH_LONG)
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
		if(currentSong.isNotEmpty()) {
			complete(currentSong, position * 100 / duration)

			if(::progressTask.isInitialized) {
				progressTask.cancel()
			}
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
					PlaybackStateCompat.STATE_PLAYING,
					position.toLong(),
					1f
				)
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