package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataTableUtils {
	private static final boolean DEBUG = false;
//	private static final boolean DEBUG = true;

	public static void readTXT(DataTable dataTable, String filepath) {
		debugLog("1 readTXT: \"" + filepath + "\"" + ", producible: " + false);
		if (GameDataFileSystem.getDefault().has(filepath)) {
			InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream(filepath);
			try {
				readTXT(dataTable, resourceAsStream, false);
			} catch (final IOException e) {
				ExceptionPopup.display(e);
			}
		} else {
			System.err.println("Failed to load file \"" + filepath + "\" from \"GameDataFileSystem.getDefault()\"");
		}
	}

	public static void readTXT(DataTable dataTable, String filepath, boolean canProduce) throws IOException {

		debugLog("2 readTXT: \"" + filepath + "\"" + ", producible: " + canProduce);
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

		String lastKey = "";
		String input = "";
		Element currentUnit = null;
		while ((input = reader.readLine()) != null) {
//			debugLog(input);
			if (!input.startsWith("//")) {
				if (input.startsWith("[") && input.contains("]")) {
					final String newKey = extractBracketContent(input);
					lastKey = newKey;
					currentUnit = dataTable.get(newKey);
					if (currentUnit == null && canProduce) {
						currentUnit = new LMUnit(newKey, dataTable);
						dataTable.put(currentUnit);
					}
				} else if (input.contains("=")) {
					final String fieldName = input.substring(0, input.indexOf("="));
					List<String> fieldValues = getFieldValues(input.substring(input.indexOf("=") + 1));

					if (currentUnit == null) {
						debugLog("[Utils] null for " + input + " (" + lastKey + ")");
					} else {
						for(int i = 0; i<fieldValues.size(); i++){
							currentUnit.setField(fieldName, fieldValues.get(i), i);

						}
					}
				}
			}
		}

		reader.close();
	}

	private static String extractBracketContent(String input) {
		final int start = input.indexOf("[") + 1;
		final int end = input.indexOf("]");
		final String newKey = input.substring(start, end);
		return newKey;
	}

	private static List<String> getFieldValues(String fieldValue) {
		List<String> fieldValues = new ArrayList<>();
		boolean withinQuotedString = false;
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fieldValue.length(); i++) {
			final char c = fieldValue.charAt(i);
			if (c == '\"') {
				withinQuotedString = !withinQuotedString;
			} else if (!withinQuotedString && (c == ',')) {
				fieldValues.add(builder.toString().trim());
				builder.setLength(0); // empty buffer
			} else {
				builder.append(c);
			}
		}
		if (builder.length() > 0) {
			fieldValues.add(builder.toString().trim());
		}
		return fieldValues;
	}


	private static class SLKReader {
		final BufferedReader reader;
		String lastLine;
		String currentLine;
		int rowStartCount = 0;
		int lastX = -1;
		int lastY = -1;
		SLKReader(final InputStream txt){
			reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));
		}

		String readLine() throws IOException {
			return reader.readLine();
		}
		String getNextLine() throws IOException {
			lastLine = currentLine;
			while ((currentLine = reader.readLine()) != null){
//				debugLog(currentLine);
				if(currentLine.startsWith("F")){
					System.out.println(currentLine);
				}
				if (currentLine.startsWith("E")) {
					return null;
				} else if (!currentLine.startsWith("O")){
					if (currentLine.contains("X1;")) {
						rowStartCount++;
					}
					if (currentLine.startsWith("F;") && currentLine.contains(";X")) {
						final String kInput = reader.readLine();
						debugLog("'" + currentLine + "' + '" + kInput + "'");
						currentLine = currentLine + kInput.substring(1);
					}

					return currentLine;
				}
			}
			return null;
		}
		String getSkippPandF() throws IOException {
			lastLine = currentLine;
			while ((currentLine = reader.readLine()) != null){
//				debugLog(currentLine);
				if(currentLine.startsWith("F")){
					System.out.println(currentLine);
				}
				if (currentLine.startsWith("E")) {
					return "";
				} else if (!currentLine.startsWith("P") && !currentLine.startsWith("F")){
					return currentLine;
				}
			}
			return "";
		}
		void incrementRow(){
			rowStartCount++;
		}
		void close() throws IOException {
			reader.close();
		}
	}

	private static class DataTableFieldNames {
		String[] dataNames;
		boolean flipMode;
		int lastFieldId = 0;
		DataTableFieldNames(String input){

			final int yIndex = input.indexOf("Y") + 1;
			final int xIndex = input.indexOf("X") + 1;
			dataNames = getDataNamesArray(input, yIndex, xIndex);
			flipMode = xIndex <= yIndex;
		}

		String[] getDataNamesArray(String input, int yIndex, int xIndex) {
			int endIndex = xIndex > yIndex ? input.lastIndexOf(";") : yIndex - 2;
			return new String[Integer.parseInt(input.substring(xIndex, endIndex))];
		}

		void addField(String fieldName, int fieldId){
			if ((fieldId - 1) >= dataNames.length) {
				dataNames = Arrays.copyOf(dataNames, fieldId);
			}
			dataNames[fieldId - 1] = fieldName;

			lastFieldId = fieldId;
		}

		String getFieldName(int fieldId){
			return dataNames[fieldId - 1];
		}
	}
	public static void readSLK(DataTable dataTable, final InputStream txt) throws IOException {
		SLKReader slkReader = new SLKReader(txt);
		String input = "";
		Element currentUnit = null;
		input = slkReader.readLine();
		if (!input.contains("ID")) {
			System.err.println("Formatting of SLK is unusual.");
		}
		input = slkReader.getSkippPandF();
		DataTableFieldNames dataNames = new DataTableFieldNames(input);
		boolean flipMode = input.indexOf("X") <= input.indexOf("Y");
		while ((input = slkReader.getNextLine()) != null) {
			if (slkReader.rowStartCount <= 1) {
				parseFieldNames(slkReader, input, dataNames, flipMode);
			} else if (input.contains("X1;")) {
				currentUnit = getUnitFromKey(dataTable, currentUnit, input);
			} else if (input.contains("K")) {
				addDataToCurrentUnit(input, currentUnit, flipMode, dataNames);
			}
		}

		slkReader.close();
	}

	private static void parseFieldNames(SLKReader slkReader, String input, DataTableFieldNames dataNames, boolean flipMode) {
		if (input.contains(";K")) {
			int entryIndex = getEntryIndex1(input, flipMode);

			if (!input.contains("X") && dataNames.lastFieldId == 0) {
				slkReader.incrementRow();
			}
			final int fieldId = getFieldId1(input, dataNames.lastFieldId, entryIndex);
			dataNames.addField(getValueField(input, entryIndex), fieldId);
		}
	}

	private static int getEntryIndex1(String input, boolean flipMode) {
		if (flipMode
				&& input.contains("X")
				&& input.indexOf("X") < input.indexOf("Y")) {
			return Math.min(input.indexOf("Y"), input.indexOf("K"));
		} else {
			return input.indexOf("K");
		}
	}

	private static int getEntryIndex(String input, boolean flipMode) {
		if (flipMode && input.contains("Y")) {
			return Math.min(input.indexOf("Y"), input.indexOf("K"));
		} else {
			return input.indexOf("K");
		}
	}

	private static String getValueField(String kInput, int entryIndex) {
		final int quotationIndex = kInput.indexOf("\"");
		if (quotationIndex < 0) {
			return kInput.substring(entryIndex + 1);
		} else {
			return kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
		}
	}
	private static Element getUnitFromKey(DataTable dataTable, Element currentUnit, String input) {
		final String newKey = getValueField(input, input.length()-1);
		if (!newKey.isEmpty()) {
			currentUnit = dataTable.computeIfAbsent(newKey);
		}
		return currentUnit;
	}


	private static int getFieldId1(String input, int lastFieldId, int entryIndex) {
		if (!input.contains("X")) {
			return  lastFieldId + 1;
		} else {
			return Integer.parseInt(input.substring(input.indexOf("X") + 1, entryIndex - 1));
		}
	}

	private static int getFieldId(String input, int entryIndex) {
		final int subXIndex = input.indexOf("X");
		if(!input.contains("X") || entryIndex - 1 < subXIndex){
			return 1;
		} else {
			return Integer.parseInt(input.substring(subXIndex + 1, entryIndex - 1));
		}
	}

	private static void addDataToCurrentUnit(String input, Element currentUnit, boolean flipMode, DataTableFieldNames dataNames) {
		int entryIndex = getEntryIndex(input, flipMode);
		final int fieldId = getFieldId(input, entryIndex);
		final String fieldValue = getValueField(input, entryIndex);

		String fieldName = dataNames.getFieldName(fieldId);
		if (fieldName != null) {
			currentUnit.setField(fieldName, fieldValue);
		}
	}
	public static DataTable fillDataTable(DataTable unitMetaData, String filePath) {
		try {
			System.out.println("filling table from file '" + filePath + "'");
			readSLK(unitMetaData, GameDataFileSystem.getDefault().getResourceAsStream(filePath));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	private static void debugLog(String s){
		if(DEBUG){
			System.out.println(s);
		}
	}
}
