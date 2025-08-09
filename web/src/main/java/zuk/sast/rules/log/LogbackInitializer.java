package zuk.sast.rules.log;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * 在文件中初始化日志信息
 */
@Configuration
public class LogbackInitializer {

    private static final String LOG_DIR = "web_workspace/logs/";
    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private void deleteLogs(){
        File dir = new File(LOG_DIR);
        if(dir.exists()){
            //移除非当天日志
            Arrays.stream(dir.listFiles()).filter(file -> {
                String dateStr = sdf1.format(new Date());
                return !file.getName().startsWith(dateStr);
            }).forEach(File::delete);
        }
    }

    private String createLogFile(){
        String date = sdf.format(new Date());
        return LOG_DIR + date + ".log";
    }

    @PostConstruct
    public void initLogger() {
        this.deleteLogs();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 创建 encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n");
        encoder.start();

        // 创建文件 appender
        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(context);
        fileAppender.setName("GLOBAL_FILE_APPENDER");
        fileAppender.setFile(this.createLogFile());
        fileAppender.setEncoder(encoder);
        fileAppender.setAppend(true);
        fileAppender.setImmediateFlush(true);
        fileAppender.start();

        // 添加到 root logger，作用于所有 logger
        Logger rootLogger = context.getLogger("ROOT");
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(fileAppender);
    }
}
