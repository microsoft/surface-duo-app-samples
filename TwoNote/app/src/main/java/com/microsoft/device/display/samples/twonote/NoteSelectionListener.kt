package com.microsoft.device.display.samples.twonote

import android.content.Context
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import com.microsoft.device.display.samples.twonote.model.INode
import java.io.File

class NoteSelectionListener(private var host: NoteListFragment, private var listView: ListView,
                            private var context: Context, private var arrayAdapter: ArrayAdapter<INode>?) : AbsListView.MultiChoiceModeListener {

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                val fileHandler = FileHandler()
                arrayAdapter?.let {
                    val list = listView.checkedItemPositions
                    for (i in it.count - 1 downTo 0) {
                        if (list.get(i)) {
                            it.getItem(i)?.let {inode ->
                                fileHandler.delete(context, "", inode)
                            }
                        }
                    }
                    arrayAdapter?.notifyDataSetChanged()
                }
                onDestroyActionMode(mode)
            }
        }
        return true
    }

    override fun onItemCheckedStateChanged(mode: ActionMode, pos: Int, id: Long, checked: Boolean) {
        updateTitle(mode)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // TODO: once alpha02 is released, we can create our own custom DuoActionBar (like DuoTabLayout) so it only appears on the left screen
        mode.menuInflater.inflate(R.menu.menu_note_selection, menu)
        updateTitle(mode)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
    }

    private fun updateTitle(mode: ActionMode) {
        mode.title = "${listView.checkedItemCount} ${host.getString(R.string.selected)}"
    }
}