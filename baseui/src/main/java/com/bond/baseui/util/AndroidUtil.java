package com.bond.baseui.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * Created by yuweichen on 16/5/20.
 */
public class AndroidUtil {

    /**
     * 适用于打开内部页面
     *
     * @param context
     * @param pkg
     * @param cls
     */
    public static void startActivityByComponentName(Context context, String pkg, String cls) {
        try {
            ComponentName componentName = new ComponentName(pkg, cls);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setAction(Intent.ACTION_VIEW);
            context.startActivity(intent);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * 根据包名打开某个app
     *
     * @param context
     * @param pkg
     */
    public static void startActivityByPkgManager(Context context, String pkg) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        try {
            intent = packageManager.getLaunchIntentForPackage(pkg);
        } catch (Exception e) {
            e.getMessage();
        }
        context.startActivity(intent);
    }


    public static boolean checkApkInstall(Context context, String pkgName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    /**
     * 检查服务是否运行
     *
     * @param context
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (serviceList == null || serviceList.isEmpty())
            return false;
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) && TextUtils.equals(
                    serviceList.get(i).service.getPackageName(), context.getPackageName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // Returns a list of application processes that are running on the device
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(context.getPackageName())
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取App安装包信息
     *
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    public static void rateApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
        //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        //存在手机里没安装应用市场的情况，跳转会包异常，做一个接收判断
        if (intent.resolveActivity(context.getPackageManager()) != null) { //可以接收
            context.startActivity(intent);
        } else {
            ToastUtil.show("您的系统中没有安装应用市场");
        }
    }

    /**
     * 设备唯一ID
     * 目前stackoverflow用的比较多的一种方法
     *
     * @return
     */
    public static String getUniqueDeviceID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
            return "android-" + new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
            return "android-exception-" + new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        }
    }


    /**
     * 获取本机IP地址
     *
     * @return
     */
    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
    // TODO: 2018/6/27
//    public static void openCamera(Activity context, Uri photoUri) {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//跳转到相机Activity
//
//        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.setClipData(ClipData.newUri(context.getContentResolver(),
//                PickConfig.PACKAGE, photoUri));
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);//告诉相机拍摄完毕输出图片到指定的Uri
//
//        context.startActivityForResult(intent, PickConfig.CAMERA_REQUEST_CODE);
//    }

    /**
     * 通过VIEW 拿到bitmap图片
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_4444);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }


    public static String saveBitmapToSystem(Context context, Bitmap bitmap) {
        if (context == null) return null;
        File pictureDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Jianshi");
        if (!pictureDir.exists()) {
            pictureDir.mkdirs();
        }
        File photoName = new File(pictureDir, "IMG-" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(photoName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            bitmap.recycle();
            fos.flush();
            fos.close();
            String url = photoName.getAbsolutePath();
            //通知扫描图片
            new MediaScanner(context, url, "image/jpeg").scanner();
            return url;
        } catch (Exception e) {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e1) {

            }
        }
        return null;
    }


    public static class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
        MediaScannerConnection conn = null;
        String filePath;
        String fileType;

        public MediaScanner(Context context, String file, String mime) {
            conn = new MediaScannerConnection(context, this);
            this.filePath = file;
            this.fileType = mime;
        }


        @Override
        public void onMediaScannerConnected() {
            conn.scanFile(filePath, fileType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.d("MediaScanner", "onScanCompleted: path:" + path);
            Log.d("MediaScanner", "onScanCompleted: uri:" + uri);
            conn.disconnect();
        }

        public void scanner() {
            conn.connect();
        }
    }


    public static void scanWithPath(Context cont, String[] pathes) {
        MediaScannerConnection.OnScanCompletedListener callback = new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                //the work which want to do when scan completed
                Log.i("FilePick", "path: " + path + ", uri: " + uri.toString());
            }
        };
        MediaScannerConnection.scanFile(cont, pathes, null, callback);

    }


    public static class MediaDirScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private File mScanDir;

        private MediaScannerConnection mScanner;

        public MediaDirScanner(File dir) {
            mScanDir = dir;
            Log.i("MediaDirScanner", mScanDir.getAbsolutePath());
        }

        @Override
        public void onMediaScannerConnected() {
            scanFile(mScanDir);
        }


        private void scanFile(File file) {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                File f = fileList[i];
                if (f.isDirectory()) {
                    scanFile(f);
                } else {
                    String path = f.getAbsolutePath();
                    mScanner.scanFile(path, null);
                    Log.i("MediaDirScanner", "Media Scan completed on file: path=" + path);
                }
            }
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.i("MediaDirScanner", "Media Scan completed on directory: path=" + path + " uri=" + uri);
            mScanner.disconnect();
            mScanner = null;
        }

        public void setScanner(MediaScannerConnection scanner) {
            mScanner = scanner;
        }

        public void scanner() {
            mScanner.connect();
        }

    }

    public static String getVersionName(Context context) {
        try {
            PackageManager packManager = context.getPackageManager();
            PackageInfo packInfo = packManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String metaValue = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                metaValue = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return metaValue;
    }

}
