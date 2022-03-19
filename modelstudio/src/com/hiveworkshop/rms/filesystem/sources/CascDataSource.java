package com.hiveworkshop.rms.filesystem.sources;

import com.hiveworkshop.blizzard.casc.io.WC3CascFileSystem;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CascDataSource implements DataSource {
	private final String[] prefixes;
	private final WarcraftIIICASC warcraftIIICASC;
	private final WC3CascFileSystem rootFileSystem;
	private final List<String> listFile;
	private final Map<String, String> fileAliases;

	public CascDataSource(final String warcraft3InstallPath, final String[] prefixes) {
		System.out.println("Creating CascDataSource; " + warcraft3InstallPath + ", " + Arrays.toString(prefixes));
		this.prefixes = prefixes;
		for (int i = 0; i < (prefixes.length / 2); i++) {
			final String temp = prefixes[i];
			prefixes[i] = prefixes[prefixes.length - i - 1];
			prefixes[prefixes.length - i - 1] = temp;
		}

		try {
			warcraftIIICASC = new WarcraftIIICASC(Paths.get(warcraft3InstallPath), true);
			rootFileSystem = warcraftIIICASC.getRootFileSystem();
			listFile = rootFileSystem.enumerateFiles();
			fileAliases = new HashMap<>();
			if (has("filealiases.json")) {
//				System.out.println("has filealiases.json");
				try (InputStream stream = getResourceAsStream("filealiases.json")) {
					stream.mark(4);
					if ('\ufeff' != stream.read()) {
						stream.reset(); // not the BOM marker
					}
					final JSONArray jsonObject = new JSONArray(new JSONTokener(stream));
					System.out.println("jsonObject.length: " + jsonObject.length());
					for (int i = 0; i < jsonObject.length(); i++) {
						final JSONObject alias = jsonObject.getJSONObject(i);
						final String src = alias.getString("src").toLowerCase(Locale.US).replace('/', '\\');
						final String dest = alias.getString("dest").toLowerCase(Locale.US).replace('/', '\\');
//						if(src.endsWith(".html")){
//							System.out.println("adding: \"" + src + "\", as: \"" + dest + "\"");
//						}
						fileAliases.put(src, dest);
						addAsDDS(alias, src, dest);
					}
				}
			} else {
				System.out.println("no filealiases.json");
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addAsDDS(JSONObject alias, String src, String dest) {
		if ((src.contains(".blp") || dest.contains(".blp"))
				&& (!alias.has("assetType") || "Texture".equals(alias.getString("assetType")))) {
			// This case: I saw a texture that resolves in game but was failing in our code here, because of this entry:
			// {"src":"Units/Human/WarWagon/SiegeEngine.blp",
			// "dest":"Textures/Steamtank.blp",
			// "assetType": "Texture"},
			// Our repo here checks BLP then DDS at a high-up application level thing, and the problem is that
			// this entry is written using .BLP but we must be able to resolve .DDS when we go to look it up.
			// The actual model is .BLP so maybe that's how the game does it,
			// but my alias mapping is happening after the .BLP->.DDS dynamic fix, and not before.
			fileAliases.put(src.replace(".blp", ".dds"), dest.replace(".blp", ".dds"));
		}
	}

	@Override
	public InputStream getResourceAsStream(String filepath) {
		String fp = getFormattedPath(filepath);
		final String resolvedAlias = fileAliases.get(fp);
		if (resolvedAlias != null) {
			fp = resolvedAlias;
		}
//		if (filepath.endsWith(".html")){
//			System.out.println("Casc#GRaS1: checking for file: \"" + filepath + "\", resolvedAlias: \"" + resolvedAlias + "\"");
//		}
		for (final String prefix : prefixes) {
			final String tempFilepath = prefix + "\\" + fp;
			final InputStream stream = internalGetResourceAsStream(tempFilepath);
//			if (filepath.endsWith(".html")){
//				System.out.println("Casc#GRaS2: checking for file: \"" + tempFilepath + "\", stream: \"" + stream + "\"");
//			}
			if (stream != null) {
				return stream;
			}
		}
		return internalGetResourceAsStream(fp);
	}

	private InputStream internalGetResourceAsStream(final String tempFilepath) {
		try {

//			if (tempFilepath.endsWith(".html")){
//				System.out.println("Casc#GRaS1: checking for file: \"" + tempFilepath + "\", is file: " + rootFileSystem.isFile(tempFilepath));
//				if(rootFileSystem.isFile(tempFilepath)){
//					System.out.println("is availible: " + rootFileSystem.isFileAvailable(tempFilepath));
//				}
//			}
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
	public ByteBuffer read(String path) {
		path = getFormattedPath(path);
		final String resolvedAlias = fileAliases.get(path);
		if (resolvedAlias != null) {
			path = resolvedAlias;
		}
		for (final String prefix : prefixes) {
			final String tempFilepath = prefix + "\\" + path;
			final ByteBuffer stream = internalRead(tempFilepath);
			if (stream != null) {
				return stream;
			}
		}
		return internalRead(path);
	}

	private ByteBuffer internalRead(final String tempFilepath) {
		try {
			if (rootFileSystem.isFile(tempFilepath) && rootFileSystem.isFileAvailable(tempFilepath)) {
				return rootFileSystem.readFileData(tempFilepath);
			}
		} catch (final IOException e) {
			throw new RuntimeException("CASC parser error for: " + tempFilepath, e);
		}
		return null;
	}

	@Override
	public File getFile(String filepath) {
		filepath = getFormattedPath(filepath);
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
		filepath = getFormattedPath(filepath);
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

	private String getFormattedPath(String filepath) {
		return filepath.toLowerCase(Locale.US).replace('/', '\\').replace(':', '\\');
	}

	@Override
	public boolean allowDownstreamCaching(final String filepath) {
		return true;
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
