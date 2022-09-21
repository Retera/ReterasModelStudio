package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReadTXT {
	private static final boolean DEBUG = false;

	public static void readTXT(DataTable dataTable, String filepath) {
		if (GameDataFileSystem.getDefault().has(filepath)) {
			InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream(filepath);
			readTXT(dataTable, resourceAsStream);
		} else {
			System.err.println("Failed to load file \"" + filepath + "\" from \"GameDataFileSystem.getDefault()\"");
		}
	}

	public static void readTXT(DataTable dataTable, InputStream inputStream) {
		try {
			readTXT(dataTable, inputStream, false);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public static void readTXT(DataTable dataTable, String filepath, boolean canProduce) throws IOException {

		if (GameDataFileSystem.getDefault().has(filepath)) {
			InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream(filepath);
			readTXT(dataTable, resourceAsStream, canProduce);
		} else {
			System.err.println("Failed to load file \"" + filepath + "\" from \"GameDataFileSystem.getDefault()\"");
		}
	}

	public static void readTXT(DataTable dataTable, final InputStream txt, final boolean canProduce) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));
		// BOM marker will only appear on the very beginning
		reader.mark(4);
		if ('\ufeff' != reader.read()) {
			reader.reset(); // not the BOM marker
		}

		String input = "";
		Element currentUnit = null;
		final boolean first = true;
		while ((input = reader.readLine()) != null) {
			if (DEBUG) {
				System.out.println(input);
			}
			if (isComment(input)) {
				continue;
			}
			if (isChunk(input)) {
				final String newKey = extractChunk(input);
//				currentUnit = dataTable.dataTable.get(new StringKey(newKey));
				currentUnit = dataTable.get(newKey);
				if (currentUnit == null) {
//					currentUnit = new Element(newKey, dataTable);
					if (canProduce) {
						currentUnit = new LMUnit(newKey, dataTable);
						dataTable.put(newKey, currentUnit);
					}

				}
//				if (currentUnit == null) {
//					currentUnit = dataTable.get(newKey.charAt(0) + "" + Character.toUpperCase(newKey.charAt(1)) + newKey.substring(2));
//					if(currentUnit == null) {
//						currentUnit = dataTable.get(newKey.charAt(0) + "" + Character.toLowerCase(newKey.charAt(1)) + newKey.substring(2));
//						if(currentUnit == null) {
//							currentUnit = dataTable.get(newKeyBase.substring(0,3) + Character.toUpperCase(newKeyBase.charAt(3)));
//							if(currentUnit == null) {
//								currentUnit = new Element(newKey, dataTable);
//								if (canProduce) {
//									currentUnit = new LMUnit(newKey, dataTable);
//									dataTable.dataTable.put(new StringKey(newKey), currentUnit);
//								}
//								currentUnit.setField("fromTXT", "1");
//							}
//						}
//					}
//				}
			} else if (hasValueField(input)) {
				final int eIndex = input.indexOf("=");
				final String fieldName = input.substring(0, eIndex);
				final String fieldValue = input.substring(eIndex + 1);

				List<String> fields = getFieldValues(input, currentUnit, fieldValue);
				for(int i = 0; i< fields.size(); i++){
					currentUnit.setField(fieldName, fields.get(i), i);
				}
			}
		}

		reader.close();
	}

	private static List<String> getFieldValues(String input, Element currentUnit, String fieldValue) {
		final StringBuilder builder = new StringBuilder();
		boolean withinQuotedString = false;
		List<String> fields = new ArrayList<>();
		for (int i = 0; i < fieldValue.length(); i++) {
			final char c = fieldValue.charAt(i);
			if (c == '\"') {
				withinQuotedString = !withinQuotedString;
			} else if (!withinQuotedString && (c == ',')) {
				fields.add(builder.toString().trim());
				builder.setLength(0); // empty buffer
			} else {
				builder.append(c);
			}
		}
		if (builder.length() > 0) {
			if (currentUnit == null) {
				System.out.println("null for " + input);
			}
			fields.add(builder.toString().trim());
		}

//		String zeroOrMoreNotAQuote = "[^\"]*";
//		String hasFullQuote = zeroOrMoreNotAQuote + "\"" + zeroOrMoreNotAQuote + "\"" + zeroOrMoreNotAQuote;
//		String evenQuotesAhead = "(?=(" + hasFullQuote + ")*$)";
//		String noQuotesAhead = "(?=(" + zeroOrMoreNotAQuote + "$)))";
//		String commaNotInQuote = "(," + evenQuotesAhead + ")|(," + noQuotesAhead + ")";
//		String[] split = fieldValue.split("(,(?=([^\"]*\"[^\"]*\"[^\"]*)*$))|(,(?=([^\"]*$)))");


		return fields;
	}

	private static String extractChunk(String input) {
		final int start = input.indexOf("[") + 1;
		final int end = input.indexOf("]");
		final String newKey = input.substring(start, end);
		return newKey;
	}

	private static boolean isChunk(String input) {
		return input.startsWith("[") && input.contains("]");
	}

	private static boolean hasValueField(String input) {
		return input.contains("=");
	}

	private static boolean isComment(String input) {
		return input.startsWith("//");
	}


	public static DataTable getTXTDataTable(String filepath) {
		final DataTable unitMetaData = new DataTable();
		try {
			readTXT(unitMetaData, filepath, true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}
}
