/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.example.photo_editor

import android.content.Intent
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    companion object {
        // Request code for image upload activity
        private const val UPLOAD_IMAGE = 1000
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

            image.alpha = savedInstanceState.getFloat("alpha")
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
        outState.putFloat("alpha", image.alpha)
        outState.putFloat("brightness", image.brightness)
        outState.putFloat("warmth", image.warmth)

        val vm by viewModels<PhotoEditorVM>()
        vm.updateImage(findViewById<ImageFilterView>(R.id.image).drawable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Select image to edit from photo gallery
        if (requestCode == UPLOAD_IMAGE && data?.data != null) {
            val uri: Uri = data.data!!
            val image = findViewById<ImageFilterView>(R.id.image)
            image.setImageBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(uri)))
        }
    }

    private fun setupLayout(savedInstanceState: Bundle?) {
        val image = findViewById<ImageFilterView>(R.id.image)
        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, UPLOAD_IMAGE)
        }

        // Common controls
        setUpSaturation(image, savedInstanceState?.getFloat("saturation"))
        setUpRotate(image)
        setUpSave(image)

        // Dual screen controls
        if (ScreenHelper.isDualMode(this)) {
            setUpTransparency(image, savedInstanceState?.getFloat("alpha"))
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
                progress: Int, fromUser: Boolean
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
                progress: Int, fromUser: Boolean
            ) {
                // brightness from 0 (black) to 1 (original) to 2 (twice as bright), progress from 0 to 100
                image.brightness = progress / 50f
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}

            override fun onStopTrackingTouch(seek: SeekBar) {}
        })
    }

    private fun setUpTransparency(image: ImageFilterView, progress: Float?) {
        val transparency = findViewById<SeekBar>(R.id.transparency)

        // Restore value
        if (progress != null) {
            image.alpha = progress
            transparency.progress = 100 - (100 * progress).toInt()
        }

        transparency.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                // alpha from 0 (transparent) to 1 (opaque), progress from 0 to 100
                image.alpha = (100 - progress) / 100f
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
                progress: Int, fromUser: Boolean
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