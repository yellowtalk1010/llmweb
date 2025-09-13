package zuk.sast.rules.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

/***
 * 文件编码格式探索工具
 */
public class FileEncodingDetector {

    public static final String UTF_8 = "UTF-8";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String US_ASCII = "US-ASCII";
    public static final String UTF_16BE = "UTF-16BE";
    public static final String UTF_16LE = "UTF-16LE";
    public static final String UTF_16 = "UTF-16";
    public static final String GBK = "GBK";
    public static final String GB2312 = "GB2312";
    public static final String OTHER = "OTHER";

    public static String detectEncoding(String filePath) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(filePath));

        // 1. BOM 判断
        if (data.length >= 3 &&
                (data[0] & 0xFF) == 0xEF &&
                (data[1] & 0xFF) == 0xBB &&
                (data[2] & 0xFF) == 0xBF) {
            return UTF_8;
        }

        // 2. UTF-8 校验
        if (isUTF8(data)) {
            return UTF_8;
        }

        // 3. 简单假设：如果不是 UTF-8，那多数中文环境是 GBK
        if (looksLikeGBK(data)) {
            return GBK;
        }

        // 4. 其他编码
        return OTHER;
    }

    private static boolean isUTF8(byte[] data) {
        int i = 0;
        while (i < data.length) {
            int c = data[i] & 0xFF;
            if (c < 0x80) { // 单字节 (ASCII)
                i++;
            } else if ((c >> 5) == 0x6 && i + 1 < data.length) { // 110x xxxx 10xx xxxx
                if ((data[i + 1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((c >> 4) == 0xE && i + 2 < data.length) { // 1110 xxxx 10xx xxxx 10xx xxxx
                if ((data[i + 1] & 0xC0) != 0x80 ||
                        (data[i + 2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((c >> 3) == 0x1E && i + 3 < data.length) { // 11110xxx 10xx xxxx 10xx xxxx 10xx xxxx
                if ((data[i + 1] & 0xC0) != 0x80 ||
                        (data[i + 2] & 0xC0) != 0x80 ||
                        (data[i + 3] & 0xC0) != 0x80) return false;
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean looksLikeGBK(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            if (b >= 0x81 && b <= 0xFE) { // GBK 双字节范围
                if (i + 1 < data.length) {
                    int b2 = data[i + 1] & 0xFF;
                    if (b2 >= 0x40 && b2 <= 0xFE && b2 != 0x7F) {
                        i++; // 跳过双字节
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    // 测试
//    public static void main(String[] args) throws Exception {
//        System.out.println(detectEncoding("test_utf8.txt")); // UTF-8
//        System.out.println(detectEncoding("test_gbk.txt"));  // GBK
//        System.out.println(detectEncoding("test_other.txt")); // Other
//    }
}
