/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.utils

import android.content.Context
import android.util.Log
import com.microsoft.device.display.samples.twonote.R
import com.microsoft.device.display.samples.twonote.models.DirEntry
import com.microsoft.device.display.samples.twonote.models.INode
import com.microsoft.device.display.samples.twonote.models.Note
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception

object FileSystem {
    // load inode information from the current directory into the DataProvider
    private fun loadDirectory(context: Context, subDir: String) {
        DataProvider.clearInodes()
        readDirEntry(context, subDir)?.let { notes ->
            for (inode in notes.inodes.size - 1 downTo 0) {
                DataProvider.addINode(notes.inodes[inode])
            }
        }
    }

    // create a new inode and add it to the DataProvider
    fun addInode(context: Context) {
        val prefix = context.resources.getString(R.string.default_note_name)
        val inode = INode("$prefix 1")
        if (DataProvider.getINodes().isNotEmpty()) {
            inode.id = DataProvider.getNextInodeId()
            inode.title = "$prefix ${inode.id}"
        }
        DataProvider.addINode(inode)
    }

    // load category information from root into the DataProvider
    fun loadCategories(context: Context, subDir: String) {
        if (DataProvider.getCategories().isEmpty()) {
            readDirEntry(context, subDir)?.let { dir ->
                for (category in dir.inodes.size - 1 downTo 0) {
                    DataProvider.addCategory(dir.inodes[category])
                }
            }

            // If no categories were found, make a new one
            if (DataProvider.getCategories().isEmpty())
                addCategory(context)

            loadDirectory(context, DataProvider.getActiveSubDirectory())
        }
    }

    // create a new category and add it to the DataProvider
    private fun addCategory(context: Context) {
        val prefix = context.resources.getString(R.string.default_category_name)
        val inode = INode(title = "$prefix 1", descriptor = "/c")
        if (DataProvider.getCategories().isNotEmpty()) {
            inode.id = DataProvider.getNextCategoryId()
            inode.title = "$prefix ${inode.id}"
        }
        DataProvider.addCategory(inode)
    }

    // load DataProvider with inodes from given category (switches from one category to another)
    fun switchCategory(context: Context, inode: INode?) {
        var newNode = inode
        if (newNode == null) {
            addCategory(context)
            newNode = DataProvider.getCategories()[0]
        }
        DataProvider.moveCategoryToTop(newNode)
        loadDirectory(context, DataProvider.getActiveSubDirectory())
    }

    // read a file and parse note data
    fun loadNote(context: Context, subDir: String, noteName: String): Note? {
        val path: String? = context.getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + noteName)
        var fileStream: FileInputStream? = null
        var objectStream: ObjectInputStream? = null

        try {
            fileStream = FileInputStream(file)
            objectStream = ObjectInputStream(fileStream)
            val note = objectStream.readObject()
            return if (note is Note) {
                note
            } else {
                Log.e(this::class.java.toString(), context.resources.getString(R.string.load_error_note))
                null
            }
        } catch (e: Exception) {
            Log.e(this::class.java.toString(), e.message.toString())
            return null
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    // read directory entry to get inodes
    private fun readDirEntry(context: Context, subDir: String): DirEntry? {
        val path: String? = context.getExternalFilesDir(null)?.absolutePath
        val file = File("$path$subDir/dEntry")
        var fileStream: FileInputStream? = null
        var objectStream: ObjectInputStream? = null

        try {
            fileStream = FileInputStream(file)
            objectStream = ObjectInputStream(fileStream)
            val entry = objectStream.readObject()
            return if (entry is DirEntry) {
                entry
            } else {
                Log.e(this::class.java.toString(), context.resources.getString(R.string.load_error_dir))
                null
            }
        } catch (e: Exception) {
            val entry = DirEntry()
            writeDirEntry(context, subDir, entry) // create a new dir entry
            return entry
        } finally {
            objectStream?.close()
            fileStream?.close()
        }
    }

    // if file path doesn't exist, create missing directories for the path
    private fun createDirectory(context: Context, subDir: String) {
        val path: String? = context.getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir)
        file.mkdirs()
    }

    // update/create directory entry
    fun writeDirEntry(context: Context, subDir: String, entry: DirEntry) {
        val path: String? = context.getExternalFilesDir(null)?.absolutePath
        createDirectory(context, subDir)
        val file = File("$path$subDir/dEntry")
        val fileStream = FileOutputStream(file)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(entry)
        objectStream.close()
        fileStream.close()
    }

    // remove an inode and its associated content from memory
    fun delete(context: Context, subDir: String, inode: INode): Boolean {
        val path: String? = context.getExternalFilesDir(null)?.absolutePath
        val file = File(path + subDir + inode.descriptor + inode.id)

        if (!DataProvider.getINodes().isNullOrEmpty()) {
            DataProvider.removeINode(inode)

            if (file.isFile) {
                return file.delete()
            }
            if (file.isDirectory) {
                return file.deleteRecursively()
            }
        }
        return false
    }

    // save a note to memory at a given file path
    fun save(context: Context, subDir: String, note: Note) {
        val path: String? = context.getExternalFilesDir(null)?.absolutePath
        val file = File("$path$subDir/n${note.id}")
        val fileStream = FileOutputStream(file)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(note)
        objectStream.close()
        fileStream.close()
    }
}
