package mpqlib;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez 
 */
public class WindowsRegistry {

    /**
     * 
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     */
    public static final String readRegistry(String location, String key){
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
            if( ! output.contains("REG_SZ")){
                    return null;
            }

            // Parse out the value
            String[] parsed = output.split("REG_SZ");
            String finalout = parsed[parsed.length-1];
            finalout = finalout.substring(finalout.indexOf(":")-1);
            return finalout;
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
    public static void main(String[] args) {

        // Sample usage
        String value = WindowsRegistry.readRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\" 
                 + "Explorer\\Shell Folders", "Personal");
        System.out.println(value);
    }
}