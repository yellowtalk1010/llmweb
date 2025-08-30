package zuk.sast.rules.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;

/***
 * 获取 resources 文件夹中文件工具类
 */
@Slf4j
public class ResourceFileUtils {

    public static File findFile(String filepath) {
        try {
            File file = ResourceUtils.getFile("classpath:" + filepath);
            log.info("resource file:" + file.getAbsolutePath() + ", exists:" + file.exists());
            return file;
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }

    }

}
