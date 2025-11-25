package main;

import java.nio.file.Path;

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
        // Create controller and view.
        // The Assignment (model) is created by the controller later
        AppController controller = new AppController();
        AppView view = AppView.create(controller);

        // Handle filename passed by command-line args
        if (args.length > 0) {
            Path fhtFile = Path.of(args[0]);
            view.startWithFile(fhtFile);
        } else {
            view.start();
        }
    }
}
