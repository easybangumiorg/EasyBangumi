package com.heyanle.buildsrc;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Base64Util {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final char last2byte = (char)Integer.parseInt("00000011", 2);
    private static final char last4byte = (char)Integer.parseInt("00001111", 2);
    private static final char last6byte = (char)Integer.parseInt("00111111", 2);
    private static final char lead6byte = (char)Integer.parseInt("11111100", 2);
    private static final char lead4byte = (char)Integer.parseInt("11110000", 2);
    private static final char lead2byte = (char)Integer.parseInt("11000000", 2);
    private static final char[] encodeTable = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static int[] toInt = new int[128];

    public static String encodeImgageToBase64(File imageFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        // 其进行Base64编码处理
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imageFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        Base64Util encoder = new Base64Util();
        return encoder.encode(data);// 返回Base64编码过的字节数组字符串
    }

    public static boolean encodeBase64ToImage(String imageBase64, String imagePath) {
        //对字节数组字符串进行Base64解码并生成图片
        if (imageBase64 == null) //图像数据为空
            return false;
        Base64Util decoder = new Base64Util();
        try {
            //Base64解码
            byte[] b = decoder.decode(imageBase64);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            //生成图片
            OutputStream out = new FileOutputStream(imagePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String encode(byte[] from) {
        StringBuilder to = new StringBuilder((int)((double)from.length * 1.34D) + 3);
        int num = 0;
        char currentByte = 0;

        int i;
        for(i = 0; i < from.length; ++i) {
            for(num %= 8; num < 8; num += 6) {
                switch(num) {
                    case 0:
                        currentByte = (char)(from[i] & lead6byte);
                        currentByte = (char)(currentByte >>> 2);
                    case 1:
                    case 3:
                    case 5:
                    default:
                        break;
                    case 2:
                        currentByte = (char)(from[i] & last6byte);
                        break;
                    case 4:
                        currentByte = (char)(from[i] & last4byte);
                        currentByte = (char)(currentByte << 2);
                        if (i + 1 < from.length) {
                            currentByte = (char)(currentByte | (from[i + 1] & lead2byte) >>> 6);
                        }
                        break;
                    case 6:
                        currentByte = (char)(from[i] & last2byte);
                        currentByte = (char)(currentByte << 4);
                        if (i + 1 < from.length) {
                            currentByte = (char)(currentByte | (from[i + 1] & lead4byte) >>> 4);
                        }
                }

                to.append(encodeTable[currentByte]);
            }
        }

        if (to.length() % 4 != 0) {
            for(i = 4 - to.length() % 4; i > 0; --i) {
                to.append("=");
            }
        }

        return to.toString();
    }

    public static byte[] decode(String s) {
        int delta = s.endsWith("==") ? 2 : (s.endsWith("=") ? 1 : 0);
        byte[] buffer = new byte[s.length() * 3 / 4 - delta];
        int mask = 255;
        int index = 0;

        for(int i = 0; i < s.length(); i += 4) {
            int c0 = toInt[s.charAt(i)];
            int c1 = toInt[s.charAt(i + 1)];
            buffer[index++] = (byte)((c0 << 2 | c1 >> 4) & mask);
            if (index >= buffer.length) {
                return buffer;
            }

            int c2 = toInt[s.charAt(i + 2)];
            buffer[index++] = (byte)((c1 << 4 | c2 >> 2) & mask);
            if (index >= buffer.length) {
                return buffer;
            }

            int c3 = toInt[s.charAt(i + 3)];
            buffer[index++] = (byte)((c2 << 6 | c3) & mask);
        }

        return buffer;
    }

    static {
        for(int i = 0; i < ALPHABET.length; toInt[ALPHABET[i]] = i++) {
        }

    }
}