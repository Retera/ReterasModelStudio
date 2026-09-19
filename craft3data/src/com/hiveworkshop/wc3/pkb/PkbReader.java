package com.hiveworkshop.wc3.pkb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a baked PopcornFX effect (".pkb", the container Warcraft III Reforged
 * uses for popcorn emitters).
 * <p>
 * Layout: a 28 byte header (magic 11 0B 00, version byte CA, version
 * major/minor/patch, generator, revision id, object count, type count, string
 * table offset), a type table of (string id, unused) pairs, then each object as
 * a u32 body size followed by flags u8, type index u32, field count u16 and
 * the fields (field id u16 + value bytes whose size follows from the class
 * schema), and finally the string table of Pascal strings. The container is
 * decoded here; what the objects mean is up to the caller.
 * <p>
 * Format knowledge comes from the WhiteoutFlakes project (BSD-3-Clause,
 * Copyright (c) 2026 Fernando Sahmkow).
 */
public final class PkbReader {
	private static final int HEADER_BYTES = 28;
	private static final int MAGIC_0 = 0x11;
	private static final int MAGIC_1 = 0x0B;
	private static final int MAGIC_2 = 0x00;
	private static final int CURRENT_VERSION_BYTE = 0xCA;

	private PkbReader() {
	}

	public static boolean looksLikePkb(final byte[] data) {
		return (data.length >= 4) && ((data[0] & 0xFF) == MAGIC_0) && ((data[1] & 0xFF) == MAGIC_1)
				&& ((data[2] & 0xFF) == MAGIC_2);
	}

	public static PkbEffect read(final InputStream stream) throws IOException {
		return read(stream.readAllBytes());
	}

	public static PkbEffect read(final byte[] data) throws IOException {
		if (data.length < HEADER_BYTES) {
			throw new IOException("PKB shorter than its 28 byte header");
		}
		if (!looksLikePkb(data)) {
			throw new IOException("not a PKB file (bad magic)");
		}
		final ByteBuffer in = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		final int versionByte = in.get(3) & 0xFF;
		if (versionByte != CURRENT_VERSION_BYTE) {
			throw new IOException(String.format("unsupported PKB version byte 0x%02X (expected 0xCA)", versionByte));
		}
		final int major = in.get(4) & 0xFF;
		final int minor = in.get(5) & 0xFF;
		final int patch = in.get(6) & 0xFF;
		final int generator = in.get(7) & 0xFF;
		final long revision = in.getInt(8) & 0xFFFFFFFFL;
		final int objectCount = in.getInt(12);
		final int typeCount = in.getInt(16);
		final int stringTableOffset = in.getInt(20);
		if ((stringTableOffset < 0) || (stringTableOffset >= data.length)) {
			throw new IOException("PKB string table offset out of range");
		}

		final List<String> strings = new ArrayList<>();
		int offset = stringTableOffset;
		final int stringCount = in.getInt(offset);
		offset += 4;
		for (int i = 0; i < stringCount; i++) {
			final int b0 = data[offset] & 0xFF;
			int length;
			if ((b0 & 0x80) == 0) {
				length = b0;
				offset += 1;
			} else {
				length = ((b0 & 0x7F) << 8) | (data[offset + 1] & 0xFF);
				offset += 2;
			}
			if ((offset + length) > data.length) {
				throw new IOException("PKB string runs past the end of the file");
			}
			strings.add(new String(data, offset, length, StandardCharsets.UTF_8));
			offset += length;
		}

		final List<String> typeNames = new ArrayList<>();
		offset = HEADER_BYTES;
		for (int i = 0; i < typeCount; i++) {
			final int nameId = in.getInt(offset);
			offset += 8;
			if ((nameId < 0) || (nameId >= strings.size())) {
				throw new IOException("PKB type table references string " + nameId + " of " + strings.size());
			}
			typeNames.add(strings.get(nameId));
		}

		final PkbSchema schema = PkbSchema.forVersion(major, minor);
		final List<PkbObject> objects = new ArrayList<>();
		for (int i = 0; i < objectCount; i++) {
			if ((offset + 4) > stringTableOffset) {
				throw new IOException("PKB object " + i + " header runs into the string table");
			}
			final int bodySize = in.getInt(offset);
			offset += 4;
			final int bodyStart = offset;
			final int bodyEnd = bodyStart + bodySize;
			if ((bodyEnd > stringTableOffset) || (bodySize < 7)) {
				throw new IOException("PKB object " + i + " body of " + bodySize + " bytes is out of range");
			}
			final int flags = data[bodyStart] & 0xFF;
			final int typeIndex = in.getInt(bodyStart + 1);
			final int fieldCount = in.getShort(bodyStart + 5) & 0xFFFF;
			if ((typeIndex < 0) || (typeIndex >= typeNames.size())) {
				throw new IOException("PKB object " + i + " has type index " + typeIndex);
			}
			final String type = typeNames.get(typeIndex);
			final PkbObject object = new PkbObject(i, type, flags);
			final List<PkbSchema.FieldDef> fields = schema.fieldsOf(type);
			int cursor = bodyStart + 7;
			for (int f = 0; f < fieldCount; f++) {
				if ((cursor + 2) > bodyEnd) {
					throw new IOException("PKB object " + i + " (" + type + ") truncated at field " + f);
				}
				final int fieldId = in.getShort(cursor) & 0xFFFF;
				cursor += 2;
				if ((fields == null) || (fieldId >= fields.size())) {
					// unknown class or field: the value size is unknowable, stop decoding this object
					break;
				}
				final PkbSchema.FieldDef def = fields.get(fieldId);
				final int size = valueSize(def.type, data, in, cursor, bodyEnd);
				if ((size <= 0) || ((cursor + size) > bodyEnd)) {
					throw new IOException("PKB object " + i + " (" + type + ") field " + def.name + " of type "
							+ def.type + " cannot be sized");
				}
				final byte[] bytes = new byte[size];
				System.arraycopy(data, cursor, bytes, 0, size);
				object.add(new PkbValue(def.name, def.type, bytes, resolveStrings(def.type, in, cursor, strings)));
				cursor += size;
			}
			objects.add(object);
			offset = bodyEnd;
		}
		return new PkbEffect(major, minor, patch, revision, generator, strings, objects);
	}

	private static List<String> resolveStrings(final String type, final ByteBuffer in, final int cursor,
			final List<String> strings) {
		if (type.equals("string") || type.equals("string_unicode")) {
			final List<String> out = new ArrayList<>(1);
			out.add(stringAt(in.getInt(cursor), strings));
			return out;
		}
		if (type.equals("string[]") || type.equals("string_unicode[]")) {
			final int count = in.getInt(cursor);
			final List<String> out = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				out.add(stringAt(in.getInt(cursor + 4 + (i * 4)), strings));
			}
			return out;
		}
		return null;
	}

	private static String stringAt(final int index, final List<String> strings) {
		if ((index == -1) || (index < 0) || (index >= strings.size())) {
			return "";
		}
		return strings.get(index);
	}

	private static int scalarBytes(final String type) {
		switch (type) {
		case "bool":
			return 1;
		case "int":
		case "uint":
		case "float":
		case "link":
		case "string":
		case "string_unicode":
		case "unknown":
			return 4;
		case "string_localized":
			return 0; // variable, handled by elementBytes
		default:
			break;
		}
		final int dim = PkbValue.dimensionOf(type);
		if (dim > 1) {
			final String prefix = type.substring(0, type.length() - 1);
			if (prefix.equals("bool")) {
				return dim;
			}
			if (prefix.equals("int") || prefix.equals("uint") || prefix.equals("float")) {
				return dim * 4;
			}
		}
		return 0;
	}

	private static int elementBytes(final String baseType, final ByteBuffer in, final int offset, final int end) {
		if (baseType.equals("string_localized")) {
			if ((offset + 4) > end) {
				return 0;
			}
			final int n = in.getInt(offset);
			return 4 + (n * 8);
		}
		return scalarBytes(baseType);
	}

	private static int valueSize(final String type, final byte[] data, final ByteBuffer in, final int offset,
			final int end) {
		if (!type.endsWith("[]")) {
			return elementBytes(type, in, offset, end);
		}
		if ((offset + 4) > end) {
			return 0;
		}
		final int count = in.getInt(offset);
		final String base = type.substring(0, type.length() - 2);
		int total = 4;
		int cursor = offset + 4;
		for (int i = 0; i < count; i++) {
			final int size = elementBytes(base, in, cursor, end);
			if (size <= 0) {
				return 0;
			}
			cursor += size;
			total += size;
		}
		return total;
	}
}
