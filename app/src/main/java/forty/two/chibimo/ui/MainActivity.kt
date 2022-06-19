package forty.two.chibimo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import forty.two.chibimo.R
import forty.two.chibimo.utils.*
import forty.two.chibimo.zeolite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Socket
import java.sql.DriverManager
import kotlin.concurrent.thread

/**
 * The default UI.
 * Here, songs can be selected from the media library
 * and either played directly or added to the queue.
 * Playback information & controls are also displayed here.
 */
class MainActivity: AppCompatActivity() {
	private var seekBarInUse = false
	private lateinit var seekBar: SeekBar
	private lateinit var txtPlaying: TextView
	private lateinit var txtTime: TextView

	companion object {
		init {
			System.loadLibrary("sodium")
			System.loadLibrary("chibimo")
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.toolbar, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
		R.id.action_settings -> {
			startActivity(Intent(this, SettingsActivity::class.java))
			true
		}
		R.id.action_resync -> {
			syncDBs()
			true
		}
		R.id.action_help -> {
			startActivity(Intent(this, HelpActivity::class.java))
			true
		}
		else -> true
	}

	/**
	 * Set the progress callback of a [forty.two.chibimo.media.PlayerService]
	 * so that we can display the current playback information too.
	 */
	private fun setPlayerCallbacks() {
		connectToPlayer {
			it.progressCallback = { position, duration ->
				runOnUiThread {
					txtPlaying.text = getString(R.string.playing, it.currentSong)
					if(!seekBarInUse) {
						seekBar.max = duration
						seekBar.setProgress(position, true)
					}
				}
			}
		}
	}

	/**
	 * Should long tapping a song play it?
	 */
	private fun longTapPlaySongs() =
		PreferenceManager.getDefaultSharedPreferences(this).getBoolean("longTapPlaysSongs", false)

	/**
	 * Should folder contents be added randomly?
	 */
	private fun addSongsRandomized() =
		PreferenceManager.getDefaultSharedPreferences(this).getBoolean("addSongsRandomized", false)

	/**
	 * Play a song.
	 */
	private fun playSong(song: String) {
		connectToPlayer {
			it.play(song)
		}
		setPlayerCallbacks()
	}

	/**
	 * Add a song to the queue.
	 */
	private fun addSong(song: String) {
		connectToPlayer {
			it.safe { it.emo.add(song) }
			it.toastController.show(getString(R.string.added_song, song))
		}
	}

	/**
	 * Rebuild the song library view.
	 * This will take some time, so we show a "Loading..." text.
	 */
	private fun rebuildMediaTree() {
		val treeBox = findViewById<ScrollView>(R.id.treeBox)

		treeBox.removeAllViews()
		treeBox.addView(TextView(this).apply {
			text = getString(R.string.loading)
			textAlignment = TextView.TEXT_ALIGNMENT_CENTER
		})

		thread {
			val dir = getMusicDir(this)
			if(dir == null) {
				runOnUiThread {
					treeBox.removeAllViews()
					treeBox.addView(TextView(this).apply {
						text = context.getString(R.string.no_music_dir)
						textAlignment = TextView.TEXT_ALIGNMENT_CENTER
					})
				}
				return@thread
			}
			val mediaTree = MediaTree(this, dir, {
				if(longTapPlaySongs()) {
					addSong(it.getMediaPath())
				} else {
					playSong(it.getMediaPath())
				}
			}, {
				if(longTapPlaySongs()) {
					playSong(it.getMediaPath())
				} else {
					addSong(it.getMediaPath())
				}
			}, { all ->
				connectToPlayer { player ->
					all.map { it.getMediaPath() }.let {
						if(addSongsRandomized()) it.shuffled() else it
					}.forEach { song ->
						player.safe { player.emo.add(song) }
					}
					player.toastController.show(getString(R.string.added_songs, all.size))
				}
			})

			runOnUiThread {
				treeBox.removeAllViews()
				treeBox.addView(mediaTree.view)
			}
		}
	}

	/**
	 * Upload local changes, then download the new song database from the emo server.
	 */
	private fun syncDBs() {
		connectToPlayer {
			lifecycleScope.launch(Dispatchers.IO) {
				try {
					it.emoConnection.uploadChanges(it.db)
					val count = it.emo.setSongsDBFromRawSongs(it.emoConnection.getSongs())
					it.toastController.show(getString(R.string.imported_songs, count))
				} catch(e: Exception) {
					Log.e("Connection error", null, e)
					it.toastController.show(getString(R.string.emo_error, e.localizedMessage))
				}
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)

		DriverManager.registerDriver(org.sqldroid.SQLDroidDriver())

		lifecycleScope.launch(Dispatchers.IO) {
			val socket = Socket("192.168.178.20", 37812)
			val fd = ParcelFileDescriptor.fromSocket(socket).fd

			if(zeoliteInit() != success) throw java.lang.RuntimeException("Could not initialize zeolite")

			val zeolite = zeoliteCreate()
			val channel = zeoliteCreateChannel(zeolite, fd)

			zeoliteChannelSend(channel, "add foo\n")
			zeoliteChannelSend(channel, "add bar\n")
			zeoliteChannelSend(channel, "add baz\n")
			zeoliteChannelSend(channel, "queue\n")

			while(true) {
				val msg = zeoliteChannelRecv(channel)
				if(msg == null || msg == "end\n") break
				print(msg)
			}
		}

		with(PreferenceManager.getDefaultSharedPreferences(this)) {
			if(getBoolean(FIRST_RUN, true)) {
				startActivity(Intent(this@MainActivity, HelpActivity::class.java))
				edit().putBoolean(FIRST_RUN, false).apply()
			} else if(getBoolean(SYNC_ON_LAUNCH, false)) {
				syncDBs()
			}
		}

		fun setPlayPauseBtnIcon(playing: Boolean) {
			with(findViewById<Button>(R.id.btnPlayPause)) {
				setCompoundDrawablesRelativeWithIntrinsicBounds(
					if(playing) {
						R.drawable.ic_baseline_pause_24
					} else {
						R.drawable.ic_baseline_play_arrow_24
					}, 0, 0, 0
				)
				gravity
			}
		}

		seekBar = findViewById(R.id.seekBar)
		seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
			@SuppressLint("SetTextI18n")
			override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
				txtTime.text = "${millisToTimeString(progress)} / ${millisToTimeString(seekBar.max)}"
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {
				seekBarInUse = true
			}

			override fun onStopTrackingTouch(p0: SeekBar?) {
				seekBarInUse = false
				connectToPlayer { it.seekTo(seekBar.progress) }
			}

		})

		txtPlaying = findViewById(R.id.txtPlaying)
		txtTime = findViewById(R.id.txtTime)

		findViewById<Button>(R.id.btnPlayPause).setOnClickListener {
			connectToPlayer {
				setPlayPauseBtnIcon(it.playPause())
			}
		}

		findViewById<Button>(R.id.btnNext).setOnClickListener {
			connectToPlayer {
				it.getAndPlayNext()
				setPlayerCallbacks()
			}
		}

		findViewById<Button>(R.id.btnRepeat).setOnClickListener {
			connectToPlayer {
				if(it.currentSong.isBlank()) return@connectToPlayer
				it.safe { it.emo.add(it.currentSong) }
				it.toastController.show(getString(R.string.repeated_song, it.currentSong))
			}
		}

		findViewById<Button>(R.id.btnStop).setOnClickListener {
			connectToPlayer {
				it.hardStop()
				it.safe { it.emo.clear() }
			}

			seekBar.progress = 0
			seekBar.max = 1
			txtPlaying.text = ""
			txtTime.text = ""
		}

		rebuildMediaTree()
		setPlayerCallbacks()
	}
}