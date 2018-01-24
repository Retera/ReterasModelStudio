package com.hiveworkshop.lang;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Convenience class to represent formated strings that are localized. It is a
 * locale independent factory to produce locale dependent Strings. This class is
 * useful to pass around localized strings when the required locale is not
 * currently know. It can also be used to easily produce same meaning localized
 * strings for multiple different locale.
 * <p>
 * Creation is lightweight, with actual localization and formating occurring
 * when a localized string is requested. Although intended for formated strings,
 * it can also be used for non-formated strings.
 * 
 * @author Imperial Good
 */
public class LocalizedFormatedString {

	/**
	 * Name of resource bundle where String is located.
	 */
	private final String baseName;

	/**
	 * Name of String resource key to use.
	 */
	private final String key;

	/**
	 * Arguments to be formated into the locale specific strings, if any.
	 */
	private final Object[] arguments;

	/**
	 * Construct a formated localized String from a resource bundle name and
	 * String resource key with accompanying formating arguments. The existence
	 * of the referenced resource is not checked until it is requested as a
	 * string.
	 * 
	 * @param baseName
	 *            name of ResourceBundle to be used.
	 * @param keyword
	 *            name of specific string resource to use.
	 * @param args
	 *            arguments used to format string resource.
	 * @throws IllegalArgumentException
	 *             if baseName or keyword are null.
	 */
	public LocalizedFormatedString(String baseName, String keyword,
			Object... args) {
		if (baseName == null)
			throw new IllegalArgumentException("baseName is null.");
		else if (keyword == null)
			throw new IllegalArgumentException("keyword is null.");
		this.baseName = baseName;
		key = keyword;
		arguments = args;
	}

	/**
	 * Construct a localized String from a resource bundle name and String
	 * resource key. The existence of the referenced resource is not checked
	 * until it is requested as a string.
	 * 
	 * @param baseName
	 *            name of ResourceBundle to be used.
	 * @param keyword
	 *            name of specific string resource to use.
	 * @throws IllegalArgumentException
	 *             if baseName or keyword are null.
	 */
	public LocalizedFormatedString(String baseName, String keyword) {
		this(baseName, keyword, (Object[]) null);
	}

	/**
	 * Returns a localized String represented by this object in a specific
	 * Locale. The String is obtained by looking up the key in a ResourceBundle.
	 * If any arguments were specified they will be formated into the string.
	 * 
	 * @param locale
	 *            the locale the returned String will target.
	 * @return a localized String.
	 */
	public String toString(Locale locale) {
		String text;
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
			text = bundle.getString(key);
		} catch (MissingResourceException e) {
			text = String.format("missing resource {%S, %S, %S}", baseName,
					key, arguments);
		}

		if (arguments != null)
			text = String.format(text, arguments);
		return text;
	}

	@Override
	public String toString() {
		return toString(Locale.getDefault());
	}

}
