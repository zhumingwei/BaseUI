package com.bond.baseui.network.http.okhttp;

import android.text.TextUtils;

import com.bond.baseui.network.http.okhttp.progress.ProgressListener;
import com.bond.baseui.network.http.okhttp.progress.ProgressResponseBody;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpHelper {
    private static final String CACHE_DIR = "responses";

    private static class HolderClass{
        private final static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    }

    public static OkHttpClient shareClient(){
        return HolderClass.okHttpClient;
    }

    public static OkHttpClient.Builder newBuilder(){
        return new OkHttpClient.Builder()
                .connectionPool(shareClient().connectionPool());
    }

    public static OkHttpClient createOkHttpClient(){
        return createOkHttpClient(null, null, null);
    }

    public static OkHttpClient createOkHttpClient(final Map<String,String> headerMap, final Map<String,String> defaultParams, ProgressListener progressListener){
        OkHttpClient.Builder builder = newBuilder();

        builder.addNetworkInterceptor(INTERCEPTOR_REQUEST_HEADER(headerMap,defaultParams))
                .addNetworkInterceptor(new StethoInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(new RetryInterceptor(1))
                .addInterceptor(new UserAgentInterceptor());

        if(progressListener!=null){
            builder.addNetworkInterceptor(INTERCEPTOR_RESPONSE_PROGRESS(progressListener));
        }

        OkHttpClient okHttpClient = builder.build();
        return okHttpClient;
    }

    public static final Interceptor INTERCEPTOR_REQUEST_HEADER(final Map<String,String> headMap,final Map<String,String> defaultParams){
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder  requestBuilder;
                requestBuilder = original.newBuilder();
                if (headMap!=null){
                    for (Map.Entry<String,String> entry:headMap.entrySet()){
                        requestBuilder.addHeader(entry.getKey(),entry.getValue());
                    }
                }
                if (defaultParams!=null && defaultParams.size()>0){
                    HttpUrl.Builder httpUrlBuilder = original.url().newBuilder();

                    for (Map.Entry<String,String> entry:defaultParams.entrySet()){
                        httpUrlBuilder.addQueryParameter(entry.getKey(),entry.getValue());
                    }
                    requestBuilder.url(httpUrlBuilder.build());
                }
                requestBuilder.method(original.method(),original.body());
                Request request = requestBuilder.build();
                Response response = chain.proceed(request);
                return response;
            }
        };
    }

    private static final Interceptor INTERCEPTOR_RESPONSE_PROGRESS(final ProgressListener progressListener){
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(),progressListener)).build();
            }
        };
    }

    private static final class UserAgentInterceptor implements Interceptor{
    private static final String USER_AGENT_HEADER_NAME ="User-Agent";
        @Override
        public Response intercept(Chain chain) throws IOException {
            final Request originalRequest = chain.request();
            if (TextUtils.isEmpty(GlobalParams.getUserAgent())){
                return chain.proceed(originalRequest);
            }
            final Request finalRequest = originalRequest.newBuilder().removeHeader(USER_AGENT_HEADER_NAME).addHeader(USER_AGENT_HEADER_NAME, GlobalParams.getUserAgent())
                    .build();
            return chain.proceed(finalRequest);
        }
    }

    private static final class RetryInterceptor implements Interceptor{
        public int maxRetry;//最大重试次数
        private int retryNum = 0;//假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

        public RetryInterceptor(int maxRetry){
            this.maxRetry = maxRetry;
        }


        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            while (!response.isSuccessful() && retryNum < maxRetry){
                retryNum ++ ;
                response = chain.proceed(request);
            }
            return response;
        }
    }
}
