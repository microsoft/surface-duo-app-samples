/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.device.dualscreen.layout.ScreenHelper

class MainActivity : AppCompatActivity(), NoteFragment.OnFragmentInteractionListener {
    companion object {
        const val LIST_FRAGMENT = "list fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!ScreenHelper.isDualMode(this)) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.single_screen_container_id,
                    ItemsListFragment(),
                    LIST_FRAGMENT
                )
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.dual_screen_start_container_id,
                    ItemsListFragment(),
                    LIST_FRAGMENT
                ).commit()
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.dual_screen_end_container_id,
                    NoteFragment(),
                    null
                ).commit()
        }
    }

    /**
     * Send data from Note Fragment to ItemsListFragment after note contents have been edited
     *
     * REVISIT: may just want to encapsulate fields (have to add drawings/photos) in one object
     *
     * @param title: updated note title
     * @param text: updated note text
     */
    override fun onNoteUpdate(title: String, text: String) {
        (supportFragmentManager.findFragmentByTag(LIST_FRAGMENT) as ItemsListFragment).updateNote(title, text)
    }
}
