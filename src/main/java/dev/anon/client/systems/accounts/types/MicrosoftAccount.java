/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.accounts.types;

import com.mojang.util.UndashedUuid;
import dev.anon.client.systems.accounts.Account;
import dev.anon.client.systems.accounts.AccountType;
import dev.anon.client.systems.accounts.MicrosoftLogin;
import net.minecraft.client.User;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MicrosoftAccount extends Account<MicrosoftAccount> {
    private @Nullable String token;

    public MicrosoftAccount(String refreshToken) {
        super(AccountType.Microsoft, refreshToken);
    }

    @Override
    public boolean fetchInfo() {
        token = auth();
        return token != null;
    }

    @Override
    public boolean login() {
        if (token == null) return false;

        super.login();

        setSession(new User(cache.username, UndashedUuid.fromStringLenient(cache.uuid), token, Optional.empty(), Optional.empty()));
        return true;
    }

    private @Nullable String auth() {
        MicrosoftLogin.LoginData data = MicrosoftLogin.login(name);
        if (!data.isGood()) return null;

        name = data.newRefreshToken;
        cache.username = data.username;
        cache.uuid = data.uuid;

        return data.mcToken;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MicrosoftAccount account)) return false;
        return account.name.equals(this.name);
    }
}
