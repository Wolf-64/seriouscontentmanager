package com.wlf.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez 
 */
public abstract class WindowsRegistry {

    /**
     *
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     */
    public static String readRegistry(String location, String key) {
        try {
            // Run reg query, then read output with StreamReader (internal class)
            Process process = Runtime.getRuntime().exec("reg query " +
                    '"'+ location + "\" /v " + key);

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            Pattern p = Pattern.compile("^\\s*([^\\s]+)\\s+([A-Z_0-9]+)\\s+(.+)$", Pattern.MULTILINE);
            Matcher m = p.matcher(output);

            String value = null;
            while (m.find()) {
                value = m.group(3);  // D:\Games\Steam
            }

            return value;
        }
        catch (Exception e) {
            return null;
        }

    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw= new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            }
            catch (IOException e) {
            }
        }

        public String getResult() {
            return sw.toString();
        }
    }
}