package zuk.sast.rules.log;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

/***
 * 在文件中初始化日志信息
 */
@Configuration
public class LogbackInitializer {

    @PostConstruct
    public void initLogger() {
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
        fileAppender.setFile("web_workspace/logs/global-log.log");
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
