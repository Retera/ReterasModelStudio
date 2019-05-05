import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import com.gtranslate.Language;
import com.gtranslate.Translator;

import de.deaod.jstormlib.MPQArchive;
import de.deaod.jstormlib.MPQArchiveCreateFlags;
import de.deaod.jstormlib.MPQArchiveOpenFlags;
import de.deaod.jstormlib.MPQArchiveOpenFlags.StreamFlag;
import de.deaod.jstormlib.exceptions.MPQAlreadyExistsException;
import de.deaod.jstormlib.exceptions.MPQFileNotFoundException;
import de.deaod.jstormlib.exceptions.MPQFormatException;
import de.deaod.jstormlib.exceptions.MPQIsAVIException;


public class WTSTranslate {
	static Language language = Language.getInstance();
	static Translator translate = Translator.getInstance();
	public static void main(String[] args) {
		try {
//			fixSkinTXT("war3mapSkin.txt", "war3map.wts");
			fixSkinTXT("Blizzard.j", "war3map.wts");
//			translateWTS("war3map.wts", language.FRENCH, language.ARABIC);
//			translateWTS("war3map.wts", language.FRENCH, language.GERMAN);
//			translateWTS("war3map.wts", language.FRENCH, language.SPANISH);
//			translateWTS("war3map.wts", language.FRENCH, language.SWEDISH);
//			translateWTS("war3map.wts", language.FRENCH, language.AFRIKAANS);
//			translateWTS("war3map.wts", language.FRENCH, language.RUSSIAN);
//			translateWTS("war3map.wts", language.FRENCH, language.UKRAINIAN);
//			translateWTS("war3map.wts", language.FRENCH, language.CHINESE);
//			translateWTS("war3map.wts", language.FRENCH, language.KOREAN);
//			translateWTS("war3map.wts", language.FRENCH, language.JAPANESE);
//			translateWTS("war3map.wts", language.FRENCH, language.CATALAN);
//			translateWTS("war3map.wts", language.FRENCH, language.PORTUGUESE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void translateWTS(String name, String from, String lang) throws IOException {
		File nameGetter = new File(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(WTSTranslate.class.getResourceAsStream(name), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(nameGetter.getName().substring(0, nameGetter.getName().lastIndexOf('.'))+"_"+lang+".wts"
				))
				, "UTF-8")
				);
		
		boolean inStr = false;
		String line = null;
		while( (line = reader.readLine()) != null ) {
			if( !inStr ) {
				if( line.contains("{") ) 
					inStr = true;
				writer.println(line);
			} else {
				if( line.contains("}") ) {
					inStr = false;
					writer.println(line);
				}
				else {
					if( line.length() > 2 ) {
						String trans = translate.translate(line, from, lang);
						trans = trans.replace("| r", "|r");
						trans = trans.replace("| n", "|n");
						trans = trans.replace("| c", "|c");
						System.out.println(line + " => " + trans);
						writer.println(trans);
					} else {
						writer.println(line);
					}
				}
			}
		}
		writer.flush();
		writer.close();
		reader.close();
	}
	public static void translateWTS(File name, String from, String lang, File output) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(name), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output)
				, "UTF-8")
				);
		
		boolean inStr = false;
		String line = null;
		while( (line = reader.readLine()) != null ) {
			if( !inStr ) {
				if( line.contains("{") ) 
					inStr = true;
				writer.println(line);
			} else {
				if( line.contains("}") ) {
					inStr = false;
					writer.println(line);
				}
				else {
					if( line.length() > 2 ) {
						String trans = translate.translate(line.replace("|", " XXXBXXGXXX "), from, lang);
						trans = trans.replace(" XXXBXXGXXX ", "|");
//						String trans = translate.translate(line, from, lang);
						trans = trans.replace("| r", "|r");
						trans = trans.replace("| n", "|n");
						trans = trans.replace("| c", "|c");
						System.out.println(line + " => " + trans);
						writer.println(trans);
					} else {
						writer.println(line);
					}
				}
			}
		}
		writer.flush();
		writer.close();
		reader.close();
	}
	public static void translateMap(File name, String from, String lang, File output) throws IOException, MPQFormatException, MPQIsAVIException, MPQFileNotFoundException, MPQAlreadyExistsException {
		copyFile(name, output);
		
		MPQArchive theMap = null;
		MPQArchiveOpenFlags flags = new MPQArchiveOpenFlags();
		flags.setOpenFlags(StreamFlag.READ_ONLY); // don't write on the map, don't hurt source
		theMap = new MPQArchive(name, flags);
		
		File tempInput = File.createTempFile("wts_trans", ".wts");
		theMap.extractFile("war3map.wts", tempInput);
		tempInput.deleteOnExit();
		
		File tempOutput = File.createTempFile("wts_trans_out", ".wts");
		tempOutput.deleteOnExit();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tempInput), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempOutput)
				, "UTF-8")
				);
		
		boolean inStr = false;
		String line = null;
		while( (line = reader.readLine()) != null ) {
			if( !inStr ) {
				if( line.contains("{") ) 
					inStr = true;
				writer.println(line);
			} else {
				if( line.contains("}") ) {
					inStr = false;
					writer.println(line);
				}
				else {
					if( line.length() > 2 ) {
						String trans = translate.translate(line.replace("|", "XXXBXXGXXX"), from, lang);
						trans = trans.replace("XXXBXXGXXX", "|");
						trans = trans.replace("| r", "|r");
						trans = trans.replace("| n", "|n");
						trans = trans.replace("| c", "|c");
						System.out.println(line + " => " + trans);
						writer.println(trans);
					} else {
						writer.println(line);
					}
				}
			}
		}
		writer.flush();
		writer.close();
		reader.close();

		
		output.delete();
		MPQArchive finMap = null;
		MPQArchiveCreateFlags cflags = new MPQArchiveCreateFlags();
		finMap = new MPQArchive(output, cflags);
		
		finMap.setMaxFileCount(theMap.getHashTableSize());
		
		File tempListfile = File.createTempFile("tempListfile", "");
		theMap.extractFile("(listfile)", tempListfile);
		BufferedReader listReader = new BufferedReader(new FileReader(tempListfile));
		String fileName = null;
		while( (fileName = listReader.readLine()) != null ) {
			if( fileName.equals("war3map.wts") )
				continue;
			File tmp = File.createTempFile("wts_builder", ".tmp");
			theMap.extractFile(fileName, tmp);
			finMap.addFile(tmp.getAbsolutePath(), fileName);
		}
		listReader.close();
		finMap.addListFile(tempListfile);
		tempListfile.delete();
		
		finMap.addFile(tempOutput.getAbsolutePath(), "war3map.wts");
		finMap.flush();
		finMap.close();
		theMap.close();
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	public static void fixSkinTXT(String name, String WTS) throws IOException {
		File nameGetter = new File(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(WTSTranslate.class.getResourceAsStream(WTS), "UTF-8"));
//		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(nameGetter.getName().substring(0, nameGetter.getName().lastIndexOf('.'))+"_"+lang+".wts"
//				))
//				, "UTF-8")
//				);
		Map<String, String> TRIGSTR = new LinkedHashMap<String, String>();
		String lastID = null;
		boolean inStr = false;
		String line = null;
		while( (line = reader.readLine()) != null ) {
			if( !inStr ) {
				if( line.contains("{") ) 
					inStr = true;
				if( line.contains("STRING") ) {
					String[] str = line.split(" ");
					lastID = "TRIGSTR_" + String.format("%3s", str[1]).replace(' ', '0');
//					System.out.println(lastID);
				}
//				writer.println(line);
			} else {
				if( line.contains("}") ) {
					inStr = false;
//					writer.println(line);
				}
				else {
//					if( line.length() > 2 ) {
						TRIGSTR.put(lastID, line);
//						String trans = translate.translate(line, from, lang);
//						trans = trans.replace("| r", "|r");
//						trans = trans.replace("| n", "|n");
//						trans = trans.replace("| c", "|c");
//						System.out.println(line + " => " + trans);
//						writer.println(trans);
//					} else {
////						writer.println(line);
//					}
				}
			}
		}
//		writer.flush();
//		writer.close();
		reader.close();
		
		for( String key: TRIGSTR.keySet() ) {
			System.out.println(key);// + ": " + TRIGSTR.get(key));
		}

		Stack<String> stack = new Stack<String>();
		
		
		for( String key: TRIGSTR.keySet() ) {
			stack.add(key);
		}
		Map<String, String> temp = new LinkedHashMap<String, String>();
		while( !stack.isEmpty() ) {
			String str = stack.pop();
			temp.put(str, TRIGSTR.get(str));
		}
		
		TRIGSTR = temp;
		for( String key: TRIGSTR.keySet() ) {
			System.err.println(key);// + ": " + TRIGSTR.get(key));
		}
		reader = new BufferedReader(new InputStreamReader(WTSTranslate.class.getResourceAsStream(name), "UTF-8"));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(nameGetter.getName().substring(0, nameGetter.getName().lastIndexOf('.'))+"_update.txt"
		))
		, "UTF-8")
		);
		while( (line = reader.readLine()) != null ) {
			for( String key: TRIGSTR.keySet() ) {
				if( line.contains(key) ) {
					line = line.replace(key, TRIGSTR.get(key));
				}
			}
			writer.println(line);
		}
		writer.flush();
		writer.close();
		reader.close();
	}
}
