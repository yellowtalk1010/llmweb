package zuk.sast.rules;

import lombok.extern.slf4j.Slf4j;
import org.h2.Zuk;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import zuk.stockapi.LoaderLocalStockData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static zuk.sast.rules.MybatisH2DatabaseConfig.DATABASE_FILE_URL;

@Slf4j
@Component
public class StartupSuccessListener {

    /***
     * springboot启动成功后，输出一下内容
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("StartupSuccessListener onApplicationReady");
        System.out.print("""
                1. 在浏览器器中输入：http://localhost:8080/config
                2. 将在工具的/workspaces/项目名称/parseConfig.json上传
                """);
        System.out.println("3. DB地址：" + Zuk.URL);
        System.out.println("4. http://localhost:8080/pages/stock"); //http://localhost:8080/pages/allStock

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dt = sdf.format(new Date());
        String url = "https://stockapi.com.cn/v1/base/bkjj?endDate="+dt+"&startDate="+dt+"&type=1&token=" + LoaderLocalStockData.TOKEN();
        System.out.println("5. hot blocks: " + url);
        url = "https://stockapi.com.cn/v1/base/bkCodeList?endDate="+dt+"&startDate="+dt+"&bkCode=880431&token=" + LoaderLocalStockData.TOKEN();
        System.out.println("6. bot blocks stock: " + url);
    }
}
