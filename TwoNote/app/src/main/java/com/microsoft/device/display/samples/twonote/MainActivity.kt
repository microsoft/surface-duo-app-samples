/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import Defines.DETAIL_FRAGMENT
import Defines.INODE
import Defines.LIST_FRAGMENT
import Defines.NOTE
import android.content.Context
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.display.samples.twonote.fragments.GetStartedFragment
import com.microsoft.device.display.samples.twonote.fragments.NoteDetailFragment
import com.microsoft.device.display.samples.twonote.fragments.NoteListFragment
import com.microsoft.device.display.samples.twonote.models.DirEntry
import com.microsoft.device.display.samples.twonote.models.INode
import com.microsoft.device.display.samples.twonote.models.Note
import com.microsoft.device.display.samples.twonote.utils.DataProvider
import com.microsoft.device.display.samples.twonote.utils.FileSystem
import com.microsoft.device.dualscreen.core.ScreenHelper
import com.microsoft.device.dualscreen.core.ScreenMode

/**
 * Activity that manages fragments and preservation of data through the app's lifecycle
 */
class MainActivity : AppCompatActivity(), NoteDetailFragment.OnFragmentInteractionListener {
    companion object {
        /**
         * Returns whether device is rotated (to the left or right) or not
         *
         * @param context: application context
         * @return true if rotated, false otherwise
         */
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
                selectSingleScreenFragment(noteSelected, note, inode)
            }
            ScreenMode.DUAL_SCREEN -> {
                selectDualScreenFragments(noteSelected, note, inode)
            }
        }
    }

    /**
     * Select which fragment should be inflated in single-screen mode
     *
     * @param noteSelected: true if savedInstanceState contained a specific note/inode, false otherwise
     * @param note: note from savedInstanceState
     * @param inode: inode from savedInstanceState
     */
    private fun selectSingleScreenFragment(noteSelected: Boolean, note: Note?, inode: INode?) {
        // Remove fragment from second container if it exists
        removeSecondFragment()

        if (noteSelected) {
            startNoteDetailFragment(R.id.first_container_id, note!!, inode!!)
        } else {
            startNoteListFragment()
        }
    }

    /**
     * Select which fragment(s) should be inflated in dual-screen mode
     *
     * @param noteSelected: true if savedInstanceState contained a specific note/inode, false otherwise
     * @param note: note from savedInstanceState
     * @param inode: inode from savedInstanceState
     */
    private fun selectDualScreenFragments(noteSelected: Boolean, note: Note?, inode: INode?) {
        // If rotated, use extended canvas pattern, otherwise use list-detail pattern
        if (isRotated(applicationContext)) {
            // Remove fragment from second container if it exists
            removeSecondFragment()

            if (noteSelected) {
                startNoteDetailFragment(R.id.first_container_id, note!!, inode!!)
            } else {
                startNoteListFragment()
            }
        } else {
            if (noteSelected) {
                startNoteDetailFragment(R.id.second_container_id, note!!, inode!!)
            } else {
                startGetStartedFragment()
            }
            startNoteListFragment()
        }
    }

    /**
     * Remove fragment from second container
     */
    private fun removeSecondFragment() {
        supportFragmentManager.findFragmentById(R.id.second_container_id)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    /**
     * Start note list view fragment in first container
     */
    private fun startNoteListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.first_container_id, NoteListFragment(), LIST_FRAGMENT)
            .commit()
    }

    /**
     * Start note detail view fragment in specified container
     *
     * @param container: container to start fragment in
     * @param note: note to display in fragment
     * @param inode: inode associated with note to display in fragment
     */
    private fun startNoteDetailFragment(container: Int, note: Note, inode: INode) {
        supportFragmentManager.beginTransaction()
            .replace(container, NoteDetailFragment.newInstance(inode, note), DETAIL_FRAGMENT)
            .commit()
    }

    /**
     * Start welcome fragment in second container
     */
    private fun startGetStartedFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.second_container_id, GetStartedFragment(), null)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val firstFrag = supportFragmentManager.findFragmentById(R.id.first_container_id)
        val secondFrag = supportFragmentManager.findFragmentById(R.id.second_container_id)

        // Save data from note detail view for configuration changes
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

    /**
     * Save NoteDetailFragment's note and inode data to outState bundle
     *
     * @param outState: bundle to save data in
     * @param frag: NoteDetailFragment to extract note/inode data from
     */
    private fun saveCurrentNote(outState: Bundle, frag: NoteDetailFragment) {
        outState.putSerializable(NOTE, frag.arguments?.getSerializable(NOTE))
        outState.putSerializable(INODE, frag.arguments?.getSerializable(INODE))
    }

    /**
     * Communicate from NoteFragment to NoteListFragment that a note/inode has been edited
     */
    override fun onINodeUpdate() {
        // Write change to file system
        FileSystem.writeDirEntry(applicationContext, DataProvider.getActiveSubDirectory(), DirEntry(DataProvider.getINodes()))

        // Notify NoteListFragment (if it exists)
        (supportFragmentManager.findFragmentByTag(LIST_FRAGMENT) as? NoteListFragment)?.updateNotesList()
    }
}
