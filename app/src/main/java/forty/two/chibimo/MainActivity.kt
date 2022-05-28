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
	}
}