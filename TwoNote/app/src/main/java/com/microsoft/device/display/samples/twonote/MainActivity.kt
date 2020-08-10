/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import Defines.DETAIL_FRAGMENT
import Defines.INODE
import Defines.LIST_FRAGMENT
import Defines.NOTE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.display.samples.twonote.includes.DataProvider
import com.microsoft.device.display.samples.twonote.includes.FileHandler
import com.microsoft.device.display.samples.twonote.includes.FileSystem
import com.microsoft.device.display.samples.twonote.structures.DirEntry
import com.microsoft.device.display.samples.twonote.structures.INode
import com.microsoft.device.display.samples.twonote.structures.Note
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode

class MainActivity : AppCompatActivity(), NoteDetailFragment.OnFragmentInteractionListener {
    companion object {
        fun isRotated(context: Context): Boolean {
            return ScreenHelper.getCurrentRotation(context) == Surface.ROTATION_90 ||
                ScreenHelper.getCurrentRotation(context) == Surface.ROTATION_270
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get data from previously selected note (if available)
        val note = savedInstanceState?.getSerializable(NOTE) as? Note
        val inode = savedInstanceState?.getSerializable(INODE) as? INode
        val noteSelected = note != null && inode != null

        when ((application as TwoNote).surfaceDuoScreenManager.screenMode) {
            ScreenMode.SINGLE_SCREEN -> {
                // Remove fragment from second container if it exists
                removeFragment(R.id.second_container_id)

                if (noteSelected) {
                    startNoteDetailFragment(R.id.first_container_id, note!!, inode!!)
                } else {
                    startNoteListFragment(R.id.first_container_id)
                }
            }
            ScreenMode.DUAL_SCREEN -> {
                if (isRotated(applicationContext)) {
                    // Remove fragment from second container if it exists
                    removeFragment(R.id.second_container_id)

                    if (noteSelected) {
                        startNoteDetailFragment(R.id.first_container_id, note!!, inode!!)
                    } else {
                        startNoteListFragment(R.id.first_container_id)
                    }
                } else {
                    if (noteSelected) {
                        startNoteDetailFragment(R.id.second_container_id, note!!, inode!!)
                    } else {
                        startGetStartedFragment()
                    }
                    startNoteListFragment(R.id.first_container_id)
                }
            }
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

        val secondFrag = supportFragmentManager.findFragmentById(R.id.second_container_id)
        val firstFrag = supportFragmentManager.findFragmentById(R.id.first_container_id)

        if (secondFrag is NoteDetailFragment) {
            if (secondFrag.deleted)
                outState.clear()
            else
                saveCurrentNote(outState, secondFrag)
        } else if (secondFrag is GetStartedFragment) {
            outState.clear()
        } else if (firstFrag is NoteDetailFragment) {
            if (firstFrag.deleted)
                outState.clear()
            else
                saveCurrentNote(outState, firstFrag)
        }
    }

    private fun saveCurrentNote(outState: Bundle, frag: NoteDetailFragment) {
        outState.putSerializable(NOTE, frag.arguments?.getSerializable(NOTE))
        outState.putSerializable(INODE, frag.arguments?.getSerializable(INODE))
    }

    /**
     * Communicate from NoteFragment to NoteListFragment that a note/inode has been edited
     *
     */
    override fun onINodeUpdate() {
        // Write change to file system
        FileSystem.writeDirEntry(applicationContext, DataProvider.getActiveSubDirectory(), DirEntry(DataProvider.getINodes()))

        // Notify NoteListFragment (if it exists)
        (supportFragmentManager.findFragmentByTag(LIST_FRAGMENT) as? NoteListFragment)?.updateArrayAdapter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        val fragment = supportFragmentManager.findFragmentByTag(DETAIL_FRAGMENT) as NoteDetailFragment
        val fileHandler = FileHandler(this)

        // request to save a file has been made, add data to newly created file
        if (requestCode == FileHandler.CREATE_FILE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                fileHandler.alterDocument(uri, fragment.noteText)
            }
        }
        // request to load file contents has been made, process the file's contents
        else if (requestCode == FileHandler.PICK_TXT_FILE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                fileHandler.processTextFileData(uri, fragment.noteText)
            }
        }
    }
}
