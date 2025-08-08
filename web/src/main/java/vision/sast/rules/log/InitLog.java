package vision.sast.rules.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.LoggerContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitLog {
    static {
        // 初始化 log 输出到指定文件
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n");
        encoder.start();

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(context);
        fileAppender.setName("MY_FILE_APPENDER");
        fileAppender.setFile("logs/dynamic-log.log");
        fileAppender.setEncoder(encoder);
        fileAppender.setImmediateFlush(true);
        fileAppender.start();

        // 强转 SLF4J 的 log 为 Logback 的 Logger
        Logger logbackLogger = (Logger) LoggerFactory.getLogger(InitLog.class);
        logbackLogger.addAppender(fileAppender);
    }

    public void doSomething() {
        log.info("这条日志会被写入 logs/dynamic-log.log");
    }

    public static void main(String[] args) {
        new InitLog().doSomething();
    }
}
