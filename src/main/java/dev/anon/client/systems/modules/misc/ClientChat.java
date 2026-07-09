package dev.anon.client.systems.modules.misc;

import dev.anon.client.events.chat.ClientChatStateChange;
import dev.anon.client.features.chat.ChatManager;
import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.render.color.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ClientChat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoConnect = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-connect")
        .description("Automatically connect to AN0N Chat when the module is enabled.")
        .defaultValue(true)
        .build()
    );

    public ClientChat() {
        super(Categories.Misc, "client-chat", "Chat with other AN0N users across servers.");
        title = "AN0N SocialChat";
        setGradient(new Color(0, 0, 0), new Color(128, 128, 128));
    }

    @Override
    public void onActivate() {
        ChatManager.get().setDisplayMessages(true);
        if (autoConnect.get()) {
            ChatManager.get().connect();
        }
    }

    @Override
    public void onDeactivate() {
        ChatManager.get().setDisplayMessages(false);
        ChatManager.get().disconnect();
    }

    @Override
    public String getInfoString() {
        ChatManager chat = ChatManager.get();
        if (chat.isLoggedIn()) return "Logged in";
        if (chat.isConnected()) return "Connected";
        if (chat.getClient().isConnecting()) return "Connecting...";
        return null;
    }
}
