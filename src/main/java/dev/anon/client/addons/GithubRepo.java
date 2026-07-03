/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.addons;

import dev.anon.client.utils.network.Http;

import javax.annotation.Nullable;

public record GithubRepo(String owner, String name, String branch, @Nullable String accessToken) {
    public GithubRepo(String owner, String name, @Nullable String accessToken) {
        this(owner, name, "master", accessToken);
    }

    public GithubRepo(String owner, String name) {
        this(owner, name, "master", null);
    }

    public String getOwnerName() {
        return owner + "/" + name;
    }

    public void authenticate(Http.Request request) {
        if (this.accessToken != null && !this.accessToken.isBlank()) {
            request.bearer(this.accessToken);
        } else {
            String personalAuthToken = System.getenv("anon.github.authorization");
            if (personalAuthToken != null && !personalAuthToken.isBlank()) {
                request.bearer(personalAuthToken);
            }
        }
    }
}
