package net.wc3c.wts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

/**
 * 
 * @author Deaod
 * 
 */
public class WTSFile {
    private final Path                 source;
    private final Map<Integer, String> trigStrings = new Hashtable<Integer, String>();
    
    private static enum ParseState {
        NEXT_TRIGSTR,
        START_OF_DATA,
        END_OF_DATA;
    }
    
    private void parse() throws IOException {
        final String content = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Files.readAllBytes(source))).toString();
        final BufferedReader sourceReader = new BufferedReader(new StringReader(content));
        ParseState state = ParseState.NEXT_TRIGSTR;
        
        // WTS files may start with a Byte Order Mark, which we will have to skip.
        sourceReader.mark(4);
        if (sourceReader.read() != 0xFEFF) {
            // first character not a BOM, unread the character.
            sourceReader.reset();
        }
        
        String currentLine = sourceReader.readLine();
        int id = 0;
        StringBuffer data = new StringBuffer();
        
        while (currentLine != null) {
            switch (state) {
                case NEXT_TRIGSTR:
                    if (currentLine.startsWith("STRING ")) {
                        id = Integer.parseInt(currentLine.substring(7));
                        state = ParseState.START_OF_DATA;
                    }
                    break;
                
                case START_OF_DATA:
                    if (currentLine.startsWith("{")) {
                        state = ParseState.END_OF_DATA;
                    }
                    break;
                
                case END_OF_DATA:
                    if (currentLine.startsWith("}")) {
                        trigStrings.put(id, data.toString());
                        data = new StringBuffer();
                        state = ParseState.NEXT_TRIGSTR;
                    } else {
                        data.append(currentLine);
                    }
                    break;
            }
            currentLine = sourceReader.readLine();
        }
        sourceReader.close();
    }
    
    public WTSFile(final Path source) throws IOException {
        this.source = source;
        
        parse();
    }
    
    public WTSFile(final String sourcePath) throws IOException {
        source = Paths.get(sourcePath);
        
        parse();
    }
    
    public String get(final int index) {
        return trigStrings.get(index);
    }
    
}
