package database;

import model.LinkedPhrases;
import model.Pair;
import model.Phrase;
import model.Utilities;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilderImplementation;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Graph Database Manager Class.
 */
public class GraphDatabaseManager implements IGraphDatabase {

    // For creating a database
    private DatabaseManagementService managementService;
    private GraphDatabaseService graphDb;

    /**
     * Open the database.
     *
     * @param databasePath The database file to open.
     * @return True if the database was successfully opened, false otherwise.
     */
    @Override
    public boolean openGraphDatabase(String databasePath) {
        DatabaseManagementServiceBuilder builder = new DatabaseManagementServiceBuilder(Paths.get(databasePath));
        builder.setConfig(GraphDatabaseSettings.strict_config_validation, false);
        managementService = builder.build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        return graphDb != null;
    }

    /**
     * Create a database.
     *
     * @param databasePath The database file to create.
     * @return True if the database was successfully opened, false otherwise.
     */
    @Override
    public boolean createGraphDatabase(String databasePath) {
        return openGraphDatabase(databasePath);
    }

    /**
     * Setup the graph database for an assignment.
     *
     * @param headings A list of headings to be used in the assignment feedback documents.
     */
    @Override
    public void setUpGraphDatabaseForAssignment(List<String> headings) {
        // Run the transaction
        try (Transaction tx = graphDb.beginTx()) {
            // Setup nodes for the headings
            for (int i = 0; i < headings.size(); i++) {
                // Current heading
                String currentHeading = headings.get(i);
                String nextHeading;

                // Figure out next heading
                if (i + 1 >= headings.size()) {
                    nextHeading = "null";
                } else {
                    nextHeading = headings.get(i + 1);
                }

                // Create heading nodes with properties
                Node node = tx.createNode();
                node.addLabel(Label.label("Heading"));
                node.setProperty("heading", currentHeading);
                node.setProperty("followed_by", nextHeading);
            }

            // Setup a node for the custom phrases
            createCustomPhrasesNode(tx);

            // Commit
            tx.commit();
        } catch (TransactionFailureException e) {
            e.printStackTrace();
        }
    }


    /* PHRASE MANAGEMENT OPERATIONS */

    /**
     * Add a new node.
     *
     * @param phrase The phrase to add.
     */
    private void addNewPhrase(Phrase phrase) {
        try (Transaction tx = graphDb.beginTx()) {
            Node phraseNode = tx.createNode();
            phraseNode.addLabel(Label.label("Phrase"));
            phraseNode.setProperty("phrase", phrase.getPhraseAsString());
            phraseNode.setProperty("usageCount", phrase.getUsageCount());
            tx.commit();
        }
    }

    /**
     * Update phrase.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to update.
     */
    @Override
    public void updatePhrase(String heading, Phrase phrase) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);
        params.put("phrase", phrase.getPhraseAsString());
        params.put("usageCount", phrase.getUsageCount());

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p:Phrase {phrase: $phrase}) " +
                        "SET p.usageCount = $usageCount";

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(query, params);
            tx.commit();
        }
    }

    /**
     * Remove a phrase.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to remove.
     */
    @Override
    public void removePhrase(String heading, String phrase) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);
        params.put("phrase", phrase);

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p:Phrase {phrase: $phrase})" +
                        "DETACH DELETE p";

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(query, params);
            tx.commit();
        }
    }

    /**
     * Update the phrases for a given heading.
     *
     * @param heading    The heading to update.
     * @param oldPhrases The list of old phrases.
     * @param newPhrases The list of new phrases.
     */
    @Override
    public void updatePhrasesForHeading(String heading, List<String> oldPhrases, List<String> newPhrases) {
        // Get the current phrases for the heading
        List<Phrase> phrasesForHeading = getPhrasesForHeading(heading);
        phrasesForHeading.addAll(getCustomPhrases()); // add custom phrases, so we can search if they are being used

        // Find which phrases have been removed and added
        List<String> removedPhrases = Utilities.getRemovalsFromList(oldPhrases, newPhrases);
        List<String> addedPhrases = Utilities.getAdditionsToList(oldPhrases, newPhrases);

        // Filter out phrases we have identified as those to remove
        List<Phrase> filteredForRemoval = phrasesForHeading
                .stream()
                .filter(phrase -> removedPhrases.contains(phrase.getPhraseAsString()))
                .collect(Collectors.toList());

        // Check the usage count of the phrases to remove
        filteredForRemoval.forEach(phraseToRemove -> {
            if (phraseToRemove.getUsageCount() == 1) {
                // Remove for good
                removePhrase(heading, phraseToRemove.getPhraseAsString());
            } else {
                // Decrement usage count
                phraseToRemove.decrementUsageCount();
                updatePhrase(heading, phraseToRemove);
            }
        });

        // Filter out phrases that are being reused
        List<Phrase> filteredForAddition = phrasesForHeading
                .stream()
                .filter(phrase -> addedPhrases.contains(phrase.getPhraseAsString()))
                .collect(Collectors.toList());
        List<String> updated = new ArrayList<>();

        // Only add to database if phrase is new, otherwise update counter of existing phrase
        filteredForAddition.forEach(phraseToAdd -> {
            phraseToAdd.incrementUsageCount();
            updatePhrase(heading, phraseToAdd);
            updated.add(phraseToAdd.getPhraseAsString());
        });

        // Add any new phrases
        addedPhrases.removeAll(updated);
        addedPhrases.forEach(phraseToAdd -> {
            Phrase phrase = new Phrase(phraseToAdd);
            phrase.incrementUsageCount();
            addPhraseForHeading(heading, phrase);
        });
    }

    /**
     * Add a phrase for a given heading.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to add.
     */
    @Override
    public void addPhraseForHeading(final String heading, Phrase phrase) {
        // Create a phrase node
        addNewPhrase(phrase);

        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);
        params.put("phrase", phrase.getPhraseAsString());
        params.put("usageCount", phrase.getUsageCount());

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p:Phrase {phrase: $phrase, usageCount: $usageCount}) " +
                        "CREATE (h)-[rel:CONTAINS]->(p)";

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(query, params);
            tx.commit();
        }
    }

    /**
     * Get the phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @return A list of phrases for the given heading.
     */
    @Override
    public List<Phrase> getPhrasesForHeading(String heading) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) -[rel:CONTAINS]-> (p:Phrase)" +
                        "RETURN h, p";

        // List of phrases to populate
        List<Phrase> phrasesForHeading = new ArrayList<>();

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            // Get the results
            Result results = tx.execute(query, params);

            // Pull out nodes
            results.stream().forEach(result -> {
                Node node = (Node) result.get("p");
                Phrase phrase = new Phrase(node.getProperty("phrase").toString());
                phrase.setUsageCount(Integer.parseInt(node.getProperty("usageCount").toString()));
                phrasesForHeading.add(phrase);
            });

            // Sort phrases into ascending order
            Collections.sort(phrasesForHeading);
            return phrasesForHeading;
        }
    }


    /* CUSTOM PHRASE METHODS */

    /**
     * Create the custom phrases node.
     */
    private void createCustomPhrasesNode(Transaction tx) {
        Node node = tx.createNode();
        node.addLabel(Label.label("Custom"));
        node.setProperty("nodeName", "customPhrases");
    }

    /**
     * Add a phrase to the custom node.
     *
     * @param phrase The phrase to add.
     */
    @Override
    public void addPhraseToCustomNode(Phrase phrase) {
        // Put custom phrase into the db
        addNewPhrase(phrase);

        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("nodeName", "customPhrases");
        params.put("phrase", phrase.getPhraseAsString());
        params.put("usageCount", phrase.getUsageCount());

        // Create query
        String query =
                "MATCH (n:Custom {nodeName: $nodeName}) " +
                        "MATCH (p:Phrase {phrase: $phrase, usageCount: $usageCount}) " +
                        "CREATE (n)-[rel:CONTAINS]->(p)";

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(query, params);
            tx.commit();
        }
    }

    /**
     * Get a list of the custom phrases.
     *
     * @return The list of custom phrases.
     */
    @Override
    public List<Phrase> getCustomPhrases() {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("nodeName", "customPhrases");

        // Create query
        String query =
                "MATCH (n:Custom {nodeName: $nodeName}) -[rel:CONTAINS]-> (p:Phrase)" +
                        "RETURN n, p";

        // List of phrases to populate
        List<Phrase> customPhrasesList = new ArrayList<>();

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            // Get the results
            Result results = tx.execute(query, params);

            // Pull out objects from the results
            results.stream().forEach(result -> {
                Node node = (Node) result.get("p");
                Phrase phrase = new Phrase(node.getProperty("phrase").toString());
                phrase.setUsageCount(Integer.parseInt(node.getProperty("usageCount").toString()));
                customPhrasesList.add(phrase);
            });

            // Sort phrases into ascending order
            Collections.sort(customPhrasesList);
        }

        return customPhrasesList;
    }


    /* LINKED PHRASE METHODS */

    /**
     * Update the link usage count.
     *
     * @param heading The heading the phrases belong to.
     * @param first   The first phrase in the pair.
     * @param second  The second phrase in the pair.
     * @param update  The update value.
     */
    private void updateLinkUsageCount(String heading, String first, String second, int update) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);
        params.put("phrase1", first);
        params.put("phrase2", second);
        params.put("update", update);

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p1:Phrase {phrase: $phrase1}) -[rel:FOLLOWED_BY]-> (p2:Phrase {phrase: $phrase2}) " +
                        "SET rel.usage = rel.usage + $update";

        // Execute query
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(query, params);
            tx.commit();
        }
    }

    /**
     * Check if a link exists between two phrases.
     *
     * @param heading The heading the phrases are for.
     * @param first   The first phrase in the pair.
     * @param second  The second phrase in the pair.
     * @return True if the link exists, false otherwise.
     */
    private boolean linkExists(String heading, String first, String second) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);
        params.put("phrase1", first);
        params.put("phrase2", second);

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p1:Phrase {phrase: $phrase1}) -[rel:FOLLOWED_BY]-> (p2:Phrase {phrase: $phrase2})" +
                        "RETURN p1.phrase, p2.phrase";

        // Execute the query
        boolean retVal = false;
        try (Transaction tx = graphDb.beginTx()) {
            Result results = tx.execute(query, params);
            retVal = results.hasNext();
        }

        return retVal;
    }

    /**
     * Create a link between two phrases.
     *
     * @param heading The heading the phrases are for.
     * @param first   The first phrase of the pair.
     * @param second  The second phrase of the pair.
     */
    private void createFollowedByLink(String heading, String first, String second) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);
        params.put("phrase1", first);
        params.put("phrase2", second);

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p1:Phrase {phrase: $phrase1}) " +
                        "MATCH (p2:Phrase {phrase: $phrase2}) " +
                        "CREATE (p1)-[rel:FOLLOWED_BY]->(p2)" +
                        "SET rel.usage = 1";

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(query, params);
            tx.commit();
        }
    }

    /**
     * Manage the links between phrases in the graph databases.
     *
     * @param heading The heading the phrases are for.
     * @param oldList The old set of phrases.
     * @param newList The new set of phrases.
     */
    @Override
    public void managePhraseLinks(String heading, List<String> oldList, List<String> newList) {
        // Get pairs for old and new lists
        List<Pair<String>> oldPairs = Utilities.getPairs(oldList);
        List<Pair<String>> newPairs = Utilities.getPairs(newList);

        // Get lists of pairs to remove and add
        List<Pair<String>> pairsToRemove = Utilities.getPairsToRemove(oldPairs, newPairs);
        List<Pair<String>> pairsToAdd = Utilities.getPairsToAdd(oldPairs, newPairs);

        // Remove pairs
        pairsToRemove.forEach(pair -> {
            if (linkExists(heading, pair.getFirst(), pair.getSecond())) {
                updateLinkUsageCount(heading, pair.getFirst(), pair.getSecond(), -1);
            } // else the node has been deleted, so no need to do anything
        });

        // Add pairs
        pairsToAdd.forEach(pair -> {
            if (linkExists(heading, pair.getFirst(), pair.getSecond())) {
                updateLinkUsageCount(heading, pair.getFirst(), pair.getSecond(), 1);
            } else {
                createFollowedByLink(heading, pair.getFirst(), pair.getSecond());
            }
        });
    }

    /**
     * Get a list of linked phrases.
     *
     * @param heading The heading the linked phrases are for.
     * @return A list of linked phrases for the given heading.
     */
    @Override
    public List<LinkedPhrases> getLinkedPhrases(String heading) {
        // Store parameters
        Map<String, Object> params = new HashMap<>();
        params.put("heading", heading);

        // Create query
        String query =
                "MATCH (h:Heading {heading: $heading}) " +
                        "MATCH (p1:Phrase) -[rel:FOLLOWED_BY]-> (p2:Phrase) " +
                        "RETURN h, p1, p2, rel";

        // List of linked phrases to populate
        List<LinkedPhrases> linkedPhrasesList = new ArrayList<LinkedPhrases>();

        // Execute the query
        try (Transaction tx = graphDb.beginTx()) {
            // Get the results
            Result results = tx.execute(query, params);

            // Pull out objects from the results
            results.stream().forEach(result -> {
                // Pull out nodes and relationship
                Node p1 = (Node) result.get("p1");
                Node p2 = (Node) result.get("p2");
                Relationship rel = (Relationship) result.get("rel");

                // Construct phrase 1
                Phrase phrase1 = new Phrase(p1.getProperty("phrase").toString());
                phrase1.setUsageCount(Integer.parseInt(p1.getProperty("usageCount").toString()));

                // Construct phrase 2
                Phrase phrase2 = new Phrase(p2.getProperty("phrase").toString());
                phrase2.setUsageCount(Integer.parseInt(p2.getProperty("usageCount").toString()));

                // Get the usage count
                int usage = Integer.parseInt(rel.getProperty("usage").toString());

                // Create the linked phrases object and store it
                LinkedPhrases linkedPhrases = new LinkedPhrases(phrase1, phrase2, usage);
                linkedPhrasesList.add(linkedPhrases);
            });
        }

        return linkedPhrasesList;
    }

}
