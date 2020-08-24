package com.hiveworkshop.rms.filesystem.sources;

import mpq.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MpqDataSource implements DataSource {

	private final MPQArchive archive;
	private final SeekableByteChannel inputChannel;
	private final ArchivedFileExtractor extractor = new ArchivedFileExtractor();

	public MpqDataSource(final MPQArchive archive, final SeekableByteChannel inputChannel) {
		this.archive = archive;
		this.inputChannel = inputChannel;
	}

	public MPQArchive getArchive() {
		return archive;
	}

	public SeekableByteChannel getInputChannel() {
		return inputChannel;
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) throws IOException {
		ArchivedFile file = null;
		try {
			file = archive.lookupHash2(new HashLookup(filepath));
		} catch (final MPQException exc) {
			if (exc.getMessage().equals("lookup not found")) {
				return null;
			} else {
				throw new IOException(exc);
			}
		}
		final ArchivedFileStream stream = new ArchivedFileStream(inputChannel, extractor, file);
		final InputStream newInputStream = Channels.newInputStream(stream);
		return newInputStream;
	}

	@Override
	public ByteBuffer read(final String path) throws IOException {
		ArchivedFile file = null;
		try {
			file = archive.lookupHash2(new HashLookup(path));
		} catch (final MPQException exc) {
			if (exc.getMessage().equals("lookup not found")) {
				return null;
			} else {
				throw new IOException(exc);
			}
		}
		try (final ArchivedFileStream stream = new ArchivedFileStream(inputChannel, extractor, file)) {
			final long size = stream.size();
			final ByteBuffer buffer = ByteBuffer.allocate((int) size);
			stream.read(buffer);
			return buffer;
		}
	}

	@Override
	public File getFile(final String filepath) throws IOException {
		ArchivedFile file = null;
		try {
			file = archive.lookupHash2(new HashLookup(filepath));
		} catch (final MPQException exc) {
			if (exc.getMessage().equals("lookup not found")) {
				return null;
			} else {
				throw new IOException(exc);
			}
		}
		final ArchivedFileStream stream = new ArchivedFileStream(inputChannel, extractor, file);
		final InputStream newInputStream = Channels.newInputStream(stream);
		String tmpdir = System.getProperty("java.io.tmpdir");
		if (!tmpdir.endsWith(File.separator)) {
			tmpdir += File.separator;
		}
		final String tempDir = tmpdir + "RMSExtract/";
		final File tempProduct = new File(tempDir + filepath.replace('\\', File.separatorChar));
		tempProduct.delete();
		tempProduct.getParentFile().mkdirs();
		Files.copy(newInputStream, tempProduct.toPath());
		tempProduct.deleteOnExit();
		return tempProduct;
	}

	@Override
	public boolean has(final String filepath) {
		try {
			archive.lookupPath(filepath);
			return true;
		} catch (final MPQException exc) {
			if (exc.getMessage().equals("lookup not found")) {
				return false;
			} else {
				throw new RuntimeException(exc);
			}
		}
	}

	@Override
	public boolean allowDownstreamCaching(final String filepath) {
		return true;
	}

	@Override
	public Collection<String> getListfile() {
		try {
			final Set<String> listfile = new HashSet<>();
			final ArchivedFile listfileContents;
			listfileContents = archive.lookupHash2(new HashLookup("(listfile)"));
			final ArchivedFileStream stream = new ArchivedFileStream(inputChannel, extractor, listfileContents);
			final InputStream newInputStream = Channels.newInputStream(stream);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(newInputStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					listfile.add(line);
				}
			} catch (final IOException exc) {
				throw new RuntimeException(exc);
			}
			return listfile;
		} catch (final MPQException exc) {
			if (exc.getMessage().equals("lookup not found")) {
				return null;
			} else {
				throw new RuntimeException(exc);
			}
		}
	}

	@Override
	public void close() throws IOException {
		inputChannel.close();
	}

}
