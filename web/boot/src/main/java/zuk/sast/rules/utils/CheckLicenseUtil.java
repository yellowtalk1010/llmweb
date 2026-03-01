package zuk.sast.rules.utils;

import org.springframework.util.Assert;

import java.time.LocalDate;

public class CheckLicenseUtil {

    public static void checkLicense(){
        LocalDate now = LocalDate.now();                   // 当前日期
        LocalDate target = LocalDate.of(2026, 12, 30);       // 目标日期：2026-2-16
        Assert.isTrue(!now.isAfter(target), "数据导入超时");
    }


}
