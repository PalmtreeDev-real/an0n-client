package dev.anon.client.systems.proxies;

import dev.anon.client.utils.network.Http;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProtonAuth {
    private static final String AUTH_API = "https://api.protonmail.ch/auth/v4";
    private static final String VPN_API = "https://api.protonvpn.ch/vpn";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String accessToken;
    private String refreshToken;
    private String uid;
    private boolean authenticated;

    public boolean isAuthenticated() {
        return authenticated && accessToken != null && !accessToken.isEmpty();
    }

    public boolean login(String email, String password) {
        try {
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

            AuthInfoResponse info = getAuthInfo(username);
            if (info == null) return false;

            SrpProofs proofs = computeSrp(username, password, info);
            if (proofs == null) return false;

            AuthResponse auth = submitAuth(info.srpSession, proofs.clientEphemeral, proofs.clientProof);
            if (auth == null) return false;

            this.accessToken = auth.accessToken;
            this.refreshToken = auth.refreshToken;
            this.uid = auth.uid;
            this.authenticated = true;

            return true;
        } catch (Exception e) {
            authenticated = false;
            return false;
        }
    }

    public void logout() {
        accessToken = null;
        refreshToken = null;
        uid = null;
        authenticated = false;
    }

    public List<VpnServer> fetchServers() {
        List<VpnServer> servers = new ArrayList<>();
        try {
            String body = Http.get(VPN_API + "/logicals")
                .header("x-pm-uid", uid)
                .header("Authorization", "Bearer " + accessToken)
                .sendString();

            if (body == null) return servers;

            int logicalsIdx = body.indexOf("\"LogicalServers\"");
            if (logicalsIdx == -1) return servers;

            int arrayStart = body.indexOf('[', logicalsIdx);
            if (arrayStart == -1) return servers;

            int depth = 0;
            int arrayEnd = -1;
            for (int i = arrayStart; i < body.length(); i++) {
                char c = body.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        arrayEnd = i;
                        break;
                    }
                }
            }
            if (arrayEnd == -1) return servers;

            String arrayContent = body.substring(arrayStart + 1, arrayEnd);
            List<String> objects = splitJsonObjects(arrayContent);

            for (String obj : objects) {
                VpnServer server = parseServer(obj);
                if (server != null && server.status == 1) {
                    servers.add(server);
                }
            }

            return servers;
        } catch (Exception e) {
            return servers;
        }
    }

    public static class VpnServer {
        public String id;
        public String name;
        public int status;
        public String country;
        public String city;
        public int tier;
        public int load;
        public String domain;
        public String entryIp;
        public String exitIp;

        public boolean isFree() { return tier == 0; }
        public boolean isPlus() { return tier == 2; }
    }

    private VpnServer parseServer(String json) {
        try {
            VpnServer server = new VpnServer();
            server.id = extractString(json, "ID");
            server.name = extractString(json, "Name");
            server.status = extractInt(json, "Status");
            server.country = extractString(json, "Country");
            server.city = extractString(json, "City");
            server.tier = extractInt(json, "Tier");
            server.load = extractInt(json, "Load");

            int serversIdx = json.indexOf("\"Servers\"");
            if (serversIdx != -1) {
                int bracket = json.indexOf('[', serversIdx);
                if (bracket != -1) {
                    int end = json.indexOf(']', bracket);
                    if (end != -1) {
                        String serverEntry = json.substring(bracket + 1, end);
                        server.domain = extractString(serverEntry, "Domain");
                        server.entryIp = extractString(serverEntry, "EntryIP");
                        server.exitIp = extractString(serverEntry, "ExitIP");
                    }
                }
            }

            return server;
        } catch (Exception e) {
            return null;
        }
    }

    // JSON Utility

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return "";
        int start = json.indexOf('"', colon + 1);
        if (start == -1) return "";
        start++;
        int end = json.indexOf('"', start);
        if (end == -1) return json.substring(start);
        return json.substring(start, end);
    }

    private static int extractInt(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return 0;
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return 0;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<String> splitJsonObjects(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    objects.add(arrayContent.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    // Auth API calls

    private AuthInfoResponse getAuthInfo(String username) {
        try {
            String body = Http.get(AUTH_API + "/info?Username=" + username).sendString();
            if (body == null) return null;

            AuthInfoResponse info = new AuthInfoResponse();
            info.modulus = extractString(body, "Modulus");
            info.serverEphemeral = extractString(body, "ServerEphemeral");
            info.salt = extractString(body, "Salt");
            info.version = extractInt(body, "Version");
            info.srpSession = extractString(body, "SRPSession");

            if (info.modulus.isEmpty() || info.serverEphemeral.isEmpty()) return null;
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    private AuthResponse submitAuth(String srpSession, String clientEphemeral, String clientProof) {
        try {
            String json = "{\"ClientEphemeral\":\"" + clientEphemeral + "\",\"ClientProof\":\"" + clientProof + "\",\"SRPSession\":\"" + srpSession + "\"}";
            String body = Http.post(AUTH_API + "/v4")
                .header("Content-Type", "application/json")
                .bodyString(json)
                .sendString();

            if (body == null) return null;

            AuthResponse response = new AuthResponse();
            response.accessToken = extractString(body, "AccessToken");
            response.refreshToken = extractString(body, "RefreshToken");
            response.uid = extractString(body, "UID");

            if (response.accessToken.isEmpty()) return null;
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    // SRP Implementation

    private static class AuthInfoResponse {
        String modulus;
        String serverEphemeral;
        String salt;
        int version;
        String srpSession;
    }

    private static class AuthResponse {
        String accessToken;
        String refreshToken;
        String uid;
    }

    private static class SrpProofs {
        String clientEphemeral;
        String clientProof;
    }

    private SrpProofs computeSrp(String username, String password, AuthInfoResponse info) {
        try {
            BigInteger N = decodeBigInteger(info.modulus);
            BigInteger g = BigInteger.valueOf(2);
            BigInteger B = decodeBigInteger(info.serverEphemeral);
            byte[] salt = Base64.getDecoder().decode(info.salt);

            byte[] a = new byte[32];
            RANDOM.nextBytes(a);
            BigInteger aInt = new BigInteger(1, a);
            BigInteger A = g.modPow(aInt, N);

            byte[] hn = sha256(toBytes(N));
            byte[] hg = sha256(toBytes(g));
            byte[] hnXorHg = xorBytes(hn, hg);

            byte[] hI = sha256(username.getBytes(StandardCharsets.UTF_8));
            byte[] passwordHash = sha256((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            byte[] xBytes = sha256(concat(salt, passwordHash));
            BigInteger x = new BigInteger(1, xBytes);

            byte[] kBytes = sha256(concat(hn, hg));
            BigInteger k = new BigInteger(1, kBytes);

            byte[] uBytes = sha256(concat(toBytes(A), toBytes(B)));
            BigInteger u = new BigInteger(1, uBytes);

            BigInteger gx = g.modPow(x, N);
            BigInteger kgx = k.multiply(gx).mod(N);
            BigInteger diff = B.subtract(kgx);
            if (diff.compareTo(BigInteger.ZERO) < 0) diff = diff.add(N);

            BigInteger exp = aInt.add(u.multiply(x));
            BigInteger S = diff.modPow(exp, N);

            byte[] K = sha256(toBytes(S));

            byte[] M1 = sha256(concat(hnXorHg, hI, salt, toBytes(A), toBytes(B), K));

            SrpProofs proofs = new SrpProofs();
            proofs.clientEphemeral = Base64.getEncoder().encodeToString(toBytes(A));
            proofs.clientProof = Base64.getEncoder().encodeToString(M1);

            return proofs;
        } catch (Exception e) {
            return null;
        }
    }

    // Cryptographic helpers

    private static BigInteger decodeBigInteger(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new BigInteger(1, bytes);
    }

    private static byte[] toBytes(BigInteger bi) {
        byte[] bytes = bi.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return bytes;
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] arr : arrays) total += arr.length;
        byte[] result = new byte[total];
        int offset = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }

    private static byte[] xorBytes(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) result[i] = (byte) (a[i] ^ b[i]);
        return result;
    }
}
