/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile;

import de.chojo.jdautil.configuration.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.core.Bot;
import de.chojo.krile.core.Data;
import de.chojo.krile.core.Threading;

import java.io.IOException;
import java.sql.SQLException;

public class Krile {
    private static Krile instance;

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        instance = new Krile();
        instance.init();
    }

    private void init() throws SQLException, IOException, InterruptedException {
        Configuration<ConfigFile> configuration = Configuration.create(new ConfigFile());
        var threading = new Threading();
        Data data = Data.create(threading, configuration);
        Bot bot = Bot.create(data, threading, configuration);
    }
}
