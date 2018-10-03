package com.bond.baseui.explorer.data;

import android.content.Context;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

/**
 * @author zhumingwei
 * @date 2018/7/6 下午1:59
 * @email zdf312192599@163.com
 */
public class PhotoDirectoryLoader extends CursorLoader {

    private final static String IMAGE_JPEG = "image/jpeg";
    private final static String IMAGE_PNG = "image/png";
    private final static String IMAGE_GIF = "image/gif";

    final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
    };

    public PhotoDirectoryLoader(Context context, boolean showGif) {
        super(context);

        setProjection(IMAGE_PROJECTION);
        setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC");

//    setSelection(
//        MIME_TYPE + "=? or " + MIME_TYPE + "=? " + (showGif ? ("or " + MIME_TYPE + "=?") : ""));
//    String[] selectionArgs;
//    if (showGif) {
//      selectionArgs = new String[] { IMAGE_JPEG, IMAGE_PNG, IMAGE_GIF };
//    } else {
//      selectionArgs = new String[] { IMAGE_JPEG, IMAGE_PNG };
//    }
//    setSelectionArgs(selectionArgs);
    }


}
