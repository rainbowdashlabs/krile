package de.chojo.krile.core;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.data.access.Authors;
import de.chojo.krile.data.access.Categories;
import de.chojo.krile.data.access.Guilds;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.bind.StaticQueryAdapter;
import de.chojo.logutil.marker.LogNotify;
import de.chojo.sadu.databases.PostgreSql;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.PostgresqlMapper;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import org.slf4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

import static org.slf4j.LoggerFactory.getLogger;

public class Data {
    private static final Logger log = getLogger(Data.class);
    private final Threading threading;
    private final Configuration<ConfigFile> configuration;
    private HikariDataSource dataSource;
    private Guilds guilds;
    private Authors authors;
    private Categories categories;
    private RepositoryData repositoryData;

    private Data(Threading threading, Configuration<ConfigFile> configuration) {
        this.threading = threading;
        this.configuration = configuration;
    }

    public static Data create(Threading threading, Configuration<ConfigFile> configuration) throws SQLException, IOException, InterruptedException {
        var data = new Data(threading, configuration);
        data.init();
        return data;
    }

    public void init() throws SQLException, IOException, InterruptedException {
        configure();
        initConnection();
        updateDatabase();
        initSaduAdapter();
        initDao();
    }

    private void initSaduAdapter() {
        StaticQueryAdapter.start(dataSource);
    }

    public void initConnection() {
        try {
            dataSource = getConnectionPool();
        } catch (Exception e) {
            log.error("Could not connect to database. Retrying in 10.");
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException ignore) {
            }
            initConnection();
        }
    }

    private void updateDatabase() throws IOException, SQLException {
        var schema = configuration.config().database().schema();
        SqlUpdater.builder(dataSource, PostgreSql.get())
                .setReplacements(new QueryReplacement("krile", schema))
                .setVersionTable(schema + ".krile_version")
                .setSchemas(schema)
                .execute();
    }

    private void configure() {
        log.info("Configuring QueryBuilder");
        var logger = getLogger("DbLogger");
        RowMapperRegistry rowMapperRegistry = new RowMapperRegistry();
        rowMapperRegistry.register(PostgresqlMapper.getDefaultMapper());
        QueryBuilderConfig.setDefault(QueryBuilderConfig.builder()
                .withExceptionHandler(err -> logger.error(LogNotify.NOTIFY_ADMIN, "An error occurred during a database request", err))
                .withExecutor(threading.botWorker())
                .rowMappers(rowMapperRegistry)
                .build());
    }

    private void initDao() {
        log.info("Creating DAOs");
        authors = new Authors();
        categories = new Categories();
        guilds = new Guilds(configuration, authors, categories);
        repositoryData = new RepositoryData(configuration, categories, authors);
    }

    private HikariDataSource getConnectionPool() {
        log.info("Creating connection pool.");
        var data = configuration.config().database();
        return DataSourceCreator.create(PostgreSql.get())
                .configure(config -> config
                        .host(data.host())
                        .port(data.port())
                        .user(data.user())
                        .password(data.password())
                        .database(data.database()))
                .create()
                .withMaximumPoolSize(data.poolSize())
                .withThreadFactory(Threading.createThreadFactory(threading.hikariGroup()))
                .forSchema(data.schema())
                .build();
    }

    public void shutDown() {
        dataSource.close();
    }

    public HikariDataSource dataSource() {
        return dataSource;
    }

    public Guilds guilds() {
        return guilds;
    }

    public Authors authors() {
        return authors;
    }

    public Categories categories() {
        return categories;
    }

    public RepositoryData repositories() {
        return repositoryData;
    }
}
