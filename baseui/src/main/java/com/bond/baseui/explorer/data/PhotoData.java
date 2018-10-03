package com.bond.baseui.explorer.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import com.bond.baseui.R;
import com.bond.baseui.explorer.PhotoDirectory;
import com.bond.baseui.explorer.photo.BitmapUtil;
import com.bond.baseui.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

/**
 * Created by yuweichen on 15/12/22.
 */
public class PhotoData {
    public final static int INDEX_ALL_PHOTOS = 0;

    public static List<PhotoDirectory> getDataFromCursor(Context context, Cursor data, boolean checkImageStatus) {
        List<PhotoDirectory> directories = new ArrayList<>();
        PhotoDirectory photoDirectoryAll = new PhotoDirectory();
        photoDirectoryAll.setName(context.getString(R.string.all_photo));
        photoDirectoryAll.setId("ALL");


        while (data.moveToNext()) {

            int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
            String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
            String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
            String path = data.getString(data.getColumnIndexOrThrow(DATA));

            if (checkImageStatus) {
                if (!BitmapUtil.checkImgCorrupted(path)) {
                    PhotoDirectory photoDirectory = new PhotoDirectory();
                    photoDirectory.setId(bucketId);
                    photoDirectory.setName(name);

                    if (!directories.contains(photoDirectory)) {
                        photoDirectory.setCoverPath(path);
                        photoDirectory.addPhoto(imageId, path);
                        photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                        directories.add(photoDirectory);
                    } else {
                        directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, path);
                    }

                    photoDirectoryAll.addPhoto(imageId, path);
                }
            } else {

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);

                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(imageId, path);
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                    directories.add(photoDirectory);
                } else {
                    directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, path);
                }

                photoDirectoryAll.addPhoto(imageId, path);
            }


        }
        if (photoDirectoryAll.getPhotoPaths().size() > 0) {
            photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotoPaths().get(0));
        }
        directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);

        return directories;
    }


    public static List<String> getRecentlyPhoto(AppCompatActivity activity, int size, boolean onlyPhoto) {
        try {

            // String sdcardPath = Environment.getExternalStorageDirectory().toString();

            ContentResolver mContentResolver = activity.getContentResolver();
            Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA},
                    MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_ADDED + " DESC"); //
            List<String> paths = new ArrayList<>();
            while (mCursor.moveToNext()) {
                // 打印LOG查看照片ID的值

                // 过滤掉不需要的图片，只获取拍照后存储照片的相册里的图片
                String path = mCursor.getString(mCursor.getColumnIndexOrThrow(DATA));
                Logger.i("Photo", path);

                if(onlyPhoto){
                    if (path.contains("DCIM/100MEDIA") || path.contains("DCIM/Camera/")
                            || path.contains("DCIM/100ANDRO") || path.contains("DCIM/100Andro")) {
                        paths.add(path);
                        // img_path.add("file://" + path);
                    }
                }else {
                    paths.add(path);
                }


                if (paths.size() == size) {
                    break;
                }
            }
            mCursor.close();

            return paths;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
