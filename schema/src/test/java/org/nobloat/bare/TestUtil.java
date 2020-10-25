package org.nobloat.bare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class TestUtil {
    public static InputStream openFile(String name) throws FileNotFoundException {
        String path = "src/test/resources";
        File file = new File(path);
        return new FileInputStream(file.getAbsolutePath() + "/" + name);
    }
}
