/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import kotlin.math.max

object DataProvider {
    private val inodes: MutableList<INode> = mutableListOf()
    private val categories: MutableList<INode> = mutableListOf()
    private var highestINodeId = 0
    private var highestCategoryId = 0

    /**
     * Add a new inode to the inode list
     */
    fun addINode(inode: INode) {
        inodes.add(0, inode)
        highestINodeId = max(inode.id, highestINodeId)
    }

    fun removeINode(inode: INode) {
        inodes.remove(inode)
    }

    fun getINodes(): MutableList<INode> {
        return inodes
    }

    fun moveINodeToTop(inode: INode) {
        if (inodes.remove(inode))
            inodes.add(0, inode)
    }

    fun getNextInodeId(): Int {
        return highestINodeId + 1
    }

    fun addCategory(inode: INode) {
        categories.add(0, inode)
        highestCategoryId = max(inode.id, highestCategoryId)
    }

    fun removeCategory(inode: INode) {
        categories.remove(inode)
    }

    fun getCategories(): MutableList<INode> {
        return categories
    }

    fun moveCategoryToTop(inode: INode) {
        if (categories.remove(inode))
            categories.add(0, inode)
    }

    fun getNextCategoryId(): Int {
        return highestCategoryId + 1
    }

    fun getActiveSubDirectory(): String {
        return if (categories.isNotEmpty()) {
            "/c${categories[0].id}"
        } else {
            ""
        }
    }

    fun getActiveCategoryName(): String {
        return if (categories.isNotEmpty())
            categories[0].title
        else
            ""
    }

    fun setActiveCategoryName(title: String) {
        if (categories.isNotEmpty())
            categories[0].title = title
    }

    fun clearInodes() {
        for (inode in inodes.size - 1 downTo 0) {
            inodes.removeAt(inode)
        }
        highestINodeId = 0
    }
}
