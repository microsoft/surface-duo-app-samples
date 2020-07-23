package com.microsoft.device.display.samples.twonote

import android.content.Context
import android.util.Log
import com.microsoft.device.display.samples.twonote.model.DataProvider
import com.microsoft.device.display.samples.twonote.model.DirEntry
import com.microsoft.device.display.samples.twonote.model.INode
import com.microsoft.device.display.samples.twonote.model.Note
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception

class FileHandler {
    companion object {

        // loads inode information from the current directory into the DataProvider
        fun loadDirectory(context: Context, subDir: String) {
            if (DataProvider.getINodes().isEmpty()) {
                readDirEntry(context, subDir)?.let { notes ->
                    for (inode in notes.inodes.size - 1 downTo 0) {
                        DataProvider.addINode(notes.inodes[inode])
                    }
                }
            }
        }

        // add a new inode
        fun addInode() {
            val inode = INode()
            if (DataProvider.getINodes().isNotEmpty()) {
                inode.id = DataProvider.getNextInodeId()
                inode.title = "Note " + inode.id
            }
            DataProvider.addINode(inode)
        }

        fun addCategory() {
            val inode = INode("Category 0")
            if (DataProvider.getCategories().isNotEmpty()) {
                inode.id = DataProvider.getNextCategoryId()
                inode.title = "Category " + inode.id
            }
            DataProvider.addCategory(inode)
        }

        fun switchCategory(inode: INode) {
            DataProvider.moveINodeToTop(inode)
        }

        // reads a file and parses note data
        fun loadNote(context: Context, subDir: String, noteName: String): Note? {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File(path + subDir + noteName)
            var fileStream: FileInputStream? = null
            var objectStream: ObjectInputStream? = null

            try {
                fileStream = FileInputStream(file)
                objectStream = ObjectInputStream(fileStream)
                val note = objectStream.readObject()
                if (note is Note) {
                    return note
                } else {
                    Log.e(this.javaClass.toString(), context.resources.getString(R.string.load_error_note))
                    return null
                }
            } catch (e: Exception) {
                Log.e(this.javaClass.toString(), e.message.toString())
                return null
            } finally {
                objectStream?.close()
                fileStream?.close()
            }
        }

        // reads directory entry to get inodes
        fun readDirEntry(context: Context, subDir: String): DirEntry? {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File("$path$subDir/dEntry")
            var fileStream: FileInputStream? = null
            var objectStream: ObjectInputStream? = null

            try {
                fileStream = FileInputStream(file)
                objectStream = ObjectInputStream(fileStream)
                val entry = objectStream.readObject()
                if (entry is DirEntry) {
                    return entry
                } else {
                    Log.e(this.javaClass.toString(), context.resources.getString(R.string.load_error_dir))
                    return null
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

        // update/create directory entry
        fun writeDirEntry(context: Context, subDir: String, entry: DirEntry) {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File("$path$subDir/dEntry")
            val fileStream = FileOutputStream(file)
            val objectStream = ObjectOutputStream(fileStream)
            objectStream.writeObject(entry)
            objectStream.close()
            fileStream.close()
        }

        // remove an inode and its associated note
        fun delete(context: Context, subDir: String, inode: INode): Boolean {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File(path + subDir + "/n" + inode.id)

            if (!DataProvider.getINodes().isNullOrEmpty()) {
                DataProvider.removeINode(inode)

                if (file.exists()) {
                    file.delete()
                    return true
                }
            }
            return false
        }
    }
}
