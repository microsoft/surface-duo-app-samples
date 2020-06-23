/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.photoeditor

import android.content.ClipData
import android.content.ContentResolver
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
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.microsoft.device.dualscreen.layout.ScreenHelper
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

        if (savedInstanceState != null) {
            val image = findViewById<ImageFilterView>(R.id.image)

            // Restore image
            val vm by viewModels<PhotoEditorVM>()
            image.setImageDrawable(vm.getImage().value)

            image.brightness = savedInstanceState.getFloat("brightness")
            image.saturation = savedInstanceState.getFloat("saturation")
            image.warmth = savedInstanceState.getFloat("warmth")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val image = findViewById<ImageFilterView>(R.id.image)

        // Common controls
        outState.putFloat("saturation", image.saturation)

        // Dual screen controls - should be saved in both modes to save seek bar position
        outState.putFloat("brightness", image.brightness)
        outState.putFloat("warmth", image.warmth)

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

            // Reset all image controls
            findViewById<SeekBar>(R.id.saturation).progress = 50
            image.saturation = 1f

            findViewById<SeekBar>(R.id.brightness)?.progress = 50
            image.brightness = 1f

            findViewById<SeekBar>(R.id.warmth)?.progress = 50
            image.warmth = 1f
        }
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
                    true
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
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Reset any previous appearance changes
                    image.alpha = 1f
                    image.setPadding(0, 0, 0, 0)
                    image.cropToPadding = false
                    image.setBackgroundColor(Color.TRANSPARENT)
                    true
                }
                else -> {
                    // Ignore other events
                    true
                }
            }
        }

        // Set up common controls
        setUpSaturation(image, savedInstanceState?.getFloat("saturation"))
        setUpRotate(image)
        setUpSave(image)

        // Set up dual screen controls
        if (ScreenHelper.isDualMode(this)) {
            setUpBrightness(image, savedInstanceState?.getFloat("brightness"))
            setUpWarmth(image, savedInstanceState?.getFloat("warmth"))
        }
    }

    private fun setUpWarmth(image: ImageFilterView, progress: Float?) {
        val warmth = findViewById<SeekBar>(R.id.warmth)

        // Restore value
        if (progress != null) {
            image.warmth = progress
            warmth.progress = (progress * 50).toInt()
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

        // Restore value
        if (progress != null) {
            image.brightness = progress
            brightness.progress = (progress * 50).toInt()
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

        // Restore value
        if (progress != null) {
            image.saturation = progress
            saturation.progress = (progress * 50).toInt()
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

    private fun applyRotationMatrix(angle: Float, image: ImageFilterView) {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val curr = image.drawable.toBitmap()
        val bm = Bitmap.createBitmap(curr, 0, 0, curr.width, curr.height, matrix, true)

        image.setImageBitmap(bm)
    }

    private fun setUpSave(image: ImageFilterView) {
        val save = findViewById<ImageButton>(R.id.save)
        save.setOnClickListener {
            // Get current size of drawable so entire ImageView is not saved (which is what drawToBitmap does)
            val rect = RectF()
            image.imageMatrix.mapRect(rect, RectF(image.drawable.bounds))

            MediaStore.Images.Media.insertImage(
                contentResolver,
                Bitmap.createBitmap(
                    image.drawToBitmap(),
                    rect.left.toInt(),
                    rect.top.toInt(),
                    rect.width().toInt(),
                    rect.height().toInt()
                ),
                "Edited photo",
                "Made with Surface Duo Photo Editor sample on ${LocalDateTime.now()}"
            )
        }
    }
}
