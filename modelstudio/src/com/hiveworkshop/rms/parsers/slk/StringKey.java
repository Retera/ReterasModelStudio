package com.hiveworkshop.rms.parsers.slk;

/**
 * A hashable wrapper object for a String that can be used as the key in a hashtable, but which disregards case as a key
 * -- except that it will remember case if directly asked for its value. The game needs this to be able to show the
 * original case of a string to the user in the editor, while still doing map lookups in a case insensitive way.
 *
 * @author Eric
 *
 */
public final class StringKey {
	private final String string;

	public StringKey(final String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (string == null ? 0 : string.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof StringKey) {
			final StringKey other = (StringKey) obj;
			if (string == null) {
				return other.string == null;
			} else {
				return string.equalsIgnoreCase(other.string);
			}
		}
		return false;
	}
}