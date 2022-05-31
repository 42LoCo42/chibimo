package forty.two.chibimo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity: AppCompatActivity() {
	private val channel = Channel<EmoMsg>()
	private val toastController = ToastController(this)

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

	private fun playSong(song: String) {
		connectToPlayer {
			it.play(song)
		}
		setPlayerCallbacks()
	}

	private fun rebuildTree() {
		val dir = getMusicDir(this) ?: return

		val treeBox = findViewById<ScrollView>(R.id.treeBox)
		treeBox.removeAllViews()
		treeBox.addView(TextView(this).apply {
			text = getString(R.string.loading)
			textAlignment = TextView.TEXT_ALIGNMENT_CENTER
		})

		thread {
			fun addFiles(node: TreeNode, dir: DocumentFile, indent: Int) {
				dir.listFiles().sortedBy { it.name }.forEach {
					if(it == null) return@forEach
					val name = it.name ?: return@forEach
					if(it.isFile) {
						node.addChild(TreeNode(TreeNodeValue(name, indent, false)))
					} else {
						val sub = TreeNode(TreeNodeValue(name, indent, true))
						node.addChild(sub)
						addFiles(sub, it, indent + 1)
					}
				}
			}

			val tree = TreeNode.root()
			addFiles(tree, dir, 0)

			fun longTapPlaySongs() =
				PreferenceManager.getDefaultSharedPreferences(this).getBoolean("longTapPlaysSongs", false)

			fun addSongsRandomized() =
				PreferenceManager.getDefaultSharedPreferences(this).getBoolean("addSongsRandomized", false)

			fun treeGetFullPath(node: TreeNode): String {
				// all nodes have the root node as parent, which has no parent
				// therefore we have to check if our grandparent is null to know
				// whether we have reached a top-level node
				return if(node.parent != null && node.parent.parent != null) {
					treeGetFullPath(node.parent) + "/"
				} else {
					""
				} + (node.value as TreeNodeValue).title
			}

			fun treePlaySong(node: TreeNode) {
				playSong(treeGetFullPath(node))
			}

			fun treeAddDir(node: TreeNode) {
				fun collectSongs(node: TreeNode): List<String> =
					if((node.value as TreeNodeValue).canExpand) {
						node.children.map { collectSongs(it) }.flatten()
					} else {
						listOf(treeGetFullPath(node))
					}

				lifecycleScope.launch(Dispatchers.IO) {
					collectSongs(node).let {
						if(addSongsRandomized()) {
							it.shuffled()
						} else {
							it
						}.forEach { song ->
							channel.send(EmoMsg.Add(song))
						}
					}
				}
			}

			fun treeAddSong(node: TreeNode) {
				lifecycleScope.launch(Dispatchers.IO) {
					channel.send(EmoMsg.Add(treeGetFullPath(node)))
				}
			}

			val treeView = AndroidTreeView(this, tree)
			treeView.setDefaultViewHolder(MyHolder::class.java)
			treeView.setDefaultNodeClickListener { node, value ->
				if((value as TreeNodeValue).canExpand) {
					node.viewHolder.view.findViewById<TextView>(R.id.node_title)
						.setCompoundDrawablesRelativeWithIntrinsicBounds(
							if(node.isExpanded) {
								R.drawable.ic_baseline_keyboard_arrow_right_24
							} else {
								R.drawable.ic_baseline_keyboard_arrow_down_24
							}, 0, 0, 0
						)
				} else {
					if(longTapPlaySongs()) {
						treeAddSong(node)
					} else {
						treePlaySong(node)
					}
				}
			}
			treeView.setDefaultNodeLongClickListener { node, value ->
				if((value as TreeNodeValue).canExpand) {
					treeAddDir(node)
				} else {
					if(longTapPlaySongs()) {
						treePlaySong(node)
					} else {
						treeAddSong(node)
					}
				}
				true
			}

			runOnUiThread {
				treeBox.removeAllViews()
				treeBox.addView(treeView.view)
			}
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.toolbar))

		val perm = Manifest.permission.READ_EXTERNAL_STORAGE
		if(checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf(perm), 37812)
		}

		val address = PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getString("emoURL", "")
		if(address.isNullOrBlank()) {
			alertConnectionError(getString(R.string.not_set))
		} else {
			alertConnecting()
			lifecycleScope.launch(Dispatchers.IO) {
				EmoConnection(channel, address, {
					runOnUiThread {
						alertConnected()
					}
				}) {
					runOnUiThread {
						alertConnectionError("$address (${it.localizedMessage})")
					}
				}.start()
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
			lifecycleScope.launch {
				channel.send(EmoMsg.GetNext)
				playSong((channel.receive() as EmoMsg.RespNext).next)
			}
		}

		findViewById<Button>(R.id.btnRepeat).setOnClickListener {
			connectToPlayer {
				lifecycleScope.launch {
					channel.send(EmoMsg.Repeat(it.currentSong))
				}
			}
		}

		findViewById<Button>(R.id.btnStop).setOnClickListener {
			connectToPlayer { it.hardStop() }
			lifecycleScope.launch(Dispatchers.IO) {
				channel.send(EmoMsg.Clear)
			}
		}

		rebuildTree()
		setPlayerCallbacks()
	}
}

data class TreeNodeValue(
	val title: String,
	val indent: Int,
	val canExpand: Boolean,
)

class MyHolder(context: Context): TreeNode.BaseNodeViewHolder<TreeNodeValue>(context) {
	private val dp = context.resources.displayMetrics.density.toInt()

	override fun createNodeView(node: TreeNode?, value: TreeNodeValue): View =
		LayoutInflater.from(context).inflate(R.layout.tree_node, null).apply {
			findViewById<TextView>(R.id.node_title).apply {
				text = value.title
				setPaddingRelative(value.indent * 32 * dp, 8 * dp, 0, 8 * dp)
				setCompoundDrawablesRelativeWithIntrinsicBounds(
					if(value.canExpand) {
						R.drawable.ic_baseline_keyboard_arrow_right_24
					} else {
						0
					}, 0, 0, 0
				)
			}
		}
}