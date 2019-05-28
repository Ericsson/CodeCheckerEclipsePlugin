package org.codechecker.eclipse.rcp.shared.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Helper functions for:
 *  * File loading from bundles.
 *  * Recursive copying of folders.
 *  * CodeChecker preparation.
 */
public class Utils {
    public static final String RES = "resources" + File.separator; 
    
    private static final String CODECHECKER = "CodeChecker";
    private static final String BIN = "bin";
    
    /**
     * Not called.
     */
    private Utils() {}
    
    /**
     * Loads a file from a bundle.
     * @param bundlename The bundle to be loaded from.
     * @param pathFrom The path to the desired file (use with "resources/").
     * @return returns a Path to the specified resource.
     * @throws URISyntaxException If this URL is not formatted strictly according to to RFC2396 and cannot be 
     *      converted to a URI.
     * @throws IOException If an error occurs during the conversion.
     */
    public static Path loadFileFromBundle(String bundlename, String pathFrom) throws URISyntaxException, IOException {
        // http://blog.vogella.com/2010/07/06/reading-resources-from-plugin/
        Bundle bundle = Platform.getBundle(bundlename);
        return Paths.get(FileLocator.toFileURL(
                FileLocator.find(bundle, new org.eclipse.core.runtime.Path(pathFrom), null)).toURI());
    }
    
    /**
     * Recursively copies folders.
     *
     * @param src The source folder, will be copied.
     * @param dest The destination folder to be copied in.
     * @return The path to the copied folder.
     * @throws IOException Could be thrown on access denied,
     *                      or if the copy fails.
     */
    public static Path copyFolder(Path src, Path dest) {
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
        return dest.resolve(src.getFileName());
    }
    
    /**
     * Convenience method for quickly get a runnable CodeChecker.
     * Has the same effect to loadCodeChecker("CodeChecker").
     * Copies into the default directory layout, and sets runnable permission to Codechecker.
     * The path to the runnable CodeChecker will be tmp/<tempTestFolder>/CodeChecker/bin/CodeChecker
     * @return The path to To the CodeChecker root directory. 
     *      Will point to tmp/<tempTestFolder>/CodeChecker .
     */
    public static Path prepareCodeChecker() {
        return prepareCodeChecker(CODECHECKER);
    }
    
    /**
     * Copies into the specified directory, and sets runnable permission to Codechecker.
     * The path to the runnable CodeChecker will be tmp/<tempTestFolder>/<into>/bin/CodeChecker
     * @param into This will be the name of the CodeChecker root folder.
     * @return The path to To the CodeChecker root directory. 
     *      Will point to tmp/<tempTestFolder>/<into> .
     */
    public static Path prepareCodeChecker(String into) {
        if (into.isEmpty() || into == null) throw new IllegalArgumentException();
        
        Path testDir = null;
        Path ccRoot = null;
        try {
            testDir = Files.createTempDirectory("CCTest");
            testDir.toFile().deleteOnExit();
            testDir = Files.createDirectory(Paths.get(testDir.toString(), into));
            ccRoot = Utils.loadFileFromBundle("org.codechecker.eclipse.rcp.shared",
                    Utils.RES + CODECHECKER);
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace(System.out);
        }
        // Get the CodeChecker stub from the test resources, and copy it to a temporary folder.
        Path ccDir = Utils.copyFolder(ccRoot, testDir);
        Path ccBinDir = Paths.get( testDir.toAbsolutePath().toString(), CODECHECKER, BIN, CODECHECKER);
        try {
            // CodeChecker must be runnable.
            Files.setPosixFilePermissions(ccBinDir, PosixFilePermissions.fromString("rwxrwxrwx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ccDir;
    }
}
