package org.neo4j.examples;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

public class EmbeddedNeo4j {
    private static final String DB_PATH = "/neo4j_db/";
    String greeting;
    // START SNIPPET: vars
    GraphDatabaseService graphDb;
    Node firstNode;
    Node secondNode;
    Relationship relationship;

    // END SNIPPET: vars

    // START SNIPPET: createReltype
    private static enum RelTypes implements RelationshipType {
        KNOWS
    }

    // END SNIPPET: createReltype

    public static void main(final String[] args) {
        EmbeddedNeo4j hello = new EmbeddedNeo4j();
        hello.createDb();
        hello.removeData();
        hello.shutDown();
    }

    void createDb() {
        clearDb();
        // START SNIPPET: startDb
        graphDb = new EmbeddedGraphDatabase(DB_PATH);
        registerShutdownHook(graphDb);
        // END SNIPPET: startDb

        // START SNIPPET: transaction
        Transaction tx = graphDb.beginTx();
        try {
            // Mutating operations go here
            // END SNIPPET: transaction
            // START SNIPPET: addData
            firstNode = graphDb.createNode();
            firstNode.setProperty("message", "Hello, ");
            secondNode = graphDb.createNode();
            secondNode.setProperty("message", "World!");

            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
            relationship.setProperty("message", "brave Neo4j ");
            // END SNIPPET: addData

            // START SNIPPET: readData
            System.out.print(firstNode.getProperty("message"));
            System.out.print(relationship.getProperty("message"));
            System.out.print(secondNode.getProperty("message"));
            // END SNIPPET: readData

            greeting = ((String) firstNode.getProperty("message")) + ((String) relationship.getProperty("message"))
                    + ((String) secondNode.getProperty("message"));

            // START SNIPPET: transaction
            tx.success();
        } finally {
            tx.finish();
        }
        // END SNIPPET: transaction
    }

    private void clearDb() {
        try {
            FileUtils.deleteRecursively(new File(DB_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void removeData() {
        Transaction tx = graphDb.beginTx();
        try {
            // START SNIPPET: removingData
            // let's remove the data
            firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
            firstNode.delete();
            secondNode.delete();
            // END SNIPPET: removingData

            tx.success();
        } finally {
            tx.finish();
        }
    }

    void shutDown() {
        System.out.println();
        System.out.println("Shutting down database ...");
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }

    // START SNIPPET: shutdownHook
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
    // END SNIPPET: shutdownHook

}
