package forty.two.chibimo

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
class PlayerService: Service() {
	data class MyBinder(val playerService: PlayerService): Binder()

	private var isStarted = false
	private var channelID = "chibimo player"
	private lateinit var manager: NotificationManager
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
			startForeground(1, createNotification())
			isStarted = true
		}
		return START_NOT_STICKY
	}

	override fun onCreate() {
		manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(
			NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_DEFAULT)
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
			Timer().scheduleAtFixedRate(progressTask, 0, 250)
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
			this, 0,
			Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
		)

		builder = NotificationCompat.Builder(this, channelID)
			.setContentIntent(intent)
			.setSmallIcon(R.drawable.ic_baseline_library_music_24)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setOnlyAlertOnce(true)
		return builder.build()
	}

	private fun updateProgress() {
		progressCallback?.let { it(position, duration) }

		manager.notify(
			1,
			builder
				.setContentTitle("Playing $currentSong")
				.setProgress(duration, position, false)
				.build()
		)
	}
}