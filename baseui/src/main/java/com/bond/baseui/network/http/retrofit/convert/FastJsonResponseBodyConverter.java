package com.bond.baseui.network.http.retrofit.convert;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody,T>{

    private static final String TAG = "FastJsonResponseBodyConverter";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private Type type;
    public  FastJsonResponseBodyConverter(Type type){
        this.type = type;
    }
    @Override
    public T convert(ResponseBody value) throws IOException {
        InputStreamReader isr = null;
        BufferedReader bf = null;
        try {
            isr = new InputStreamReader(value.byteStream(),UTF_8);
            bf = new BufferedReader(isr);
            StringBuffer stringBuffer = new StringBuffer();
            String line ;
            while ((line = bf.readLine())!=null){
                stringBuffer.append(line);
            }

            String content = stringBuffer.toString();
            return JSON.parseObject(content,type);
        }catch (Exception e){
            Log.e("TAG",e.getMessage());
            e.printStackTrace();
        }finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ignored) {
                }
            }
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }
}
