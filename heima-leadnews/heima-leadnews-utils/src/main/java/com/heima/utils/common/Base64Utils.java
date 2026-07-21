package com.heima.utils.common;
//
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

import java.util.Base64;
public class Base64Utils {

    /**
     * 解码
     * @param base64
     * @return
     */
//    public static byte[] decode(String base64){
//        BASE64Decoder decoder = new BASE64Decoder();
//        try {
//            // Base64解码
//            byte[] b = decoder.decodeBuffer(base64);
//            for (int i = 0; i < b.length; ++i) {
//                if (b[i] < 0) {// 调整异常数据
//                    b[i] += 256;
//                }
//            }
//            return b;
//        } catch (Exception e) {
//            return null;
//        }
//    }

    public static byte[] decode(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * 编码
     * @param data
     * @return
     * @throws Exception
     */
//    public static String encode(byte[] data) {
//        BASE64Encoder encoder = new BASE64Encoder();
//        return encoder.encode(data);
//    }

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}