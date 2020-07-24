package com.microsoft.device.display.samples.twonote

import android.content.Context
import android.os.FileUtils
import android.provider.ContactsContract
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
            Log.d("FILE_HANDLER", "loading from directory $subDir")
            DataProvider.clearInodes()
            readDirEntry(context, subDir)?.let { notes ->
                for (inode in notes.inodes.size - 1 downTo 0) {
                    DataProvider.addINode(notes.inodes[inode])
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
            Log.d("FILE_HANDLER", "inode size: ${DataProvider.getINodes().size}")
        }

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

        fun addCategory(context: Context) {
            // TODO: put all of the default names in strings.xml for future localization
            val inode = INode(title = "Category 0", descriptor = "/c")
            if (DataProvider.getCategories().isNotEmpty()) {
                inode.id = DataProvider.getNextCategoryId()
                inode.title = "Category " + inode.id
            }
            DataProvider.addCategory(inode)
        }

        fun switchCategory(context: Context, inode: INode?) {
            var newNode = inode
            if (newNode == null) {
                addCategory(context)
                newNode = DataProvider.getCategories()[0]
            }
            DataProvider.moveCategoryToTop(newNode)
            loadDirectory(context, DataProvider.getActiveSubDirectory())
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
                    Log.d("FILE_HANDLER", "loading note $path$subDir$noteName")
                    return note
                } else {
                    Log.e(this::class.java.toString(), context.resources.getString(R.string.load_error_note))
                    return null
                }
            } catch (e: Exception) {
                Log.e(this::class.java.toString(), e.message.toString())
                return null
            } finally {
                objectStream?.close()
                fileStream?.close()
            }
        }

        // reads directory entry to get inodes
        fun readDirEntry(context: Context, subDir: String): DirEntry? {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File(path + subDir + "/dEntry")
            var fileStream: FileInputStream? = null
            var objectStream: ObjectInputStream? = null

            try {
                fileStream = FileInputStream(file)
                objectStream = ObjectInputStream(fileStream)
                val entry = objectStream.readObject()
                if (entry is DirEntry) {
                    return entry
                } else {
                    Log.e(this::class.java.toString(), context.resources.getString(R.string.load_error_dir))
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

        fun createDirectory(context: Context, subDir: String) {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File(path + subDir)
            file.mkdirs()
        }

        // update/create directory entry
        fun writeDirEntry(context: Context, subDir: String, entry: DirEntry) {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            createDirectory(context, subDir)
            val file = File(path + subDir + "/dEntry")
            val fileStream = FileOutputStream(file)
            val objectStream = ObjectOutputStream(fileStream)
            objectStream.writeObject(entry)
            objectStream.close()
            fileStream.close()
        }

        // remove an inode and its associated note
        fun delete(context: Context, subDir: String, inode: INode): Boolean {
            val path: String? = context.getExternalFilesDir(null)?.absolutePath
            val file = File(path + subDir + inode.descriptor + inode.id)

            if (!DataProvider.getINodes().isNullOrEmpty()) {
                DataProvider.removeINode(inode)

                if (file.isFile) {
                    Log.d("FILE_HANDLER", "deleting note $path$subDir${inode.descriptor}${inode.id}")
                    return file.delete()
                }
                if (file.isDirectory) {
                    return file.deleteRecursively()
                }
            }
            return false
        }

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
}
