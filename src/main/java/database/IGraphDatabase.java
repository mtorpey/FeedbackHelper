package database;

import java.util.List;

import org.json.simple.JSONObject;

import model.LinkedPhrases;
import model.Phrase;

/**
 * Graph Database Interface.
 */
public interface IGraphDatabase {
    /**
     * Open the database.
     *
     * @param databasePath The database file to open.
     * @return True if the database was successfully opened, false otherwise.
     */
    boolean openGraphDatabase(String databasePath);

    /**
     * Create a database.
     *
     * @param databasePath The database file to create.
     * @return True if the database was successfully opened, false otherwise.
     */
    boolean createGraphDatabase(String databasePath);

    /**
     * Setup the graph database for an assignment.
     *
     * @param headings A list of headings to be used in the assignment feedback documents.
     */
    void setUpGraphDatabaseForAssignment(List<String> headings);

    /**
     * Add phrases for a given heading to the database
     *
     * @param heading The heading the phrase belongs to.
     */
    void addHeadingObject(String heading);

    /**
     * Remove phrases for a given heading in the database
     *
     * @param heading The heading to remove.
     * @return the previous phrases associated with heading
     */
    JSONObject removeHeadingObject(String heading);

    /**
     * Update phrase.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to update.
     */
    void updatePhrase(String heading, Phrase phrase);

    /**
     * Remove a phrase.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to remove.
     */
    void removePhrase(String heading, String phrase);

    /**
     * Update the phrases for a given heading.
     *
     * @param heading    The heading to update.
     * @param oldPhrases The list of old phrases.
     * @param newPhrases The list of new phrases.
     */
    void updatePhrasesForHeading(String heading, List<String> oldPhrases, List<String> newPhrases);

    /**
     * Add a phrase for a given heading.
     *
     * @param heading The heading the phrase belongs to.
     * @param phrase  The phrase to add.
     */
    void addPhraseForHeading(String heading, Phrase phrase);

    /**
     * Get the phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @return A list of phrases for the given heading.
     */
    List<Phrase> getPhrasesForHeading(String heading);

    /**
     * Add a phrase to the custom node.
     *
     * @param phrase The phrase to add.
     */
    void addPhraseToCustomNode(Phrase phrase);

    /**
     * Get a list of the custom phrases.
     *
     * @return The list of custom phrases.
     */
    List<Phrase> getCustomPhrases();

    /**
     * Manage the links between phrases in the graph databases.
     *
     * @param heading The heading the phrases are for.
     * @param oldList The old set of phrases.
     * @param newList The new set of phrases.
     */
    void managePhraseLinks(String heading, List<String> oldList, List<String> newList);

    /**
     * Get a list of linked phrases.
     *
     * @param heading The heading the linked phrases are for.
     * @return A list of linked phrases for the given heading.
     */
    List<LinkedPhrases> getLinkedPhrases(String heading);
}
