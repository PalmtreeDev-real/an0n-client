/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.accounts;

import com.mojang.util.UndashedUuid;
import dev.anon.client.utils.misc.ISerializable;
import dev.anon.client.utils.misc.NbtException;
import dev.anon.client.utils.network.AnonExecutor;
import dev.anon.client.utils.render.PlayerHeadTexture;
import dev.anon.client.utils.render.PlayerHeadUtils;
import net.minecraft.nbt.CompoundTag;

import static dev.anon.client.AnonClient.mc;

public class AccountCache implements ISerializable<AccountCache> {
    public String username = "";
    public String uuid = "";
    private PlayerHeadTexture headTexture;
    private volatile boolean loadingHead;

    public PlayerHeadTexture getHeadTexture() {
        return headTexture != null ? headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void loadHead() {
        loadHead(null);
    }

    public void loadHead(Runnable callback) {
        if (headTexture != null || uuid == null || uuid.isBlank()) {
            if (callback != null) mc.execute(callback);
            return;
        }

        if (loadingHead) return;

        loadingHead = true;

        AnonExecutor.execute(() -> {
            byte[] head = PlayerHeadUtils.fetchHead(UndashedUuid.fromStringLenient(uuid));

            mc.execute(() -> {
                if (head != null) headTexture = new PlayerHeadTexture(head, true);
                loadingHead = false;
                if (callback != null) callback.run();
            });
        });
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString("username", username);
        tag.putString("uuid", uuid);

        return tag;
    }

    @Override
    public AccountCache fromTag(CompoundTag tag) {
        if (tag.getString("username").isEmpty() || tag.getString("uuid").isEmpty()) throw new NbtException();

        username = tag.getString("username").get();
        uuid = tag.getString("uuid").get();
        loadHead();

        return this;
    }
}
