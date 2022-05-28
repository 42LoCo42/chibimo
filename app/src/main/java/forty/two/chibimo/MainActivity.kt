package forty.two.chibimo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView

class MainActivity: AppCompatActivity() {
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

	fun DocumentFile.child(path: String): DocumentFile? {
		var current = this
		for(component in path.split("/")) {
			current = current.findFile(component) ?: return null
		}
		return current
	}

	private fun getMusicDir(): Uri = Uri.parse(
		PreferenceManager.getDefaultSharedPreferences(this).getString(MUSIC_DIRECTORY, "invalid")
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.toolbar))

		val perm = Manifest.permission.READ_EXTERNAL_STORAGE
		if(checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf(perm), 37812)
		}

		data class TreeNodeValue(
			val title: String,
			val indent: Int,
		)

		class MyHolder(context: Context): TreeNode.BaseNodeViewHolder<TreeNodeValue>(context) {
			val dp = context.resources.displayMetrics.density.toInt()

			@SuppressLint("SetTextI18n", "InflateParams")
			override fun createNodeView(node: TreeNode?, value: TreeNodeValue): View =
				LayoutInflater.from(context).inflate(R.layout.tree_node, null).apply {
					findViewById<TextView>(R.id.node_title).apply {
						text = value.title
						setPaddingRelative(value.indent * 32 * dp, 8 * dp, 0, 8 * dp)
					}
				}
		}

		val treeBox = findViewById<ScrollView>(R.id.treeBox)

		val tree = TreeNode.root()
		val parent = TreeNode(TreeNodeValue("parent", 0))
		val child0 = TreeNode(TreeNodeValue("child 0", 1))
		val child1 = TreeNode(TreeNodeValue("child 1", 1))
		parent.addChildren(child0, child1)
		tree.addChildren(parent)
		val treeView = AndroidTreeView(this, tree)

		treeView.setDefaultViewHolder(MyHolder::class.java)
		treeView.setDefaultNodeClickListener { node, value ->
			println(node)
			println(value)
			node.viewHolder.view.findViewById<TextView>(R.id.node_title)
				.setCompoundDrawablesRelativeWithIntrinsicBounds(
					if(node.isExpanded) {
						R.drawable.ic_baseline_keyboard_arrow_right_24
					} else {
						R.drawable.ic_baseline_keyboard_arrow_down_24
					}, 0, 0, 0
				)
		}

		treeBox.addView(treeView.view)

		findViewById<Button>(R.id.btnTest).setOnClickListener {
			val dir = DocumentFile.fromTreeUri(this, getMusicDir()) ?: return@setOnClickListener
			val file = dir.child("inabakumori/14 - Lagtrain.opus") ?: return@setOnClickListener

			MediaPlayer().apply {
				setAudioAttributes(
					AudioAttributes.Builder()
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
						.setUsage(AudioAttributes.USAGE_MEDIA)
						.build()
				)
				setDataSource(this@MainActivity, file.uri)
				prepare()
				start()
			}
		}
	}
}