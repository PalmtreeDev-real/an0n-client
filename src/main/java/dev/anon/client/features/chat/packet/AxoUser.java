package dev.anon.client.features.chat.packet;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class AxoUser {
    @SerializedName("name")
    private String name;

    @SerializedName("uuid")
    private UUID uuid;

    public AxoUser() {}

    public AxoUser(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() { return name; }
    public UUID getUuid() { return uuid; }
}
