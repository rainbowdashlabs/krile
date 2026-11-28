/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.chojo.jdautil.configuration.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.core.Bot;
import de.chojo.krile.core.Data;
import de.chojo.krile.core.KrileModule;
import de.chojo.krile.core.Threading;

import java.io.IOException;
import java.sql.SQLException;

public class Krile {
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        Configuration<ConfigFile> configuration = Configuration.create(new ConfigFile());
        var threading = new Threading();
        Data data = Data.create(threading, configuration);
        Injector injector = Guice.createInjector(new KrileModule(configuration, threading, data));
        injector.getInstance(Bot.class).start();
    }
}
