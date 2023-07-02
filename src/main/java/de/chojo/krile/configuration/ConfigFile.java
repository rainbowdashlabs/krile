package de.chojo.krile.configuration;

import de.chojo.krile.configuration.elements.BaseSettings;
import de.chojo.krile.configuration.elements.Database;

public class ConfigFile {
    private BaseSettings baseSettings = new BaseSettings();
    private Database database = new Database();

    public BaseSettings baseSettings() {
        return baseSettings;
    }

    public Database database() {
        return database;
    }
}
