/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.configuration.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class BaseSettings {
    private String token = "";
    private List<Long> botOwner = new ArrayList<>();
    private long botGuild = 0L;
    private boolean testMode = false;

    public String token() {
        return token;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }

    public long botGuild() {
        return botGuild;
    }

    /**
     * Whether commands are registered in the bot guild only, so changes show up immediately while developing.
     *
     * @return true in test mode
     */
    public boolean testMode() {
        return testMode;
    }
}
