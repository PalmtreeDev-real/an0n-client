package dev.anon.client.systems.modules.misc;

import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.systems.proxies.Proxies;
import dev.anon.client.systems.proxies.Proxy;
import dev.anon.client.systems.proxies.ProxyType;
import dev.anon.client.systems.proxies.ProtonOAuth;
import dev.anon.client.utils.network.AnonExecutor;

import java.util.ArrayList;
import java.util.List;

public class ProtonVpn extends Module {
    private final SettingGroup sgAccount = settings.createGroup("Account");
    private final SettingGroup sgProxy = settings.createGroup("Proxy");

    private final Setting<String> clientId = sgAccount.add(new StringSetting.Builder()
        .name("client-id")
        .description("Proton OAuth client ID (register at account.proton.me/oauth).")
        .defaultValue("anon-vpn-client")
        .build()
    );

    private final Setting<ProtonVpnCountry> serverLocation = sgAccount.add(new EnumSetting.Builder<ProtonVpnCountry>()
        .name("server-location")
        .description("Preferred Proton VPN server location.")
        .defaultValue(ProtonVpnCountry.NL)
        .build()
    );

    private final Setting<Boolean> autoConnect = sgAccount.add(new BoolSetting.Builder()
        .name("auto-connect")
        .description("Automatically sign in and connect when enabled.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> useLocalApp = sgProxy.add(new BoolSetting.Builder()
        .name("use-local-app")
        .description("Use the local Proton VPN app SOCKS5 proxy (127.0.0.1:1080). Requires app running.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> proxyHost = sgProxy.add(new StringSetting.Builder()
        .name("proxy-host")
        .description("Custom SOCKS5 proxy host (when use-local-app is off).")
        .defaultValue("127.0.0.1")
        .visible(() -> !useLocalApp.get())
        .build()
    );

    private final Setting<Integer> proxyPort = sgProxy.add(new IntSetting.Builder()
        .name("proxy-port")
        .description("Custom SOCKS5 proxy port.")
        .defaultValue(1080)
        .min(1)
        .sliderMax(65535)
        .visible(() -> !useLocalApp.get())
        .build()
    );

    private ProtonOAuth.Session session;
    private Proxy activeProxy;
    private String currentServerName;

    public enum AuthState { NONE, SIGNING_IN, AUTHENTICATED, CONNECTED, ERROR }

    private AuthState authState = AuthState.NONE;
    private String errorMessage;

    public ProtonVpn() {
        super(Categories.Misc, "proton-vpn", "Proton VPN. Sign in with your Proton Account in your browser then connect.");
    }

    @Override
    public void onActivate() {
        if (authState == AuthState.SIGNING_IN) return;

        if (session != null && session.isExpired()) {
            session = null;
            authState = AuthState.NONE;
        }

        if (session == null) {
            if (autoConnect.get()) {
                authState = AuthState.SIGNING_IN;
                signInAndConnect();
            } else {
                warning("Sign in required. Toggle auto-connect or set up manually.");
                if (isActive()) toggle();
            }
            return;
        }

        authState = AuthState.AUTHENTICATED;
        connectToVpn();
    }

    @Override
    public void onDeactivate() {
        if (activeProxy != null) {
            activeProxy.enabled.set(false);
            activeProxy = null;
        }
        if (authState != AuthState.SIGNING_IN) {
            authState = session != null ? AuthState.AUTHENTICATED : AuthState.NONE;
        }
        currentServerName = null;
    }

    public void signInAndConnect() {
        AnonExecutor.execute(() -> {
            try {
                ProtonOAuth.signIn(clientId.get(), new ProtonOAuth.AuthCallback() {
                    @Override
                    public void onSuccess(ProtonOAuth.Session s) {
                        session = s;
                        authState = AuthState.AUTHENTICATED;
                        info("Signed in to Proton successfully.");

                        if (autoConnect.get()) {
                            connectToVpn();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        errorMessage = error;
                        authState = AuthState.ERROR;
                        error("Sign in failed: " + error);
                        if (isActive()) toggle();
                    }
                });
            } catch (Exception e) {
                errorMessage = e.getMessage();
                authState = AuthState.ERROR;
                error("Sign in error: " + e.getMessage());
                if (isActive()) toggle();
            }
        });
    }

    private void connectToVpn() {
        if (session == null) {
            error("Not signed in.");
            if (isActive()) toggle();
            return;
        }

        AnonExecutor.execute(() -> {
            try {
                info("Fetching Proton VPN servers...");

                String serverData = ProtonOAuth.fetchServers(session);
                if (serverData == null) {
                    if (session.isExpired()) {
                        info("Session expired, refreshing...");
                        ProtonOAuth.refreshToken(clientId.get(), session, new ProtonOAuth.AuthCallback() {
                            @Override
                            public void onSuccess(ProtonOAuth.Session s) {
                                session = s;
                                connectToVpn();
                            }

                            @Override
                            public void onError(String error) {
                                session = null;
                                authState = AuthState.NONE;
                                error("Session expired. Please sign in again.");
                                if (isActive()) toggle();
                            }
                        });
                        return;
                    }
                    error("Failed to fetch servers.");
                    if (isActive()) toggle();
                    return;
                }

                String selectedServer = parseBestServer(serverData, serverLocation.get().name());
                if (selectedServer == null) {
                    error("No servers available for " + serverLocation.get().getDisplayName());
                    if (isActive()) toggle();
                    return;
                }

                currentServerName = selectedServer;
                String addr = useLocalApp.get() ? "127.0.0.1" : proxyHost.get();
                int port = useLocalApp.get() ? 1080 : proxyPort.get();

                Proxies proxies = Proxies.get();
                for (Proxy p : proxies) {
                    if (p.address.get().equals(addr) && p.port.get().equals(port) && p.name.get().startsWith("Proton-VPN")) {
                        activeProxy = p;
                        proxies.setEnabled(p, true);
                        authState = AuthState.CONNECTED;
                        info("Connected to " + currentServerName);
                        return;
                    }
                }

                Proxy proxy = new Proxy.Builder()
                    .name("Proton-VPN-" + serverLocation.get().name())
                    .type(ProxyType.Socks5)
                    .address(addr)
                    .port(port)
                    .build();

                if (proxies.add(proxy)) {
                    proxies.setEnabled(proxy, true);
                    activeProxy = proxy;
                    authState = AuthState.CONNECTED;
                    info("Connected to " + currentServerName);
                }
            } catch (Exception e) {
                error("Connection error: " + e.getMessage());
                if (isActive()) toggle();
            }
        });
    }

    private String parseBestServer(String json, String countryCode) {
        try {
            int logicalsIdx = json.indexOf("\"LogicalServers\"");
            if (logicalsIdx == -1) return null;

            int arrayStart = json.indexOf('[', logicalsIdx);
            if (arrayStart == -1) return null;

            int depth = 0;
            int arrayEnd = -1;
            for (int i = arrayStart; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) { arrayEnd = i; break; }
                }
            }
            if (arrayEnd == -1) return null;

            String arrayContent = json.substring(arrayStart + 1, arrayEnd);
            List<String> objects = splitJsonObjects(arrayContent);

            String bestServer = null;
            int bestLoad = Integer.MAX_VALUE;
            String bestName = null;

            for (String obj : objects) {
                String country = extractJsonString(obj, "Country");
                int status = extractJsonInt(obj, "Status");

                if (country != null && country.equalsIgnoreCase(countryCode) && status == 1) {
                    String name = extractJsonString(obj, "Name");
                    int load = extractJsonInt(obj, "Load");
                    String domain = extractServerDomain(obj);

                    if (name != null && domain != null && load < bestLoad) {
                        bestLoad = load;
                        bestName = name + " (" + country + ")";
                    }
                }
            }

            if (bestName != null) return bestName;

            for (String obj : objects) {
                String country = extractJsonString(obj, "Country");
                int status = extractJsonInt(obj, "Status");

                if (country != null && country.equalsIgnoreCase(countryCode) && status == 1) {
                    String name = extractJsonString(obj, "Name");
                    if (name != null) {
                        String countryStr = extractJsonString(obj, "Country");
                        return name + " (" + countryStr + ")";
                    }
                }
            }

            if (bestServer == null) {
                for (String obj : objects) {
                    int status = extractJsonInt(obj, "Status");
                    if (status == 1) {
                        String name = extractJsonString(obj, "Name");
                        if (name != null) {
                            return name + " ("
                                + extractJsonString(obj, "Country") + ")";
                        }
                    }
                }
            }

            return bestServer;
        } catch (Exception e) {
            return null;
        }
    }

    // JSON utilities

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

    private static String extractJsonString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + key.length() + 2);
        if (colon == -1) return null;
        int start = json.indexOf('"', colon + 1);
        if (start == -1) return null;
        start++;
        int end = json.indexOf('"', start);
        if (end == -1) return json.substring(start);
        return json.substring(start, end);
    }

    private static int extractJsonInt(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return 0;
        int colon = json.indexOf(':', idx + key.length() + 2);
        if (colon == -1) return 0;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String extractServerDomain(String serverJson) {
        int serversIdx = serverJson.indexOf("\"Servers\"");
        if (serversIdx == -1) return null;
        int bracket = serverJson.indexOf('[', serversIdx);
        if (bracket == -1) return null;
        int end = serverJson.indexOf(']', bracket);
        if (end == -1) return null;
        String entry = serverJson.substring(bracket + 1, end);
        String domain = extractJsonString(entry, "Domain");
        if (domain != null && !domain.isEmpty()) return domain;
        return extractJsonString(entry, "EntryIP");
    }

    @Override
    public String getInfoString() {
        if (authState == AuthState.CONNECTED && currentServerName != null) {
            return currentServerName;
        }
        if (authState == AuthState.SIGNING_IN) return "Signing in...";
        if (authState == AuthState.AUTHENTICATED) return "Authenticated";
        if (authState == AuthState.ERROR) return "Error";
        return null;
    }

    public AuthState getAuthState() { return authState; }
    public String getCurrentServer() { return currentServerName; }
    public String getErrorMessage() { return errorMessage; }

    public enum ProtonVpnCountry {
        NL("Netherlands"), US("United States"), JP("Japan"),
        CH("Switzerland"), SE("Sweden"), NO("Norway"),
        IS("Iceland"), DE("Germany"), FR("France"),
        GB("United Kingdom"), CA("Canada"), AU("Australia"),
        IT("Italy"), ES("Spain"), BE("Belgium"),
        DK("Denmark"), IE("Ireland"), PL("Poland"),
        AT("Austria"), CZ("Czech Republic"), FI("Finland"),
        PT("Portugal"), RO("Romania"), SG("Singapore"),
        HK("Hong Kong"), IN("India"), BR("Brazil"),
        ZA("South Africa"), TR("Turkey"), AE("United Arab Emirates"),
        RU("Russia");

        private final String displayName;
        ProtonVpnCountry(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
