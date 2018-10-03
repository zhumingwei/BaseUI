package com.bond.baseui.util.encrypt;


import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author zhumingwei
 * @date 2018/7/5 下午3:45
 * @email zdf312192599@163.com
 */
public class Md5Util {
    public static String getHmacMd5Str(String s) {
        return getHmacMd5Str(s, "zhumingwei2018");
    }

    public static String getHmacMd5Str(String s, String keyString) {
        String sEncodedString = null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes(StandardCharsets.UTF_8), "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);

            byte[] bytes = mac.doFinal(s.getBytes(StandardCharsets.UTF_8));

            return new String(Base64.encode(bytes, Base64.DEFAULT), StandardCharsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sEncodedString;
    }
}
