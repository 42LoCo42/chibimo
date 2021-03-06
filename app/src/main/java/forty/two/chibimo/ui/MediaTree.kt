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

/**
 * A tree view of music files in a song library.
 */
class MediaTree(
	context: Context,
	dir: DocumentFile,
	private val onNodeClick: (MediaTreeNode) -> Unit,
	private val onNodeLongClick: (MediaTreeNode) -> Unit,
	private val onDirLongClick: (List<MediaTreeNode>) -> Unit,
): AndroidTreeView(context) {
	/**
	 * Recursively add the items of [dir] to [node].
	 * They will have the specified [indent].
	 */
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
				if(node is MediaTreeNode) onDirLongClick(node.getAllChildren())
			} else {
				if(node is MediaTreeNode) onNodeLongClick(node)
			}
			true
		}
	}
}

/**
 * A single view in the media tree.
 */
data class MediaTreeNode(val value: MediaTreeItem): TreeNode(value) {
	/**
	 * Get the full path to the contained song.
	 */
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

	/**
	 * If this node is a folder, get all its children.
	 * Otherwise, just return the node itself.
	 */
	fun getAllChildren(): List<MediaTreeNode> =
		if(value.canExpand) {
			children.map { (it as MediaTreeNode).getAllChildren() }.flatten()
		} else {
			listOf(this)
		}
}

/**
 * Data stored in a [MediaTreeNode].
 * Every node has a [title], an [indent] level
 * and a file/directory switch ([canExpand])
 */
data class MediaTreeItem(
	val title: String,
	val indent: Int,
	val canExpand: Boolean,
)

/**
 * The holder class for views inside of media tree nodes.
 */
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