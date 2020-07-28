package com.microsoft.device.display.samples.twonote

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.dualscreen.layout.ScreenHelper

class NoteSelectionListener(
    private var host: NoteListFragment,
    private var listView: ListView,
    private var arrayAdapter: ArrayAdapter<INode>
) : AbsListView.MultiChoiceModeListener {

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                val list = listView.checkedItemPositions
                for (i in arrayAdapter.count - 1 downTo 0) {
                    if (list.get(i)) {
                        arrayAdapter.getItem(i)?.let { inode ->
                            host.context?.let { cntx ->
                                FileHandler.delete(cntx, DataProvider.getActiveSubDirectory(), inode)
                            }
                        }
                    }
                }
                arrayAdapter.notifyDataSetChanged()
                host.exitDetailFragment(true)
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