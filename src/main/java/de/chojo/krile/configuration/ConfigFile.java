/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.configuration;

import de.chojo.krile.configuration.elements.BaseSettings;
import de.chojo.krile.configuration.elements.Database;
import de.chojo.krile.configuration.elements.Repositories;

public class ConfigFile {
    private final BaseSettings baseSettings = new BaseSettings();
    private final Database database = new Database();
    private final Repositories repositories = new Repositories();
    private final Links links = new Links();

    public BaseSettings baseSettings() {
        return baseSettings;
    }

    public Database database() {
        return database;
    }

    public Repositories repositories() {
        return repositories;
    }

    public Links links() {
        return links;
    }
}
