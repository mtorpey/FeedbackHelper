package view;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class LogoIcon extends BaseMultiResolutionImage {
    private static final int[] SIZES = { 256, 128, 64, 32, 24, 16, 8 };
    private static final LogoIcon INSTANCE = create();

    public static LogoIcon getInstance() {
        return INSTANCE;
    }

    private static LogoIcon create() {
        LogoIcon out;
        try {
            URL url = FeedbackScreen.class.getResource("/logo.png");
            BufferedImage baseImage = ImageIO.read(url);
            Image[] images = new Image[SIZES.length];
            for (int i = 0; i < SIZES.length; i++) {
                images[i] = scaleNearest(baseImage, SIZES[i]);
            }
            ImageIO.write((BufferedImage)images[6], "png", new File("debug_32.png"));
            out = new LogoIcon(images);
        } catch (IOException e) {
            out = new LogoIcon();
        }
        System.out.println("Available resolutions:");
        for (Image img : out.getResolutionVariants()) {
            System.out.println(" - " + img.getWidth(null) + "x" + img.getHeight(null));
        }
        Image best = out.getResolutionVariant(32, 32);
        System.out.println("Swing chose: " + best.getWidth(null) + "x" + best.getHeight(null));
        return out;
    }

    private LogoIcon(Image... resolutionVariants) {
        super(resolutionVariants);
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
