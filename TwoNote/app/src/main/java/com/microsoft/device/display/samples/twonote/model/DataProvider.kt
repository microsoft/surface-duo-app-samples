/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

object DataProvider {
    val inodes: MutableList<INode> = mutableListOf()

    /**
     * Add a new inode to the inode list
     */
    fun addINode(inode: INode) {
        inodes.add(inode)
    }
}
