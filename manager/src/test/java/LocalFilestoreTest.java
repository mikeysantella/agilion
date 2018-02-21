import com.agilion.services.files.LocalFileStore;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.UUID;

/**
 * Created by Alex_Lappy_486 on 2/20/18.
 */
public class LocalFilestoreTest
{
    @Test
    public void test() throws IOException {
        LocalFileStore fs = new LocalFileStore(System.getProperty("user.dir")+"/AgilionLocalFileStore/");

        File testFile = new File("test.txt");
        FileWriter writer = new FileWriter(testFile);

        writer.write("TEST");
        writer.close();

        InputStream is = new FileInputStream(testFile);
        String t = fs.storeFile(is, UUID.randomUUID().toString());

        // This line will throw an exception if the returned string isn't a valid URI
        URL url = new URL(t);

        // Clean Up
        testFile.delete();
    }
}
