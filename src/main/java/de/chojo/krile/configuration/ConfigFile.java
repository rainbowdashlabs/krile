package de.chojo.krile.configuration;

import de.chojo.krile.configuration.elements.BaseSettings;
import de.chojo.krile.configuration.elements.Database;
import de.chojo.krile.configuration.elements.Repositories;

public class ConfigFile {
    private BaseSettings baseSettings = new BaseSettings();
    private Database database = new Database();
private Repositories repositories = new Repositories();
    public BaseSettings baseSettings() {
        return baseSettings;
    }

    public Database database() {
        return database;
    }

    public Repositories repositories() {
        return repositories;
    }
}
