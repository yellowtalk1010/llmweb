package zuk.sast.rules.utils;

import org.springframework.util.Assert;

import java.time.LocalDate;

public class CheckLicenseUtil {

    public static void checkLicense(){
        LocalDate now = LocalDate.now();                   // 当前日期
        LocalDate target = LocalDate.of(2026, 1, 1);       // 目标日期：2026-01-0
        Assert.isTrue(!now.isAfter(target), "数据导入超时");
    }

}
