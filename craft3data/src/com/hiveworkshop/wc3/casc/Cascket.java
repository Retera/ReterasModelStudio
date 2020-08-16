package com.hiveworkshop.wc3.casc;

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
import com.hiveworkshop.blizzard.casc.vfs.VirtualFileSystem.PathResult;
import com.hiveworkshop.json.JSONArray;
import com.hiveworkshop.json.JSONObject;
import com.hiveworkshop.json.JSONTokener;
import com.hiveworkshop.wc3.mpq.Codebase;
import com.hiveworkshop.wc3.user.WindowsRegistry;

/**
 * CASC was dangerously close to put Matrix Eater in a casket, so we named it
 * Cascket. Put your casc in a basket
 *
 * @author etheller
 *
 */
public class Cascket implements Codebase {
	private final Map<String, PathResult> pathToResult = new HashMap<>();
	private final String[] prefixes;
	private WarcraftIIICASC warcraftIIICASC;
	private FileSystem rootFileSystem;
	private List<String> listFile;
	private Map<String, String> fileAliases;

	public static String getLocalization() {
		try {
			final String readRegistry = WindowsRegistry.readRegistry(
					"HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Classic Launcher\\w3", "Locale");
			if ((readRegistry == null) || (readRegistry.length() < 1)) {
				return "enUS";
			}
			return readRegistry;
		} catch (final Exception exc) {
			return "enUS";
		}
//		return "eses";
	}

	public String predictLocalization() {
//		String tags = warcraftIIICASC.getBuildInfo().getField(warcraftIIICASC.getActiveRecordIndex(), "Tags");
//		String[] splitTags = tags.split("\\?");
//		for (String splitTag : splitTags) {
//			String trimmedTag = splitTag.trim();
//			int spaceIndex = trimmedTag.indexOf(' ');
//			if (spaceIndex != -1) {
//				String firstPart = trimmedTag.substring(0, spaceIndex);
//				String secondPart = trimmedTag.substring(spaceIndex + 1);
//				if (secondPart.equals("speech") || secondPart.equals("text")) {
//					return firstPart;
//				}
//			}
//		}
		return "enUS";
	}

	public Cascket(final String warcraft3InstallPath) {

		// final var localIndexFile = Paths.get("C:\\Program Files (x86)\\StarCraft
		// II\\SC2Data\\data\\0000000139.idx");
		try {
			warcraftIIICASC = new WarcraftIIICASC(Paths.get(warcraft3InstallPath), true);
			rootFileSystem = warcraftIIICASC.getRootFileSystem();
			listFile = rootFileSystem.enumerateFiles();
			final String locale = predictLocalization();
			prefixes = new String[] { "war3.mpq", "deprecated.mpq", locale + "-war3local.mpq", "war3.w3mod",
					"deprecated.w3mod", "war3.w3mod\\_locales\\" + locale + ".w3mod" };
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

	public Collection<String> getListfile() {
		return listFile;
	}

}
