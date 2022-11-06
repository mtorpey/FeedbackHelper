package main;

import controller.AppController;
import controller.IAppController;
import model.AppModel;
import model.IAppModel;
import view.AppView;
import view.IAppView;
import view.LoadingScreen;

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
        // Show splash screen
        new Thread(LoadingScreen::showSplashScreen).start();

        // Load everything up
        IAppModel model = new AppModel();
        IAppController controller = new AppController(model);
        IAppView view = new AppView(controller);

        // Start the view
        view.start();
    }

}
