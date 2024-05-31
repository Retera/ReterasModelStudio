package com.hiveworkshop.rms.ui.application.model.nodepanels;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TwiSearchFilter {
	private final static String escapeChar = "\\\\";
	private final static String regexEscapeString;
	static {
		char backSlash = '\\';
		char[] regexEscapes = new char[] {'(', ')', '[', ']', '{', '}', '^', '|', '\\', '+', '?', '.', '*', '&', '/', '<'};
		StringBuilder sb = new StringBuilder();
		for (char c : regexEscapes) {
			sb.append(backSlash).append(c);
		}
		regexEscapeString = "(?=[" + sb.toString() + "])";
	}

	private boolean userRegex;
	private boolean caseSensitive;
	private boolean matchAnyWhere;
	private String prefix;
	private String subFix;
	private String escapeString;

	public TwiSearchFilter() {
		this (false, false, true);
	}

	public TwiSearchFilter(boolean userRegex, boolean caseSensitive, boolean matchAnyWhere) {
		this.userRegex = userRegex;
		this.caseSensitive = caseSensitive;
		this.matchAnyWhere = matchAnyWhere;
		escapeString = userRegex ? "" : regexEscapeString;
		prefix = (caseSensitive ? "" : "(?i)") + (matchAnyWhere ? ".*" : "");
		subFix = (matchAnyWhere ? ".*" : "");
	}

	public boolean anyMatch(String searchText, String... itemStrings) {
		String fullSearchText = prefix + searchText + subFix;
//		System.out.println("searchText: " + searchText);
//		System.out.println("itemStrings: " + Arrays.toString(itemStrings));
//		System.out.println("fullSearchText: " + fullSearchText);
		for (String s : itemStrings) {
			String s1 = s.replaceAll(escapeString, escapeChar);
//			System.out.println("escapedS: " + s1);
			if (s1.matches(fullSearchText)) {
				return true;
			}
		}
		return false;
	}

	public boolean anyMatch(String searchText, Stream<String> itemStrings) {
		String fullSearchText = prefix + searchText + subFix;
		return itemStrings.anyMatch(s -> s.replaceAll(escapeString, escapeChar).matches(fullSearchText));
	}

	public boolean anyMatch2(String searchText, Stream<Supplier<String>> itemStrings) {
		String fullSearchText = prefix + searchText + subFix;
		return itemStrings.anyMatch(s -> s.get().replaceAll(escapeString, escapeChar).matches(fullSearchText));
	}

	public Pattern getPattern(String searchString) {
		int flags = 0;
		flags |= !caseSensitive ? Pattern.CASE_INSENSITIVE : 0;
		searchString = searchString.replaceAll(escapeString, escapeChar);
		return Pattern.compile(searchString, flags);
	}

	public TwiSearchFilter setUserRegex(boolean userRegex) {
		this.userRegex = userRegex;
		escapeString = userRegex ? "" : regexEscapeString;
		return this;
	}

	public boolean isUserRegex() {
		return userRegex;
	}

	public TwiSearchFilter setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		prefix = (caseSensitive ? "" : "(?i)") + (matchAnyWhere ? ".*" : "");
		return this;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public TwiSearchFilter setMatchAnyWhere(boolean matchAnyWhere) {
		this.matchAnyWhere = matchAnyWhere;
		prefix = (caseSensitive ? "" : "(?i)") + (matchAnyWhere ? ".*" : "");
		subFix = (matchAnyWhere ? ".*" : "");
		return this;
	}

	public boolean isMatchAnyWhere() {
		return matchAnyWhere;
	}
}
