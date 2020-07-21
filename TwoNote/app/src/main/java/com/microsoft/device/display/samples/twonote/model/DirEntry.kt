/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.microsoft.device.display.samples.twonote.model

import java.io.Serializable

data class DirEntry(
    val inodes: MutableList<INode>
) : Serializable