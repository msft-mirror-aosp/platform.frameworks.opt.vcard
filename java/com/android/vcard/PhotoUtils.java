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
package com.android.vcard;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhotoUtils {
    private static final String LOG_TAG = VCardConstants.LOG_TAG;

    private static final int JPEG_QUALITY_LEVEL_MAX = 85;
    private static final int JPEG_QUALITY_LEVEL_MIN = 70;
    private static final int JPEG_QUALITY_LEVEL_DECREMENT = 5;
    private static final int KB_IN_BYTES = 1024;

    /**
     * Returns a bitmap created from given photo URI. Returns null if any error happens.
     */
    @Nullable
    static Bitmap getBitmapFromAsset(@NonNull final ContentResolver cr,
            @NonNull final Uri photoUri) {
        AssetFileDescriptor fd = null;
        try {
            fd = cr.openAssetFileDescriptor(photoUri, "r");
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException happened while opening photo with Uri: " + photoUri, e);
        }

        if (fd == null) {
            Log.e(LOG_TAG, "Failed to open asset file from Uri: " + photoUri);
            return null;
        }

        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = fd.createInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException happened from createInputStream", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException happened from InputStream.close()", e);
                }
            }
        }

        try {
            fd.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException happened from AssetFileDescriptor.close()", e);
        }

        if (bitmap == null) {
            Log.e(LOG_TAG, "Failed to decode bitmap from Uri: " + photoUri);
            return null;
        }

        return bitmap;
    }

    /**
     * Creates a resized bitmap if any of dimensions in given image exceeds the given limit.
     * If the given image is already smaller the limit, it just returns the given bitmap.
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    @NonNull
    public static Bitmap resizeBitmapIfNeeded(@NonNull Bitmap originalBitmap, int dimensionLimit) {
        Log.d(LOG_TAG, "resizeBitmapIfNeeded dimensionLimit=" + dimensionLimit);
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        if (width == 0 || height == 0) {
            Log.w(LOG_TAG, "One of width/height is 0. Ignoring...");
            return originalBitmap;
        }

        if (width <= dimensionLimit && height <= dimensionLimit) {
            Log.i(LOG_TAG, "No need to resize, already smaller than given dimension limit.");
            return originalBitmap;
        }

        double ratio = Math.min(dimensionLimit / (double) width, dimensionLimit / (double) height);
        int newWidth = (int) Math.round(width * ratio);
        int newHeight = (int) Math.round(height * ratio);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        Log.d(LOG_TAG, "Resized contact image dimension: " + newWidth + "x" + newHeight);
        return resizedBitmap;
    }

    /**
     * Returns JPEG compressed data of the given bitmap to match the size limit (in bytes)
     * by using high quality level from low quality level.
     * <p>
     * If the image could not be compressed under the size limit, this method returns null.
     */
    @Nullable
    static byte[] compressBitmap(@NonNull Bitmap bitmap, int fileSizeLimit) {
        Log.d(LOG_TAG, "compressBitmap fileSizeLimit: " + fileSizeLimit / KB_IN_BYTES + " KB");

        byte[] compressedData = null;

        // JPEG compress using quality level from max to min values
        int imageQuality = JPEG_QUALITY_LEVEL_MAX;
        while (imageQuality >= JPEG_QUALITY_LEVEL_MIN) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, outputStream);

            if (outputStream.size() <= fileSizeLimit) {
                Log.d(LOG_TAG, "Using image quality as " + imageQuality + ". size: "
                        + outputStream.size() / KB_IN_BYTES + " KB");
                compressedData = outputStream.toByteArray();
                break;
            }
            imageQuality -= JPEG_QUALITY_LEVEL_DECREMENT;
        }

        return compressedData;
    }
}
