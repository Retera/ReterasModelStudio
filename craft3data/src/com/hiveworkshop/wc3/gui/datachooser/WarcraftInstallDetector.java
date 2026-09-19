package com.hiveworkshop.wc3.gui.datachooser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC.FileSystem;
import com.hiveworkshop.nio.ByteBufferInputStream;

/**
 * Headless detection of what a Warcraft III install folder contains and which
 * data sources should be layered to read it. Used by the data source chooser
 * and by command line tools, so it never opens a dialog: when the locale
 * cannot be determined it returns the candidates and lets the caller decide.
 */
public final class WarcraftInstallDetector {
	public enum PatchFormat {
		/** CASC, but still MPQ-named nested archives (1.30). */
		PATCH130,
		/** 1.31: war3.w3mod, locales as nested w3mods. */
		PATCH131,
		/** 1.32 to 2.0.x: adds the _hd.w3mod Reforged layer. */
		PATCH132,
		/** 3.0.0 "Forsaken Kingdom": adds the _de.w3mod Definitive Edition layer. */
		PATCH300,
		UNKNOWN
	}

	/** Result of probing a CASC install. */
	public static final class CascInstallInfo {
		public final PatchFormat patchFormat;
		/** The locale in use if it could be determined, else null. */
		public final String locale;
		/** Locales that exist in the install and have their string tables available. */
		public final List<String> availableLocales;
		public final String launcherDbLocale;
		public final String originalInstallLocale;

		CascInstallInfo(final PatchFormat patchFormat, final String locale, final List<String> availableLocales,
				final String launcherDbLocale, final String originalInstallLocale) {
			this.patchFormat = patchFormat;
			this.locale = locale;
			this.availableLocales = availableLocales;
			this.launcherDbLocale = launcherDbLocale;
			this.originalInstallLocale = originalInstallLocale;
		}
	}

	private static final String[] KNOWN_LOCALES = { "enUS", "deDE", "esES", "esMX", "frFR", "itIT", "koKR", "plPL",
			"ptBR", "ruRU", "zhCN", "zhTW" };
	private static final String[] MPQ_LOAD_ORDER = { "War3.mpq", "War3Local.mpq", "War3x.mpq", "War3xlocal.mpq",
			"War3Patch.mpq", "Deprecated.mpq" };

	private WarcraftInstallDetector() {
	}

	public static boolean isCascInstall(final Path installPath) {
		return Files.exists(installPath.resolve("Data/indices")) || Files.exists(installPath.resolve("Data/data"));
	}

	/**
	 * Case-insensitive lookup of a file in a folder. Game folders copied to Linux
	 * keep whatever casing the installer used ("war3.mpq" next to
	 * "War3Patch.mpq"), and Path.resolve is case sensitive there.
	 */
	public static Path findFileIgnoreCase(final Path folder, final String fileName) {
		final Path direct = folder.resolve(fileName);
		if (Files.exists(direct)) {
			return direct;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
			for (final Path candidate : stream) {
				if (candidate.getFileName().toString().equalsIgnoreCase(fileName)) {
					return candidate;
				}
			}
		} catch (final IOException e) {
			// treat as absent
		}
		return null;
	}

	/** The MPQ archives of a classic install, in load order (later overrides earlier). */
	public static List<Path> findClassicMpqs(final Path installPath) {
		final List<Path> result = new ArrayList<>();
		for (final String name : MPQ_LOAD_ORDER) {
			final Path mpq = findFileIgnoreCase(installPath, name);
			if (mpq != null) {
				result.add(mpq);
			}
		}
		return result;
	}

	public static List<DataSourceDescriptor> classicDescriptors(final Path installPath) {
		final List<DataSourceDescriptor> descriptors = new ArrayList<>();
		for (final Path mpq : findClassicMpqs(installPath)) {
			descriptors.add(new MpqDataSourceDescriptor(mpq.toString()));
		}
		return descriptors;
	}

	/**
	 * Probes a CASC install. The returned locale is non-null when exactly one
	 * sensible choice exists (the launcher's, the original install's, or the only
	 * available one), preferring enUS when several are available and nothing
	 * else decides.
	 */
	public static CascInstallInfo probeCasc(final Path installPath, final CascDataSource.Product product)
			throws IOException {
		String launcherDbLocale = null;
		try {
			final List<String> launcherDb = Files.readAllLines(installPath.resolve("Launcher.db"));
			if (!launcherDb.isEmpty() && (launcherDb.get(0).trim().length() == 4)) {
				launcherDbLocale = launcherDb.get(0).trim();
			}
		} catch (final Exception e) {
			// no launcher db
		}
		String originalInstallLocale = null;
		PatchFormat patchFormat;
		final List<String> available = new ArrayList<>();
		try (WarcraftIIICASC casc = new WarcraftIIICASC(installPath, true, product.getKey())) {
			try {
				final String tags = casc.getBuildInfo().getField(casc.getActiveRecordIndex(), "Tags");
				for (final String tag : tags.split("\\?")) {
					final String trimmed = tag.trim();
					final int space = trimmed.indexOf(' ');
					if (space != -1) {
						final String first = trimmed.substring(0, space);
						final String second = trimmed.substring(space + 1);
						if ((second.equals("speech") || second.equals("text")) && (first.length() == 4)) {
							originalInstallLocale = first;
						}
					}
				}
			} catch (final Exception e) {
				// tags are optional
			}
			final FileSystem root = casc.getRootFileSystem();
			if (root.isFile("war3.mpq\\units\\unitdata.slk")) {
				patchFormat = PatchFormat.PATCH130;
			} else if (root.isFile("war3.w3mod\\_de.w3mod\\units\\human\\footman\\footman.mdx")) {
				patchFormat = PatchFormat.PATCH300;
			} else if (root.isFile("war3.w3mod\\_hd.w3mod\\units\\human\\footman\\footman.mdx")) {
				patchFormat = PatchFormat.PATCH132;
			} else if (root.isFile("war3.w3mod\\units\\unitdata.slk")) {
				patchFormat = PatchFormat.PATCH131;
			} else {
				patchFormat = PatchFormat.UNKNOWN;
			}
			final Set<String> candidates = new LinkedHashSet<>();
			if (launcherDbLocale != null) {
				candidates.add(launcherDbLocale);
			}
			if (originalInstallLocale != null) {
				candidates.add(originalInstallLocale);
			}
			if (root.isFile("index") && root.isFileAvailable("index")) {
				final ByteBuffer buffer = root.readFileData("index");
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new ByteBufferInputStream(buffer)))) {
					String line;
					while ((line = reader.readLine()) != null) {
						final String[] split = line.split("\\|");
						if ((split.length >= 3) && (split[2].length() == 4)) {
							candidates.add(split[2]);
						}
					}
				}
			}
			candidates.addAll(Arrays.asList(KNOWN_LOCALES));
			for (final String candidate : candidates) {
				if (localeIsAvailable(root, patchFormat, candidate)) {
					available.add(candidate);
				}
			}
		}
		String locale = null;
		if (available.size() == 1) {
			locale = available.get(0);
		} else if (!available.isEmpty()) {
			if ((launcherDbLocale != null) && available.contains(launcherDbLocale)) {
				locale = launcherDbLocale;
			} else if ((originalInstallLocale != null) && available.contains(originalInstallLocale)) {
				locale = originalInstallLocale;
			} else if (available.contains("enUS")) {
				locale = "enUS";
			} else {
				locale = available.get(0);
			}
		}
		return new CascInstallInfo(patchFormat, locale, available, launcherDbLocale, originalInstallLocale);
	}

	private static boolean localeIsAvailable(final FileSystem root, final PatchFormat patchFormat,
			final String locale) throws IOException {
		final String lower = locale.toLowerCase(Locale.US);
		final String probe;
		switch (patchFormat) {
		case PATCH130:
			probe = lower + "-war3local.mpq\\units\\campaignunitstrings.txt";
			break;
		case PATCH131:
		case PATCH132:
		case PATCH300:
			probe = "war3.w3mod\\_locales\\" + lower + ".w3mod\\units\\campaignunitstrings.txt";
			break;
		default:
			return false;
		}
		return root.isFile(probe) && root.isFileAvailable(probe);
	}

	/**
	 * The default prefix layering for a patch and locale. The Reforged (HD) layer
	 * is included for 1.32+; the SD/HD/DE mode buttons in the chooser swap it.
	 */
	public static List<String> defaultPrefixes(final PatchFormat patchFormat, final String locale) {
		final String lower = locale.toLowerCase(Locale.US);
		switch (patchFormat) {
		case PATCH130:
			return new ArrayList<>(Arrays.asList("war3.mpq", "deprecated.mpq", lower + "-war3local.mpq"));
		case PATCH131:
			return new ArrayList<>(Arrays.asList("war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
					"war3.w3mod\\_locales\\" + lower + ".w3mod"));
		case PATCH132:
		case PATCH300:
			return new ArrayList<>(Arrays.asList("war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
					"war3.w3mod\\_locales\\" + lower + ".w3mod", "war3.w3mod\\_hd.w3mod",
					"war3.w3mod\\_hd.w3mod\\_locales\\" + lower + ".w3mod"));
		default:
			return new ArrayList<>(Arrays.asList("war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
					"war3.w3mod\\_locales\\" + lower + ".w3mod"));
		}
	}

	/** Prefixes for the 3.0 Definitive Edition graphics layer instead of the Reforged one. */
	public static List<String> definitiveEditionPrefixes(final String locale) {
		final String lower = locale.toLowerCase(Locale.US);
		return new ArrayList<>(Arrays.asList("war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
				"war3.w3mod\\_locales\\" + lower + ".w3mod", "war3.w3mod\\_de.w3mod",
				"war3.w3mod\\_de.w3mod\\_locales\\" + lower + ".w3mod"));
	}

	/**
	 * Descriptors for an install with no questions asked. For CASC installs the
	 * locale is auto-detected (enUS preferred); pass a non-null locale to force
	 * it.
	 *
	 * @throws IOException if the CASC storage cannot be read
	 */
	public static List<DataSourceDescriptor> detect(final Path installPath, final CascDataSource.Product product,
			final String forcedLocale) throws IOException {
		if (!isCascInstall(installPath)) {
			return classicDescriptors(installPath);
		}
		final CascInstallInfo info = probeCasc(installPath, product);
		String locale = forcedLocale != null ? forcedLocale : info.locale;
		if (locale == null) {
			locale = "enUS";
		}
		final CascDataSourceDescriptor descriptor = new CascDataSourceDescriptor(installPath.toString(),
				new ArrayList<String>(), product);
		for (final String prefix : defaultPrefixes(info.patchFormat, locale)) {
			descriptor.addPrefix(prefix);
		}
		final List<DataSourceDescriptor> descriptors = new ArrayList<>();
		descriptors.add(descriptor);
		return descriptors;
	}
}
