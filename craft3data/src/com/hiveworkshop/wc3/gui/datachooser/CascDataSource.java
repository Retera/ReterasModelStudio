package com.hiveworkshop.wc3.gui.datachooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC.FileSystem;
import com.hiveworkshop.json.JSONArray;
import com.hiveworkshop.json.JSONObject;
import com.hiveworkshop.json.JSONTokener;

public class CascDataSource implements DataSource {
	private final String[] prefixes;
	private WarcraftIIICASC warcraftIIICASC;
	private FileSystem rootFileSystem;
	private List<String> listFile;
	private Map<String, String> fileAliases;

	public CascDataSource(final String warcraft3InstallPath, final String[] prefixes) {
		this.prefixes = prefixes;

		try {
			warcraftIIICASC = new WarcraftIIICASC(Paths.get(warcraft3InstallPath), true);
			rootFileSystem = warcraftIIICASC.getRootFileSystem();
			listFile = rootFileSystem.enumerateFiles();
			fileAliases = new HashMap<>();
			if (this.has("filealiases.json")) {
				try (InputStream stream = this.getResourceAsStream("filealiases.json")) {
					stream.mark(4);
					if ('\ufeff' != stream.read()) {
						stream.reset(); // not the BOM marker
					}
					final JSONArray jsonObject = new JSONArray(new JSONTokener(stream));
					for (int i = 0; i < jsonObject.length(); i++) {
						final JSONObject alias = jsonObject.getJSONObject(i);
						final String src = alias.getString("src");
						final String dest = alias.getString("dest");
						fileAliases.put(src.toLowerCase().replace('/', '\\'), dest.toLowerCase().replace('/', '\\'));
					}
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getResourceAsStream(String filepath) {
		filepath = filepath.toLowerCase();
		final String resolvedAlias = fileAliases.get(filepath);
		if (resolvedAlias != null) {
			filepath = resolvedAlias;
		}
		for (final String prefix : prefixes) {
			final String tempFilepath = prefix + "\\" + filepath;
			final InputStream stream = internalGetResourceAsStream(tempFilepath);
			if (stream != null) {
				return stream;
			}
		}
		return internalGetResourceAsStream(filepath);
	}

	private InputStream internalGetResourceAsStream(final String tempFilepath) {
		try {
			if (rootFileSystem.isFile(tempFilepath) && rootFileSystem.isFileAvailable(tempFilepath)) {
				final ByteBuffer buffer = rootFileSystem.readFileData(tempFilepath);
				if (buffer.hasArray()) {
					return new ByteArrayInputStream(buffer.array());
				}
				final byte[] data = new byte[buffer.remaining()];
				buffer.clear();
				buffer.get(data);
				return new ByteArrayInputStream(data);
			}
		} catch (final IOException e) {
			throw new RuntimeException("CASC parser error for: " + tempFilepath, e);
		}
		return null;
	}

	@Override
	public File getFile(String filepath) {
		filepath = filepath.toLowerCase();
		final String resolvedAlias = fileAliases.get(filepath);
		if (resolvedAlias != null) {
			filepath = resolvedAlias;
		}
		for (final String prefix : prefixes) {
			final String tempFilepath = prefix + "\\" + filepath;
			final File file = internalGetFile(tempFilepath);
			if (file != null) {
				return file;
			}
		}
		return internalGetFile(filepath);
	}

	private File internalGetFile(final String tempFilepath) {
		try {
			if (rootFileSystem.isFile(tempFilepath) && rootFileSystem.isFileAvailable(tempFilepath)) {
				final ByteBuffer buffer = rootFileSystem.readFileData(tempFilepath);
				String tmpdir = System.getProperty("java.io.tmpdir");
				if (!tmpdir.endsWith(File.separator)) {
					tmpdir += File.separator;
				}
				final String tempDir = tmpdir + "MatrixEaterExtract/";
				final File tempProduct = new File(tempDir + tempFilepath.replace('\\', File.separatorChar));
				tempProduct.delete();
				tempProduct.getParentFile().mkdirs();
				try (final FileChannel fileChannel = FileChannel.open(tempProduct.toPath(), StandardOpenOption.CREATE,
						StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING)) {
					fileChannel.write(buffer);
				}
				tempProduct.deleteOnExit();
				return tempProduct;
			}
		} catch (final IOException e) {
			throw new RuntimeException("CASC parser error for: " + tempFilepath, e);
		}
		return null;
	}

	@Override
	public boolean has(String filepath) {
		filepath = filepath.toLowerCase();
		final String resolvedAlias = fileAliases.get(filepath);
		if (resolvedAlias != null) {
			filepath = resolvedAlias;
		}
		for (final String prefix : prefixes) {
			final String tempFilepath = prefix + "\\" + filepath;
			try {
				if (rootFileSystem.isFile(tempFilepath)) {
					return true;
				}
			} catch (final IOException e) {
				throw new RuntimeException("CASC parser error for: " + tempFilepath, e);
			}
		}
		try {
			return rootFileSystem.isFile(filepath);
		} catch (final IOException e) {
			throw new RuntimeException("CASC parser error for: " + filepath, e);
		}
	}

	@Override
	public Collection<String> getListfile() {
		return listFile;
	}

	@Override
	public void close() throws IOException {
		warcraftIIICASC.close();
	}

}
