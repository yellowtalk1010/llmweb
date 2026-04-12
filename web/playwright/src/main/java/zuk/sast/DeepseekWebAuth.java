//package zuk.sast;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.microsoft.playwright.*;
//import com.microsoft.playwright.options.Cookie;
//import com.microsoft.playwright.options.RequestOptions;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//public class DeepseekWebAuth {
//
//    public static class ProgressCallback {
//        public void onProgress(String s) {
//            System.out.println(s);
//        }
//    }
//
//    public interface OpenUrlCallback {
//        boolean openUrl(String url) throws Exception;
//    }
//
//    public static class DeepSeekWebCredentials {
//        private final String cookie;
//        private final String bearer;
//        private final String userAgent;
//
//        public DeepSeekWebCredentials(String cookie, String bearer, String userAgent) {
//            this.cookie = cookie;
//            this.bearer = bearer;
//            this.userAgent = userAgent;
//        }
//
//        public String getCookie() {
//            return cookie;
//        }
//
//        public String getBearer() {
//            return bearer;
//        }
//
//        public String getUserAgent() {
//            return userAgent;
//        }
//
//        @Override
//        public String toString() {
//            return "DeepSeekWebCredentials{" +
//                    "cookie='" + cookie + '\'' +
//                    ", bearer='" + bearer + '\'' +
//                    ", userAgent='" + userAgent + '\'' +
//                    '}';
//        }
//    }
//
//    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//
//    public static void main(String[] args) {
//        try (Playwright playwright = Playwright.create()) {
//
//            try {
//                /***
//                 * 1. 启动系统已经安装的chrome
//                 * 2. 前提是系统的chrome启动时，启用了9222端口，
//                 *    命令执行： .\chrome.exe --remote-debugging-port=9222 --user-data-dir=D:\temp\chrome-debug
//                 *    检查端口： netstat -ano | findstr 9222
//                 */
//                String deepseekChatURL = "https://chat.deepseek.com";
//                Browser browser = playwright.chromium().connectOverCDP("http://127.0.0.1:9222");
//                List<BrowserContext> browserContexts = browser.contexts();
//                for (BrowserContext browserContext : browserContexts) {
//                    List<Page> pages = browserContext.pages().stream().filter(p->{
//                        System.out.println("网页地址：" + p.url());
//                        return p.url().startsWith(deepseekChatURL);
//                    }).toList();
//
//                    Page deepseekPage = null;
//                    if (pages==null || pages.size()==0) {
//                        System.out.println("打开deepseek");
//                        browserContext.newPage().navigate(deepseekChatURL);
//                        deepseekPage = browserContext.pages().get(0);
//                    }
//                    else {
//                        deepseekPage = pages.get(0);
//                    }
//
//                    List<Cookie> cookieList = browserContext.cookies(deepseekChatURL);
//                    System.out.println(deepseekChatURL + "的cookie输出：");
//                    AtomicReference<String> cookieStr = new AtomicReference<>("");
//                    cookieList.stream().forEach(cookie -> {
//                        String name = cookie.name;
//                        String value = cookie.value;
//                        String cURL = cookie.url;
//                        System.out.println();
//                        String s = name + "=" + value  + "; " + cookieStr.get();
//                        cookieStr.set(s);
//                    });
//                    System.out.println("cookieStr:" + cookieStr.get());
//
//                    APIResponse apiResponse = deepseekPage.request().get("https://chat.deepseek.com/api/v0/users/current");
//                    if(apiResponse.ok()){
//                        String data = apiResponse.text();
//                        System.out.println(data);   //输出: {"code":40002,"msg":"Missing Token","data":null}
//                    }
//
//                    String userAgent = (String) deepseekPage.evaluate("() => navigator.userAgent");
//                    System.out.println("User-Agent: " + userAgent); //输出内容： User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36
//
//                    DeepseekWebAuth deepseekWebAuth = new DeepseekWebAuth();
//                    deepseekWebAuth.waitForLogin(browserContext, deepseekPage, userAgent, true);
//
//                    Thread.sleep(2000);
//
//                    Locator textarea = deepseekPage.locator("textarea[placeholder='给 DeepSeek 发送消息 ']");
////                    textarea.waitFor();
////                    textarea.fill("用joern写一个c/c++/java三种语言的数据溢出功能");
//                    textarea.fill("halo");
//
//                    Locator sendButton = deepseekPage.locator("div[role='button'][aria-disabled='false']").last();
//                    sendButton.click();
//
//
////                    DeepseekWebAuth deepseekWebAuth = new DeepseekWebAuth();
////                    deepseekWebAuth.loginDeepseekWebAttachOnly(9222, "", new ProgressCallback());
////
////                    System.out.println("等待");
////
////                    DeepseekWebClient.DeepSeekWebClientOptions options = new DeepseekWebClient.DeepSeekWebClientOptions();
////                    options.setBearer("");
////                    options.setCookie(cookieStr.get());
////                    options.setUserAgent(userAgent);
//
////                    DeepseekWebClient client = new DeepseekWebClient(options);
////                    client.init();
////                    DeepseekWebClient.DeepSeekChatSession session = client.createChatSession();
////                    DeepseekWebClient.ChatCompletionParams params = new DeepseekWebClient.ChatCompletionParams();
////                    params.setSessionId(session.getChatSessionId());
////                    params.setMessage("你好");
////                    params.setModel("deepseek-chat");
////                    params.setSearchEnabled(true);
////                    params.setPreempt(false);
////                    params.setTimeout(Duration.ofSeconds(60));
////
////                    try (InputStream in = client.chatCompletions(params)) {
////                        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
////                        System.out.println(text);
////                    }
//
//
//                    System.out.println("完成");
//                    Thread.sleep(1000*60 * 30);
//
//                }
//
//            }
//            catch (Exception exception) {
//                exception.printStackTrace();
//            }
//
//        }
//    }
//
//
//    /**
//     * 只 attach 到已有 Chrome。
//     */
//    public DeepSeekWebCredentials loginDeepseekWebAttachOnly(int cdpPort, String cdpUrl, ProgressCallback onProgress) throws Exception {
//        String finalCdpUrl = (cdpUrl != null && !cdpUrl.isBlank())
//                ? cdpUrl
//                : "http://127.0.0.1:" + cdpPort;
//
//        onProgress.onProgress("Connecting to existing Chrome...");
//
//        try (Playwright playwright = Playwright.create()) {
//            onProgress.onProgress("Waiting for browser debugger...");
//
//            String wsUrl = waitForChromeWebSocketUrl(finalCdpUrl, 10, 500, 2000);
//            if (wsUrl == null) {
//                throw new IllegalStateException("Failed to connect to Chrome at " + finalCdpUrl);
//            }
//
//            onProgress.onProgress("Connecting to browser...");
//            Browser browser = playwright.chromium().connectOverCDP(wsUrl);
//
//            try {
//                BrowserContext context = browser.contexts().isEmpty()
//                        ? browser.newContext()
//                        : browser.contexts().get(0);
//
//                Page page = findOrCreateDeepSeekPage(context, onProgress);
//
//                onProgress.onProgress("Checking for existing DeepSeek session...");
//
//                List<Cookie> existingCookies = context.cookies(Arrays.asList(
//                        "https://chat.deepseek.com",
//                        "https://deepseek.com"
//                ));
//                String cookieString = joinCookies(existingCookies);
//
//                boolean hasDeviceId = cookieString.contains("d_id=");
//                boolean hasSessionId = cookieString.contains("ds_session_id=");
//                boolean hasSessionInfo = cookieString.contains("HWSID=") || cookieString.contains("uuid=");
//
//                String bearer = "";
//                String userAgent = (String) page.evaluate("() => navigator.userAgent");
//
//                if ((hasDeviceId || hasSessionId || hasSessionInfo || existingCookies.size() > 3)
//                        && cookieString.length() > 10) {
//                    onProgress.onProgress("Found existing DeepSeek session!");
//
//                    try {
//                        page.navigate("https://chat.deepseek.com/",
//                                new Page.NavigateOptions().setTimeout(5000));
//                    } catch (Exception ignored) {
//                    }
//
//                    try {
//                        Map<String, String> localStorageData = (Map<String, String>) page.evaluate(
//                                "() => {\n" +
//                                        "  const data = {};\n" +
//                                        "  for (let i = 0; i < localStorage.length; i++) {\n" +
//                                        "    const key = localStorage.key(i);\n" +
//                                        "    if (key) data[key] = localStorage.getItem(key) || '';\n" +
//                                        "  }\n" +
//                                        "  return data;\n" +
//                                        "}"
//                        );
//
//                        for (Map.Entry<String, String> entry : localStorageData.entrySet()) {
//                            String key = entry.getKey();
//                            String value = entry.getValue();
//
//                            if (key == null || value == null) {
//                                continue;
//                            }
//
//                            String lowerKey = key.toLowerCase(Locale.ROOT);
//                            if (lowerKey.contains("token") || lowerKey.contains("auth")) {
//                                try {
//                                    JsonNode parsed = OBJECT_MAPPER.readTree(value);
//                                    JsonNode tokenNode = parsed.get("token");
//                                    if (tokenNode != null && tokenNode.isTextual() && !tokenNode.asText().isBlank()) {
//                                        bearer = tokenNode.asText();
//                                        break;
//                                    } else if (parsed.isTextual() && parsed.asText().length() > 20) {
//                                        bearer = parsed.asText();
//                                        break;
//                                    }
//                                } catch (Exception e) {
//                                    if (value.length() > 20) {
//                                        bearer = value;
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    } catch (Exception ignored) {
//                    }
//
//                    if (bearer.isBlank()) {
//                        onProgress.onProgress("Requesting DeepSeek API to capture token...");
//                        try {
//                            APIResponse response = context.request().get(
//                                    "https://chat.deepseek.com/api/v0/users/current",
//                                    RequestOptions.create().setHeader("Cookie", cookieString)
//                            );
//                            if (response.ok()) {
//                                JsonNode data = OBJECT_MAPPER.readTree(response.text());
//                                bearer = data.path("data").path("biz_data").path("token").asText("");
//                            }
//                        } catch (Exception ignored) {
//                        }
//                    }
//
////                    return new DeepSeekWebCredentials(cookieString, bearer, userAgent);
//                }
//
//                onProgress.onProgress("No existing session found. Opening DeepSeek for login...");
//
//                page.navigate("https://chat.deepseek.com/");
//                userAgent = (String) page.evaluate("() => navigator.userAgent");
//
//                onProgress.onProgress(
//                        "Please login to DeepSeek in the opened browser window. The session token will be captured automatically once you are logged in."
//                );
//
//                return waitForLogin(context, page, userAgent, false);
//            } finally {
//                try {
//                    browser.close();
//                } catch (Exception ignored) {
//                }
//            }
//        }
//    }
//
//    /**
//     * 更接近你 TS 里的 loginDeepseekWeb。
//     * attachOnly=true 时接管已有 Chrome；否则这里预留给你自行补 launch Chrome 的逻辑。
//     */
//    public DeepSeekWebCredentials loginDeepseekWeb(
//            boolean attachOnly,
//            int cdpPort,
//            String cdpUrl,
//            ProgressCallback onProgress,
//            OpenUrlCallback openUrlCallback
//    ) throws Exception {
//        if (!attachOnly) {
//            throw new UnsupportedOperationException("当前示例只实现 attachOnly 模式；launch Chrome 的逻辑请按你的工程补上。");
//        }
//
//        String finalCdpUrl = (cdpUrl != null && !cdpUrl.isBlank())
//                ? cdpUrl
//                : "http://127.0.0.1:" + cdpPort;
//
//        onProgress.onProgress("Connecting to existing Chrome (attach mode)...");
//
//        try (Playwright playwright = Playwright.create()) {
//            onProgress.onProgress("Waiting for browser debugger...");
//
//            String wsUrl = waitForChromeWebSocketUrl(finalCdpUrl, 10, 500, 2000);
//            if (wsUrl == null) {
//                throw new IllegalStateException("Failed to resolve Chrome WebSocket URL from " + finalCdpUrl);
//            }
//
//            onProgress.onProgress("Connecting to browser...");
//            Browser browser = playwright.chromium().connectOverCDP(wsUrl);
//
//            try {
//                BrowserContext context = browser.contexts().isEmpty()
//                        ? browser.newContext()
//                        : browser.contexts().get(0);
//
//                Page page = findOrCreateDeepSeekPage(context, onProgress);
//
//                onProgress.onProgress("Checking for existing DeepSeek session...");
//
//                List<Cookie> existingCookies = context.cookies(Arrays.asList(
//                        "https://chat.deepseek.com",
//                        "https://deepseek.com"
//                ));
//                String cookieString = joinCookies(existingCookies);
//
//                boolean hasDeviceId = cookieString.contains("d_id=");
//                boolean hasSessionId = cookieString.contains("ds_session_id=");
//                boolean hasSessionInfo = cookieString.contains("HWSID=") || cookieString.contains("uuid=");
//                boolean hasValidSession = (hasDeviceId || hasSessionId || hasSessionInfo || existingCookies.size() > 3)
//                        && cookieString.length() > 10;
//
//                String bearer = "";
//                String userAgent = (String) page.evaluate("() => navigator.userAgent");
//
//                if (hasValidSession) {
//                    onProgress.onProgress("Found existing session, attempting to capture credentials...");
//
//                    try {
//                        APIResponse response = context.request().get("https://chat.deepseek.com/api/v0/users/current");
//                        if (response.ok()) {
//                            JsonNode data = OBJECT_MAPPER.readTree(response.text());
//                            bearer = data.path("data").path("biz_data").path("token").asText("");
//                            if (!bearer.isBlank()) {
////                                onProgress.onProgress("Successfully captured credentials!");
//                                return new DeepSeekWebCredentials(cookieString, bearer, userAgent);
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.out.println("[DeepSeek] Could not auto-capture token: " + e);
//                    }
//
//                    onProgress.onProgress("Session detected but token expired. Redirecting to login page...");
//                    hasValidSession = false;
//                }
//
//                page.navigate("https://chat.deepseek.com/");
//                userAgent = (String) page.evaluate("() => navigator.userAgent");
//
//                if (hasValidSession) {
//                    onProgress.onProgress("Session detected but token expired. Please re-login in the browser window.");
//                } else {
//                    onProgress.onProgress("Please login to DeepSeek in the opened browser window. The session token will be captured automatically once you are logged in.");
//                }
//
//                return waitForLogin(context, page, userAgent, true);
//            } finally {
//                try {
//                    browser.close();
//                } catch (Exception ignored) {
//                }
//            }
//        }
//    }
//
//
//    private DeepSeekWebCredentials waitForLogin(
//            BrowserContext context,
//            Page page,
//            String userAgent,
//            boolean rejectOnPageClose
//    ) throws Exception {
//
//        System.out.println("waitForLogin");
//
//        AtomicReference<String> capturedBearer = new AtomicReference<>(null);
//        AtomicBoolean resolved = new AtomicBoolean(false);
//        CompletableFuture<DeepSeekWebCredentials> future = new CompletableFuture<>();
//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
//
//        Runnable tryResolve = () -> {
//            if (resolved.get()) {
//                return;
//            }
//
//            String bearer = capturedBearer.get();
//            if (bearer == null || bearer.isBlank()) {
////                return;
//            }
//
//            try {
//                List<Cookie> cookies = context.cookies(Arrays.asList(
//                        "https://chat.deepseek.com",
//                        "https://deepseek.com"
//                ));
//
//                if (cookies.isEmpty()) {
//                    return;
//                }
//
//                String cookieString = joinCookies(cookies);
//                boolean hasDeviceId = cookieString.contains("d_id=");
//                boolean hasSessionId = cookieString.contains("ds_session_id=");
//                boolean hasSessionInfo = cookieString.contains("HWSID=") || cookieString.contains("uuid=");
//
//                if (hasDeviceId || hasSessionId || hasSessionInfo || cookies.size() > 3) {
//                    if (resolved.compareAndSet(false, true)) {
//                        System.out.println("[DeepSeek] Credentials captured");
//                        future.complete(new DeepSeekWebCredentials(cookieString, bearer, userAgent));
//                    }
//                }
//            } catch (Exception e) {
//                System.err.println("[DeepSeek] Failed to fetch cookies: " + e);
//            }
//        };
//
//        page.onRequest(request -> {
//            try {
//                String url = request.url();
//                System.out.println("onRequest>>>>>>>>>>>>>>>:" + url);
//                if (url.contains("/api/v0/") && !url.contains("completion")) {
//                    Map<String, String> headers = request.headers();
//                    String auth = headers.get("authorization");
//                    if (auth != null && auth.startsWith("Bearer ")) {
//                        if (capturedBearer.get() == null) {
//                            System.out.println("[DeepSeek Research] Captured Bearer Token.");
//                            capturedBearer.set(auth.substring(7));
//                        }
//                        tryResolve.run();
//                    }
//
//                    if (url.contains("/api/v0/chat/completion")) {
//                        System.out.println("[DeepSeek Research] Completion Request Headers Check: { hasAuth: " + (auth != null) + " }");
//                    }
//                }
//                else if (url.contains("completion")){
//                    String postData = request.postData();
//                    System.out.println("postData：" + postData);
//                    Response response = request.response();
//                    String responseText = response.text();
////                    System.out.println("==========================>" + responseText);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        page.onResponse(response -> {
//            try {
//                String url = response.url();
//                System.out.println("onResponse>>>>>>>>>>>>>>>:" + url);
//                if (url.contains("/api/v0/users/current") && response.ok()) {
//                    JsonNode body = OBJECT_MAPPER.readTree(response.text());
//                    JsonNode tokenNode = body.path("data").path("biz_data").path("token");
//                    String tokenFromResponse = tokenNode.isTextual() ? tokenNode.asText("") : "";
//
//                    if (!tokenFromResponse.isBlank()) {
//                        if (capturedBearer.get() == null) {
//                            System.out.println("[DeepSeek] Captured token from users/current response");
//                            capturedBearer.set(tokenFromResponse);
//                        }
//                        tryResolve.run();
//                    }
//                }
//                else if(url.contains("completion")){
//                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>responseText:");
//                    String responseText = response.text();
//                    System.out.println(responseText);
//                    System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//                }
//            } catch (Exception ignored) {
//            }
//        });
//
//        if (rejectOnPageClose) {
//            page.onClose(pageClosed -> {
//                if (resolved.compareAndSet(false, true)) {
//                    future.completeExceptionally(
//                            new IllegalStateException("Browser window closed before login was captured.")
//                    );
//                }
//            });
//        }
//
//        ScheduledFuture<?> pollTask = scheduler.scheduleAtFixedRate(tryResolve, 2, 2, TimeUnit.SECONDS);
//        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
//            if (resolved.compareAndSet(false, true)) {
//                future.completeExceptionally(new TimeoutException("Login timed out (5 minutes)."));
//            }
//        }, 5, TimeUnit.MINUTES);
//
//        try {
//            return future.get();
//        } catch (ExecutionException e) {
//            Throwable cause = e.getCause();
//            if (cause instanceof Exception ex) {
//                throw ex;
//            }
//            throw new RuntimeException(cause);
//        } finally {
//            pollTask.cancel(true);
//            timeoutTask.cancel(true);
//            scheduler.shutdownNow();
//        }
//    }
//
//
//    private Page findOrCreateDeepSeekPage(BrowserContext context, ProgressCallback onProgress) {
//        List<Page> existingPages = context.pages();
//        for (Page p : existingPages) {
//            String url = safeUrl(p);
//            if (url.contains("deepseek.com") || url.contains("chat.deepseek.com")) {
//                onProgress.onProgress("Found existing DeepSeek page, switching to it...");
//                try {
//                    p.bringToFront();
//                } catch (Exception ignored) {
//                }
//                return p;
//            }
//        }
//
//        onProgress.onProgress("Opening DeepSeek page...");
//        return context.newPage();
//    }
//
//
//    private String safeUrl(Page page) {
//        try {
//            return page.url();
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//    private String joinCookies(List<Cookie> cookies) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < cookies.size(); i++) {
//            Cookie c = cookies.get(i);
//            if (i > 0) {
//                sb.append("; ");
//            }
//            sb.append(c.name).append("=").append(c.value);
//        }
//        return sb.toString();
//    }
//
//
//    /**
//     * 从 http://127.0.0.1:9222/json/version 取 webSocketDebuggerUrl
//     */
//    private String waitForChromeWebSocketUrl(String cdpUrl, int maxRetries, long sleepMillis, int requestTimeoutMillis)
//            throws IOException, InterruptedException {
//        for (int i = 0; i < maxRetries; i++) {
//            String ws = getChromeWebSocketUrl(cdpUrl, requestTimeoutMillis);
//            if (ws != null && !ws.isBlank()) {
//                return ws;
//            }
//            Thread.sleep(sleepMillis);
//        }
//        return null;
//    }
//
//    private String getChromeWebSocketUrl(String cdpUrl, int requestTimeoutMillis) throws IOException, InterruptedException {
//        String url = cdpUrl.endsWith("/") ? cdpUrl + "json/version" : cdpUrl + "/json/version";
//
//        HttpClient client = HttpClient.newBuilder()
//                .connectTimeout(Duration.ofMillis(requestTimeoutMillis))
//                .build();
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .timeout(Duration.ofMillis(requestTimeoutMillis))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() != 200) {
//            return null;
//        }
//
//        try {
//            String body = response.body();
//            JsonNode json = OBJECT_MAPPER.readTree(body);
//            return json.path("webSocketDebuggerUrl").asText(null);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//}
//
//
