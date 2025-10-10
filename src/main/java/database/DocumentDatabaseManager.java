package database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;

import model.Assignment;
import model.FeedbackDocument;

/**
 * Document Database Manager Class.
 */
public class DocumentDatabaseManager implements IDocumentDatabase {

    private static final String FILE_SUFFIX = "-documents.no2";

    // Database variable
    private Nitrite databaseConnection;

    /**
     * Create a collection in the database.
     *
     * @param collectionName The name of the collection to create.
     * @return The newly created collection.
     */
    @Override
    public NitriteCollection createCollection(String collectionName) {
        return databaseConnection.getCollection(collectionName);
    }

    /**
     * Check if the database connection is open.
     *
     * @return True if open/ready, false otherwise.
     */
    @Override
    public boolean documentDatabaseIsReady() {
        return !databaseConnection.isClosed();
    }

    /**
     * Open the database.
     *
     * @param databasePath The database file to open, without the filename suffix.
     * @return True if the database was successfully opened, false otherwise.
     */
    @Override
    public boolean openDocumentDatabase(String databasePath) {
        this.databaseConnection = Nitrite.builder()
                .compressed()
                .filePath(databasePath + FILE_SUFFIX)
                .openOrCreate("user", "password");

        return !this.databaseConnection.isClosed();
    }

    /**
     * Create a database.
     *
     * @param databasePath The database file to create, without the filename suffix.
     * @return True if the database was successfully opened, false otherwise.
     */
    @Override
    public boolean createDocumentDatabase(String databasePath) {
        return openDocumentDatabase(databasePath);
    }

    /**
     * Create the feedback documents in the database.
     *
     * @param assignment The assignment the database is for.
     * @return True if the documents were created, false otherwise.
     */
    @Override
    public boolean createFeedbackDocuments(Assignment assignment) {
        // Store the assignment's feedback documents into the database
        NitriteCollection assignmentCollection = databaseConnection.getCollection(assignment.getDatabaseCollectionName());

        // Create each feedback document
        assignment.getFeedbackDocuments().forEach(feedbackDocument -> {
            // Check if this document exists
            Cursor documents = assignmentCollection.find(Filters.eq("feedbackFor", feedbackDocument.getStudentId()));
            if (documents.totalCount() > 0) return;

            // Store the actual FeedbackDocument object.
            Document dbDoc = Document.createDocument("feedbackFor", feedbackDocument.getStudentId());
            dbDoc.put("feedbackDocObject", feedbackDocument);

            // Setup the headings of the document
            for (String heading : assignment.getAssignmentHeadings()) {
                dbDoc.put(heading, "");
            }

            // Store grade
            dbDoc.put("grade", feedbackDocument.getGrade());

            // Commit the document
            assignmentCollection.insert(dbDoc);
        });

        // Commit the created documents
        databaseConnection.commit();

        return databaseConnection.hasCollection(assignment.getDatabaseName());
    }

    /**
     * Load the feedback documents for a given assignment.
     *
     * @param assignment The assignment to load the feedback documents for.
     * @return A list of feedback documents for the given assignment.
     */
    @Override
    public List<FeedbackDocument> loadFeedbackDocumentsForAssignment(Assignment assignment) {
        if (documentDatabaseIsReady()) {
            // Get the collection name and check if it exists
            String collectionName = assignment.getDatabaseCollectionName();
            if (databaseConnection.hasCollection(collectionName)) {
                // Get the collection
                NitriteCollection assignmentCollection = databaseConnection.getCollection(collectionName);
                Cursor results = assignmentCollection.find();

                // Create a list of feedback documents
                List<FeedbackDocument> feedbackDocuments = new ArrayList<FeedbackDocument>();

                // For each document in the collection, load it into the list of feedback documents
                results.forEach(result -> {
                    // Get the feedback document from the database document
                    FeedbackDocument feedbackDocument = (FeedbackDocument) result.get("feedbackDocObject");

                    // Assign data into feedback document
                    assignment.getAssignmentHeadings().forEach(heading -> {
                        feedbackDocument.setDataForHeading(heading, (String) result.get(heading));
                    });

                    // Assign the grade
                    feedbackDocument.setGrade((double) result.get("grade"));

                    // Store the feedback document
                    feedbackDocuments.add(feedbackDocument);
                });
                return feedbackDocuments;
            } else {
                System.out.println("Collection not found :(");
            }
        }
        return null;
    }

    /**
     * Save a feedback document.
     *
     * @param assignment      The assignment the feedback document belongs to.
     * @param studentId       The student ID of the feedback document.
     * @param headingsAndData The data of the feedback document.
     * @param grade           The grade assigned to the feedback document.
     * @return True id the document was saved, false otherwise.
     */
    @Override
    public boolean saveFeedbackDocument(Assignment assignment, String studentId, Map<String, String> headingsAndData, double grade) {
        // Check the collection exists
        if (databaseConnection.hasCollection(assignment.getDatabaseCollectionName())) {
            // Get the collection
            NitriteCollection collection = databaseConnection.getCollection(assignment.getDatabaseCollectionName());

            // Find the document to update
            Cursor documents = collection.find(Filters.eq("feedbackFor", studentId));
            Document document = documents.toList().get(0);

            // Save all the data
            headingsAndData.keySet().forEach(heading -> {
                document.put(heading, headingsAndData.get(heading));
            });

            // Save the grade
            document.put("grade", grade);

            // Update the document and commit changes
            collection.update(document);
            databaseConnection.commit();
            return true;
        }
        return false;
    }

    /**
     * Update a feedback document.
     *
     * @param assignment The assignment the feedback document belongs to.
     * @param studentId  The student ID of the feedback document.
     */
    @Override
    public void updateFeedbackDocument(Assignment assignment, String studentId) {
        // Check if the collection exists
        if (databaseConnection.hasCollection(assignment.getDatabaseCollectionName())) {

            // Get the collection
            NitriteCollection collection = databaseConnection.getCollection(assignment.getDatabaseCollectionName());

            // Get the document
            Cursor documents = collection.find(Filters.eq("feedbackFor", studentId));
            Document document = documents.toList().get(0);

            // Extract the currently stored feedback document
            FeedbackDocument feedbackDocument = (FeedbackDocument) document.get("feedbackDocObject");

            // Update the feedback document's data
            assignment.getAssignmentHeadings().forEach(heading -> {
                feedbackDocument.setDataForHeading(heading, (String) document.get(heading));
            });

            // Update the feedback document's grade
            feedbackDocument.setGrade((double) document.get("grade"));

            // Store the updated feedback document in the assignment
            assignment.setFeedbackDocument(studentId, feedbackDocument);
        }
    }
}
