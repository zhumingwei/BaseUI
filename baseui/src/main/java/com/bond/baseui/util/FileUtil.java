package com.bond.baseui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;


import com.bond.baseui.logger.Logger;
import com.bond.baseui.util.encrypt.Md5Util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yuweichen on 15/12/11.
 */
public class FileUtil {


    public static File createFile(String folderPath, String fileName) {
        File destDir = new File(folderPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return new File(folderPath, fileName);
    }

    /* public static File getCompressFileFromExternalCacheDir(Context context, String originalPath) {
         String fileName = String.valueOf(originalPath.hashCode());
         String filePath = getCachePath(context) + File.separator + "compress_img" + File.separator + fileName;
         File file = new File(filePath);
         if (checkFileExists(file))
             return file;
         return null;
     }
 */
    public static File saveBitmapToExternalCacheDir(Context context, Bitmap bitmap, String originalPath, int quality) {
        String folderPath = getCachePath(context) + File.separator + "compress_img" + File.separator;
        String fileName = Md5Util.getHmacMd5Str(originalPath);
        return saveBitmapToSdCard(bitmap, folderPath, fileName, quality);
    }

    /**
     * 保存图片到本地
     *
     * @param bitmap
     * @param folderPath
     * @param fileName
     * @param quality
     * @return
     */
    public static File saveBitmapToSdCard(Bitmap bitmap, String folderPath, String fileName, int quality) {
        try {
            if (bitmap != null) {
                File file = FileUtil.createFile(folderPath, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
                byte[] bytes = stream.toByteArray();
                fos.write(bytes);
                fos.close();
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeStringToSdCard(String filePath, String data) {
        try {
            //  Logger.e("writeStringToSdCard:" + filePath);
            FileOutputStream fos = new FileOutputStream(filePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(data);
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("writeStringToSdCard:" + e.getMessage());
        }
    }


    public static String getFileDirPath(Context context, String fileName, boolean debug) {
        return getFileDirPath(context, null, fileName, debug);
    }

    public static String getFileDirPath(Context context, String folder, String fileName) {
        return getFileDirPath(context, folder, fileName, false);
    }

    public static String getFileDirPath(Context context, String folder, String fileName, boolean debug) {
        String filePath;
        File file;
        if (debug && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            file = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        } else {
            file = context.getFilesDir();
        }

        if (TextUtils.isEmpty(folder)) {
            filePath = file + File.separator + fileName;
        } else {

            File f = new File(file + File.separator + folder);
            if (!f.exists()) {
                f.mkdirs();
            }

            filePath = file + File.separator + folder + File.separator + fileName;

        }


        Logger.d("filePath:" + filePath);
        return filePath;
    }

    public static String getCacheFilePath(Context context, String fileName) {
        return getCacheFilePath(context, fileName, false);
    }


    /**
     * 获取文件缓存目录
     *
     * @param context
     * @return
     */
    public static String getCacheFilePath(Context context, String fileName, boolean debug) {

        String filePath = context.getCacheDir() + File.separator + fileName;
        if (debug && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            filePath = context.getExternalCacheDir() + File.separator + fileName;
        }
        return filePath;
    }


    /**
     * 通过FileReader的方式读取读取字符串
     *
     * @param filePath
     * @return
     */
    public static String getFileByFileReader(String filePath) {
        FileReader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(filePath);
            br = new BufferedReader(reader);
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void delete(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }


        } catch (Exception e) {
            e.getMessage();
        }
    }

    public static void delete(String folderPath, String fileName) {
        try {
            File file = new File(folderPath, fileName);
            if (file.exists()) {
                file.delete();
            }

        } catch (Exception e) {
            e.getMessage();
        }
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @param deleteThisPath
     * @param filePath
     * @return
     */
    public static boolean deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 如果下面还有文件
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static String getFormatSize(double size) {
        return getFormatSize(size, 2);
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size, int newScale) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(newScale, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(newScale, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(newScale, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(newScale, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath) {
        return getFileSize(new File(filePath));
    }

    public static long getFileSize(File file) {
        long size = 0;
        if (file != null && file.exists()) {
            size = file.length();
        }
        return size; //  long / 1024 = K
    }

    /**
     * 获取文件大小
     *
     * @param size 字节
     * @return
     */
    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
        float temp = (float) size / 1024;
        if (temp >= 1024) {
            return df.format(temp / 1024) + "M";
        } else {
            return df.format(temp) + "K";
        }
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return B/KB/MB/GB
     */
    public static String formatFileSize(long fileS) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0.00B";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }


    /**
     * 获取文件的后缀名
     *
     * @param fileName 完整的文件名
     * @return 文件的后缀名 例：".3gp"
     */

    public static String getFileSuffixName(String fileName) {
        String suffixName = null;
        if (fileName.lastIndexOf(".") > 0) {
            suffixName = fileName.substring(fileName.lastIndexOf("."));
        }
        return suffixName;
    }

    public static String getSuffix(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String suffixes = "avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|txt|html|zip|java|doc";
        Pattern pat = Pattern.compile("[\\w]+[\\.](" + suffixes + ")");//正则判断
        Matcher mc = pat.matcher(url);//条件匹配
        String suffix = "";
        while (mc.find()) {
            suffix = mc.group();//截取文件名后缀名
        }
        if (!TextUtils.isEmpty(suffix)) {
            suffix = suffix.substring(suffix.indexOf("."), suffix.length());
        } else {
            suffix = ".temp";
        }
        return suffix;
    }


    /**
     * 计算SD卡的剩余空间
     *
     * @return 返回-1，说明没有安装sd卡
     */
    @SuppressWarnings("deprecation")
    public static long getFreeDiskSpace() {
        String status = Environment.getExternalStorageState();
        long freeSpace = 0;
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long availableBlocks = stat.getAvailableBlocks();
                freeSpace = availableBlocks * blockSize / 1024;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return -1;
        }
        return (freeSpace);
    }


    /**
     * 检查文件是否存在
     *
     * @param filaPath 如："/xxx/xxx.jpg"
     * @return
     */
    public static boolean checkFileExists(String filaPath) {
        return checkFileExists(new File(filaPath));
    }


    public static boolean checkFileExists(File file) {
        return file.exists();
    }


    /**
     * 复制文件
     *
     * @param source
     * @param target
     * @throws IOException
     */
    public static void nioTransferCopy(File source, File target) throws IOException {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inStream != null)
                inStream.close();
            if (in != null)
                in.close();
            if (outStream != null)
                outStream.close();
            if (out != null)
                out.close();
        }
    }

    /**
     * 检测是否挂载 SD 卡
     */
    public static boolean isSdCardMounted() {
        String externalStorageStatus = Environment.getExternalStorageState();
        boolean isMounted;
        if (externalStorageStatus.equals(Environment.MEDIA_MOUNTED)) {
            isMounted = true;
        } else
            isMounted = false;
        return isMounted;
    }

    /**
     * 得到 SD 卡路径
     */
    public static String getSDPath() {

        if (isSdCardMounted()) {//判断sd卡是否存在
            File sdDir = Environment.getExternalStorageDirectory();//获取根目录
            return sdDir.getPath();
        }
        return null;
    }


    /**
     * 获取拍照的 Uri
     *
     * @param context
     * @return
     */
    public static Uri getPhotoUri(Context context) {

        File photoPath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "camera_photos");
        if (!photoPath.exists() && !photoPath.mkdir()) {
            throw new RuntimeException("Folder cannot be created.");
        }
        File photoFile = null;
        try {
            photoFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".pic", photoPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName(), photoFile);
        return uri;
    }

    public static void clearPhotoDir(Context context) {
        String photoPath = getCachePath(context) + File.separator + "photo_img" + File.separator;
        File destDir = new File(photoPath);
        if (!destDir.exists()) return;
        File[] photos = destDir.listFiles();
        for (File photo : photos) {
            photo.delete();
        }
    }

    public static void clearFileDir(String filePath) {
        File destDir = new File(filePath);
        if (!destDir.exists()) return;
        File[] files = destDir.listFiles();
        for (File file : files) {
            file.delete();
        }
    }


    public static boolean checkLastModify(long last, String filePath) {
        File destDir = new File(filePath);
        if (!destDir.exists()) return false;
        File[] files = destDir.listFiles();
        for (File file : files) {
            if (last < file.lastModified()) return false;
        }
        return true;
    }


    public static String getRecordPath(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + "Records" + File.separator;
    }

    public static void clearRecordDir(Context context) {
        clearFileDir(getRecordPath(context));
    }

    public static String getCachePath(Context context) {
        String cachePath;
        if (isSdCardMounted()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        } else if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        } else if (!srcFile.exists()) {
            throw new FileNotFoundException("Source \'" + srcFile + "\' does not exist");
        } else if (srcFile.isDirectory()) {
            throw new IOException("Source \'" + srcFile + "\' exists but is a directory");
        } else if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source \'" + srcFile + "\' and destination \'" + destFile + "\' are the same");
        } else {
            File parentFile = destFile.getParentFile();
            if (parentFile != null && !parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination \'" + parentFile + "\' directory cannot be created");
            } else if (destFile.exists() && !destFile.canWrite()) {
                throw new IOException("Destination \'" + destFile + "\' exists but is read-only");
            } else {
                nioTransferCopy(srcFile, destFile);

            }
        }
    }


    /**
     * 从一个数量流里读取数据,返回以byte数组形式的数据。
     * </br></br>
     * 需要注意的是，如果这个方法用在从本地文件读取数据时，一般不会遇到问题，但如果是用于网络操作，就经常会遇到一些麻烦(available()方法的问题)。所以如果是网络流不应该使用这个方法。
     *
     * @param in 要读取的输入流
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream in) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            byte[] b = new byte[in.available()];
            int length = 0;
            while ((length = in.read(b)) != -1) {
                os.write(b, 0, length);
            }

            b = os.toByteArray();

            in.close();
            in = null;

            os.close();
            os = null;

            return b;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(String filepath, String charset) throws IOException {


        File file = new File(filepath);
        StringBuilder fileContents = new StringBuilder();

        BufferedReader br;

        br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        while (line != null) {
            fileContents.append(line);
            line = br.readLine();
        }

        br.close();


        return fileContents.toString();
    }

}
