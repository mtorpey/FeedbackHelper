package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Metadata {

    private static final String METADATA_PATH = "/META-INF/maven/uk.myoung/FeedbackHelper/pom.properties";

    public static String getVersion() {
        Properties p = new Properties();
        InputStream is = Metadata.class.getResourceAsStream(METADATA_PATH);
        if (is != null) {
            try {
                p.load(is);
                return p.getProperty("version", "");
            } catch (IOException e) {
                // Could not load version metadata (not running from jar?)
            }
        }
        return "unknown";
    }
}
