/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.vcard.tests;

import android.graphics.Bitmap;

import com.android.vcard.PhotoUtils;

import junit.framework.TestCase;

public class PhotoUtilsTest extends TestCase {
    /**
     * Test case for a bitmap with dimensions exceeding the limit.
     *
     * Original size: 480x480, limit: 300 -> Expected resized size: 300x300.
     */
    public void testResizeBitmapIfNeeded_LargeBitmap() {
        final int dimensionLimit = 300;

        // Both the width/height exceed the 300 pixel limit
        Bitmap originalBitmap = Bitmap.createBitmap(480, 480, Bitmap.Config.ARGB_8888);

        Bitmap resizedBitmap = PhotoUtils.resizeBitmapIfNeeded(originalBitmap, dimensionLimit);

        assertNotSame("The resized bitmap should be a different object.",
                originalBitmap, resizedBitmap);
        assertEquals("The width of the resized bitmap should be 300.",
                300, resizedBitmap.getWidth());
        assertEquals("The height of the resized bitmap should be 300.",
                300, resizedBitmap.getHeight());
    }

    /**
     * Test case for a bitmap with only one dimension exceeding the limit.
     * The aspect ratio should be preserved.
     *
     * Original size: 400x200, limit: 300 -> Expected resized size: 300x150.
     */
    public void testResizeBitmapIfNeeded_LargeBitmap_preservesAspectRatio() {
        final int dimensionLimit = 300;

        // Only the width exceeds the 300 pixel limit
        Bitmap originalBitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);

        Bitmap resizedBitmap = PhotoUtils.resizeBitmapIfNeeded(originalBitmap, dimensionLimit);

        assertNotSame("The resized bitmap should be a different object.",
                originalBitmap, resizedBitmap);
        assertEquals("The width of the resized bitmap should be 300.",
                300, resizedBitmap.getWidth());
        assertEquals("The height of the resized bitmap should be 150.",
                150, resizedBitmap.getHeight());
    }

    /**
     * Test case for a bitmap with dimensions exactly same with the limit.
     * The method should not resize it, but return the same object.
     *
     * Original size: 300x300, limit: 300 -> Returns same object
     */
    public void testResizeBitmapIfNeeded_ExactSizeBitmap() {
        final int dimensionLimit = 300;

        Bitmap originalBitmap = Bitmap.createBitmap(
                dimensionLimit, dimensionLimit, Bitmap.Config.ARGB_8888);
        Bitmap resizedBitmap = PhotoUtils.resizeBitmapIfNeeded(originalBitmap, dimensionLimit);

        // Verify that the original bitmap object was returned without resizing.
        assertSame("The original bitmap object should be returned.", originalBitmap, resizedBitmap);
    }

    /**
     * Test case for a bitmap with dimensions smaller than the 300x300 threshold.
     * The method should not resize it, but return the same object.
     *
     * Original size: 50x50, limit: 300 -> Returns same object
     */
    public void testResizeBitmapIfNeeded_SmallBitmap() {
        final int dimensionLimit = 300;

        Bitmap originalBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);

        Bitmap resizedBitmap = PhotoUtils.resizeBitmapIfNeeded(originalBitmap, dimensionLimit);

        // Verify that the original bitmap object was returned without resizing.
        assertSame("The original bitmap object should be returned.", originalBitmap, resizedBitmap);
    }
}
