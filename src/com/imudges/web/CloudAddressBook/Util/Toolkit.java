package com.imudges.web.CloudAddressBook.Util;

import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class Toolkit {

    public static final Log log = Logs.get();

    public static String captcha_attr = "nutz_captcha";

    public static boolean checkCaptcha(String expected, String actual) {
        if (expected == null || actual == null || actual.length() == 0 || actual.length() > 24)
            return false;
        return actual.equalsIgnoreCase(expected);
    }

    public static String passwordEncode(String password, String slat) {
        String str = slat + password + slat;
        return Lang.digest("SHA-512", str);
    }

    private static final String Iv = "\0\0\0\0\0\0\0\0";
    private static final String Transformation = "DESede/CBC/PKCS5Padding";

    public static String _3DES_encode(byte[] key, byte[] data) {
        SecretKey deskey = new SecretKeySpec(key, "DESede");
        IvParameterSpec iv = new IvParameterSpec(Iv.getBytes());
        try {
            Cipher c1 = Cipher.getInstance(Transformation);
            c1.init(Cipher.ENCRYPT_MODE, deskey, iv);
            byte[] re = c1.doFinal(data);
            return Lang.fixedHexString(re);
        } catch (Exception e) {
            log.info("3DES FAIL?", e);
            e.printStackTrace();
        }
        return null;
    }

    public static String _3DES_decode(byte[] key, byte[] data) {
        SecretKey deskey = new SecretKeySpec(key, "DESede");
        IvParameterSpec iv = new IvParameterSpec(Iv.getBytes());
        try {
            Cipher c1 = Cipher.getInstance(Transformation);
            c1.init(Cipher.DECRYPT_MODE, deskey, iv);
            byte[] re = c1.doFinal(data);
            return new String(re);
        } catch (Exception e) {
            log.debug("BAD 3DES decode", e);
        }
        return null;
    }

    public static NutMap kv2map(String kv) {
        NutMap re = new NutMap();
        if (kv == null || kv.length() == 0 || !kv.contains("="))
            return re;
        String[] tmps = kv.split(",");
        for (String tmp : tmps) {
            if (!tmp.contains("="))
                continue;
            String[] tmps2 = tmp.split("=", 2);
            re.put(tmps2[0], tmps2[1]);
        }
        return re;
    }


//
//    public static String randomPasswd(User usr) {
//        String passwd = R.sg(10).next();
//        String slat = R.sg(48).next();
//        usr.setSalt(slat);
//        usr.setPassword(passwordEncode(passwd, slat));
//        return passwd;
//    }
//
//    public static void generatePasswd(User usr,String passwd) {
//        String slat = R.sg(48).next();
//        usr.setSalt(slat);
//        usr.setPassword(passwordEncode(passwd, slat));
//    }


    public static byte[] hexstr2bytearray(String str) {
        byte[] re = new byte[str.length() / 2];
        for (int i = 0; i < re.length; i++) {
            int r = Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            re[i] = (byte) r;
        }
        return re;
    }

    //获得ak
    public static String getAccessKey(){
        String slat = R.sg(48).next();
        return Lang.digest("SHA-512", Lang.digest("SHA-512",System.currentTimeMillis() + slat) + slat).substring(32);
    }

    public static NutMap getSuccessResult(String msg, Object data){
        NutMap result = new NutMap();
        return result.setv("ret",0).setv("msg", msg).setv("data", data);
    }

    public static NutMap getFailResult(int ret, String msg, Object data){
        NutMap result = new NutMap();
        return result.setv("ret",ret).setv("msg", msg).setv("data", data);
    }

    //检查String 是否合法，tag 1 判空，0 不判空
    /**
     * true:合法
     * false:非法
     * */
    public static boolean checkStr(String str,int tag){
        boolean resTag = false;
        switch (tag){
            case 1:
                if(str != null && !str.equals("")){
                    resTag = true;
                } else {
                    resTag = false;
                }
                break;
            case 0:
                if(str != null){
                    resTag = true;
                } else {
                    resTag = false;
                }
                break;

            default:
                break;
        }
        return resTag;
    }

    //Url编码
    public static String encodeUrlString(String str, String charset) {
        String strret = null;
        if (str == null)
            return str;
        try {
            strret = java.net.URLEncoder.encode(str, charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return strret;
    }

    //判断手机号合法性
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }
}