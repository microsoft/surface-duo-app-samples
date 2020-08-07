/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

object Defines {
    // File Handler references //
    const val DEFAULT_FILE_NAME = "drawing"
    const val IMAGE_PREFIX = "image/"
    const val PLAIN_TEXT = "text/plain"
    const val TEXT_PREFIX = "text/"

    // Image Handler constants //
    const val MIN_DIMEN = 250
    const val RENDER_TIMER = 50L
    const val RESIZE_SPEED = 15
    const val THRESHOLD = 0.2

    // Inking scaling constants //
    const val LAND_TO_PORT = 3f / 4
    const val PORT_TO_LAND = 4f / 3

    // Fragment name constants //
    const val DETAIL_FRAGMENT = "detail fragment"
    const val LIST_FRAGMENT = "list fragment"
    const val INODE = "inode"
    const val NOTE = "note"
}
