package database;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.dizitart.no2.NitriteCollection;

import model.Assignment;
import model.FeedbackDocument;
import model.StudentId;

/**
 * Document Database Interface.
 */
public interface IDocumentDatabase {
    /**
     * Create a collection in the database.
     *
     * @param collectionName The name of the collection to create.
     * @return The newly created collection.
     */
    NitriteCollection createCollection(String collectionName);

    /**
     * Check if the database connection is open.
     *
     * @return True if open/ready, false otherwise.
     */
    boolean documentDatabaseIsReady();

    /**
     * Open the database.
     *
     * @param assignmentDirectory The directory containing the assignment.
     * @param databaseName The assignment name to be used for this database's filename.
     * @return True if the database was successfully opened, false otherwise.
     */
    boolean openDocumentDatabase(Path assignmentDirectory, String databaseName);

    /**
     * Create a database.
     *
     * @param assignmentDirectory The directory containing the assignment.
     * @param databaseName The assignment name to be used for this database's filename.
     * @return True if the database was successfully opened, false otherwise.
     */
    boolean createDocumentDatabase(Path assignmentDirectory, String databaseName);

    /**
     * Create the feedback documents in the database.
     *
     * @param assignment The assignment the database is for.
     * @return True if the documents were created, false otherwise.
     */
    boolean createFeedbackDocuments(Assignment assignment);

    /**
     * Load the feedback documents for a given assignment.
     *
     * @param assignment The assignment to load the feedback documents for.
     * @return A list of feedback documents for the given assignment.
     */
    List<FeedbackDocument> loadFeedbackDocumentsForAssignment(Assignment assignment);

    /**
     * Save a feedback document.
     *
     * @param assignment      The assignment the feedback document belongs to.
     * @param studentId       The student ID of the feedback document.
     * @param headingsAndData The data of the feedback document.
     * @param grade           The grade assigned to the feedback document.
     * @return True id the document was saved, false otherwise.
     */
    boolean saveFeedbackDocument(
        Assignment assignment,
        StudentId studentId,
        Map<String, String> headingsAndData,
        double grade
    );

    /**
     * Update a feedback document.
     *
     * @param assignment The assignment the feedback document belongs to.
     * @param studentId  The student ID of the feedback document.
     */
    void updateFeedbackDocument(Assignment assignment, StudentId studentId);
}
