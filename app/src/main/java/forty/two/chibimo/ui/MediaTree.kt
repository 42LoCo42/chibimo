package forty.two.chibimo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import forty.two.chibimo.R

/**
 * @author: Leon Schumacher (Matrikelnummer 19101)
 */
class MediaTree(
	context: Context,
	dir: DocumentFile,
	private val onNodeClick: (MediaTreeNode) -> Unit,
	private val onNodeLongClick: (MediaTreeNode) -> Unit,
	private val onDirLongClick: (List<MediaTreeNode>) -> Unit,
): AndroidTreeView(context) {
	private fun addMediaItems(node: TreeNode, dir: DocumentFile, indent: Int) {
		dir.listFiles().sortedBy { it.name }.forEach {
			if(it == null) return@forEach
			val name = it.name ?: return@forEach
			if(name.startsWith(".")) return@forEach
			if(it.isFile) {
				node.addChild(MediaTreeNode(MediaTreeItem(name, indent, false)))
			} else {
				val sub = MediaTreeNode(MediaTreeItem(name, indent, true))
				node.addChild(sub)
				addMediaItems(sub, it, indent + 1)
			}
		}
	}

	private fun getAllChildren(node: MediaTreeNode): List<MediaTreeNode> =
		if(node.value.canExpand) {
			node.children.map { getAllChildren(it as MediaTreeNode) }.flatten()
		} else {
			listOf(node)
		}

	init {
		val root = TreeNode.root()
		super.mRoot = root
		addMediaItems(root, dir, 0)

		setDefaultViewHolder(MyHolder::class.java)

		setDefaultNodeClickListener { node, rawValue ->
			val value = rawValue as MediaTreeItem

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
				if(node is MediaTreeNode) onNodeClick(node)
			}
		}

		setDefaultNodeLongClickListener { node, rawValue ->
			val value = rawValue as MediaTreeItem

			if(value.canExpand) {
				if(node is MediaTreeNode) onDirLongClick(getAllChildren(node))
			} else {
				if(node is MediaTreeNode) onNodeLongClick(node)
			}
			true
		}
	}
}

data class MediaTreeNode(val value: MediaTreeItem): TreeNode(value) {
	fun getMediaPath(): String {
		// all nodes have the root node as parent, which has no parent
		// therefore we have to check if our grandparent is null to know
		// whether we have reached a top-level node
		return if(parent != null && parent.parent != null && parent is MediaTreeNode) {
			(parent as MediaTreeNode).getMediaPath() + "/"
		} else {
			""
		} + value.title
	}
}

data class MediaTreeItem(
	val title: String,
	val indent: Int,
	val canExpand: Boolean,
)

class MyHolder(context: Context): TreeNode.BaseNodeViewHolder<MediaTreeItem>(context) {
	private val dp = context.resources.displayMetrics.density.toInt()

	override fun createNodeView(node: TreeNode?, value: MediaTreeItem): View =
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