package zuk.sast;

import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PlaywrightTest {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {

            try {
                /***
                 * 1. 启动系统已经安装的chrome
                 * 2. 前提是系统的chrome启动时，启用了9222端口，
                 *    命令执行： .\chrome.exe --remote-debugging-port=9222 --user-data-dir=D:\temp\chrome-debug
                 *    检查端口： netstat -ano | findstr 9222
                 */
                Browser browser = playwright.chromium().connectOverCDP("http://127.0.0.1:9222");
                List<BrowserContext> browserContexts = browser.contexts();
                for (BrowserContext browserContext : browserContexts) {
                    List<Page> pages = browserContext.pages();
                    for (Page page : pages) {
                        String url = page.url();
                        System.out.println(url); //输出当前页面的url地址
                    }
                }

//                BrowserContext context = browser.newContext();
//                Page page = context.newPage();
//                page.navigate("https://playwright.dev/");
//                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot-" + playwright.chromium().name() + ".png")));
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    }
}