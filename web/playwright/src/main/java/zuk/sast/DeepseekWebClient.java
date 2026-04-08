package zuk.sast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeepseekWebClient {

    private static final String BASE_URL = "https://chat.deepseek.com";

    public static Page playwrightPage = null;
//    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpClient httpClient;
    private String cookie;
    private String bearer;
    private String userAgent;

    private String deviceId = "";

    public DeepseekWebClient(DeepSeekWebClientOptions options) {

        this.cookie = options.getCookie() == null ? "" : options.getCookie();
        this.bearer = options.getBearer() == null ? "" : options.getBearer();
        this.userAgent = options.getUserAgent() == null || options.getUserAgent().isBlank()
                ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                : options.getUserAgent();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public Map<String, String> fetchHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Cookie", cookie);
        headers.put("User-Agent", userAgent);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "*/*");
        if (!bearer.isBlank()) {
            headers.put("Authorization", "Bearer " + bearer);
        }
        headers.put("Referer", BASE_URL + "/");
        headers.put("Origin", BASE_URL);
        headers.put("x-client-platform", "web");
        headers.put("x-client-version", "1.7.0");
        headers.put("x-app-version", "20241129.1");
        headers.put("x-client-locale", "zh_CN");
        headers.put("x-client-timezone-offset", "28800");

        headers.put("authorization", "Bearer ah0leAC28XQ+vIjpAMvVGohher7h6UHWNcHGLJ18eYRS2TF6OB4fAFZNQJbs8lTs");  //这里缺少认证信息
        return headers;
    }

    public void init() {
        if (!deviceId.isBlank()) {
            return;
        }

        try {
            HttpRequest request = requestBuilder(
                    BASE_URL + "/api/v0/client/settings?did=&scope=banner",
                    "GET",
                    null,
                    fetchHeaders(),
                    null
            ).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 2) {
                // 当前 TS 也没真正提取 did，只是先请求 settings
            }
        } catch (Exception e) {
            System.out.println("[DeepSeekWebClient] Failed to fetch settings: " + e.getMessage());
        }
    }

    public DeepSeekPowChallenge createPowChallenge(String targetPath) throws IOException, InterruptedException {
        System.out.println("[DeepSeekWebClient] Creating PoW challenge for " + targetPath + "...");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("target_path", targetPath);

        HttpRequest request = requestBuilder(
                BASE_URL + "/api/v0/chat/create_pow_challenge",
                "POST",
                DeepseekWebAuth.OBJECT_MAPPER.writeValueAsString(body),
                fetchHeaders(),
                null
        ).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("Failed to create PoW challenge: " + response.statusCode() + " " + response.body());
        }

        DeepSeekPowResponse data = DeepseekWebAuth.OBJECT_MAPPER.readValue(response.body(), DeepSeekPowResponse.class);
        DeepSeekPowChallenge challenge = null;

        if (data != null && data.data != null && data.data.bizData != null && data.data.bizData.challenge != null) {
            challenge = data.data.bizData.challenge;
        } else if (data != null && data.data != null && data.data.challenge != null) {
            challenge = data.data.challenge;
        } else if (data != null && data.challenge != null) {
            challenge = data.challenge;
        }

        if (challenge == null) {
            throw new IOException("PoW challenge missing in response");
        }

        return challenge;
    }

    public Number solvePow(DeepSeekPowChallenge challenge) throws Exception {
        String algorithm = challenge.getAlgorithm();
        String target = challenge.getChallenge();
        String salt = challenge.getSalt();
        int difficulty = challenge.getDifficulty();
        Long expireAt = challenge.getExpireAt();

        System.out.println("[DeepSeekWebClient] Solving PoW (" + algorithm + ", difficulty: " + difficulty + ")...");

        if ("sha256".equalsIgnoreCase(algorithm)) {
            long start = System.currentTimeMillis();
            int nonce = 0;

            while (true) {
                String input = salt + target + nonce;
                String hash = sha256Hex(input);

                int zeroBits = 0;
                for (int i = 0; i < hash.length(); i++) {
                    int val = Integer.parseInt(String.valueOf(hash.charAt(i)), 16);
                    if (val == 0) {
                        zeroBits += 4;
                    } else {
                        zeroBits += Integer.numberOfLeadingZeros(val) - 28;
                        break;
                    }
                }

                int targetDifficulty = difficulty > 1000
                        ? (int) Math.floor(Math.log(difficulty) / Math.log(2))
                        : difficulty;

                if (zeroBits >= targetDifficulty) {
                    System.out.println("[DeepSeekWebClient] SHA256 PoW solved in "
                            + (System.currentTimeMillis() - start) + "ms, nonce: " + nonce);
                    return nonce;
                }

                nonce++;
                if (nonce > 1_000_000) {
                    throw new IllegalStateException("SHA256 PoW timeout");
                }
            }
        }

        if ("DeepSeekHashV1".equalsIgnoreCase(algorithm)) {
            if (playwrightPage == null) {
                throw new IllegalStateException("DeepSeekHashV1 requires Playwright Page");
            }
            if (expireAt == null) {
                throw new IllegalArgumentException("DeepSeekHashV1 requires expire_at");
            }
            return solveDeepSeekHashV1WithPlaywright(playwrightPage, challenge);
        }


        throw new IllegalArgumentException("Unsupported PoW algorithm: " + algorithm);
    }

    public DeepSeekChatSession createChatSession() throws IOException, InterruptedException {
        String targetPath = "/api/v0/chat_session/create";

        Map<String, String> headers = fetchHeaders();


        HttpRequest request = requestBuilder(
                BASE_URL + targetPath,
                "POST",
                "{}",
                headers,
                null
        ).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("Failed to create chat session: " + response.statusCode() + " " + response.body());
        }

        DeepSeekChatSessionResponse data = DeepseekWebAuth.OBJECT_MAPPER.readValue(response.body(), DeepSeekChatSessionResponse.class);
        DeepSeekChatSession bizData = data != null && data.data != null ? data.data.bizData : null;

        if (bizData == null) {
            throw new IOException("Missing biz_data in createChatSession response");
        }

        String sessionId = bizData.getId() != null && !bizData.getId().isBlank()
                ? bizData.getId()
                : bizData.getChatSessionId();

        DeepSeekChatSession result = new DeepSeekChatSession();
        result.setBizId(bizData.getBizId());
        result.setTitle(bizData.getTitle());
        result.setId(bizData.getId());
        result.setChatSessionId(sessionId);
        return result;
    }

    public InputStream chatCompletions(ChatCompletionParams params) throws Exception {
        String targetPath = "/api/v0/chat/completion";

        DeepSeekPowChallenge challenge = createPowChallenge(targetPath);
        Number answer = solvePow(challenge);

        Map<String, Object> powPayload = new LinkedHashMap<>();
        powPayload.put("algorithm", challenge.getAlgorithm());
        powPayload.put("challenge", challenge.getChallenge());
        powPayload.put("difficulty", challenge.getDifficulty());
        powPayload.put("salt", challenge.getSalt());
        powPayload.put("signature", challenge.getSignature());
        if (challenge.getExpireAt() != null) {
            powPayload.put("expire_at", challenge.getExpireAt());
        }
        powPayload.put("answer", answer);
        powPayload.put("target_path", targetPath);

        String powResponse = Base64.getEncoder()
                .encodeToString(DeepseekWebAuth.OBJECT_MAPPER.writeValueAsBytes(powPayload));

        Map<String, String> headers = new LinkedHashMap<>(fetchHeaders());
        headers.put("x-ds-pow-response", powResponse);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_session_id", params.getSessionId());
        body.put("parent_message_id", params.getParentMessageId());
        body.put("prompt", params.getMessage());
        body.put("ref_file_ids", params.getFileIds() == null ? List.of() : params.getFileIds());

        boolean thinkingEnabled = !(
                "deepseek-chat".equals(params.getModel())
                        && (params.getModel() == null || !params.getModel().contains("reasoning"))
        );

        body.put("thinking_enabled", thinkingEnabled);
        body.put("search_enabled", params.getSearchEnabled() == null ? Boolean.TRUE : params.getSearchEnabled());
        body.put("preempt", params.getPreempt() == null ? Boolean.FALSE : params.getPreempt());

        HttpRequest request = requestBuilder(
                BASE_URL + targetPath,
                "POST",
                DeepseekWebAuth.OBJECT_MAPPER.writeValueAsString(body),
                headers,
                params.getTimeout()
        ).build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() / 100 != 2) {
            String errorText = readAll(response.body());
            throw new IOException("Chat completion failed: " + response.statusCode() + " " + errorText);
        }

        return response.body();
    }

    private HttpRequest.Builder requestBuilder(
            String url,
            String method,
            String body,
            Map<String, String> headers,
            Duration timeout
    ) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        if (timeout != null) {
            builder.timeout(timeout);
        }

        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        return builder;
    }

    private static String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static String readAll(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        }
    }

    private Number solveDeepSeekHashV1WithPlaywright(Page page, DeepSeekPowChallenge challenge) throws Exception {
        long start = System.currentTimeMillis();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("challenge", challenge.getChallenge());
        payload.put("salt", challenge.getSalt());
        payload.put("difficulty", challenge.getDifficulty());
        payload.put("expire_at", challenge.getExpireAt() == null ? null : challenge.getExpireAt().doubleValue());
//        payload.put("wasmBase64", SHA3_WASM_B64);

        String json = DeepseekWebAuth.OBJECT_MAPPER.writeValueAsString(payload);

        Object result = page.evaluate(
                """
                async (json) => {
                  const arg = JSON.parse(json);
                  const { challenge, salt, difficulty, expire_at, wasmBase64 } = arg;
    
                  const bin = atob(wasmBase64);
                  const wasmBytes = new Uint8Array(bin.length);
                  for (let i = 0; i < bin.length; i++) {
                    wasmBytes[i] = bin.charCodeAt(i);
                  }
    
                  if (
                    wasmBytes.length < 4 ||
                    wasmBytes[0] !== 0x00 ||
                    wasmBytes[1] !== 0x61 ||
                    wasmBytes[2] !== 0x73 ||
                    wasmBytes[3] !== 0x6d
                  ) {
                    throw new Error(
                      "Invalid wasm header: " +
                      Array.from(wasmBytes.slice(0, 8))
                        .map(x => x.toString(16).padStart(2, "0"))
                        .join(" ")
                    );
                  }
    
                  const { instance } = await WebAssembly.instantiate(wasmBytes, { wbg: {} });
                  const exports = instance.exports;
    
                  const memory = exports.memory;
                  const alloc = exports.__wbindgen_export_0;
                  const addToStack = exports.__wbindgen_add_to_stack_pointer;
                  const wasmSolve = exports.wasm_solve;
    
                  const prefix = `${salt}_${expire_at}_`;
    
                  const encodeString = (str) => {
                    const buf = new TextEncoder().encode(str);
                    const ptr = alloc(buf.length, 1);
                    new Uint8Array(memory.buffer).set(buf, ptr);
                    return [ptr, buf.length];
                  };
    
                  const [ptrC, lenC] = encodeString(challenge);
                  const [ptrP, lenP] = encodeString(prefix);
                  const retptr = addToStack(-16);
    
                  wasmSolve(retptr, ptrC, lenC, ptrP, lenP, difficulty);
    
                  const view = new DataView(memory.buffer);
                  const status = view.getInt32(retptr, true);
                  const answer = view.getFloat64(retptr + 8, true);
    
                  addToStack(16);
    
                  if (status === 0) {
                    throw new Error("DeepSeekHashV1 failed to find solution");
                  }
    
                  return answer;
                }
                """,
                json
        );

        if (!(result instanceof Number)) {
            throw new IllegalStateException("DeepSeekHashV1 returned unexpected result: " + result);
        }

        double answer = ((Number) result).doubleValue();
        System.out.println("[DeepSeekWebClient] DeepSeekHashV1 solved in "
                + (System.currentTimeMillis() - start) + "ms, answer: " + answer);
        return answer;
    }

    private static String loadWasmBase64() {
        try (InputStream in = DeepseekWebClient.class.getClassLoader().getResourceAsStream("deepseek_sha3_wasm.b64")) {
            if (in == null) {
                throw new IllegalStateException("deepseek_sha3_wasm.b64 not found in resources");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r", "")
                    .replace("\n", "")
                    .trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load deepseek_sha3_wasm.b64", e);
        }
    }

    public static class DeepSeekWebClientOptions {
        private String cookie;
        private String bearer;
        private String userAgent;

        public DeepSeekWebClientOptions() {
        }

        public DeepSeekWebClientOptions(String cookie, String bearer, String userAgent) {
            this.cookie = cookie;
            this.bearer = bearer;
            this.userAgent = userAgent;
        }

        public String getCookie() {
            return cookie;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }

        public String getBearer() {
            return bearer;
        }

        public void setBearer(String bearer) {
            this.bearer = bearer;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }

    public static class ChatCompletionParams {
        private String sessionId;
        private String message;
        private String model;
        private List<String> fileIds;
        private Boolean searchEnabled;
        private Boolean preempt;
        private Object parentMessageId;
        private Duration timeout;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<String> getFileIds() {
            return fileIds;
        }

        public void setFileIds(List<String> fileIds) {
            this.fileIds = fileIds;
        }

        public Boolean getSearchEnabled() {
            return searchEnabled;
        }

        public void setSearchEnabled(Boolean searchEnabled) {
            this.searchEnabled = searchEnabled;
        }

        public Boolean getPreempt() {
            return preempt;
        }

        public void setPreempt(Boolean preempt) {
            this.preempt = preempt;
        }

        public Object getParentMessageId() {
            return parentMessageId;
        }

        public void setParentMessageId(Object parentMessageId) {
            this.parentMessageId = parentMessageId;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekPowChallenge {
        private String algorithm;
        private String challenge;
        private int difficulty;
        private String salt;
        private String signature;

        @JsonProperty("expire_at")
        private Long expireAt;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getChallenge() {
            return challenge;
        }

        public void setChallenge(String challenge) {
            this.challenge = challenge;
        }

        public int getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(int difficulty) {
            this.difficulty = difficulty;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public Long getExpireAt() {
            return expireAt;
        }

        public void setExpireAt(Long expireAt) {
            this.expireAt = expireAt;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekChatSession {
        @JsonProperty("biz_id")
        private String bizId;

        @JsonProperty("chat_session_id")
        private String chatSessionId;

        private String title;
        private String id;

        public String getBizId() {
            return bizId;
        }

        public void setBizId(String bizId) {
            this.bizId = bizId;
        }

        public String getChatSessionId() {
            return chatSessionId;
        }

        public void setChatSessionId(String chatSessionId) {
            this.chatSessionId = chatSessionId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekPowResponse {
        public PowData data;
        public DeepSeekPowChallenge challenge;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PowData {
        @JsonProperty("biz_data")
        public PowBizData bizData;

        public DeepSeekPowChallenge challenge;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PowBizData {
        public DeepSeekPowChallenge challenge;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekChatSessionResponse {
        public ChatSessionData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatSessionData {
        @JsonProperty("biz_data")
        public DeepSeekChatSession bizData;
    }
}