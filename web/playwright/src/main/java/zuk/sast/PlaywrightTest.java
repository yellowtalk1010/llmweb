package zuk.sast;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
                    APIResponse apiResponse = deepseekPage.request().get("https://chat.deepseek.com/api/v0/users/current");
                    if(apiResponse.ok()){
                        String data = apiResponse.text();
                        System.out.println(data);   //输出: {"code":40002,"msg":"Missing Token","data":null}
                    }

                    String userAgent = deepseekPage.evaluate("() => navigator.userAgent").toString();
                    System.out.println("User-Agent: " + userAgent); //输出内容： User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36


                    AtomicReference<String> capturedBearer = new AtomicReference<>(null);
                    deepseekPage.onRequest(request -> {
                        //监听一个请求
                        String url = request.url();
                        if (url.contains("/api/v0/")) {
                            Map<String, String> headers = request.headers();
                            String auth = headers.get("authorization");
                            System.out.println("auth:" + auth);

                            if (auth != null && auth.startsWith("Bearer ")) {
                                if (capturedBearer.get() == null) {
                                    System.out.println("[DeepSeek Research] Captured Bearer Token.");
                                    capturedBearer.set(auth.substring(7));
                                }

//                                tryResolve(); // Java 里直接调用方法（同步）
                            }

                            if (url.contains("/api/v0/chat/completion")) {
                                System.out.println("[DeepSeek Research] Completion Request Headers Check: { hasAuth: " + (auth != null) + " }");
                            }
                        }
                    });

                    deepseekPage.onResponse(response -> {
                        String url = response.url();

                        // users/current returns token in data.biz_data.token
                        if (url.contains("/api/v0/users/current") && response.ok()) {
                            try {
                                String bodyText = response.text();
                                System.out.println(bodyText);
                            }
                            catch (Exception exception){
                                exception.printStackTrace();
                            }
                        }
                    });
//                    deepseekPage.onResponse(response -> {
//                        String url = response.url();
//
//                        // users/current returns token in data.biz_data.token
//                        if (url.contains("/api/v0/users/current") && response.ok()) {
//                            try {
//                                String bodyText = response.text();
//                                JsonNode body = objectMapper.readTree(bodyText);
//
//                                JsonNode tokenNode = body.path("data").path("biz_data").path("token");
//                                if (tokenNode.isTextual() && !tokenNode.asText().isEmpty()) {
//                                    if (capturedBearer.get() == null) {
//                                        System.out.println("[DeepSeek] Captured token from users/current response");
//                                        capturedBearer.set(tokenNode.asText());
//                                    }
//                                    tryResolve();
//                                }
//                            } catch (Exception ignored) {
//                                // ignore
//                            }
//                        }
//                    });

                    deepseekPage.onClose(page -> {
                        System.out.println("关闭");
                    });

                    System.out.println("等待");
                    Thread.sleep(1000*60 * 30);

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