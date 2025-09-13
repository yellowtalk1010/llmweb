package zuk.sast.rules.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    /**
     * 获取指定日期所在月份的第一天
     * @param date 输入日期
     * @return 月份的第一天
     */
    public static Date getFirstDayOfMonth(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate firstDay = localDate.withDayOfMonth(1);
        return Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取指定日期所在月份的最后一天
     * @param date 输入日期
     * @return 月份的最后一天
     */
    public static Date getLastDayOfMonth(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate lastDay = YearMonth.from(localDate).atEndOfMonth();
        return Date.from(lastDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 格式化日期输出
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
