package controller;

import database.DocumentDatabaseManager;
import model.AppModel;
import model.Assignment;
import model.FeedbackDocument;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Map;

public class AppController {

    AppModel appModel;
    DocumentDatabaseManager documentDatabase;

    public AppController(AppModel appModel) {
        this.appModel = appModel;
        this.documentDatabase = new DocumentDatabaseManager();
    }

    public Assignment createAssignment(String assignmentTitle, String headings, File studentManifestFile) {
        // Create assignment in the model
        Assignment assignment = appModel.createAssignment(assignmentTitle, headings, studentManifestFile);

        // Create the assignment database
        documentDatabase.createDocumentDatabase(assignment.getDatabaseName());

        // Create the feedback files for the assignment in the database
        documentDatabase.createFeedbackDocuments(assignment);

        System.out.println("Assignment: " + assignment.getDatabaseName());
        return assignment;
    }

    public Assignment loadAssignment(String assignmentFilePath) {
        Assignment assignment = appModel.loadAssignment(assignmentFilePath);
        loadFeedbackDocuments(assignment);
        return assignment;
    }

    public void saveAssignment(Assignment assignment, String fileName) {
        assignment.saveAssignmentDetails(fileName);
    }

    private void loadFeedbackDocuments(Assignment assignment) {
        documentDatabase.openDocumentDatabase(assignment.getDatabaseName());
        List<FeedbackDocument> feedbackDocuments = documentDatabase.loadFeedbackDocumentsForAssignment(assignment);
        assignment.setFeedbackDocuments(feedbackDocuments);

        System.out.println("1st doc is: " + assignment.getFeedbackDocuments().get(0).getHeadingData("1"));
        System.out.println("Got back " + assignment.getFeedbackDocuments().size() + " documents from db");
    }

    public void getFeedbackDocument(Assignment assignment, String studentId) {

    }

    public void saveFeedbackDocument(String studentId) {
        appModel.notifySubscribers("saveDoc", studentId);
    }

    public void saveFeedbackDocument(Assignment assignment, String studentId, Map<String, String> headingsAndData) {
        documentDatabase.saveFeedbackDocument(assignment, studentId, headingsAndData);
    }

    public void setCurrentDocInView(String studentId) {
        appModel.setCurrentScreenView(studentId);
    }

    public String getLastDocInView() {
        return appModel.getLastScreenView();
    }

    public String getCurrentDocInView() {
        return appModel.getCurrentScreenView();
    }

    public void displayNewDocument(Assignment assignment, String studentId) {
        System.out.println("New doc clicked: " + studentId);
        documentDatabase.updateAndStoreFeedbackDocument(assignment, studentId);
        appModel.setCurrentScreenView(studentId);
    }

    public void registerWithModel(PropertyChangeListener propertyChangeListener){
        appModel.subscribe(propertyChangeListener);
    }

}
