package zuk.sast;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                    Set<String> set = new HashSet<>();
                    for (Page page : pages) {
                        set.add(page.url());
                    }

                    List<String> urls = Arrays.asList(
                            "https://chat.deepseek.com/",
                            "https://chatgpt.com/",
                            "https://www.baidu.com/"
                    );

                    for (String url : urls) {
                        if(!set.contains(url)){
                            browserContext.newPage().navigate(url);
                            System.out.println("在浏览器中打开网址：" + url);
                        }
                    }

                    String deepseekChatURL = urls.get(0);
                    List<Cookie> cookieList = browserContext.cookies(deepseekChatURL);
                    System.out.println(deepseekChatURL + "的cookie输出：");
                    cookieList.stream().forEach(cookie -> {
                        String name = cookie.name;
                        String value = cookie.value;
                        String cURL = cookie.url;
                        System.out.println(name + "=" + value);
                    });

                    System.out.println();

                    Page deepseekPage = pages.stream().filter(p->{
                        return p.url().contains("deepseek.com");
                    }).toList().get(0);
                    APIResponse response = deepseekPage.request().get("https://chat.deepseek.com/api/v0/users/current");
                    if(response.ok()){
                        String data = response.text();
                        System.out.println(data);
                    }

                    try {
                        String userAgent = deepseekPage.evaluate("() => navigator.userAgent").toString();
                        System.out.println("User-Agent: " + userAgent);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
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