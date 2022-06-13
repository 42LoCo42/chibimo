package forty.two.chibimo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import forty.two.chibimo.*
import forty.two.chibimo.db.Changes
import forty.two.chibimo.db.Songs
import forty.two.chibimo.net.EmoMsg
import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import java.sql.DriverManager
import kotlin.concurrent.thread

class MainActivity: AppCompatActivity() {
	private var seekBarInUse = false
	private lateinit var seekBar: SeekBar
	private lateinit var txtPlaying: TextView
	private lateinit var txtTime: TextView

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.toolbar, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
		R.id.action_settings -> {
			startActivity(Intent(this, SettingsActivity::class.java))
			true
		}
		R.id.action_reconnect -> {
			connectToPlayer { it.connect() }
			true
		}
		else -> true
	}

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

	private fun longTapPlaySongs() =
		PreferenceManager.getDefaultSharedPreferences(this).getBoolean("longTapPlaysSongs", false)

	private fun addSongsRandomized() =
		PreferenceManager.getDefaultSharedPreferences(this).getBoolean("addSongsRandomized", false)

	private fun playSong(song: String) {
		connectToPlayer {
			it.play(song)
		}
		setPlayerCallbacks()
	}

	private fun addSong(song: String) {
		connectToEmo { it.send(EmoMsg.Add(song)) }
	}

	private fun rebuildMediaTree() {
		val treeBox = findViewById<ScrollView>(R.id.treeBox)

		treeBox.removeAllViews()
		treeBox.addView(TextView(this).apply {
			text = getString(R.string.loading)
			textAlignment = TextView.TEXT_ALIGNMENT_CENTER
		})

		thread {
			val dir = getMusicDir(this) ?: return@thread
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
				connectToEmo { emo ->
					all.map { it.getMediaPath() }.let {
						if(addSongsRandomized()) it.shuffled() else it
					}.forEach {
						emo.send(EmoMsg.Add(it))
					}
				}
			})

			runOnUiThread {
				treeBox.removeAllViews()
				treeBox.addView(mediaTree.view)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		DriverManager.registerDriver(org.sqldroid.SQLDroidDriver())

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
				it.withEmo { emo -> emo.send(EmoMsg.Repeat(it.currentSong)) }
			}
		}

		findViewById<Button>(R.id.btnStop).setOnClickListener {
			connectToPlayer {
				it.hardStop()
				it.withEmo { emo -> emo.send(EmoMsg.Clear) }
			}

			seekBar.progress = 0
			txtPlaying.text = ""
			txtTime.text = ""
		}

		connectToPlayer {
			fun updateToolbar(connected: Boolean) {
				runOnUiThread {
					if(connected) {
						toolbar.title = getString(R.string.app_name)
						toolbar.setBackgroundColor(resources.getColor(R.color.purple_200, theme))
					} else {
						toolbar.title = getString(R.string.app_name) + getString(R.string.offline)
						toolbar.setBackgroundColor(resources.getColor(R.color.design_default_color_error, theme))
					}
				}
			}

			it.onConnect = { updateToolbar(true) }
			it.onDisconnect = { updateToolbar(false) }
			updateToolbar(it.isConnected)
		}

		rebuildMediaTree()
		setPlayerCallbacks()

		thread {
			getExternalFilesDir(null)?.apply {
				val db = Database.connect("jdbc:sqlite:$absolutePath/songs.db")
				db.tryCreateTable(Songs)
				db.tryCreateTable(Changes)

				Log.e("main - songs", db.from(Songs).select().totalRecords.toString())
				Log.e("main - changes", db.from(Changes).select().totalRecords.toString())
			}
		}
	}
}