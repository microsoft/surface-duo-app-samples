/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.example.photo_editor

import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.drawToBitmap
import com.microsoft.device.dualscreen.layout.ScreenHelper
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    companion object {
        //image pick code
        private const val IMAGE_PICK_CODE = 1000

        // Float array to track image property changes (represents 4x5 matrix)
        private var filterArray =
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f
            )
    }

    private enum class ImageProperty { SATURATION, TRANSPARENCY, BRIGHTNESS }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLayout(savedInstanceState)

        if (savedInstanceState != null) {
            val image = findViewById<ImageView>(R.id.image)

            // Restore image
            val vm by viewModels<PhotoEditorVM>()
            image.setImageDrawable(vm.getImage().value)

            // Update color filter matrix and apply to ImageView
            filterArray = savedInstanceState.getFloatArray("filterArray")!!
            image.colorFilter = ColorMatrixColorFilter(ColorMatrix(filterArray))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Common controls
        outState.putInt("saturation", findViewById<SeekBar>(R.id.saturation).progress)
        outState.putFloatArray("filterArray", filterArray)

        // Dual screen controls - should be saved in both modes to save seek bar position
        outState.putInt("transparency", 100 - (100 * filterArray[18]).toInt())
        outState.putInt("brightness", filterArray[4].toInt())

        val vm by viewModels<PhotoEditorVM>()
        vm.updateImage(findViewById<ImageView>(R.id.image).drawable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Select image to edit from photo gallery
        if (requestCode == IMAGE_PICK_CODE && data?.data != null) {
            val uri: Uri = data.data!!
            val image = findViewById<ImageView>(R.id.image)
            image.setImageBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(uri)))
        }
    }

    private fun setupLayout(savedInstanceState: Bundle?) {
        val image = findViewById<ImageView>(R.id.image)
        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Common controls
        setUpSaturation(image, savedInstanceState?.getInt("saturation"))
        setUpRotate(image)
        setUpSave(image)

        // Dual screen controls
        if (ScreenHelper.isDualMode(this)) {
            setUpTransparency(image, savedInstanceState?.getInt("transparency"))
            setUpBrightness(image, savedInstanceState?.getInt("brightness"))
        }
    }

    private fun setUpBrightness(image: ImageView, progress: Int?) {
        val brightness = findViewById<SeekBar>(R.id.brightness)

        // Restore value
        if (progress != null)
            brightness.progress = progress

        brightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                image.colorFilter = updateColorMatrix(progress.toFloat(), ImageProperty.BRIGHTNESS)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}

            override fun onStopTrackingTouch(seek: SeekBar) {}
        })
    }

    private fun setUpTransparency(image: ImageView, progress: Int?) {
        val transparency = findViewById<SeekBar>(R.id.transparency)

        // Restore value
        if (progress != null)
            transparency.progress = progress

        transparency.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                // alpha from 0 (opaque) to 1 (transparent), progress from 0 to 100
                image.colorFilter =
                    updateColorMatrix((100 - progress) / 100f, ImageProperty.TRANSPARENCY)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}

            override fun onStopTrackingTouch(seek: SeekBar) {}
        })
    }

    private fun setUpSaturation(image: ImageView, progress: Int?) {
        val saturation = findViewById<SeekBar>(R.id.saturation)

        // Restore value
        if (progress != null)
            saturation.progress = progress

        saturation.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                // saturation from 0 (grayscale) to 1 (original) to 2 (highly saturated), progress from 0 to 100
                image.colorFilter = updateColorMatrix(progress / 50f, ImageProperty.SATURATION)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}

            override fun onStopTrackingTouch(seek: SeekBar) {}
        })
    }

    private fun updateColorMatrix(
        progress: Float,
        property: ImageProperty
    ): ColorMatrixColorFilter {
        when (property) {
            ImageProperty.SATURATION -> {
                // Taken from ColorMatrix method setSaturation
                val invSat = 1 - progress
                val r = 0.213f * invSat
                val g = 0.715f * invSat
                val b = 0.072f * invSat
                filterArray[0] = r + progress
                filterArray[1] = g
                filterArray[2] = b
                filterArray[5] = r
                filterArray[6] = g + progress
                filterArray[7] = b
                filterArray[10] = r
                filterArray[11] = g
                filterArray[12] = b + progress
            }
            ImageProperty.TRANSPARENCY -> {
                filterArray[18] = progress
            }
            ImageProperty.BRIGHTNESS -> {
                filterArray[4] = progress
                filterArray[9] = progress
                filterArray[14] = progress
            }
        }
        return ColorMatrixColorFilter(ColorMatrix(filterArray))
    }

    private fun setUpRotate(image: ImageView) {
        val left = findViewById<ImageButton>(R.id.rotate_left)

        left.setOnClickListener {
            applyRotationMatrix(270f, image)
        }

        val right = findViewById<ImageButton>(R.id.rotate_right)
        right.setOnClickListener {
            applyRotationMatrix(90f, image)
        }
    }

    private fun applyRotationMatrix(angle: Float, image: ImageView) {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val curr = image.drawable.toBitmap()
        val bm = Bitmap.createBitmap(curr, 0, 0, curr.width, curr.height, matrix, true)

        image.setImageBitmap(bm)
    }

    private fun setUpSave(image: ImageView) {
        val save = findViewById<ImageButton>(R.id.save)
        save.setOnClickListener {
            MediaStore.Images.Media.insertImage(
                contentResolver,
                image.drawToBitmap(),
                "Edited photo",
                "Made with Surface Duo Photo Editor sample on ${LocalDateTime.now()}"
            )
        }
    }

}