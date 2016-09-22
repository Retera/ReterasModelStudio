package net.wc3c.ode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import net.wc3c.util.Log;
import net.wc3c.w3o.W3BFile;
import net.wc3c.w3o.W3OBase;
import net.wc3c.w3o.W3TFile;
import net.wc3c.w3o.W3UFile;
import net.wc3c.wts.WTSFile;
import de.deaod.jstormlib.MPQArchive;
import de.deaod.jstormlib.MPQCompressionFlags;
import de.deaod.jstormlib.MPQCompressionFlags.Compression;
import de.deaod.jstormlib.MPQFile;
import de.deaod.jstormlib.MPQFileFlags;
import de.deaod.jstormlib.MPQFileOpenScope;

public final class ObjectDataExtractor {
    private static final String FILE_NAME_SCRIPT = "war3map.j";
    private static final String FILE_NAME_WTS    = "war3map.wts";
    private static final String FILE_NAME_W3U    = "war3map.w3u";
    private static final String FILE_NAME_W3T    = "war3map.w3t";
    private static final String FILE_NAME_W3B    = "war3map.w3b";
    
    static String readFileToString(final String path, final Charset encoding) throws IOException {
        final byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
    
    private static <T extends W3OBase<?>> T loadW3OFile(final Class<T> cls, final MPQArchive map,
            final String archivedFile, final File file, final WTSFile wts) throws Exception {
        T w3o;
        if (map.hasFile(archivedFile)) {
            Log.info("Found " + archivedFile);
            map.extractFile(archivedFile, file);
            
            w3o = cls.getConstructor(String.class, WTSFile.class).newInstance(file.getPath(), wts);
        } else {
            w3o = cls.getConstructor(WTSFile.class).newInstance(wts);
        }
        return w3o;
    }
    
    private ObjectDataExtractor(final String odeFolderPath, final String mapPath) throws Exception {
        Log.entry(odeFolderPath, mapPath);
        final MPQArchive map = new MPQArchive(mapPath);
        
        final File odeFolder = new File(odeFolderPath);
        final File jFile = new File(odeFolder, FILE_NAME_SCRIPT);
        final File wtsFile = new File(odeFolder, FILE_NAME_WTS);
        final File w3uFile = new File(odeFolder, FILE_NAME_W3U);
        final File w3tFile = new File(odeFolder, FILE_NAME_W3T);
        final File w3bFile = new File(odeFolder, FILE_NAME_W3B);
        
        map.extractFile(FILE_NAME_SCRIPT, jFile);
        map.extractFile(FILE_NAME_WTS, wtsFile);
        
        Log.info("Loading JASS into memory");
        String scriptContent = readFileToString(jFile.getPath(), StandardCharsets.UTF_8);
        Log.info("Loading WTS from file");
        final WTSFile wts = new WTSFile(wtsFile.getPath());
        
        final List<Extractor<?, ?>> extractors = new LinkedList<Extractor<?, ?>>();
        
        //-------------------------------------------------------
        // Unit Data Extractor
        extractors.add(new UnitDataExtractor(odeFolder, loadW3OFile(W3UFile.class, map, FILE_NAME_W3U, w3uFile, wts)));
        
        //-------------------------------------------------------
        // Item Data Extractor
        extractors.add(new ItemDataExtractor(odeFolder, loadW3OFile(W3TFile.class, map, FILE_NAME_W3T, w3tFile, wts)));
        
        //-------------------------------------------------------
        // Destructable Data Extractor
        extractors.add(new DestructableDataExtractor(odeFolder, loadW3OFile(
                W3BFile.class,
                map,
                FILE_NAME_W3B,
                w3bFile,
                wts)));
        
        for (final Extractor<?, ?> extractor : extractors) {
            scriptContent = extractor.processScript(scriptContent);
        }
        
        Log.info("Writing new script file to disk");
        final FileWriter fw = new FileWriter(jFile);
        fw.write(scriptContent);
        fw.close();
        
        Log.info("Replacing script file in map");
        final MPQFile file = new MPQFile(map, FILE_NAME_SCRIPT, MPQFileOpenScope.MPQ);
        file.removeFromArchive();
        file.close();
        
        final MPQCompressionFlags compr = new MPQCompressionFlags();
        compr.setCompression(Compression.BZIP2);
        
        map.addFile(jFile.getAbsolutePath(), FILE_NAME_SCRIPT, MPQFileFlags.fromInteger(0x200), compr);
        map.compactArchive((String) null);
        map.close();
        
        Log.exit();
    }
    
    public static void main(final String[] args) {
        Log.entry((Object[]) args);
        final long startTime = System.nanoTime();
        
        final String odeFolderPath = args[0];
        final String mapPath = args[1];
        try {
            @SuppressWarnings("unused")
            final ObjectDataExtractor ode = new ObjectDataExtractor(odeFolderPath, mapPath);
            
            Log.info("Finished. Took {} ms", (System.nanoTime() - startTime) / 1_000_000d);
        } catch (final Exception ex) {
            final StringWriter result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            javax.swing.JOptionPane.showMessageDialog(null, "Error in Object Data Exporter:\n" + result.toString());
            Log.exception(ex);
        }
        
        Log.exit();
    }
    
}
