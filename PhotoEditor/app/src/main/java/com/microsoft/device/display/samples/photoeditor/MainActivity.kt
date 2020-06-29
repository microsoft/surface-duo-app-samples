/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.photoeditor

import android.content.ClipData
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.DragEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.io.IOException
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    companion object {
        // Request code for image select activity
        private const val SELECT_IMAGE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLayout(savedInstanceState)

        savedInstanceState?.let {
            val image = findViewById<ImageFilterView>(R.id.image)

            // Restore image
            val vm by viewModels<PhotoEditorVM>()
            image.setImageDrawable(vm.getImage().value)

            image.brightness = it.getFloat("brightness")
            image.saturation = it.getFloat("saturation")
            image.warmth = it.getFloat("warmth")
        }
    }

    /**
     * Saves relevant control information (ex: SeekBar progress) to pass between states when orientation or spanning changes
     * @param outState: Bundle that contains the information to pass between states
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val image = findViewById<ImageFilterView>(R.id.image)

        // SeekBar progress data
        outState.putFloat("saturation", image.saturation)
        outState.putFloat("brightness", image.brightness)
        outState.putFloat("warmth", image.warmth)

        // Selected control in dropdown (only present in single-screen views)
        val controls = findViewById<Spinner>(R.id.controls)
        controls?.let {
            outState.putInt("selectedControl", it.selectedItemPosition)
        }

        // Actual edited image - saved in ViewModel
        val vm by viewModels<PhotoEditorVM>()
        vm.updateImage(findViewById<ImageFilterView>(R.id.image).drawable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Select image to edit from photo gallery
        if (requestCode == SELECT_IMAGE && data?.data != null) {
            val uri: Uri = data.data!!
            val image = findViewById<ImageFilterView>(R.id.image)
            image.setImageBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(uri)))

            resetControls(image)
        }
    }

    private fun resetControls(image: ImageFilterView) {
        findViewById<SeekBar>(R.id.saturation).progress = 50
        image.saturation = 1f

        findViewById<SeekBar>(R.id.brightness)?.progress = 50
        image.brightness = 1f

        findViewById<SeekBar>(R.id.warmth)?.progress = 50
        image.warmth = 1f
    }

    private fun setupLayout(savedInstanceState: Bundle?) {
        val image = findViewById<ImageFilterView>(R.id.image)

        // Set up click handling for importing images from photo gallery
        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_IMAGE)
        }

        // Set up drag/drop handling for importing images
        image.setOnDragListener { v, event ->
            // Check file type of dragged data
            val isImage = event.clipDescription?.getMimeType(0).toString().startsWith("image/")

            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // If an image file is being dragged, change appearance of ImageFilterView to show that it's a drop target
                    if (isImage) {
                        image.alpha = 0.5f
                        image.setPadding(20, 20, 20, 20)
                        image.cropToPadding = true
                        image.setBackgroundColor(Color.parseColor("grey"))
                    }
                }
                DragEvent.ACTION_DROP -> {
                    // If an image file is being dropped, change what the ImageFilterView displays
                    if (isImage) {
                        val item: ClipData.Item = event.clipData.getItemAt(0)
                        val uri = item.uri

                        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                            ActivityCompat.requestDragAndDropPermissions(this, event)
                            (v as ImageFilterView).setImageURI(uri)
                        } else {
                            (v as ImageFilterView).setImageURI(uri)
                        }
                        resetControls(image)
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Reset any previous appearance changes
                    image.alpha = 1f
                    image.setPadding(0, 0, 0, 0)
                    image.cropToPadding = false
                    image.setBackgroundColor(Color.TRANSPARENT)
                }
                else -> {
                    // Ignore other events
                }
            }
            true
        }

        // Set up all controls
        setUpSaturation(image, savedInstanceState?.getFloat("saturation"))
        setUpBrightness(image, savedInstanceState?.getFloat("brightness"))
        setUpWarmth(image, savedInstanceState?.getFloat("warmth"))
        setUpRotate(image)
        setUpSave(image)

        // Set up single screen control dropdown
        if (!ScreenHelper.isDualMode(this)) {
            setUpToggle(savedInstanceState?.getInt("selectedControl"))
        }
    }

    private fun setUpToggle(selectedControl: Int?) {
        // Set up contents of controls dropdown
        val controls = findViewById<Spinner>(R.id.controls)

        ArrayAdapter.createFromResource(
            this,
            R.array.controls_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            controls.adapter = adapter
        }

        // Restore value from previous state if available, otherwise default to first item in list (saturation)
        controls.setSelection(selectedControl ?: 0)

        // Set up response to changing the selected control
        val sat = findViewById<SeekBar>(R.id.saturation)
        val bright = findViewById<SeekBar>(R.id.brightness)
        val warmth = findViewById<SeekBar>(R.id.warmth)

        controls.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                when (parent.getItemAtPosition(pos)) {
                    getString(R.string.saturation) -> {
                        sat.visibility = View.VISIBLE
                        bright.visibility = View.INVISIBLE
                        warmth.visibility = View.INVISIBLE
                    }
                    getString(R.string.brightness) -> {
                        sat.visibility = View.INVISIBLE
                        bright.visibility = View.VISIBLE
                        warmth.visibility = View.INVISIBLE
                    }
                    getString(R.string.warmth) -> {
                        sat.visibility = View.INVISIBLE
                        bright.visibility = View.INVISIBLE
                        warmth.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setUpWarmth(image: ImageFilterView, progress: Float?) {
        val warmth = findViewById<SeekBar>(R.id.warmth)

        // Restore value from previous state if available
        progress?.let {
            image.warmth = it
            warmth.progress = (it * 50).toInt()
        }

        warmth.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // warmth from 0.5 (cold) to 1 (original) to 2 (warm), progress from 0 to 100
                    image.warmth = progress / 50f
                }

                override fun onStartTrackingTouch(seek: SeekBar) {}

                override fun onStopTrackingTouch(seek: SeekBar) {}
            })
    }

    private fun setUpBrightness(image: ImageFilterView, progress: Float?) {
        val brightness = findViewById<SeekBar>(R.id.brightness)

        // Restore value from previous state if available
        progress?.let {
            image.brightness = it
            brightness.progress = (it * 50).toInt()
        }

        brightness.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // brightness from 0 (black) to 1 (original) to 2 (twice as bright), progress from 0 to 100
                    image.brightness = progress / 50f
                }

                override fun onStartTrackingTouch(seek: SeekBar) {}

                override fun onStopTrackingTouch(seek: SeekBar) {}
            })
    }

    private fun setUpSaturation(image: ImageFilterView, progress: Float?) {
        val saturation = findViewById<SeekBar>(R.id.saturation)

        // Restore value from previous state if available
        progress?.let {
            image.saturation = it
            saturation.progress = (it * 50).toInt()
        }

        saturation.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // saturation from 0 (grayscale) to 1 (original) to 2 (hyper-saturated), progress from 0 to 100
                    image.saturation = progress / 50f
                }

                override fun onStartTrackingTouch(seek: SeekBar) {}

                override fun onStopTrackingTouch(seek: SeekBar) {}
            })
    }

    private fun setUpRotate(image: ImageFilterView) {
        val left = findViewById<ImageButton>(R.id.rotate_left)

        left.setOnClickListener {
            applyRotationMatrix(270f, image)
        }

        val right = findViewById<ImageButton>(R.id.rotate_right)
        right.setOnClickListener {
            applyRotationMatrix(90f, image)
        }
    }

    /**
     * Rotates image by specified angle
     * @param angle: angle to rotate image
     * @param image: ImageFilterView that contains current image
     */
    private fun applyRotationMatrix(angle: Float, image: ImageFilterView) {
        // Create rotation angle with given matrix
        val matrix = Matrix()
        matrix.postRotate(angle)

        // Apply rotation matrix to the image
        val curr = image.drawable.toBitmap()
        val bm = Bitmap.createBitmap(curr, 0, 0, curr.width, curr.height, matrix, true)

        // Update ImageFilterView object with rotated bitmap
        image.setImageBitmap(bm)
    }

    private fun setUpSave(image: ImageFilterView) {
        val save = findViewById<ImageButton>(R.id.save)
        save.setOnClickListener {
            // Get current size of drawable so entire ImageView is not drawn to bitmap
            val rect = RectF()
            image.imageMatrix.mapRect(rect, RectF(image.drawable.bounds))
            val bm = Bitmap.createBitmap(
                image.drawToBitmap(),
                rect.left.toInt(),
                rect.top.toInt(),
                rect.width().toInt(),
                rect.height().toInt()
            )

            // Fill ContentValues with image information
            val values = ContentValues()
            values.apply {
                put(MediaStore.Images.Media.TITLE, getString(R.string.photo_name))
                put(MediaStore.Images.Media.DISPLAY_NAME, getString(R.string.photo_name))
                put(MediaStore.Images.Media.DESCRIPTION, "${getString(R.string.photo_description)} ${LocalDateTime.now()}")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${getString(R.string.pictures_folder)}/${getString(R.string.app_name)}")
                put(MediaStore.Images.Media.IS_PENDING, true)
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000) // seconds
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis()) // milliseconds
            }

            // Try to save image to photo gallery
            try {
                val uri = this.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                ) ?: throw IOException("MainActivity: ${getString(R.string.null_uri)}")

                val stream = this.contentResolver.openOutputStream(uri) ?: throw IOException("MainActivity: ${getString(R.string.null_stream)}")
                if (!bm.compress(Bitmap.CompressFormat.JPEG, 100, stream)) throw IOException("MainActivity: ${getString(R.string.bitmap_error)}")
                stream.close()

                values.put(MediaStore.Images.Media.IS_PENDING, false)
                this.contentResolver.update(uri, values, null, null)
            } catch (e: Exception) {
                Toast.makeText(this, "${getString(R.string.image_save_error)}\n${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
