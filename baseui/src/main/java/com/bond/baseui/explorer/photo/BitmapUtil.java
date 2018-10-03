package com.bond.baseui.explorer.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.bond.baseui.BuildConfig;
import com.bond.baseui.network.http.okhttp.OkHttpHelper;
import com.bond.baseui.util.FileUtil;
import com.bond.baseui.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yuweichen on 15/12/11.
 */
public class BitmapUtil {

    /**
     * convert Bitmap to byte array
     */
    public static byte[] bitmapToByte(Bitmap b) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * convert byte array to Bitmap
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    /**
     * 把bitmap转换成Base64编码String
     */
    public static String bitmapToString(Bitmap bitmap) {
        return Base64.encodeToString(bitmapToByte(bitmap), Base64.DEFAULT);
    }

    /**
     * convert Drawable to Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
    }

    /**
     * convert Bitmap to Drawable
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(bitmap);
    }


    public static Bitmap getBitmapFromFile(Context context, String filePath, float width, float height) {
        return getBitmapFromFile(context, filePath, width, height, Bitmap.Config.ARGB_8888);
    }


    public static int[] bitmapSize(Bitmap bitmap) {
        return new int[]{bitmap.getWidth(), bitmap.getHeight()};
    }


    /**
     * 检查文件是否损坏
     * Check if the file is corrupted
     *
     * @param filePath
     * @return
     */
    public static boolean checkImgCorrupted(String filePath) {
        long fileSize = FileUtil.getFileSize(filePath) / 1024; //|| fileSize<10
        if (fileSize < 5)
            return true;
//        BitmapFactory.Options options = null;
//        if (options == null) {
//            options = new BitmapFactory.Options();
//        }
//        options.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeFile(filePath, options);
//      //
//        if (options.mCancel || options.outWidth == -1
//                || options.outHeight == -1 ) {
//            return true;
//        }


        return false;
    }

    /**
     * 从文件中获取图片
     *
     * @param filePath 图片的路径
     * @return
     */
    public static Bitmap getBitmapFromFile(Context context, String filePath, float width, float height, Bitmap.Config config) {
        Bitmap bitmap;
        // 获得屏幕高宽

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        int degress = readPictureDegree(filePath);// 检测图片是否旋转
        if (w == 0) {
            if (h == 0) {
                bitmap = getResizeBitmapByPath(filePath, width, height, config);
            } else {
                bitmap = getResizeBitmapByPath(filePath, width, (float) h, config);
            }

        } else {
            if (h == 0) {
                bitmap = getResizeBitmapByPath(filePath, (float) w, height, config);
            } else {
                bitmap = getResizeBitmapByPath(filePath, (float) w, (float) h, config);
            }
        }

        if (degress != 0) {// 旋转处理
            bitmap = rotaingImageView(degress, bitmap);
        }
        return bitmap;
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param filePath 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String filePath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }


    /**
     * 从文件路径获取bitmap
     *
     * @param filePath
     * @param width    输出的宽度,用于计算缩放比例
     * @param height   输出的高度,用于计算缩放比例
     * @param config   RGB质量: Bitmap.Config.ARGB_8888 | Bitmap.Config.ARGB_4444 | Bitmap.Config.RGB_565
     * @return
     */
    public static Bitmap getResizeBitmapByPath(String filePath, float width, float height, Bitmap.Config config) {
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, newOpts);// 此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int originW = newOpts.outWidth; // 原始宽
        int originH = newOpts.outHeight; // 原始高
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int scale = 1;// scale=1表示不缩放
        // 此算法凑合
        if (originW > originH && originW > width) {// 如果宽度大的话根据宽度固定大小缩放
            scale = (int) (newOpts.outWidth / width);
        } else if (originW < originH && originW > height) {// 如果高度高的话根据宽度固定大小缩放
            scale = (int) (newOpts.outHeight / height);
        }
        // Logger.d("scale is: "+ scale);

        if (scale == 1) {
            scale = 2;
        }

        if (scale <= 0) {
            scale = 1;
        }

        newOpts.inSampleSize = scale;// 设置缩放比例
        newOpts.inPreferredConfig = config;
        bitmap = BitmapFactory.decodeFile(filePath, newOpts);
        return bitmap;// 压缩好比例大小后再进行质量压缩
    }

    public static Bitmap getBitmapFromFile(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        return bitmap;
    }


    /***
     * 图片的缩放方法
     *
     * @param bgimage
     *            ：源图片资源
     * @param newWidth
     *            ：缩放后宽度
     * @param newHeight
     *            ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    /**
     * @param bitmap
     * @param path    文件路径+文件名
     * @param maxSize 压缩后最大值 KB
     * @throws IOException
     */
    public static File saveBitmap(Bitmap bitmap, String path, int maxSize) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            bitmap = BitmapUtil.imageZoom(bitmap, maxSize);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                return file;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap imageZoom(Bitmap bitmap, int maxSize) {
        //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        //将字节换成KB
        double mid = b.length / 1024;
        //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > maxSize) {
            //获取bitmap大小 是允许最大大小的多少倍
            double i = mid / maxSize;
            //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
            bitmap = zoomImage(bitmap, bitmap.getWidth() / Math.sqrt(i),
                    bitmap.getHeight() / Math.sqrt(i));
        }
        return bitmap;
    }

    /**
     * 获取文件缓存目录
     *
     * @param context
     * @return
     */
    public static String getCachePath(Context context) {
        String filePath = context.getCacheDir() + File.separator;
        if (BuildConfig.DEBUG) {
            filePath = FileUtil.getCachePath(context) + File.separator;
        }
        return filePath;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, int maxSize, final boolean needRecycle) {
        if (bmp == null) {
            return null;
        }

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        int options = 100;
        bmp.compress(Bitmap.CompressFormat.JPEG, options, output);
        int outputLength = output.size();
        while (outputLength > maxSize) {
            if (outputLength > 10 * maxSize) {
                options -= 30;
            } else if (outputLength > 5 * maxSize) {
                options -= 20;
            } else {
                options -= 10;
            }

            output.reset();
            bmp.compress(Bitmap.CompressFormat.JPEG, options, output);
            outputLength = output.size();
        }

        if (needRecycle) {
            bmp.recycle();
        }

        final byte[] result = output.toByteArray();
        IOUtil.closeQuietly(output);

        return result;
    }


    @WorkerThread
    public static Bitmap decodeUrl(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        InputStream is = null;
        try {
            OkHttpClient client = OkHttpHelper.shareClient();
            Request request = new Request.Builder().get()
                    .url(imageUrl)
                    .build();
            Response response = client.newCall(request).execute();
            is = response.body().byteStream();
//            is = new URL(imageUrl).openStream();
            return BitmapFactory.decodeStream(is);
//            if (is.available() < 50 * 1024) {
//                return BitmapFactory.decodeStream(is);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeQuietly(is);
        }

        return null;
    }

    public static Bitmap decodeFile(String path, float width, float height) {
        int inSampleSize = getInSampleSize(path, width, height);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inSampleSize = inSampleSize;
        newOpts.inJustDecodeBounds = false;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(path, newOpts);
    }

    private static int getInSampleSize(String path, float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, newOpts);
        int outWidth = newOpts.outWidth;
        int outHeight = newOpts.outHeight;
        return (int) getScale(width, height, outWidth, outHeight);
    }

    public static double getScale(float targetWidth, float targetHeight, float bmpWidth, float bmpHeight) {
        double be;
        if (bmpWidth >= bmpHeight) {
            float widthScale = bmpWidth / targetHeight;
            float heightScale = bmpHeight / targetWidth;
            if (widthScale >= heightScale) {
                be = Math.rint(widthScale);
            } else {
                be = Math.rint(heightScale);
            }
        } else {
            float widthScale = bmpWidth / targetWidth;
            float heightScale = bmpHeight / targetHeight;
            if (widthScale >= heightScale) {
                be = widthScale;
            } else {
                be = heightScale;
            }
        }
        if (be <= 0) {
            return 1.0;
        }

        return be;
    }

    public static File saveBitmapToExternal(Bitmap bitmap, String targetFileDirPath) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        File targetFileDir = new File(targetFileDirPath);
        if (!targetFileDir.exists() && !targetFileDir.mkdirs()) {
            return null;
        }

        File targetFile = new File(targetFileDir, UUID.randomUUID().toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);
            baos.writeTo(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                baos.flush();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }


}
