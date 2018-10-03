package com.bond.baseui.explorer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.bond.baseui.explorer.data.PhotoData;
import com.bond.baseui.explorer.data.PhotoDirectoryLoader;

import java.util.List;

/**
 * @author zhumingwei
 * @date 2018/7/6 上午11:57
 * @email zdf312192599@163.com
 */
public class DataStoreHelper {

    public static void getPhotoDirs(AppCompatActivity activity, Bundle args, PhotosResultCallback resultCallBack) {
        PhotoDirLoaderCallbacks callbacks = new PhotoDirLoaderCallbacks(activity, args.getBoolean(PickConfig.Companion.getEXTRA_CHECK_IMAGE()), resultCallBack);
        activity.getSupportLoaderManager()
                .initLoader(0, args, callbacks);
    }

    static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context context;
        private PhotosResultCallback resultCallback;
        private boolean checkImageStatus;

        public PhotoDirLoaderCallbacks(Context context, boolean checkImageStatus, PhotosResultCallback resultCallBack) {
            this.context = context;
            this.resultCallback = resultCallBack;
            this.checkImageStatus = checkImageStatus;
        }

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            return new PhotoDirectoryLoader(context, args.getBoolean(PickConfig.Companion.getEXTRA_SHOW_GIF(), false));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            try {
                if (data == null) return;
                List<PhotoDirectory> directories = PhotoData.getDataFromCursor(context, data, checkImageStatus);
                data.close();
                if (resultCallback != null) {
                    resultCallback.onResultCallback(directories);
                }

            } catch (Exception e) {

            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }

    }


    public interface PhotosResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }

}
