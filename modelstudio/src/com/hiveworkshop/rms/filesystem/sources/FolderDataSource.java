package com.hiveworkshop.rms.filesystem.sources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FolderDataSource implements DataSource {

	private final Path folderPath;
	private final Set<String> listfile;

	public FolderDataSource(final Path folderPath) {
		this.folderPath = folderPath;
		listfile = new HashSet<>();
		try {
			Files.walk(folderPath).filter(Files::isRegularFile).forEach(t -> listfile.add(folderPath.relativize(t).toString()));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getResourceAsStream(String filepath) throws IOException {
		filepath = filepath.replace(':', File.separatorChar);
		if (!has(filepath)) {
			return null;
		}
		return Files.newInputStream(folderPath.resolve(filepath), StandardOpenOption.READ);
	}

	@Override
	public File getFile(String filepath) throws IOException {
		filepath = filepath.replace(':', File.separatorChar);
		if (!has(filepath)) {
			return null;
		}
		return new File(folderPath.toString() + File.separatorChar + filepath);
	}

	@Override
	public ByteBuffer read(String path) throws IOException {
		path = path.replace(':', File.separatorChar);
		if (!has(path)) {
			return null;
		}
		return ByteBuffer.wrap(Files.readAllBytes(Paths.get(path)));
	}

	@Override
	public boolean has(String filepath) {
		filepath = filepath.replace(':', File.separatorChar);
		if ("".equals(filepath)) {
			return false; // special case for folder data source, dont do this
		}
		return Files.exists(folderPath.resolve(filepath));
	}

	@Override
	public boolean allowDownstreamCaching(final String filepath) {
		return false;
	}

	@Override
	public Collection<String> getListfile() {
		return listfile;
	}

	@Override
	public void close() {
	}

}
