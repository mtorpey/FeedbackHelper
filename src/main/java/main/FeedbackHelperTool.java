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

        // Start appropriately for the OS and supplied file (if any)
        try {
            // Handle filename passed by desktop (MacOS)
            Desktop.getDesktop().setOpenFileHandler(e -> view.startWithFile(e.getFiles().get(0).toPath()));
            // This might delete the home screen, hence the use of SwingUtilities::invokeLater in AppView.
            view.start();
            System.out.println("Created an open file handler (for MacOS)");
        } catch (UnsupportedOperationException e) {
            // Handle filename passed by command-line args (Linux/Windows)
            System.out.println("Not setting an open file handler (not MacOS)");
            if (args.length > 0) {
                Path fhtFile = Path.of(args[0]);
                view.startWithFile(fhtFile);
            } else {
                view.start();
            }
        }

    }
}
