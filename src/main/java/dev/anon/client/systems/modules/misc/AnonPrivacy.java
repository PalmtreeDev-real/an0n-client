package dev.anon.client.systems.modules.misc;

import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.systems.proxies.Proxies;
import dev.anon.client.systems.proxies.Proxy;
import dev.anon.client.systems.proxies.ProxyType;

public class AnonPrivacy extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> proxyAddress = sgGeneral.add(new StringSetting.Builder()
        .name("proxy-ip")
        .description("The proxy/VPN server IP address.")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> proxyPort = sgGeneral.add(new IntSetting.Builder()
        .name("proxy-port")
        .description("The proxy/VPN server port.")
        .defaultValue(1080)
        .min(1)
        .sliderMax(65535)
        .build()
    );

    private final Setting<ProxyType> proxyType = sgGeneral.add(new EnumSetting.Builder<ProxyType>()
        .name("proxy-type")
        .description("The type of proxy to use.")
        .defaultValue(ProxyType.Socks5)
        .build()
    );

    private final Setting<Boolean> autoConnect = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-connect")
        .description("Automatically connect to the proxy when enabled.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> hideMods = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-mods")
        .description("Hide mod list from the server.")
        .defaultValue(true)
        .build()
    );

    private Proxy activeProxy;
    private boolean connected;

    public AnonPrivacy() {
        super(Categories.Misc, "an0n-privacy", "Privacy protection with proxy/VPN support.");
    }

    @Override
    public void onActivate() {
        connected = false;
        activeProxy = null;

        if (proxyAddress.get().isEmpty()) {
            error("No proxy address set.");
            toggle();
            return;
        }

        if (autoConnect.get()) {
            connectProxy();
        }
    }

    @Override
    public void onDeactivate() {
        if (connected && activeProxy != null) {
            activeProxy.enabled.set(false);
            connected = false;
        }
        activeProxy = null;
    }

    private void connectProxy() {
        Proxies proxies = Proxies.get();

        for (Proxy p : proxies) {
            if (p.address.get().equals(proxyAddress.get()) && p.port.get().equals(proxyPort.get())) {
                activeProxy = p;
                proxies.setEnabled(p, true);
                connected = true;
                info("Connected to existing proxy: " + proxyAddress.get() + ":" + proxyPort.get());
                return;
            }
        }

        Proxy proxy = new Proxy.Builder()
            .name("AN0N-VPN")
            .type(proxyType.get())
            .address(proxyAddress.get())
            .port(proxyPort.get())
            .build();

        if (proxies.add(proxy)) {
            proxies.setEnabled(proxy, true);
            activeProxy = proxy;
            connected = true;
            info("Connected to proxy: " + proxyAddress.get() + ":" + proxyPort.get());
        }
    }

    public boolean shouldHideMods() {
        return isActive() && hideMods.get();
    }
}
