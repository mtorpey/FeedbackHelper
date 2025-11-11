package main;

import controller.AppController;
import view.AppView;

/**
 * Feedback Helper Tool Main Class.
 */
public class FeedbackHelperTool {

    /**
     * Main method.
     *
     * @param args The arguments to the program (none expected).
     */
    public static void main(String[] args) {
        FeedbackHelperTool fht = new FeedbackHelperTool();
        fht.start();
    }

    /**
     * Start the program.
     */
    public void start() {
        // Create controller and view.
        // The Assignment (model) is created by the controller later
        AppController controller = new AppController();
        AppView view = new AppView(controller);

        // Start the view
        view.start();
    }
}
