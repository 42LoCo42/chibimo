package forty.two.chibimo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import kotlin.concurrent.thread

class MainActivity: AppCompatActivity() {
	val toastController = ToastController(this)

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

	private fun getMusicDir() = getMusicDirUri(this)?.let {
		DocumentFile.fromTreeUri(this, it)
	}

	private fun DocumentFile.child(path: String): DocumentFile? {
		var current = this
		for(component in path.split("/")) {
			current = current.findFile(component) ?: return null
		}
		return current
	}

	private fun rebuildTree() {
		val dir = getMusicDir() ?: return

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

			val treeView = AndroidTreeView(this, tree)
			treeView.setDefaultViewHolder(MyHolder::class.java)
			treeView.setDefaultNodeClickListener { node, rawValue ->
				val value = rawValue as TreeNodeValue
				if(value.canExpand) {
					node.viewHolder.view.findViewById<TextView>(R.id.node_title)
						.setCompoundDrawablesRelativeWithIntrinsicBounds(
							if(node.isExpanded) {
								R.drawable.ic_baseline_keyboard_arrow_right_24
							} else {
								R.drawable.ic_baseline_keyboard_arrow_down_24
							}, 0, 0, 0
						)
				} else {
					println(value)
				}
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

	private fun alertInvalidAddress(address: String) {
		toastController.show(getString(R.string.invalid_address, address), Toast.LENGTH_LONG)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.toolbar))

		val perm = Manifest.permission.READ_EXTERNAL_STORAGE
		if(checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf(perm), 37812)
		}

		findViewById<Button>(R.id.btnTest).setOnClickListener {
			val address = PreferenceManager.getDefaultSharedPreferences(this)
				.getString("emoURL", "")
			if(address.isNullOrBlank()) {
				alertInvalidAddress(getString(R.string.not_set))
				return@setOnClickListener
			}

			alertConnecting()
			thread {
				TcpClient(address) {
					runOnUiThread {
						alertInvalidAddress("$address (${it.localizedMessage})")
					}
				}.start()
			}
		}

		rebuildTree()
	}

	fun play(song: String) {
		val uri = getMusicDirUri(this)?.let {
			DocumentFile.fromTreeUri(this, it)?.child(song)?.uri
		} ?: return

		MediaPlayer().apply {
			setAudioAttributes(
				AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA).build()
			)
			setDataSource(this@MainActivity, uri)
			prepare()
			start()
			setOnCompletionListener {
				Toast.makeText(this@MainActivity, "completed $song", Toast.LENGTH_SHORT).show()
				release()
			}
		}
	}
}

data class TreeNodeValue(
	val title: String,
	val indent: Int,
	val canExpand: Boolean,
)

class MyHolder(context: Context): TreeNode.BaseNodeViewHolder<TreeNodeValue>(context) {
	private val dp = context.resources.displayMetrics.density.toInt()

	@SuppressLint("SetTextI18n", "InflateParams")
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