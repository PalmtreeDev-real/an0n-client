/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.accounts;

import dev.anon.client.systems.System;
import dev.anon.client.systems.Systems;
import dev.anon.client.systems.accounts.types.CrackedAccount;
import dev.anon.client.systems.accounts.types.MicrosoftAccount;
import dev.anon.client.systems.accounts.types.SessionAccount;
import dev.anon.client.systems.accounts.types.TheAlteningAccount;
import dev.anon.client.utils.misc.NbtException;
import dev.anon.client.utils.misc.NbtUtils;
import dev.anon.client.utils.network.AnonExecutor;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Accounts extends System<Accounts> implements Iterable<Account<?>> {
    private List<Account<?>> accounts = new ArrayList<>();

    private static final List<String> DEFAULT_CRACKED_NAMES = Arrays.asList(
        "Steve", "Alex", "Noob", "Pro123", "Player1", "Guest", "User",
        "Creeper", "Enderman", "Skeleton", "Zombie", "Piglin",
        "xXPlayerXx", "Gamer99", "Miner456", "CraftMaster", "Digger789",
        "AN0N_User1", "AN0N_User2", "AN0N_User3", "AN0N_User4", "AN0N_User5",
        "Shadow", "NightOwl", "DarkKnight", "Phoenix", "DragonSlayer",
        "Warrior", "Wizard", "Archer", "Paladin", "Berserker",
        "Thunder", "Lightning", "Storm", "Blaze", "Frost"
    );

    public Accounts() {
        super("accounts");
    }

    @Override
    public void init() {
        if (isFirstInit) {
            for (String name : DEFAULT_CRACKED_NAMES) {
                accounts.add(new CrackedAccount(name));
            }
            save();
        }
    }

    public static Accounts get() {
        return Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        accounts.add(account);
        save();
    }

    public boolean exists(Account<?> account) {
        return accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (accounts.remove(account)) {
            save();
        }
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public @NotNull Iterator<Account<?>> iterator() {
        return accounts.iterator();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.put("accounts", NbtUtils.listToTag(accounts));

        return tag;
    }

    @Override
    public Accounts fromTag(CompoundTag tag) {
        AnonExecutor.execute(() -> accounts = NbtUtils.listFromTag(tag.getListOrEmpty("accounts"), tag1 -> {
            CompoundTag t = (CompoundTag) tag1;
            if (!t.contains("type")) return null;

            AccountType type = AccountType.valueOf(t.getStringOr("type", ""));

            try {
                return switch (type) {
                    case Cracked -> new CrackedAccount(null).fromTag(t);
                    case Microsoft -> new MicrosoftAccount(null).fromTag(t);
                    case TheAltening -> new TheAlteningAccount(null).fromTag(t);
                    case Session -> new SessionAccount(null).fromTag(t);
                };
            } catch (NbtException _) {
                return null;
            }
        }));

        return this;
    }
}
