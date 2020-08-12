import android.graphics.Matrix

/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

object Defines {
    // File Handler references //
    const val IMAGE_PREFIX = "image/"
    const val TEXT_PREFIX = "text/"

    // Image Handler constants //
    const val MIN_DIMEN = 250
    const val RENDER_TIMER = 50L
    const val RESIZE_SPEED = 15
    const val THRESHOLD = 0.2

    // Rotation scaling constants //
    // Each screen is 1800 by 1350 px:
    // --> PORT_TO_LAND = 1800 / 1350 = 4/3
    // --> LAND_TO_PORT = 1350 / 1800 = 3/4
    const val SCALE_RATIO = 4f / 3
    val LAND_TO_PORT = Matrix().apply {
        postScale(1 / SCALE_RATIO, 1 / SCALE_RATIO)
    }
    val PORT_TO_LAND = Matrix().apply {
        postScale(SCALE_RATIO, SCALE_RATIO)
    }

    // Fragment name constants //
    const val DETAIL_FRAGMENT = "detail fragment"
    const val LIST_FRAGMENT = "list fragment"
    const val INODE = "inode"
    const val NOTE = "note"
}
