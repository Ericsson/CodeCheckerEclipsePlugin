package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Unit test file loader helper class.
 */
public class UrlFileLoader {
    
    /**
     * Utility method for creating test files, because tycho runs unit test in an OSGi container,
     * but the eclipse IDE junit laucher won't. The resource that's returned by the class loader
     * is a file in the IDE context, but bundlesource in the CLI context, and this is the best way
     * I have found to convert it reliably on both environments.
     * @param url The url in String form.
     * @return The file if can be
     * @throws IOException 
     */
    public static File getFileFromUrl(String path, String fileName, String extension) {
        File file = null;
       
        ClassLoader cl = UrlFileLoader.class.getClassLoader();
        
        try {
            file = File.createTempFile(fileName, extension);
            file.deleteOnExit();
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            String url = path + File.separator + fileName + "." + extension;
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(cl.getResource(url).openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                out.write(inputLine + System.lineSeparator());
                //System.out.println(inputLine);
            }
            in.close();
            out.close();
        } catch (IOException e1) {
            //throw e1;
        }
        return file;
    }
    
}
