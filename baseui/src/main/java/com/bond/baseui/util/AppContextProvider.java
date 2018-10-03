package com.bond.baseui.util;

import android.app.Application;

import com.bond.baseui.network.image.ImageLoader;

public class AppContextProvider {
    private static Application app;

    public static void init(Application app) {
        AppContextProvider.app = app;
//        ImageLoader.init(app);
    }

    public static Application get() {
        return app;
    }
}
