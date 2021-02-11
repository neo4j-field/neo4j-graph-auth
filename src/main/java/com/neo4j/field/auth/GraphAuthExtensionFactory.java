package com.neo4j.field.auth;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionEventListener;
import org.neo4j.kernel.extension.ExtensionFactory;
import org.neo4j.kernel.extension.ExtensionType;
import org.neo4j.kernel.extension.context.ExtensionContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.logging.Log;
import org.neo4j.logging.internal.LogService;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@ServiceProvider
public class GraphAuthExtensionFactory extends ExtensionFactory<GraphAuthExtensionFactory.Dependencies> {

    public GraphAuthExtensionFactory() {
        super(ExtensionType.GLOBAL, "graphAuthExtension");
    }

    public static final String SECURITY_DATABASE_NAME = "security";

    public static DatabaseManagementService dbms = null;
    private static LogService logService = null;
    private static Log log = null;

    public static List<Map<String, Object>> execute(String database, String cypher) throws Exception {
        return execute(database, cypher, new HashMap<>());
    }

    public static List<Map<String, Object>> execute(String database, String cypher, Map<String, Object> params)
            throws Exception  {
        try {
            final GraphDatabaseService db = dbms.database(database);
            try (Transaction tx = db.beginTx()) {
                try (Result result = tx.execute(cypher, params)) {
                    return result.stream().collect(Collectors.toList());
                } finally {
                    tx.commit();
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute query: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Lifecycle newInstance(ExtensionContext context, Dependencies dependencies) {
        return new LifecycleAdapter() {
            private final Map<String, TransactionEventListener> listeners = new HashMap<>();

            @Override
            public void init() throws Exception {
                super.init();
                dbms = dependencies.dbms();
                logService = dependencies.log();
                log = logService.getUserLog(GraphAuthExtensionFactory.class);
            }

            @Override
            public void start() throws Exception {
                super.start();

                // Initialize our security database
                final String systemDb = GraphDatabaseSettings.SYSTEM_DATABASE_NAME;
                final String cypher = StandardCharsets.UTF_8.decode(
                        ByteBuffer.wrap(
                                getClass().getResourceAsStream("/bootstrap.cypher")
                                        .readAllBytes()))
                        .toString();

                if (dbms.listDatabases().stream()
                        .noneMatch(name -> name == SECURITY_DATABASE_NAME)) {
                    execute(systemDb, "CREATE DATABASE $db WAIT",
                            Collections.singletonMap("db", SECURITY_DATABASE_NAME));
                    log.info("created security db '%s'", SECURITY_DATABASE_NAME);
                    execute(SECURITY_DATABASE_NAME, cypher);
                    log.info("bootstrapped security db");
                }

                // Register our Ownership Listener
                OwnershipListener listener = new OwnershipListener(dbms, logService.getUserLog(OwnershipListener.class));
                listeners.put(GraphDatabaseSettings.DEFAULT_DATABASE_NAME, listener);
                dbms.registerTransactionEventListener(GraphDatabaseSettings.DEFAULT_DATABASE_NAME, listener);
            }

            @Override
            public void stop() throws Exception {
                super.stop();
                listeners.keySet().stream()
                        .forEach(key -> dbms.unregisterTransactionEventListener(key, listeners.get(key)));
            }
        };
    }

    public interface Dependencies {
        DatabaseManagementService dbms();
        LogService log();
    }
}
