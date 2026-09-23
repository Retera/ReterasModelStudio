package com.hiveworkshop.wc3.gui;

import java.util.Locale;

import com.hiveworkshop.wc3.user.SaveProfile;

/**
 * Which code path turns a .mdx/.mdl file into an {@code EditableModel}. The classic parser is this program's own
 * {@code wc3.mdl} text reader and {@code wc3.mdx} chunk reader; the Warsmash-derived parser is the
 * {@code com.hiveworkshop.rms.parsers.mdlx} package copied from tw1lac's fork (a port of Ghostwolf's mdx-m3-viewer),
 * bridged into the classic object model through binary MDX. The system property {@value #SYSTEM_PROPERTY}
 * ({@code classic}, {@code warsmash} or {@code fallback}) overrides the saved preference, which the headless
 * {@code -convert} command line uses as {@code --parser=warsmash}.
 */
public enum ModelParserPreference {
	CLASSIC("Classic (built-in)"),
	WARSMASH("Warsmash-derived (TRMS fork parser)"),
	CLASSIC_THEN_WARSMASH("Classic, fall back to Warsmash-derived on failure");

	public static final String SYSTEM_PROPERTY = "rms.modelParser";
	public static final String CLI_FLAG_PREFIX = "--parser=";

	private final String label;

	ModelParserPreference(final String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

	public static ModelParserPreference fromOrdinal(final Integer ordinal) {
		if ((ordinal == null) || (ordinal < 0) || (ordinal >= values().length)) {
			return CLASSIC;
		}
		return values()[ordinal];
	}

	public static ModelParserPreference fromKeyword(final String keyword) {
		if (keyword == null) {
			return null;
		}
		switch (keyword.trim().toLowerCase(Locale.ROOT)) {
		case "classic":
		case "builtin":
		case "built-in":
			return CLASSIC;
		case "warsmash":
		case "trms":
		case "mdlx":
			return WARSMASH;
		case "fallback":
		case "auto":
			return CLASSIC_THEN_WARSMASH;
		default:
			return null;
		}
	}

	/** The parser to use right now: the system property when set, else the saved preference, else classic. */
	public static ModelParserPreference current() {
		final ModelParserPreference override = fromKeyword(System.getProperty(SYSTEM_PROPERTY));
		if (override != null) {
			return override;
		}
		try {
			final ProgramPreferences preferences = SaveProfile.get().getPreferences();
			return preferences == null ? CLASSIC : preferences.getModelParser();
		}
		catch (final Throwable t) {
			return CLASSIC;
		}
	}

	/**
	 * Removes a {@code --parser=<keyword>} argument from a command line, applying it as the system property.
	 *
	 * @return the remaining arguments
	 */
	public static String[] stripCommandLineFlag(final String[] args) {
		final java.util.List<String> remaining = new java.util.ArrayList<>();
		for (final String arg : args) {
			if ((arg != null) && arg.startsWith(CLI_FLAG_PREFIX)) {
				final String keyword = arg.substring(CLI_FLAG_PREFIX.length());
				if (fromKeyword(keyword) == null) {
					System.err.println("Unknown " + CLI_FLAG_PREFIX + " value '" + keyword
							+ "', expected classic, warsmash or fallback");
				}
				else {
					System.setProperty(SYSTEM_PROPERTY, keyword);
				}
			}
			else {
				remaining.add(arg);
			}
		}
		return remaining.toArray(new String[0]);
	}
}
