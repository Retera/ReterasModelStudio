package com.hiveworkshop.wc3.gui.modeledit.wizards;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

/**
 * The game's event object tables (sounds, splats, ubersplats, footprints,
 * spawned models) read from the active data source, as (id, label) pairs for
 * the Add > Event Object browser. Cached per session; call {@link #dropCache}
 * when the data source changes.
 */
public final class EventObjectCatalog {
	public enum Kind {
		SOUND("SNDX", "Sound", "UI\\SoundInfo\\AnimLookups.slk"),
		SPLAT("SPLX", "Splat (blood, ground decal)", "Splats\\SplatData.slk"),
		UBERSPLAT("UBRX", "Ubersplat (building shadow, crater)", "Splats\\UberSplatData.slk"),
		FOOTPRINT("FPTX", "Footprint", "Splats\\SplatData.slk"),
		SPAWN("SPNX", "Spawn object (model)", "Splats\\SpawnData.slk");

		public final String prefix;
		public final String displayName;
		public final String slkPath;

		Kind(final String prefix, final String displayName, final String slkPath) {
			this.prefix = prefix;
			this.displayName = displayName;
			this.slkPath = slkPath;
		}

		public static Kind fromName(final String eventObjectName) {
			for (final Kind kind : values()) {
				if ((eventObjectName != null) && eventObjectName.startsWith(kind.prefix)) {
					return kind;
				}
			}
			return null;
		}
	}

	public static final class Entry {
		public final String id;
		public final String label;

		Entry(final String id, final String label) {
			this.id = id;
			this.label = label;
		}

		@Override
		public String toString() {
			return label == null || label.isEmpty() || label.equals(id) ? id : id + "  " + label;
		}
	}

	private static final Map<String, List<Entry>> CACHE = new HashMap<>();

	private EventObjectCatalog() {
	}

	public static void dropCache() {
		synchronized (CACHE) {
			CACHE.clear();
		}
	}

	public static List<Entry> entries(final Kind kind) {
		synchronized (CACHE) {
			List<Entry> entries = CACHE.get(kind.slkPath);
			if (entries == null) {
				entries = load(kind.slkPath, 1, null, 2);
				if (entries.isEmpty() && (kind == Kind.SOUND)) {
					// Reforged dropped AnimLookups.slk; AnimSounds.slk carries the codes instead
					entries = load("UI\\SoundInfo\\AnimSounds.slk", -1, "AnimationEventCode", 1);
				}
				CACHE.put(kind.slkPath, entries);
			}
			return entries;
		}
	}

	/**
	 * @param idColumn     1-based column of the 4 letter code, or -1 to find it by
	 *                     header name
	 * @param idHeader     header text of the code column when idColumn is -1
	 * @param labelColumn  1-based column of the human readable label
	 */
	private static List<Entry> load(final String path, final int idColumn, final String idHeader,
			final int labelColumn) {
		final List<Entry> entries = new ArrayList<>();
		try (InputStream stream = MpqCodebase.get().getResourceAsStream(path)) {
			if (stream == null) {
				return entries;
			}
			final Map<Integer, Map<Integer, String>> rows = readCells(stream);
			int idCol = idColumn;
			if ((idCol < 0) && (idHeader != null) && !rows.isEmpty()) {
				final Map<Integer, String> header = rows.values().iterator().next();
				for (final Map.Entry<Integer, String> cell : header.entrySet()) {
					if (idHeader.equalsIgnoreCase(cell.getValue())) {
						idCol = cell.getKey();
					}
				}
			}
			if (idCol < 0) {
				return entries;
			}
			for (final Map<Integer, String> row : rows.values()) {
				final String id = row.get(idCol);
				if ((id == null) || (id.length() != 4) || id.equals("INIT") || id.equals("TEST")) {
					continue;
				}
				String label = row.get(labelColumn);
				if (label == null) {
					label = "";
				}
				// spawn tables carry a model path; show the file name
				final int slash = label.lastIndexOf('\\');
				if (slash >= 0) {
					label = label.substring(slash + 1);
				}
				entries.add(new Entry(id, label));
			}
		} catch (final IOException | RuntimeException e) {
			System.err.println("Could not read " + path + ": " + e);
		}
		Collections.sort(entries, (a, b) -> a.id.compareTo(b.id));
		return entries;
	}

	/** Minimal SLK cell reader: row → (column → value). */
	static Map<Integer, Map<Integer, String>> readCells(final InputStream stream) throws IOException {
		final Map<Integer, Map<Integer, String>> rows = new TreeMap<>();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.ISO_8859_1));
		String line;
		int x = 0;
		int y = 0;
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("C;")) {
				continue;
			}
			String value = null;
			for (final String part : line.substring(2).split(";")) {
				if (part.isEmpty()) {
					continue;
				}
				switch (part.charAt(0)) {
				case 'X':
					x = parseInt(part.substring(1), x);
					break;
				case 'Y':
					y = parseInt(part.substring(1), y);
					break;
				case 'K':
					value = part.substring(1);
					if ((value.length() >= 2) && value.startsWith("\"") && value.endsWith("\"")) {
						value = value.substring(1, value.length() - 1);
					}
					break;
				default:
					break;
				}
			}
			if (value != null) {
				rows.computeIfAbsent(y, k -> new HashMap<>()).put(x, value);
			}
		}
		return rows;
	}

	private static int parseInt(final String text, final int fallback) {
		try {
			return Integer.parseInt(text.trim());
		} catch (final NumberFormatException e) {
			return fallback;
		}
	}
}
