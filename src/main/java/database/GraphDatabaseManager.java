package database;

import model.LinkedPhrases;
import model.Pair;
import model.Phrase;
import model.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Database manager for phrases, including their headings and usage counts.
 *
 * This used to use a graph database system, neo4j.  This was changed for
 * simplicity, and now uses a simple json store that outputs to a single file.
 *
 * The structure of the file is as follows.  The overall database is a single
 * JSON object, with two keys: used_phrases and custom_phrases.  used_phrases is
 * an object with a key for each heading.  The value for each heading, and the
 * value for custom_phrases, is an object with the used phrases as keys and
 * their usage counts as their values.
 */
public class GraphDatabaseManager implements IGraphDatabase {

    private static final String FILE_SUFFIX = "-phrases.json";
    private static final String USED_PHRASES_KEY = "used_phrases";
    private static final String CUSTOM_PHRASES_KEY = "custom_phrases";

    // Database is a JSON object, which is written to disk regularly
    private File databaseFile;
    private JSONObject database;

    /**
     * Try to open an existing database.
     *
     * @param databasePath The database file to open, without the filename suffix.
     * @return True if the database was successfully opened, false otherwise.
     */
    @Override
    public boolean openGraphDatabase(String databasePath) {
        databaseFile = new File(databasePath + FILE_SUFFIX);
        System.out.print("Attempting to load phrases from " + databaseFile + " . . . ");
        if (databaseFile.exists()) {
            // Load the database into memory
            loadFromFile();
            System.out.println("success!");
            return true;
        }
        // Does not exist yet
        System.out.println("failure (does not exist).");
        return false;
    }

    /**
     * Create a database.  See also setUpGraphDatabaseForAssignment.
     *
     * @param databasePath The database file to create, without the filename suffix.
     * @return True if the database was successfully opened, false otherwise.
     */
    @Override
    public boolean createGraphDatabase(String databasePath) {
        // File should not exist yet
        if (openGraphDatabase(databasePath)) {  // initialises databaseFile
            return false;
        }

        // Start with an empty JSON object
        database = new JSONObject();
        dumpToFile();
        return true;
    }

    /**
     * Setup the graph database for an assignment, after it has been created.
     *
     * @param headings A list of headings to be used in the assignment feedback documents.
     */
    @Override
    public void setUpGraphDatabaseForAssignment(List<String> headings) {
        JSONObject usedPhrases = new JSONObject();
        for (String heading: headings) {
            usedPhrases.put(heading, new JSONObject());
        }

        database.put(USED_PHRASES_KEY, usedPhrases);
        database.put(CUSTOM_PHRASES_KEY, new JSONObject());

        dumpToFile();
    }

    /**
     * Update the count for the given phrase, adding it if not already present.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to update.
     */
    @Override
    public void updatePhrase(String heading, Phrase phrase) {
        updatePhrase(getHeadingObject(heading), phrase);
    }

    /* Update the given phrase in the given JSON object. */
    private void updatePhrase(JSONObject object, Phrase phrase) {
        object.put(phrase.getPhraseAsString(), phrase.getUsageCount());
        dumpToFile();
    }

    /**
     * Add a phrase for a given heading.  Identical to updatePhrase in this
     * implementation.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to add.
     */
    @Override
    public void addPhraseForHeading(final String heading, Phrase phrase) {
        updatePhrase(heading, phrase);  // adds a new entry if it does not exist already
    }

    /**
     * Remove a phrase from the given heading.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to remove.
     */
    @Override
    public void removePhrase(String heading, String phrase) {
        removePhrase(getHeadingObject(heading), phrase);
    }

    /* Remove a phrase from the given JSON object. */
    private void removePhrase(JSONObject object, String phrase) {
        object.remove(phrase);
        dumpToFile();
    }

    /* Return the JSON object corresponding to a particular heading. */
    private JSONObject getHeadingObject(String heading) {
        JSONObject usedPhrases = (JSONObject) database.get(USED_PHRASES_KEY);
        return (JSONObject) usedPhrases.get(heading);
    }

    /**
     * Add a phrase to the custom node.
     *
     * @param phrase The phrase to add.
     */
    @Override
    public void addPhraseToCustomNode(Phrase phrase) {
        getCustomObject().put(phrase.getPhraseAsString(), phrase.getUsageCount());
        dumpToFile();
    }

    /* Return the JSON object corresponding to custom phrases. */
    private JSONObject getCustomObject() {
        return (JSONObject) database.get(CUSTOM_PHRASES_KEY);
    }

    /**
     * Get the phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @return A list of phrases for the given heading.
     */
    @Override
    public List<Phrase> getPhrasesForHeading(String heading) {
        return getPhrasesFromObject(getHeadingObject(heading));
    }

    /**
     * Get a list of the custom phrases.
     *
     * @return The list of custom phrases.
     */
    @Override
    public List<Phrase> getCustomPhrases() {
        return getPhrasesFromObject(getCustomObject());
    }

    /*
     * Return the phrases stored in the given JSON object.
     *
     * @param object Either some heading or the custom phrases.
     */
    public List<Phrase> getPhrasesFromObject(JSONObject object) {
        List<Phrase> phrases = new ArrayList<>();

        // Read from the JSON object
        for (String phraseString: (Set<String>) object.keySet()) {
            Phrase phrase = new Phrase(phraseString);
            int count = ((Number) object.get(phraseString)).intValue();  // hopefully less than 4 billion
            phrase.setUsageCount(count);
            phrases.add(phrase);
        }

        // Sort phrases into ascending order
        Collections.sort(phrases);
        return phrases;
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
        // Find which phrases have been removed and added.
        List<String> removedPhrases = Utilities.getRemovalsFromList(oldPhrases, newPhrases);
        List<String> addedPhrases = Utilities.getAdditionsToList(oldPhrases, newPhrases);
        // Be careful not to modify these, because we're about to use them twice!

        // Update phrase counts for this heading and for custom phrases
        updatePhrasesForObject(getHeadingObject(heading), removedPhrases, addedPhrases, true, true);
        updatePhrasesForObject(getCustomObject(), removedPhrases, addedPhrases, false, false);
    }

    /* Update phrases in the given object, given these removed and added phrases. */
    private void updatePhrasesForObject(JSONObject object, List<String> removedPhrases, List<String> addedPhrases, boolean addIfNew, boolean removeIfZero) {
        // Filter out phrases we have identified as those to remove
        List<Phrase> filteredForRemoval = getPhrasesFromObject(object)
                .stream()
                .filter(phrase -> removedPhrases.contains(phrase.getPhraseAsString()))
                .collect(Collectors.toList());

        // Check the usage count of the phrases to remove
        filteredForRemoval.forEach(phraseToRemove -> {
            if (phraseToRemove.getUsageCount() == 1 && removeIfZero) {
                // Remove for good
                removePhrase(object, phraseToRemove.getPhraseAsString());
            } else {
                // Decrement usage count
                phraseToRemove.decrementUsageCount();
                updatePhrase(object, phraseToRemove);
            }
        });

        // Filter out phrases that are being reused
        List<Phrase> filteredForAddition = getPhrasesFromObject(object)
                .stream()
                .filter(phrase -> addedPhrases.contains(phrase.getPhraseAsString()))
                .collect(Collectors.toList());
        List<String> updated = new ArrayList<>();

        // Update counter of existing phrase
        filteredForAddition.forEach(phraseToAdd -> {
            phraseToAdd.incrementUsageCount();
            updatePhrase(object, phraseToAdd);
            updated.add(phraseToAdd.getPhraseAsString());
        });

        // Add any new phrases
        if (addIfNew) {
            addedPhrases.forEach(phraseToAdd -> {
                if (!updated.contains(phraseToAdd)) {
                    Phrase phrase = new Phrase(phraseToAdd);
                    phrase.incrementUsageCount();
                    updatePhrase(object, phrase);
                }
            });
        }
    }

    /* Populate the JSON model from the file. */
    private void loadFromFile() {
        try (FileReader reader = new FileReader(databaseFile)) {
            database = (JSONObject) new JSONParser().parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /* Write out the JSON model to the file. */
    private void dumpToFile() {
        try (FileWriter writer = new FileWriter(databaseFile)) {
            writer.write(database.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * LINKED PHRASE METHODS
     *
     * This functionality is being removed, so most methods here do nothing.
     */

    /**
     * Update the link usage count.  Does nothing in this implementation.
     *
     * @param heading The heading the phrases belong to.
     * @param first   The first phrase in the pair.
     * @param second  The second phrase in the pair.
     * @param update  The update value.
     */
    private void updateLinkUsageCount(String heading, String first, String second, int update) {
        // Do nothing
    }

    /**
     * Check if a link exists between two phrases.  Does nothing in this implementation.
     *
     * @param heading The heading the phrases are for.
     * @param first   The first phrase in the pair.
     * @param second  The second phrase in the pair.
     * @return True if the link exists, false otherwise.
     */
    private boolean linkExists(String heading, String first, String second) {
        return false;
    }

    /**
     * Create a link between two phrases.  Does nothing in this implementation.
     *
     * @param heading The heading the phrases are for.
     * @param first   The first phrase of the pair.
     * @param second  The second phrase of the pair.
     */
    private void createFollowedByLink(String heading, String first, String second) {
        // Do nothing
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
        // Empty list
        List<LinkedPhrases> linkedPhrasesList = new ArrayList<LinkedPhrases>();
        return linkedPhrasesList;
    }

}
