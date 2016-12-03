package com.hiveworkshop.wc3.jworldedit.wipdesign.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import com.JStormLib.MPQArchive;
import com.JStormLib.MPQArchiveException;

public class FileRetriever2 {
	public static void main(final String[] args) {
		try {
			final MPQArchive mpq = MPQArchive.openArchive(Paths.get("C:\\Program Files (x86)\\Battle.net\\Battle.net.6734", "Battle.net.mpq").toFile());
			final File listfile = new File("C:\\temp\\bnet2\\listfile.txt");
			mpq.extractFile("(listfile)", listfile);
			final BufferedReader reader = new BufferedReader(new FileReader(listfile));
			String line = null;
			while( (line = reader.readLine()) != null ) {
				if( line.startsWith("catalog\\cache") ) {
					try {
						mpq.extractFile(line, new File("C:\\Temp\\bnet2\\" +line.substring(line.lastIndexOf('\\')) + ".png"));
						System.err.println(line);
					} catch (final Exception e) {
						System.err.println("Did not find: " + line);
						e.printStackTrace();
					}
				}
			}
		} catch (final MPQArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		SeekableByteChannel sbc;
//		try {
//			sbc = Files.newByteChannel(Paths.get("C:\\Program Files (x86)\\Battle.net\\Battle.net.6890", "Battle.net.mpq"), EnumSet.of(StandardOpenOption.READ));
//			final MPQArchive mpq = new MPQArchive(sbc);
//			ArchivedFile file = null;
//			try {
//				file = mpq.lookupHash2(new HashLookup(
//						"(listfile)"));
//			} catch (final MPQException exc) {
//				throw new IOException(exc);
//			}
//			final ArchivedFileStream stream = new ArchivedFileStream(
//					sbc, extractor, file);
//			final InputStream newInputStream = Channels
//					.newInputStream(stream);
//			final BufferedReader reader = new BufferedReader(new InputStreamReader(newInputStream));
//			String line = null;
//			while( (line = reader.readLine()) != null ) {
//				if( line.startsWith("catalog\\cache") ) {
//					try {
//						final ArchivedFile cachedFile = mpq.lookupHash2(new HashLookup(line));
//						final ArchivedFileStream cachedFileStream = new ArchivedFileStream(
//								sbc, extractor, cachedFile);
//						final InputStream cachedFileInputStream = Channels
//								.newInputStream(cachedFileStream);
//						Files.copy(cachedFileInputStream, new File("C:\\Temp\\bnet\\" +line.substring(line.lastIndexOf('\\')) + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);
//						System.err.println(line);
//					} catch (final Exception e) {
////						System.err.println("Did not find: " + line);
////						e.printStackTrace();
//					}
//				}
//			}
//		} catch (final IOException e1) {
//			throw new RuntimeException(e1);
//		} catch (final MPQException e) {
//			throw new RuntimeException(e);
//		}
		catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
