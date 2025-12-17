package uk.myoung.feedbackhelper.infrastructure;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DesktopActions {

    public static boolean canMoveToTrash() {
        return Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH);
    }

    /** Attempt to move the given file to the trash. */
    public static void moveToTrashIfExists(Path path) {
        try {
            if (Files.exists(path)) {
                Desktop.getDesktop().moveToTrash(path.toFile());
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            // Do nothing
        }
    }
}
