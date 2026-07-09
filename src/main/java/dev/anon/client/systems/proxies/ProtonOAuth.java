package dev.anon.client.systems.proxies;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProtonOAuth {
    private static final String AUTH_URL = "https://account.proton.me/oauth/authorize";
    private static final String TOKEN_URL = "https://oauth.proton.me/token";
    private static final String VPN_API = "https://api.protonvpn.ch";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static class Session {
        public final String accessToken;
        public final String refreshToken;
        public final String uid;
        public final long expiresAt;

        public Session(String accessToken, String refreshToken, String uid, long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.uid = uid;
            this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiresAt;
        }
    }

    public interface AuthCallback {
        void onSuccess(Session session);
        void onError(String error);
    }

    public static void signIn(String clientId, AuthCallback callback) {
        try {
            String verifier = generateCodeVerifier();
            String challenge = generateCodeChallenge(verifier);
            String state = generateState();

            int port = findFreePort();
            CompletableFuture<String> codeFuture = new CompletableFuture<>();

            ServerSocket serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            String redirectUri = "http://127.0.0.1:" + port + "/callback";

            Thread serverThread = new Thread(() -> {
                try (serverSocket) {
                    serverSocket.setSoTimeout((int) TimeUnit.MINUTES.toMillis(5));
                    Socket client = serverSocket.accept();
                    handleCallbackRequest(client, codeFuture, state);
                } catch (SocketTimeoutException e) {
                    codeFuture.completeExceptionally(new Exception("Sign-in timed out. Please try again."));
                } catch (Exception e) {
                    if (!codeFuture.isDone()) {
                        codeFuture.completeExceptionally(e);
                    }
                }
            }, "ProtonOAuth-Server");
            serverThread.setDaemon(true);
            serverThread.start();

            String authorizeUrl = AUTH_URL
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(clientId, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                + "&code_challenge=" + URLEncoder.encode(challenge, "UTF-8")
                + "&code_challenge_method=S256"
                + "&state=" + URLEncoder.encode(state, "UTF-8")
                + "&scope=vpn+full";

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(authorizeUrl));
            } else {
                callback.onError("Unable to open browser. Visit: " + authorizeUrl);
                return;
            }

            String code = codeFuture.get(5, TimeUnit.MINUTES);
            if (code == null) {
                callback.onError("No authorization code received.");
                return;
            }

            exchangeCodeForTokens(clientId, redirectUri, code, verifier, callback);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private static void handleCallbackRequest(Socket client, CompletableFuture<String> codeFuture, String expectedState) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        OutputStream out = client.getOutputStream();

        String requestLine = reader.readLine();
        if (requestLine == null) return;

        String[] parts = requestLine.split(" ");
        String path = parts.length > 1 ? parts[1] : "/";

        URI uri = URI.create("http://localhost" + path);
        String query = uri.getQuery();

        String code = null;
        String state = null;
        String error = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    String val = URLDecoder.decode(kv[1], "UTF-8");
                    switch (kv[0]) {
                        case "code" -> code = val;
                        case "state" -> state = val;
                        case "error" -> error = val;
                    }
                }
            }
        }

        String response;
        if (error != null) {
            codeFuture.completeExceptionally(new Exception("Proton auth error: " + error));
            response = "<html><body><h2>Sign-in Failed</h2><p>" + error + "</p><p>Close this window and try again.</p></body></html>";
        } else if (code != null && expectedState.equals(state)) {
            codeFuture.complete(code);
            response = "<html><body><h2>Sign-in Successful!</h2><p>You can close this window and return to Minecraft.</p><script>window.close()</script></body></html>";
        } else if (code != null && !expectedState.equals(state)) {
            codeFuture.completeExceptionally(new Exception("State mismatch. Possible CSRF attack."));
            response = "<html><body><h2>Security Error</h2><p>State mismatch. Close this window and try again.</p></body></html>";
        } else {
            response = "<html><body><h2>Signing in...</h2><p>Waiting for authorization.</p></body></html>";
        }

        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        String httpResponse = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/html; charset=UTF-8\r\n"
            + "Content-Length: " + body.length + "\r\n"
            + "Connection: close\r\n"
            + "\r\n";

        out.write(httpResponse.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
        client.close();
    }

    private static void exchangeCodeForTokens(String clientId, String redirectUri, String code, String verifier, AuthCallback callback) {
        Thread exchangeThread = new Thread(() -> {
            try {
                String body = "grant_type=authorization_code"
                    + "&code=" + URLEncoder.encode(code, "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                    + "&client_id=" + URLEncoder.encode(clientId, "UTF-8")
                    + "&code_verifier=" + URLEncoder.encode(verifier, "UTF-8");

                URL url = URI.create(TOKEN_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == 200) {
                    String json = readStream(conn.getInputStream());
                    String accessToken = extractJsonString(json, "access_token");
                    String refreshToken = extractJsonString(json, "refresh_token");
                    long expiresIn = extractJsonLong(json, "expires_in");
                    String uid = extractJsonString(json, "uid");

                    if (accessToken != null) {
                        Session session = new Session(accessToken, refreshToken != null ? refreshToken : "", uid != null ? uid : "", expiresIn > 0 ? expiresIn : 3600);
                        callback.onSuccess(session);
                    } else {
                        callback.onError("No access token in response.");
                    }
                } else {
                    String errorBody = readStream(conn.getErrorStream());
                    callback.onError("Token exchange failed (" + status + "): " + errorBody);
                }
            } catch (Exception e) {
                callback.onError("Token exchange error: " + e.getMessage());
            }
        }, "ProtonOAuth-Exchange");
        exchangeThread.setDaemon(true);
        exchangeThread.start();
    }

    public static void refreshToken(String clientId, Session session, AuthCallback callback) {
        if (session.refreshToken.isEmpty()) {
            callback.onError("No refresh token available. Please sign in again.");
            return;
        }

        Thread refreshThread = new Thread(() -> {
            try {
                String body = "grant_type=refresh_token"
                    + "&refresh_token=" + URLEncoder.encode(session.refreshToken, "UTF-8")
                    + "&client_id=" + URLEncoder.encode(clientId, "UTF-8");

                URL url = URI.create(TOKEN_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == 200) {
                    String json = readStream(conn.getInputStream());
                    String accessToken = extractJsonString(json, "access_token");
                    String newRefreshToken = extractJsonString(json, "refresh_token");
                    long expiresIn = extractJsonLong(json, "expires_in");

                    if (accessToken != null) {
                        Session newSession = new Session(
                            accessToken,
                            newRefreshToken != null ? newRefreshToken : session.refreshToken,
                            session.uid,
                            expiresIn > 0 ? expiresIn : 3600
                        );
                        callback.onSuccess(newSession);
                    } else {
                        callback.onError("No access token in refresh response.");
                    }
                } else {
                    callback.onError("Token refresh failed (" + status + ")");
                }
            } catch (Exception e) {
                callback.onError("Token refresh error: " + e.getMessage());
            }
        }, "ProtonOAuth-Refresh");
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    public static String fetchServers(Session session) {
        try {
            URL url = URI.create(VPN_API + "/vpn/logicals").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("x-pm-uid", session.uid);
            conn.setRequestProperty("Authorization", "Bearer " + session.accessToken);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int status = conn.getResponseCode();
            if (status == 200) {
                return readStream(conn.getInputStream());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // PKCE helpers

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallenge(String verifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(verifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            return verifier;
        }
    }

    private static String generateState() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static int findFreePort() {
        try (ServerSocket s = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return s.getLocalPort();
        } catch (Exception e) {
            return 18234;
        }
    }

    // JSON utilities

    private static String extractJsonString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + key.length() + 2);
        if (colon == -1) return null;
        int start = json.indexOf('"', colon + 1);
        if (start == -1) return null;
        start++;
        int end = json.indexOf('"', start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private static long extractJsonLong(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return 0;
        int colon = json.indexOf(':', idx + key.length() + 2);
        if (colon == -1) return 0;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        try {
            return (long) Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }
}
