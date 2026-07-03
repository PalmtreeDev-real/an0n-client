package dev.anon.client.features.chat.packet;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class PacketSerializer implements JsonSerializer<AxochatPacket.C2S> {
    private final Map<Class<?>, String> registry = new LinkedHashMap<>();

    public void registerPacket(String name, Class<?> clazz) {
        registry.put(clazz, name);
    }

    @Override
    public JsonElement serialize(AxochatPacket.C2S src, Type typeOfSrc, JsonSerializationContext context) {
        String packetName = registry.getOrDefault(src.getClass(), "UNKNOWN");
        JsonObject obj = new JsonObject();
        obj.addProperty("m", packetName);

        boolean hasFields;
        try {
            hasFields = src.getClass().getDeclaredFields().length > 0
                || src.getClass().getFields().length > 0;
        } catch (Exception e) {
            hasFields = false;
        }

        if (hasFields) {
            obj.add("c", context.serialize(src));
        }

        return obj;
    }
}
