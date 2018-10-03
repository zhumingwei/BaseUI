package com.bond.baseui.network.http.retrofit;

import android.text.TextUtils;

import com.bond.baseui.network.http.okhttp.GlobalParams;
import com.bond.baseui.network.http.okhttp.OkHttpHelper;
import com.bond.baseui.network.http.okhttp.progress.ProgressListener;
import com.bond.baseui.network.http.retrofit.convert.FastJsonConvertFactory;

import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiFactory {

    public static String BASE_URL;

    public static void init(String baseUrl) {
        BASE_URL = baseUrl;
    }

    public static <T> T createRetrofitService(Class<T> clazz) {
        return createRetrofitService(clazz, BASE_URL);
    }

    public static <T> T createRetrofitService(Class<T> clazz, String baseUrl) {
        return createRetrofitService(clazz, baseUrl, GlobalParams.getHeaders(), GlobalParams.getParams());
    }

    /**
     * 创建一个基于OkHttp的retrofit服务
     *
     * @param clazz
     * @param baseUrl
     * @param headerMap
     * @param defaultParams
     * @param <T>
     * @return
     */
    public static <T> T createRetrofitService(Class<T> clazz, String baseUrl, final Map<String, String> headerMap, final Map<String, String> defaultParams) {
        return createRetrofitService(clazz, baseUrl, headerMap, defaultParams, null);
    }

    /**
     * 创建一个基于OkHttp的retrofit服务
     *
     * @param clazz            需要生成的API
     * @param baseUrl          API的Host地址
     * @param headerMap        一些头信息
     * @param progressListener 处理下载进度
     * @param <T>
     * @return
     */
    public static <T> T createRetrofitService(Class<T> clazz, String baseUrl, final Map<String, String> headerMap, final Map<String, String> defaultParams, ProgressListener progressListener) {

        if (TextUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("baseUrl not be null,please init...");
        }

        OkHttpClient okClient = OkHttpHelper.createOkHttpClient(headerMap, defaultParams, progressListener);

        Retrofit client = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(FastJsonConvertFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        T service = (T) client.create(clazz);
        return service;
    }
}
