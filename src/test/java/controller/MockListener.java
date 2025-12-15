package controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.AssignmentListener;
import model.Phrase;
import model.StudentId;

public class MockListener implements AssignmentListener {

    private record Event(String type, String message) {}

    private Collection<Thread> threads;
    private List<Event> events;

    public MockListener() {
        threads = new ArrayList<>();
        events = new ArrayList<>();
    }

    public synchronized void joinAllThreads() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Override
    public void handleHeadingsUpdated(List<String> headings) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleHeadingsUpdated'");
    }

    @Override
    public void handleNewStudent(StudentId studentId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleNewStudent'");
    }

    @Override
    public void handleGradeUpdate(StudentId studentId, double grade) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleGradeUpdate'");
    }

    @Override
    public void handlePhraseAdded(String heading, Phrase phrase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePhraseAdded'");
    }

    @Override
    public void handlePhraseDeleted(String heading, Phrase phrase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePhraseDeleted'");
    }

    @Override
    public void handlePhraseCounterUpdated(String heading, Phrase phrase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePhraseCounterUpdated'");
    }

    @Override
    public void handleCustomPhraseAdded(String heading, Phrase phrase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCustomPhraseAdded'");
    }

    @Override
    public void handleCustomPhraseDeleted(String heading, String phrase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCustomPhraseDeleted'");
    }

    @Override
    public synchronized void handleSaveThread(Thread saveThread) {
        threads.add(saveThread);
    }

    @Override
    public void handleExported(Path outputDirectory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleExported'");
    }

    @Override
    public void handleInfo(String message) {
        events.add(new Event("Info", message));
    }

    @Override
    public void handleError(String description, Exception exception) {
        events.add(new Event("Error", description));
    }
}
