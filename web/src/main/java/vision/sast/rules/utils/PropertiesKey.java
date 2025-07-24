package vision.sast.rules.utils;

import java.util.Properties;

public class PropertiesKey {
    public static String codeFormat = "codeFormat"; //编码格式
    public static String LLM = "LLM";             //是否支持大模型
    public static Properties properties = new Properties();
    static {
        properties.setProperty(codeFormat, "GBK");
        properties.setProperty(LLM, Boolean.FALSE.toString());
    }
}
