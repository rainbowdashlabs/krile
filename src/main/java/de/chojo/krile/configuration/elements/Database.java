/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.configuration.elements;


@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Database {
    private String host = "localhost";
    private String port = "5432";
    private String database = "krile";
    private String schema = "krile";
    private String user = "krile";
    private String password = "password";
    private int poolSize = 5;

    public String host() {
        return host;
    }

    public String port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String schema() {
        return schema;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public int poolSize() {
        return poolSize;
    }
}
