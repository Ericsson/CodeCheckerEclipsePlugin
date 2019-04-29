package org.codechecker.eclipse.plugin.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Some useful utility methods.
 */
public class Utils {

    /**
     * Hidden constructor in Utility class.
     */
    private Utils() {};

    /**
     * Recursively copies folders.
     *
     * @param src The source folder, will be copied.
     * @param dest The destination folder to be copied in.
     * @throws IOException Could be thrown on access denied,
     *                      or if the copy fails.
     */
    public static void copyFolder(Path src, Path dest) {
        try {
            Files.walk(src).forEach(source -> {
                try {
                    Files.copy(source, dest.resolve(src.getParent().relativize(source)),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
