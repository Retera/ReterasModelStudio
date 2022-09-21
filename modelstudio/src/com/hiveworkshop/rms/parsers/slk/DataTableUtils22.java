package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataTableUtils22 {
	private static final boolean DEBUG = false;

	public static void readTXT(DataTable dataTable, String filepath) {
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
			if (input.startsWith("//")) {
				continue;
			}
			if (input.startsWith("[") && input.contains("]")) {
				final int start = input.indexOf("[") + 1;
				final int end = input.indexOf("]");
				final String newKey = input.substring(start, end);
				currentUnit = dataTable.get(newKey);
				if (currentUnit == null && canProduce) {
					currentUnit = new LMUnit(newKey, dataTable);
					dataTable.put(currentUnit);

				}
			} else if (input.contains("=")) {
				final int eIndex = input.indexOf("=");
				final String fieldValue = input.substring(eIndex + 1);
				int fieldIndex = 0;
				final StringBuilder builder = new StringBuilder();
				boolean withinQuotedString = false;
				final String fieldName = input.substring(0, eIndex);
				for (int i = 0; i < fieldValue.length(); i++) {
					final char c = fieldValue.charAt(i);
					if (c == '\"') {
						withinQuotedString = !withinQuotedString;
					} else if (!withinQuotedString && (c == ',')) {
						currentUnit.setField(fieldName, builder.toString().trim(), fieldIndex++);
						builder.setLength(0); // empty buffer
					} else {
						builder.append(c);
					}
				}
				if (builder.length() > 0) {
					if (currentUnit == null) {
						System.out.println("null for " + input);
					}
					currentUnit.setField(fieldName, builder.toString().trim(), fieldIndex++);
				}
			}
		}

		reader.close();
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
				if (DEBUG) {
					System.out.println(currentLine);
				}
				if (currentLine.startsWith("E")) {
					return null;
				} else if (!currentLine.startsWith("O")){
					if (currentLine.contains("X1;")) {
						rowStartCount++;
					}
					return currentLine;
				}
			}
			return null;
		}
		String getSkippPandF() throws IOException {
			lastLine = currentLine;
			while ((currentLine = reader.readLine()) != null){
				if (DEBUG) {
					System.out.println(currentLine);
				}
				if (currentLine.startsWith("E")) {
					return null;
				} else if (!currentLine.startsWith("P") && !currentLine.startsWith("F")){
					return currentLine;
				}
			}
			return null;
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

	public static void readSLK2(DataTable dataTable, final InputStream txt) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));

		String input = "";
		Element currentUnit = null;
		input = reader.readLine();
		if (!input.contains("ID")) {
			System.err.println("Formatting of SLK is unusual.");
		}
		input = reader.readLine();
		while (input.startsWith("P;") || input.startsWith("F;")) {
			input = reader.readLine();
		}
		final int yIndex = input.indexOf("Y") + 1;
		final int xIndex = input.indexOf("X") + 1;
		String[] dataNames = getDataNamesArray(input, yIndex, xIndex);
		boolean flipMode = xIndex <= yIndex;
		int rowStartCount = 0;
		int lastFieldId = 0;
		while ((input = reader.readLine()) != null && rowStartCount <= 1) {
			if (DEBUG) {
				System.out.println(input);
			}
			if (input.startsWith("E")) {
				break;
			}
			if (input.startsWith("O;")) {
				continue;
			}
			if (input.contains("X1;")) {
				rowStartCount++;
			}

			final String kInput;
			if (input.startsWith("F;")) {
				kInput = reader.readLine();
				if (DEBUG) {
					System.out.println(kInput);
				}
			} else {
				kInput = input;
			}

			if (rowStartCount <= 1) {
				int entryIndex = kInput.indexOf("K");
				if ((entryIndex != -1) && (kInput.charAt(entryIndex - 1) == ';')) {
					final int subXIndex = input.indexOf("X");
					final int subYIndex = input.indexOf("Y");
					if (flipMode && 0 <= subXIndex && subXIndex < input.indexOf("Y") && input.equals(kInput)) {
						entryIndex = Math.min(input.indexOf("Y"), kInput.indexOf("K"));
					}

					if (input.indexOf("X") < 0 && lastFieldId == 0) {
						rowStartCount++;
					}
					final int fieldId = getFieldId(input, lastFieldId, kInput, entryIndex);


					if ((fieldId - 1) >= dataNames.length) {
						dataNames = Arrays.copyOf(dataNames, fieldId);
					}
					dataNames[fieldId - 1] = getValueField(kInput, entryIndex);
					lastFieldId = fieldId;
				}
			}
		}
//		while ((input = reader.readLine()) != null) {
//			if (DEBUG) {
//				System.out.println(input);
//			}
//			if (input.startsWith("E")) {
//				break;
//			}
//			if (input.startsWith("O;")) {
//				continue;
//			}
//			if (input.startsWith("F;")) {
//				final String kInput = reader.readLine();
//				if (DEBUG) {
//					System.out.println(kInput);
//				}
//				if (input.contains("X1;") || input.endsWith("X1")) {
//					currentUnit = getUnitFromKey(dataTable, currentUnit, kInput);
//				} else if (kInput.contains("K")) {
//					addDataToCurrentUnit(input, currentUnit, flipMode, dataNames, kInput);
//				}
//			} else {
//				if (input.contains("X1;")) {
//					currentUnit = getUnitFromKey(dataTable, currentUnit, input);
//				} else if (input.contains("K")) {
//					addDataToCurrentUnit(input, currentUnit, flipMode, dataNames, input);
//				}
//			}
//		}
		while ((input = reader.readLine()) != null) {
			if (DEBUG) {
				System.out.println(input);
			}
			if (input.startsWith("E")) {
				break;
			}
			if (input.startsWith("O;")) {
				continue;
			}
			final String kInput;
			if (input.startsWith("F;")) {
				kInput = reader.readLine();
				if (DEBUG) {
					System.out.println(kInput);
				}
			} else {
				kInput = input;
			}
			if (input.contains("X1;") || (!input.equals(kInput) && input.endsWith("X1"))) {
				currentUnit = getUnitFromKey(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
//				addDataToCurrentUnit(input, currentUnit, flipMode, dataNames, kInput);
			}
		}

		reader.close();
	}

	public static void readSLK(DataTable dataTable, final InputStream txt) throws IOException {
		SLKReader slkReader = new SLKReader(txt);
//		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));

		String input = "";
		Element currentUnit = null;
		input = slkReader.readLine();
		if (!input.contains("ID")) {
			System.err.println("Formatting of SLK is unusual.");
		}
		input = slkReader.getSkippPandF();
		final int yIndex = input.indexOf("Y") + 1;
		final int xIndex = input.indexOf("X") + 1;
//		String[] dataNames = getDataNamesArray(input, yIndex, xIndex);
		DataTableFieldNames dataNames = new DataTableFieldNames(input);
		boolean flipMode = xIndex <= yIndex;
		int rowStartCount = 0;
		int lastFieldId = 0;
		while ((input = slkReader.getNextLine()) != null) {
			debugLog(input);
			final String kInput;
			if (input.startsWith("F;")) {
				kInput = slkReader.readLine();
				debugLog(kInput);
			} else {
				kInput = input;
			}
			if (slkReader.rowStartCount <= 1) {
				int entryIndex = kInput.indexOf("K");
				if ((entryIndex != -1) && (kInput.charAt(entryIndex - 1) == ';')) {
					final int subXIndex = input.indexOf("X");
					final int subYIndex = input.indexOf("Y");
					if (flipMode && subXIndex < subYIndex && 0 <= subXIndex && input.equals(kInput)) {
						entryIndex = Math.min(subYIndex, entryIndex);
					}

					if (subXIndex < 0 && lastFieldId == 0) {
						slkReader.incrementRow();
					}
					final int fieldId = getFieldId1(input, lastFieldId, kInput, entryIndex);
					dataNames.addField(getValueField(kInput, entryIndex), fieldId);
					lastFieldId = fieldId;
				}

				continue;
			}
			if (input.contains("X1;") || (!input.equals(kInput) && input.endsWith("X1"))) {
				currentUnit = getUnitFromKey(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
				addDataToCurrentUnit(input, currentUnit, flipMode, dataNames, kInput);
			}
		}

		slkReader.close();
	}
	public static void readSLK1(DataTable dataTable, final InputStream txt) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));

		String input = "";
		Element currentUnit = null;
		input = reader.readLine();
		if (!input.contains("ID")) {
			System.err.println("Formatting of SLK is unusual.");
		}
		input = reader.readLine();
		while (input.startsWith("P;") || input.startsWith("F;")) {
			input = reader.readLine();
		}
		final int yIndex = input.indexOf("Y") + 1;
		final int xIndex = input.indexOf("X") + 1;
		String[] dataNames = getDataNamesArray(input, yIndex, xIndex);
		boolean flipMode = xIndex <= yIndex;
		int rowStartCount = 0;
		int lastFieldId = 0;
		while ((input = reader.readLine()) != null) {
			if (DEBUG) {
				System.out.println(input);
			}
			if (input.startsWith("E")) {
				break;
			}
			if (input.startsWith("O;")) {
				continue;
			}
			if (input.contains("X1;")) {
				rowStartCount++;
			}
			final String kInput;
			if (input.startsWith("F;")) {
				kInput = reader.readLine();
				if (DEBUG) {
					System.out.println(kInput);
				}
			} else {
				kInput = input;
			}
			if (rowStartCount <= 1) {
				int entryIndex = kInput.indexOf("K");
				if ((entryIndex != -1) && (kInput.charAt(entryIndex - 1) == ';')) {
					final int subXIndex = input.indexOf("X");
					final int subYIndex = input.indexOf("Y");
					if (flipMode && subXIndex < subYIndex && 0 <= subXIndex && input.equals(kInput)) {
						entryIndex = Math.min(subYIndex, entryIndex);
					}

					if (subXIndex < 0 && lastFieldId == 0) {
						rowStartCount++;
					}
					final int fieldId = getFieldId1(input, lastFieldId, kInput, entryIndex);


					if ((fieldId - 1) >= dataNames.length) {
						dataNames = Arrays.copyOf(dataNames, fieldId);
					}
					dataNames[fieldId - 1] = getValueField(kInput, entryIndex);
					lastFieldId = fieldId;
				}

				continue;
			}
			if (input.contains("X1;") || (!input.equals(kInput) && input.endsWith("X1"))) {
				currentUnit = getUnitFromKey(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
//				addDataToCurrentUnit(input, currentUnit, flipMode, dataNames, kInput);
			}
		}

		reader.close();
	}

	private static String getValueField(String kInput, int entryIndex) {
		final int quotationIndex = kInput.indexOf("\"");
		if (quotationIndex == -1) {
			return kInput.substring(entryIndex + 1);
		} else {
			return kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
		}
	}

	private static String[] getDataNamesArray(String input, int yIndex, int xIndex) {
		int endIndex = xIndex > yIndex ? input.lastIndexOf(";") : yIndex - 2;
		return new String[Integer.parseInt(input.substring(xIndex, endIndex))];
	}

	private static void addDataToCurrentUnit2(String input, Element currentUnit, boolean flipMode, String[] dataNames) {
		int entryIndex = getEntryIndex(input, flipMode);

		final int fieldId = getFieldId(input, entryIndex);

		String fieldValue = getValueField(input, entryIndex);

		if (dataNames[fieldId - 1] != null) {
			currentUnit.setField(dataNames[fieldId - 1], fieldValue);
		}
	}

	private static int getFieldId(String input, int lastFieldId, String kInput, int entryIndex) {
		if (!input.contains("X")) {
			return  lastFieldId + 1;
		} else {
			final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : entryIndex - 1;
			return Integer.parseInt(input.substring(input.indexOf("X") + 1, fieldIdEndIndex));
		}
	}
	private static int getFieldId1(String input, int lastFieldId, String kInput, int entryIndex) {
		if (!input.contains("X")) {
			return  lastFieldId + 1;
		} else {
			final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : entryIndex - 1;
			return Integer.parseInt(input.substring(input.indexOf("X") + 1, fieldIdEndIndex));
		}
	}

	private static int getFieldId(String input, int entryIndex) {
		final int subXIndex = input.indexOf("X");
		if(subXIndex < 0 || entryIndex - 1 < subXIndex){
			return 1;
		} else {
			return Integer.parseInt(input.substring(subXIndex + 1, entryIndex - 1));
		}
	}

	private static int getFieldId(String input, String kInput, int entryIndex) {
		final int subXIndex = input.indexOf("X");
		if(subXIndex < 0 || kInput.equals(input) && entryIndex - 1 < subXIndex){
			return 1;
		} else {
			final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : entryIndex - 1;
			return Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
		}
	}


	private static int getEntryIndex(String input, boolean flipMode, String kInput) {
		if (flipMode
				&& input.equals(kInput)
				&& input.contains("X")
				&& input.indexOf("X") < kInput.indexOf("Y") ) {
			return Math.min(kInput.indexOf("Y"), kInput.indexOf("K"));
		} else {
			return kInput.indexOf("K");
		}
	}
	private static int getEntryIndex(String input, boolean flipMode) {
		if (flipMode && input.contains("Y")) {
			return Math.min(input.indexOf("Y"), input.indexOf("K"));
		} else {
			return input.indexOf("K");
		}
	}

	private static void addDataToCurrentUnit(String input, Element currentUnit, boolean flipMode, DataTableFieldNames dataNames, String kInput) {
		int entryIndex = getEntryIndex(kInput, flipMode);

		final int fieldId = getFieldId(input, kInput, entryIndex);

		String fieldValue = getValueField(kInput, entryIndex);

		if (dataNames.getFieldName(fieldId) != null) {
			currentUnit.setField(dataNames.getFieldName(fieldId), fieldValue);
		}
	}

	private static Element getUnitFromKey(DataTable dataTable, Element currentUnit, String kInput) {
		final int start = kInput.indexOf("\"") + 1;
		final int end = kInput.lastIndexOf("\"");
		if ((start - 1) != end) {
			final String newKey = kInput.substring(start, end);
			currentUnit = dataTable.computeIfAbsent(newKey);
		}
		return currentUnit;
	}

	public static DataTable getDataTable(String filePath) {
		final DataTable unitMetaData = new DataTable();
		try {
			readSLK(unitMetaData, GameDataFileSystem.getDefault().getResourceAsStream(filePath));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}
	public static DataTable fillDataTable(DataTable unitMetaData, String filePath) {
		try {
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
