package com.hiveworkshop.wc3.jworldedit.wipdesign.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import mpq.ArchivedFile;
import mpq.ArchivedFileExtractor;
import mpq.ArchivedFileStream;
import mpq.HashLookup;
import mpq.MPQArchive;
import mpq.MPQException;

public class FileRetriever {
	static ArchivedFileExtractor extractor = new ArchivedFileExtractor();
	public static void main(final String[] args) {
		SeekableByteChannel sbc;
		try {
			sbc = Files.newByteChannel(Paths.get("C:\\Program Files (x86)\\Battle.net\\Battle.net.6890", "Battle.net.mpq"), EnumSet.of(StandardOpenOption.READ));
			final MPQArchive mpq = new MPQArchive(sbc);
			ArchivedFile file = null;
			try {
				file = mpq.lookupHash2(new HashLookup(
						"(listfile)"));
			} catch (final MPQException exc) {
				throw new IOException(exc);
			}
			final ArchivedFileStream stream = new ArchivedFileStream(
					sbc, extractor, file);
			final InputStream newInputStream = Channels
					.newInputStream(stream);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(newInputStream));
			String line = null;
			while( (line = reader.readLine()) != null ) {
				if( line.startsWith("catalog\\cache") ) {
					try {
						final ArchivedFile cachedFile = mpq.lookupHash2(new HashLookup(line));
						final ArchivedFileStream cachedFileStream = new ArchivedFileStream(
								sbc, extractor, cachedFile);
						final InputStream cachedFileInputStream = Channels
								.newInputStream(cachedFileStream);
						Files.copy(cachedFileInputStream, new File("C:\\Temp\\bnet\\" +line.substring(line.lastIndexOf('\\')) + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);
						System.err.println(line);
					} catch (final Exception e) {
//						System.err.println("Did not find: " + line);
//						e.printStackTrace();
					}
				}
			}
		} catch (final IOException e1) {
			throw new RuntimeException(e1);
		} catch (final MPQException e) {
			throw new RuntimeException(e);
		}
	}
}
