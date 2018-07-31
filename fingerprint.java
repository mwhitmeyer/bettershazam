import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Created by whitt on 5/27/2018.
 *
 * purpose is to serve as a function that takes mp3 files, turns into byte array, fingerprints them,
 * and adds them to our giant hashtable
 *
 */
public class fingerprint {

    public static byte[] readmp3(String mp3filePath) {
        Path path = Paths.get(mp3filePath);
        try {
            byte[] data = Files.readAllBytes(path);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

    }
}
