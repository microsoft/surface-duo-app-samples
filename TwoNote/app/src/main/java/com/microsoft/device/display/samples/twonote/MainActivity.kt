/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.layout.ScreenHelper

class MainActivity : AppCompatActivity(), NoteDetailFragment.OnFragmentInteractionListener {
    companion object {
        const val LIST_FRAGMENT = "list fragment"
        const val DETAIL_FRAGMENT = "detail fragment"
        const val NOTE = "note"
        const val INODE = "inode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get data from previously selected note (if available)
        val note = savedInstanceState?.getSerializable(NOTE)
        val inode = savedInstanceState?.getSerializable(INODE)

        if (!ScreenHelper.isDualMode(this)) {
            // Remove the dual screen container fragments if they exist
            removeFragment(R.id.dual_screen_start_container_id)
            removeFragment(R.id.dual_screen_end_container_id)

            if (note is Note && inode is INode) {
                startNoteDetailFragment(R.id.single_screen_container_id, note, inode)
            } else {
                startNoteListFragment(R.id.single_screen_container_id)
            }
        } else {
            // Remove the single screen container fragment if it exists
            removeFragment(R.id.single_screen_container_id)

            if (note is Note && inode is INode) {
                startNoteDetailFragment(R.id.dual_screen_end_container_id, note, inode)
            } else {
                startGetStartedFragment()
            }

            startNoteListFragment(R.id.dual_screen_start_container_id)
        }
    }

    private fun removeFragment(containerId: Int) {
        supportFragmentManager.findFragmentById(containerId)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun startNoteListFragment(container: Int) {
        supportFragmentManager.beginTransaction()
            .replace(container, NoteListFragment(), LIST_FRAGMENT)
            .commit()
    }

    private fun startNoteDetailFragment(container: Int, note: Note, inode: INode) {
        supportFragmentManager.beginTransaction()
            .replace(container, NoteDetailFragment.newInstance(inode, note), DETAIL_FRAGMENT)
            .commit()
    }

    private fun startGetStartedFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.dual_screen_end_container_id, GetStartedFragment(), null)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val fragDual = supportFragmentManager.findFragmentById(R.id.dual_screen_end_container_id)
        val fragSingle = supportFragmentManager.findFragmentById(R.id.single_screen_container_id)

        // REVISIT: couldn't really think of a cleaner way to do this (because using
        // ScreenHelper.isDualMode didn't return the expected values)
        if (fragDual is NoteDetailFragment) {
            if (fragDual.deleted)
                outState.clear()
            else
                saveCurrentNote(outState, fragDual)
        } else if (fragSingle is NoteDetailFragment) {
            if (fragSingle.deleted)
                outState.clear()
            else
                saveCurrentNote(outState, fragSingle)
        }
    }

    private fun saveCurrentNote(outState: Bundle, frag: NoteDetailFragment) {
        outState.putSerializable(NOTE, frag.arguments?.getSerializable(NOTE))
        outState.putSerializable(INODE, frag.arguments?.getSerializable(INODE))
    }

    /**
     * Send data from NoteFragment to NoteListFragment after note contents have been edited
     *
     * @param title: updated note/inode title
     */
    override fun onINodeUpdate(inode: INode, title: String) {
        (supportFragmentManager.findFragmentByTag(LIST_FRAGMENT) as NoteListFragment).updateINode(inode, title)
    }
}
