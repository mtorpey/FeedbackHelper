package main;

import controller.AppController;
import controller.IAppController;
import model.AppModel;
import model.IAppModel;
import view.AppView;
import view.IAppView;
import view.LoadingScreen;

import java.util.Collection;
import org.neo4j.configuration.*;
import org.neo4j.service.Services;


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

        testSettingsLoaders();
    }

    private void testSettingsLoaders() {

        final Collection DEFAULT_SETTING_CLASSES =
            Services.loadAll(SettingsDeclaration.class).stream()
            .map(c -> c.getClass())
            .toList();
        
        final Collection DEFAULT_GROUP_SETTING_CLASSES =
            Services.loadAll(GroupSetting.class).stream()
            .map(c -> c.getClass())
            .toList();
        
        final Collection DEFAULT_SETTING_MIGRATORS =
            Services.loadAll(SettingMigrator.class);
        
        System.out.println(String.format("Settings loader triple: (%d, %d, %d)",
                                         DEFAULT_SETTING_CLASSES.size(),
                                         DEFAULT_GROUP_SETTING_CLASSES.size(),
                                         DEFAULT_SETTING_MIGRATORS.size()));
    }

}
