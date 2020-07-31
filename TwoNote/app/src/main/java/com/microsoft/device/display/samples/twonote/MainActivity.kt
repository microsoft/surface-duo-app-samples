/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.display.samples.twonote.includes.FileHandler
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import com.microsoft.device.dualscreen.core.ScreenHelper

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
            removeFragment(R.id.second_container_id)

            if (note is Note && inode is INode) {
                startNoteDetailFragment(R.id.first_container_id, note, inode)
            } else {
                startNoteListFragment(R.id.first_container_id)
            }
        } else {
            // Remove the single screen container fragment if it exists

            if (note is Note && inode is INode) {
                startNoteDetailFragment(R.id.second_container_id, note, inode)
            } else {
                startGetStartedFragment()
            }

            startNoteListFragment(R.id.first_container_id)
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
            .replace(R.id.second_container_id, GetStartedFragment(), null)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val fragDual = supportFragmentManager.findFragmentById(R.id.second_container_id)
        val fragSingle = supportFragmentManager.findFragmentById(R.id.first_container_id)

        // REVISIT: couldn't really think of a cleaner way to do this (because using
        // ScreenHelper.isDualMode didn't return the expected values)
        if (fragDual is NoteDetailFragment) {
            if (fragDual.deleted)
                outState.clear()
            else
                saveCurrentNote(outState, fragDual)
        } else if (fragDual is GetStartedFragment) {
            outState.clear()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        val fragment = supportFragmentManager.findFragmentByTag(DETAIL_FRAGMENT) as NoteDetailFragment
        val fileHandler = FileHandler(this, fragment.noteText, contentResolver)

        // request to save a file has been made, add data to newly created file
        if (requestCode == FileHandler.CREATE_FILE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                fileHandler.alterDocument(uri)
            }
        }
        // request to load file contents has been made, process the file's contents
        else if (requestCode == FileHandler.PICK_TXT_FILE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                fileHandler.processFileData(uri, null)
            }
        }
    }
}
