package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static Bitmap loadScaledBitmap(Context context, Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        }
        int sample = 1;
        while (options.outWidth / sample > 1024 || options.outHeight / sample > 1024) {
            sample *= 2;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(stream, null, options);
        }
        int rotation = readRotation(context, uri);
        if (bitmap != null && rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    public static Bitmap scaleDown(Bitmap source, int max) {
        int width = source.getWidth();
        int height = source.getHeight();
        float ratio = Math.min(1f, (float) max / Math.max(width, height));
        return Bitmap.createScaledBitmap(source, Math.round(width * ratio), Math.round(height * ratio), true);
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    public static Bitmap base64ToBitmap(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int readRotation(Context context, Uri uri) {
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(stream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException | NullPointerException e) {
            return 0;
        }
    }
}
