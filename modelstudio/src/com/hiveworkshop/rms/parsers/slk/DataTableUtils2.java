package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataTableUtils2 {
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

//	public static void readTXT(DataTable dataTable, final File f) {
//		readTXT(dataTable, f, false);
//	}
//
//	public static void readTXT(DataTable dataTable, final File f, final boolean canProduce) {
//		try {
//			readTXT(dataTable, new FileInputStream(f), canProduce);
//		} catch (final IOException e) {
//			ExceptionPopup.display(e);
//		}
//	}

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
				if (currentUnit == null) {
					// currentUnit = dataTable.get(newKey.charAt(0) + "" +
					// Character.toUpperCase(newKey.charAt(1)) +
					// newKey.substring(2));
					// if( currentUnit == null ) {
					// currentUnit = dataTable.get(newKey.charAt(0) + "" +
					// Character.toLowerCase(newKey.charAt(1)) +
					// newKey.substring(2));
					// if( currentUnit == null ) {
					// currentUnit = dataTable.get(newKeyBase.substring(0,3) +
					// Character.toUpperCase(newKeyBase.charAt(3)));
					// if( currentUnit == null ) {
					currentUnit = new Element(newKey, dataTable);
					if (canProduce) {
						currentUnit = new LMUnit(newKey, dataTable);
						dataTable.put(currentUnit);
					}
					// currentUnit.setField("fromTXT", "1");
					// }
					// }
					// }
				}
			} else if (input.contains("=")) {
				final int eIndex = input.indexOf("=");
				final String fieldValue = input.substring(eIndex + 1);
				// if (fieldValue.length() > 1 && fieldValue.startsWith("\"") &&
				// fieldValue.endsWith("\"")) {
				// fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
				// }
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



	public static void readSLK(DataTable dataTable, final InputStream txt) throws IOException {
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
		int colCount = 0;
		int rowCount = 0;
		boolean flipMode = false;
		if (xIndex > yIndex) {
			colCount = Integer.parseInt(input.substring(xIndex, input.lastIndexOf(";")));
			rowCount = Integer.parseInt(input.substring(yIndex, xIndex - 2));
		} else {
			rowCount = Integer.parseInt(input.substring(yIndex, input.lastIndexOf(";")));
			colCount = Integer.parseInt(input.substring(xIndex, yIndex - 2));
			flipMode = true;
		}
		int rowStartCount = 0;
		String[] dataNames = new String[colCount];
		// for( int i = 0; i < colCount && rowStartCount <= 1; i++ ) {
		// input = reader.readLine();
		// dataNames[i] = input.substring(input.indexOf("\"")+1,
		// input.lastIndexOf("\""));
		// }
		//
		int col = 0;
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
				col = 0;
			} else {
				col++;
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
				final int subXIndex = input.indexOf("X");
				final int subYIndex = input.indexOf("Y");
				if ((subYIndex >= 0) && (subYIndex < subXIndex)) {
					final int eIndex = kInput.indexOf("K");
					final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
					if ((eIndex == -1) || (kInput.charAt(eIndex - 1) != ';')) {
						continue;
					}
					final int fieldId;
					if (subXIndex < 0) {
						if (lastFieldId == 0) {
							rowStartCount++;
						}
						fieldId = lastFieldId + 1;
					} else {
						fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
					}

					final int quotationIndex = kInput.indexOf("\"");
					if ((fieldId - 1) >= dataNames.length) {
						dataNames = Arrays.copyOf(dataNames, fieldId);
					}
					if (quotationIndex == -1) {
						dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
					} else {
						dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
					}
					lastFieldId = fieldId;
				} else {
					int eIndex = kInput.indexOf("K");
					if ((eIndex == -1) || (kInput.charAt(eIndex - 1) != ';')) {
						continue;
					}
					final int fieldId;
					if (subXIndex < 0) {
						if (lastFieldId == 0) {
							rowStartCount++;
						}
						fieldId = lastFieldId + 1;
					} else {
						if (flipMode && input.contains("Y") && (input.equals(kInput))) {
							eIndex = Math.min(subYIndex, eIndex);
						}
						final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
						fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
					}

					final int quotationIndex = kInput.indexOf("\"");
					if ((fieldId - 1) >= dataNames.length) {
						dataNames = Arrays.copyOf(dataNames, fieldId);
					}
					if (quotationIndex == -1) {
						dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
					} else {
						dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
					}
					lastFieldId = fieldId;
				}
				continue;
			}
			// if( rowStartCount == 2)
			// System.out.println(Arrays.toString(dataNames));
			if (input.contains("X1;") || ((!input.equals(kInput)) && input.endsWith("X1"))) {
				final int start = kInput.indexOf("\"") + 1;
				final int end = kInput.lastIndexOf("\"");
				if ((start - 1) != end) {
					final String newKey = kInput.substring(start, end);
					currentUnit = dataTable.get(newKey);
					if (currentUnit == null) {
						currentUnit = new Element(newKey, dataTable);
						dataTable.put(currentUnit);
					}
				}
			} else if (kInput.contains("K")) {
				final int subXIndex = input.indexOf("X");
				int eIndex = kInput.indexOf("K");
				if (flipMode && kInput.contains("Y")) {
					eIndex = Math.min(kInput.indexOf("Y"), eIndex);
				}
				final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
				final int fieldId = (subXIndex == -1) || (subXIndex > fieldIdEndIndex) ? 1
						: Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
				String fieldValue = kInput.substring(eIndex + 1);
				if ((fieldValue.length() > 1) && fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
					fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
				}
				if (dataNames[fieldId - 1] != null) {
					currentUnit.setField(dataNames[fieldId - 1], fieldValue);
				}
			}
		}

		reader.close();
	}

	public static DataTable getDataTable(String filePath) {
		final DataTable unitMetaData = new DataTable();
		try {
//			readSLK(unitMetaData, filePath);
			readSLK(unitMetaData, GameDataFileSystem.getDefault().getResourceAsStream(filePath));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}
	public static void readSLK(DataTable dataTable, final File f) {
		try {
			readSLK(dataTable, new FileInputStream(f));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public static void readSLK(DataTable dataTable, String filePath) throws IOException {
		readSLK(dataTable, GameDataFileSystem.getDefault().getResourceAsStream(filePath));
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
