package com.hand.dot.util;

import java.io.File;
import java.util.UUID;

public class FileUtil {

    public static File createRandomFilePath(File dir, String extension) {
        return new File(dir + "/" + UUID.randomUUID().toString() + "." + extension);
    }

    public static File createRandomFilePath(File dir) {
        return new File(dir + "/" + UUID.randomUUID().toString());
    }

}
