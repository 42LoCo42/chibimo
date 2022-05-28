package forty.two.chibimo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.toolbar))

		data class TreeNodeValue(
			val title: String,
			val indent: Int,
		)

		class MyHolder(context: Context): TreeNode.BaseNodeViewHolder<TreeNodeValue>(context) {
			@SuppressLint("SetTextI18n", "InflateParams")
			override fun createNodeView(node: TreeNode?, value: TreeNodeValue): View =
				LayoutInflater.from(context).inflate(R.layout.tree_node, null).apply {
					findViewById<TextView>(R.id.node_title).text = "    ".repeat(value.indent) + value.title
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
			Toast.makeText(this, (value as TreeNodeValue).title, Toast.LENGTH_SHORT).show()
		}

		treeBox.addView(treeView.view)
	}
}