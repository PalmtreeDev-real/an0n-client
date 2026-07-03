package dev.anon.client.features.chat.packet;

public interface AxochatPacket {
    interface C2S extends AxochatPacket {}
    interface S2C extends AxochatPacket {}
}
