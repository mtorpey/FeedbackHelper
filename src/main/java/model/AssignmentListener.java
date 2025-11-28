package model;

import java.nio.file.Path;
import java.util.List;

public interface AssignmentListener {
    /*
      Some old messages have been removed.

      The following had no effect anyway:
      OLD_VALUE_DUMMY_MESSAGE = "oldValue";
      ASSIGNMENT_MESSAGE = "assignment";
      CREATED_MESSAGE = "created";

      The following are based on concepts like pagination that are no longer
      handled by the model:
      DOC_VIEW_CHANGE_MESSAGE = "docViewChange";
      PHRASE_PANEL_CHANGE_MESSAGE = "phrasePanelChange";
      RESET_PHRASES_PANEL_MESSAGE = "resetPhrasesPanel";
    */

    // RESET_FEEDBACK_BOXES_MESSAGE = "resetFeedbackBoxes";
    // "editHeading"
    void handleHeadingsUpdated(List<String> headings);

    void handleNewStudent(StudentId studentId);

    void handleGradeUpdate(StudentId studentId, double grade);

    // NEW_PHRASE_MESSAGE = "newPhrase";
    void handlePhraseAdded(String heading, Phrase phrase);

    // DELETE_PHRASE_MESSAGE = "deletePhrase";
    void handlePhraseDeleted(String heading, Phrase phrase);

    // UPDATE_PHRASE_COUNTER_MESSAGE = "updatePhraseCounter";
    void handlePhraseCounterUpdated(String heading, Phrase phrase);

    // NEW_CUSTOM_PHRASE_MESSAGE = "newCustomPhrase";
    void handleCustomPhraseAdded(String heading, Phrase phrase);

    void handleCustomPhraseDeleted(String heading, String phrase);

    void handleSaveThread(Thread saveThread);

    void handleExported(Path outputDirectory);

    void handleInfo(String message);

    // ERROR_MESSAGE = "error";
    void handleError(String description, Exception exception);
}
