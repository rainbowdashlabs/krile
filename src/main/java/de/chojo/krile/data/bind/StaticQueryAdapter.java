package de.chojo.krile.data.bind;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import de.chojo.sadu.wrapper.stage.QueryStage;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Static query adapter for SADU as singleton.
 */
public class StaticQueryAdapter {
    private static final Logger log = getLogger(StaticQueryAdapter.class);
    private static QueryFactory factory;

    public static QueryStage<Void> builder() {
        assertInit();
        return factory.builder();
    }

    public static <T> QueryStage<T> builder(Class<T> clazz) {
        assertInit();
        return factory.builder(clazz);
    }

    public static void start(DataSource dataSource, QueryBuilderConfig config){
        if (factory != null) throw new AlreadyInitializedException();
        factory = new QueryFactory(dataSource, config);
        log.info("Static SADU query adapter started");
    }

    public static void start(DataSource dataSource){
        if (factory != null) throw new AlreadyInitializedException();
        factory = new QueryFactory(dataSource);
        log.info("Static SADU query adapter started");
    }

    private static void assertInit() {
        if (factory == null) throw new NotInitializedException();
    }

    private static class NotInitializedException extends RuntimeException {

    }

    private static class AlreadyInitializedException extends RuntimeException {

    }
}
