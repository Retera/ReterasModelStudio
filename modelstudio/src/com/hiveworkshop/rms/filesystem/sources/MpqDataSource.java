package com.hiveworkshop.rms.filesystem.sources;

import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MpqFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;

public class MpqDataSource implements DataSource {

	private final JMpqEditor archive;

	public MpqDataSource(final JMpqEditor archive) {
		this.archive = archive;
	}

	public JMpqEditor getArchive() {
		return archive;
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) throws IOException {
		MpqFile file = getMpqFile(filepath);
		if (file == null) return null;
		return new ByteArrayInputStream(file.extractToBytes());
	}

	@Override
	public ByteBuffer read(final String path) throws IOException {
		MpqFile file = getMpqFile(path);

		return ByteBuffer.wrap(file.extractToBytes());
	}

	@Override
	public File getFile(final String filepath) throws IOException {
		MpqFile file = getMpqFile(filepath);
		if (file == null) {
			return null;
		}

		String tmpdir = System.getProperty("java.io.tmpdir");
		if (!tmpdir.endsWith(File.separator)) {
			tmpdir += File.separator;
		}
		final String tempDir = tmpdir + "RMSExtract/";
		final File tempProduct = new File(tempDir + filepath.replace('\\', File.separatorChar));
		tempProduct.delete();
		tempProduct.getParentFile().mkdirs();
		file.extractToFile(tempProduct);
		tempProduct.deleteOnExit();
		return tempProduct;
	}

	private MpqFile getMpqFile(String filepath) throws IOException {
		MpqFile file;
		try {
			file = archive.getMpqFile(filepath);
		} catch (final Exception exc) {
			if (exc.getMessage().startsWith("File Not Found")) {
				return null;
			} else {
				throw new IOException(exc);
			}
		}
		return file;
	}

	@Override
	public boolean has(final String filepath) {
		return archive.hasFile(filepath);
	}

	@Override
	public boolean allowDownstreamCaching(final String filepath) {
		return true;
	}

	@Override
	public Collection<String> getListfile() {
		return archive.getFileNames();
	}

	@Override
	public void close() throws IOException {
		archive.close();
	}

}
