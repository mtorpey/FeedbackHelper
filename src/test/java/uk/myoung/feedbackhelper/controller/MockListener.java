package uk.myoung.feedbackhelper.controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.myoung.feedbackhelper.model.AssignmentListener;
import uk.myoung.feedbackhelper.model.Phrase;
import uk.myoung.feedbackhelper.model.StudentId;

public class MockListener implements AssignmentListener {

    private record Event(String type, String message) {}

    private Collection<Thread> threads;
    private List<Event> events;

    public MockListener() {
        threads = new ArrayList<>();
        events = new ArrayList<>();
    }

    private void addEvent(String type, String message) {
        synchronized(events) {
            events.add(new Event(type, message));
        }
    }

    public boolean hasEvent(String typePattern, String messagePattern) {
        synchronized(events) {
            return events.stream().anyMatch(e -> e.type().matches(typePattern) && e.message().matches(messagePattern));
        }
    }

    public synchronized void joinAllThreads() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Override
    public void handleHeadingsUpdated(List<String> headings) {
        addEvent("headingsUpdated", String.join(",", headings));
    }

    @Override
    public void handleNewStudent(StudentId studentId) {
        addEvent("newStudent", studentId.id());
    }

    @Override
    public void handleGradeUpdate(StudentId studentId, double grade) {
        addEvent("gradeUpdate", studentId.id() + "," + grade);
    }

    @Override
    public void handlePhraseAdded(String heading, Phrase phrase) {
        addEvent("phraseAdded", phrase.getPhraseAsString() + " to " + heading);
    }

    @Override
    public void handlePhraseDeleted(String heading, Phrase phrase) {
        addEvent("phraseDeleted", phrase.getPhraseAsString() + " from " + heading);
    }

    @Override
    public void handlePhraseCounterUpdated(String heading, Phrase phrase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePhraseCounterUpdated'");
    }

    @Override
    public void handleCustomPhraseAdded(String heading, Phrase phrase) {
        addEvent("customPhraseAdded", phrase.getPhraseAsString() + " to " + heading);
    }

    @Override
    public void handleCustomPhraseDeleted(String heading, String phrase) {
        addEvent("customPhraseDeleted", phrase + " from " + heading);
    }

    @Override
    public synchronized void handleSaveThread(Thread saveThread) {
        threads.add(saveThread);
    }

    @Override
    public void handleExported(Path outputDirectory) {
        addEvent("exported", "to " + outputDirectory);
    }

    @Override
    public void handleCustomPhraseReordered(String heading, int oldPos, int newPos) {
        addEvent("customPhraseReordered", "from " + oldPos + " to " + newPos + " in " + heading);
    }

    @Override
    public void handleInfo(String message) {
        addEvent("Info", message);
    }

    @Override
    public void handleError(String description, Exception exception) {
        addEvent("Error", description);
    }
}
