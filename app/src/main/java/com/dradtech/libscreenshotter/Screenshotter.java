package com.dradtech.libscreenshotter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;

/**
 * Created by siddhant on 5/9/17.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class Screenshotter implements ImageReader.OnImageAvailableListener {

    private static final String TAG = "LibScreenshotter";

    private VirtualDisplay virtualDisplay;
    public static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;

    private int width;
    private int height;

    private Context context;

    private int resultCode;
    private Intent data;
    private ScreenshotCallback cb;

    private static Screenshotter mInstance;

    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private volatile int imageAvailable = 0;

    /**
     * Get the single instance of the Screenshotter class.
     * @return the instance
     */
    public static Screenshotter getInstance() {
        if (mInstance == null) {
            mInstance = new Screenshotter();
        }
        return mInstance;
    }

    private Screenshotter() {}

    /**
     * Takes the screenshot of whatever currently is on the default display.
     * @param resultCode The result code returned by the request for accessing MediaProjection permission
     * @param data The intent returned by the same request
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Screenshotter takeScreenshot(Context context, int resultCode, Intent data, final ScreenshotCallback cb) {
        this.context = context;
        this.cb = cb;
        this.resultCode = resultCode;
        this.data = data;

        imageAvailable = 0;
        //mImageReader = ImageReader.newInstance(width, height, ImageFormat.RGB_565, 2);
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mMediaProjection == null) {
            mMediaProjection = mediaProjectionManager.getMediaProjection(this.resultCode, this.data);
            if (mMediaProjection == null) {
                Log.e(TAG, "MediaProjection null. Cannot take the screenshot.");
            }
        }
        try {
            virtualDisplay = mMediaProjection.createVirtualDisplay("Screenshotter",
                    width, height, 50,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            mImageReader.setOnImageAvailableListener(Screenshotter.this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the size of the screenshot to be taken
     * @param width width of the requested bitmap
     * @param height height of the request bitmap
     * @return the singleton instance
     */
    public Screenshotter setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {

        Image image = null;
        FileOutputStream fos = null;
        Bitmap bitmap = null;

        synchronized (this) {
            ++imageAvailable;
             if (imageAvailable != 2) {
                 image = reader.acquireLatestImage();
                 if (image == null) return;
                    image.close();
                 return;
                }
            }
        try {
            image = reader.acquireLatestImage();
            if (image == null) return;
            final Image.Plane[] planes = image.getPlanes();
            final Buffer buffer = planes[0].getBuffer().rewind();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            // create bitmap
            bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            /*
            // write bitmap to a file

            //get path to external storage (SD card)
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File myDir = new File(path + "/UtilityApp");

            //create storage directories, if they don't exist
            myDir.mkdirs();

            String filename = "/myscreen_"+ IMAGES_PRODUCED +".png";
            File file = new File (myDir, filename);
            if (file.exists ()) file.delete ();

            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

            } catch (FileNotFoundException e) {
                Log.w("TAG", "Error saving image file: " + e.getMessage());
                //return false;
            } catch (IOException e) {
                Log.w("TAG", "Error saving image file: " + e.getMessage());
                //return false;
            }

            //fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png");
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            IMAGES_PRODUCED++;
            Log.e(TAG, "captured image: " + IMAGES_PRODUCED); */
            tearDown();
            image.close();

            cb.onScreenshot(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            if (bitmap != null) {
                bitmap.recycle();
            }

            if (image != null) {
                image.close();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void tearDown() {
        virtualDisplay.release();
        mMediaProjection.stop();
        mMediaProjection = null;
        mImageReader = null;
    }
}