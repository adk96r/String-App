package adk.string;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.util.Log;
import android.util.Size;

import com.google.android.gms.vision.Frame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Crafted by Adk on 12/18/2017.
 * A set of Utilities used at different places in this project.
 */
class Utility {

    private static final String TAG = Utility.class.getSimpleName();
    static final String CHROME_DICT_BASE_LINK = "https://www.google.co.in/search?site=async/dictw&q=Dictionary#dobs=";

    static int dpToPixels(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * Returns a Rect whose top < bottom and left < right (properly oriented). Swaps the coordinate
     * pairs if necessary to satisfy the above condition.
     *
     * @param rect Rect which may or may not be oriented.
     * @return Rect that is oriented properly.
     */
    static Rect orient(Rect rect) {

        Rect r = new Rect(rect);

        if (r.left > r.right) {
            r.left = rect.right;
            r.right = rect.left;
        }
        if (r.top > r.bottom) {
            r.bottom = rect.top;
            r.top = rect.bottom;
        }

        return r;
    }

    /**
     * Returns a RectF whose top < bottom and left < right (properly oriented). Swaps the coordinate
     * pairs if necessary to satisfy the above condition.
     *
     * @param rect Rect which may or may not be oriented.
     * @return RectF that is oriented properly.
     */
    static RectF orientF(Rect rect) {

        RectF r = new RectF(rect.left, rect.top, rect.right, rect.bottom);

        if (r.left > r.right) {
            r.left = rect.right;
            r.right = rect.left;
        }
        if (r.top > r.bottom) {
            r.bottom = rect.top;
            r.top = rect.bottom;
        }

        return r;
    }


    static void setOverlayStartingPoint(Rect rect, Point startingPoint) {
        rect.top = startingPoint.y;
        rect.left = startingPoint.x;
    }

    static void setOverlayEndingPoint(Rect rect, Point endingPoint) {
        rect.bottom = endingPoint.y;
        rect.right = endingPoint.x;
    }


    private static Rect invertXYAndReflectY(Rect rect, Frame.Metadata metadata) {
        return new Rect(
                rect.top, metadata.getHeight() - rect.left,
                rect.bottom, metadata.getHeight() - rect.right
        );
    }

    /**
     * Returns a Bitmap extracted from a cropped section of a given frame which is compressed into
     * a JPEG.
     *
     * @param frame    Frame to crop and compress.
     * @param cropRect Rect that represents the region to be cropped.
     * @return Bitmap Bitmap obtained after compressing the compressed cropped frame.
     */
    static Bitmap getCroppedBitmapFromFrame(Frame frame, Rect cropRect) {
        YuvImage yuvImage;
        byte[] jpegArray;

        Frame.Metadata metadata = frame.getMetadata();
        ByteBuffer buffer = frame.getGrayscaleImageData();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Rect perfectedRect = orient(invertXYAndReflectY(orient(cropRect), metadata));

        try {
            yuvImage = new YuvImage(buffer.array(), ImageFormat.NV21, metadata.getWidth(),
                    metadata.getHeight(), null);
            yuvImage.compressToJpeg(perfectedRect, 100, byteArrayOutputStream);
            jpegArray = byteArrayOutputStream.toByteArray();
            return BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception - " + e.getMessage());
            Log.d(TAG, "Cam Frame  size - " + metadata.getWidth() + " " + metadata.getHeight());
            Log.d(TAG, "Crop Rect  size - " + perfectedRect);
            return null;
        }

    }

    /**
     * Rotates the given bitmap by the specified angle.
     *
     * @param bitmap Bitmap to rotate.
     * @param angle  int Angle to rotate the bitmap by.
     * @return Bitmap The rotated Bitmap
     */
    static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    /**
     * Returns the screen's dimensions.
     *
     * @param activity Activity used to extract the size from the display.
     * @return Size Size of the screen.
     */
    static Size getScreenDimensions(Activity activity) {
        Point p = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(p);
        return new Size(p.x, p.y);
    }

    /**
     * Merges two lists together without duplication.
     *
     * @param first  List in which will the second List will be merged into without duplication.
     * @param second List to be merged.
     */
    @SuppressWarnings("unchecked")
    static void merge(Collection first, Collection second) {
        for (Object object : second) {
            if (!first.contains(object)) {
                first.add(object);
            }
        }
    }
}
