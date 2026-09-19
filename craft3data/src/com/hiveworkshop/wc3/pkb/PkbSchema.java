package com.hiveworkshop.wc3.pkb;

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

/**
 * Field layout of every PopcornFX HBO class a baked effect can contain: field
 * names and types in field-id order. Loaded from {@code res/pkb/hbo_fields_*.txt}
 * (tables derived from the WhiteoutFlakes project, BSD-3-Clause).
 */
public final class PkbSchema {
	public static final class FieldDef {
		public final String name;
		public final String type;

		FieldDef(final String name, final String type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			return name + ":" + type;
		}
	}

	private static PkbSchema v25;
	private static PkbSchema v29;

	private final Map<String, List<FieldDef>> classes = new HashMap<>();

	private PkbSchema() {
	}

	/** Schema for a baked file version (2.9 and later differ from 2.5). */
	public static synchronized PkbSchema forVersion(final int major, final int minor) {
		final boolean newer = (major > 2) || ((major == 2) && (minor >= 9));
		if (newer) {
			if (v29 == null) {
				v29 = load("/pkb/hbo_fields_v29.txt");
			}
			return v29;
		}
		if (v25 == null) {
			v25 = load("/pkb/hbo_fields_v25.txt");
		}
		return v25;
	}

	private static PkbSchema load(final String resource) {
		final PkbSchema schema = new PkbSchema();
		try (InputStream stream = PkbSchema.class.getResourceAsStream(resource)) {
			if (stream == null) {
				throw new IllegalStateException("missing schema resource " + resource);
			}
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				final String[] parts = line.split("\t", -1);
				final List<FieldDef> fields = schema.classes.computeIfAbsent(parts[0], k -> new ArrayList<>());
				if ((parts.length >= 3) && !parts[1].isEmpty()) {
					fields.add(new FieldDef(parts[1], parts[2]));
				}
			}
		} catch (final IOException e) {
			throw new IllegalStateException("cannot read schema resource " + resource, e);
		}
		return schema;
	}

	/** Fields of a class, or null when the class is unknown to this schema. */
	public List<FieldDef> fieldsOf(final String className) {
		final List<FieldDef> fields = classes.get(className);
		return fields == null ? null : Collections.unmodifiableList(fields);
	}

	public boolean knows(final String className) {
		return classes.containsKey(className);
	}
}
