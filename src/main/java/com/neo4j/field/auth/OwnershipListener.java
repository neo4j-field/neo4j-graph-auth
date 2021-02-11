package com.neo4j.field.auth;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListener;
import org.neo4j.logging.Log;

/**
 * Set ownership labels on Nodes as users create them.
 */
public class OwnershipListener implements TransactionEventListener<Integer> {
    private final DatabaseManagementService dbms;
    private final Log log;

    public OwnershipListener(DatabaseManagementService dbms, Log log) {
        this.dbms = dbms;
        this.log = log;
    }

    @Override
    public Integer beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) throws Exception {
        int cnt = 0;
        final String userLabel = String.format("_owner_%s", data.username());
        log.info("processing commit from user: " + data.username());

        // Tag new nodes
        for (Node node : data.createdNodes()) {
            node.addLabel(Label.label(userLabel));
            cnt++;
        };

        // Make sure users don't drop the ownership labels on "accident" ;-)
        for (LabelEntry entry : data.removedLabels()) {
            entry.node().addLabel(entry.label());
        }

        return cnt;
    }

    @Override
    public void afterCommit(TransactionData data, Integer state, GraphDatabaseService databaseService) {
        log.info("labeled " + state + " nodes");
    }

    @Override
    public void afterRollback(TransactionData data, Integer state, GraphDatabaseService databaseService) {
        // nop
    }
}
