package main;

import java.awt.Desktop;
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
     * @param args The arguments to the program, which may be an fht filename.
     */
    public static void main(String[] args) {
        // Create controller and view.
        // The Assignment (model) is created by the controller later
        AppController controller = new AppController();
        AppView view = AppView.create(controller);

        // Handle filename passed by desktop (MacOS)
        try {
            Desktop.getDesktop().setOpenFileHandler(e -> view.startWithFile(Path.of(e.getSearchTerm())));
            System.out.println("Created an open file handler (for MacOS)");
        } catch (UnsupportedOperationException e) {
            System.out.println("Not setting an open file handler (not MacOS)");
        }

        // Handle filename passed by command-line args (Linux/Windows)
        if (args.length > 0) {
            Path fhtFile = Path.of(args[0]);
            System.out.println("Application started with file " + fhtFile);
            view.startWithFile(fhtFile);
        } else {
            view.start();
        }
    }
}
