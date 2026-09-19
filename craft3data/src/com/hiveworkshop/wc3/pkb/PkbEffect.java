package com.hiveworkshop.wc3.pkb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A parsed baked PopcornFX effect: header, string table and object list. */
public final class PkbEffect {
	public final int versionMajor;
	public final int versionMinor;
	public final int versionPatch;
	public final long revisionId;
	public final int generator;
	private final List<String> strings;
	private final List<PkbObject> objects;

	PkbEffect(final int versionMajor, final int versionMinor, final int versionPatch, final long revisionId,
			final int generator, final List<String> strings, final List<PkbObject> objects) {
		this.versionMajor = versionMajor;
		this.versionMinor = versionMinor;
		this.versionPatch = versionPatch;
		this.revisionId = revisionId;
		this.generator = generator;
		this.strings = Collections.unmodifiableList(strings);
		this.objects = Collections.unmodifiableList(objects);
	}

	public List<String> getStrings() {
		return strings;
	}

	public List<PkbObject> getObjects() {
		return objects;
	}

	/** Object at a 0-based index (what {@link PkbValue#asLink()} returns), or null. */
	public PkbObject object(final int index) {
		return ((index < 0) || (index >= objects.size())) ? null : objects.get(index);
	}

	public PkbObject follow(final PkbObject from, final String linkField) {
		return from == null ? null : object(from.getLink(linkField));
	}

	public List<PkbObject> followAll(final PkbObject from, final String linkArrayField) {
		final List<PkbObject> out = new ArrayList<>();
		if (from != null) {
			for (final int index : from.getLinks(linkArrayField)) {
				final PkbObject target = object(index);
				if (target != null) {
					out.add(target);
				}
			}
		}
		return out;
	}

	public PkbObject firstOfType(final String type) {
		for (final PkbObject object : objects) {
			if (object.type.equals(type)) {
				return object;
			}
		}
		return null;
	}

	public List<PkbObject> allOfType(final String type) {
		final List<PkbObject> out = new ArrayList<>();
		for (final PkbObject object : objects) {
			if (object.type.equals(type)) {
				out.add(object);
			}
		}
		return out;
	}

	public PkbObject getRootEffect() {
		return firstOfType("CParticleEffect");
	}
}
