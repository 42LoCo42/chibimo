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
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.media.app.NotificationCompat.MediaStyle
import forty.two.chibimo.R
import forty.two.chibimo.db.Changes
import forty.two.chibimo.db.Songs
import forty.two.chibimo.emo.Emo
import forty.two.chibimo.emo.EmoConnection
import forty.two.chibimo.ui.MainActivity
import forty.two.chibimo.ui.ToastController
import forty.two.chibimo.utils.child
import forty.two.chibimo.utils.getMusicDir
import forty.two.chibimo.utils.tryCreateTable
import org.ktorm.database.Database
import java.util.*

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
const val CHANNEL_ID = "chibimoPlayer"

/**
 * The central part of chibimo.
 * This service manages music playback and holds database & server connections.
 */
class PlayerService: LifecycleService() {
	/**
	 * Stub binder that just contains a reference to [PlayerService]
	 */
	data class MyBinder(val playerService: PlayerService): Binder()

	val toastController = ToastController(this)
	val db: Database by lazy {
		Database.connect("jdbc:sqlite:${getExternalFilesDir(null)}/songs.db").apply {
			tryCreateTable(Songs)
			tryCreateTable(Changes)
		}
	}
	val emo: Emo by lazy {
		Emo(db)
	}
	val emoConnection: EmoConnection by lazy {
		EmoConnection(this)
	}

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

	override fun onBind(intent: Intent): IBinder {
		super.onBind(intent)
		return MyBinder(this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		super.onStartCommand(intent, flags, startId)
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

	/**
	 * Run something inside a try-catch block that will display exceptions with a [Toast].
	 */
	fun safe(block: () -> Unit) {
		try {
			block()
		} catch(e: Exception) {
			Log.e("Connection error", null, e)
			toastController.show(getString(R.string.emo_error, e.localizedMessage))
		}
	}

	/**
	 * Finish the current song, possibly completing it.
	 */
	private fun finish(song: String, completion: Int) {
		Log.d("player", "completed $song at $completion")
		if(completion >= 80) safe { emo.complete(song) }
	}

	/**
	 * Play the next song returned from emo.
	 */
	fun getAndPlayNext() {
		safe { play(emo.next()) }
	}

	/**
	 * Play the specified song.
	 */
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

	/**
	 * Toggle song playback.
	 */
	fun playPause(): Boolean {
		if(player.state == MediaPlayerState.Started) {
			player.pause()
		} else if(player.state == MediaPlayerState.Paused) {
			player.start()
		}
		return player.isPlaying
	}

	/**
	 * Seek to a specified position in milliseconds of the current song.
	 */
	fun seekTo(position: Int) {
		if(player.state == MediaPlayerState.Started || player.state == MediaPlayerState.Paused)
			player.seekTo(position)
	}

	/**
	 * Stop the current song
	 */
	private fun stop() {
		if(currentSong.isNotEmpty()) {
			finish(currentSong, position * 100 / duration)

			if(::progressTask.isInitialized) {
				progressTask.cancel()
			}
		}
		currentSong = ""
	}

	/**
	 * Stop the entire service.
	 */
	fun hardStop() {
		stop()
		player.release()
		stopSelf()
	}

	/**
	 * Create the background service notification.
	 */
	private fun createNotification(): Notification {
		val intent = PendingIntent.getActivity(
			this, 42,
			Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
		)

		mediaSession = MediaSessionCompat(this, "PlayerService").apply {
			setCallback(object: MediaSessionCompat.Callback() {
				override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
					val event = mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
					if(event.action == KeyEvent.ACTION_UP) return true

					when(event.keyCode) {
						KeyEvent.KEYCODE_MEDIA_NEXT -> getAndPlayNext()
						KeyEvent.KEYCODE_MEDIA_PREVIOUS -> player.seekTo(0)
						KeyEvent.KEYCODE_MEDIA_PLAY ->
							if(player.state == MediaPlayerState.Paused) player.start()
							else getAndPlayNext()
						KeyEvent.KEYCODE_MEDIA_PAUSE ->
							if(player.state == MediaPlayerState.Started) player.pause()
						else -> toastController.show("Unknown event $event")
					}
					return true
				}
			})
		}
		mediaStyle = MediaStyle().setMediaSession(mediaSession.sessionToken)
		builder = NotificationCompat.Builder(this, CHANNEL_ID)
			.setStyle(mediaStyle)
			.setContentIntent(intent)
			.setSmallIcon(R.drawable.ic_baseline_library_music_24)
			.setOnlyAlertOnce(true)
			.setOngoing(true)
		return builder.build()
	}

	/**
	 * Update the background service notification
	 * with the current playback progress.
	 */
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