package view;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class LogoIcon {

    private static final int SIZE = 512; // should scale down to smaller sizes nicely
    private static Image image;

    public static Image getImage() throws IOException {
        if (image == null) {
            create();
        }
        return image;
    }

    public static Icon getIcon() throws IOException {
        return new ImageIcon(getImage());
    }

    public static JLabel createLabel() {
        try {
            return new JLabel(getIcon());
        } catch (IOException e) {
            alertError(e);
        }
        return new JLabel("FeedbackHelper");
    }
    
    public static void applyIcon(JFrame frame) {
        try {
            // Set the icon
            frame.setIconImage(getImage());
        } catch (IOException e) {
            // Problem: alert user
            alertError(e);
        }
    }

    private static void create() throws IOException {
        URL url = FeedbackScreen.class.getResource("/logo.png");
        BufferedImage baseImage = ImageIO.read(url);
        Image scaledImage = scaleNearest(baseImage, SIZE);
        image = scaledImage;
    }

    private static void alertError(IOException e) {
        e.printStackTrace();
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(
                null,
                "Error loading FeedbackHelper icon",
                "Error!",
                JOptionPane.ERROR_MESSAGE
            )
        );
    }

    private static Image scaleNearest(BufferedImage src, int size) {
        BufferedImage dst = new BufferedImage(size, size, src.getType());
        AffineTransform at = AffineTransform.getScaleInstance(
            (double) size / src.getWidth(),
            (double) size / src.getHeight()
        );
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(src, dst);
    }
}
