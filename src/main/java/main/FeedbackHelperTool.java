package main;

import controller.AppController;
import model.AppModel;
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
        // Load everything up
        AppModel model = new AppModel();
        AppController controller = new AppController(model);
        AppView view = new AppView(controller);

        // Start the view
        view.start();
    }
}
