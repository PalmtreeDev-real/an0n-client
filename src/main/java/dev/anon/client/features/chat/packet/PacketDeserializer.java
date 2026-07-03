package dev.anon.client.features.chat.packet;

import com.google.gson.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PacketDeserializer implements JsonDeserializer<AxochatPacket.S2C> {
    private final Map<String, Class<? extends AxochatPacket.S2C>> registry = new LinkedHashMap<>();

    public void registerPacket(String name, Class<? extends AxochatPacket.S2C> clazz) {
        registry.put(name, clazz);
    }

    @Override
    public AxochatPacket.S2C deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String packetName = obj.get("m").getAsString();

        Class<? extends AxochatPacket.S2C> clazz = registry.get(packetName);
        if (clazz == null) return null;

        if (obj.has("c")) {
            return context.deserialize(obj.get("c"), clazz);
        }

        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new JsonParseException("Cannot instantiate packet: " + packetName, e);
        }
    }
}
