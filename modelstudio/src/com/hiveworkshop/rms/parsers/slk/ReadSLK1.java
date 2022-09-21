package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class ReadSLK1 {
	private static final boolean DEBUG = false;

	public static void readSLK(DataTable dataTable, final File f) {
		try {
			readSLK(dataTable, new FileInputStream(f));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public static void readSLK(DataTable dataTable, String filepath) throws IOException {
		readSLK(dataTable, GameDataFileSystem.getDefault().getResourceAsStream(filepath));
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
			final String kInput = getkInput(reader, input);
			if (rowStartCount <= 1) {
				int eIndex = kInput.indexOf("K");
				if ((eIndex == -1) || (kInput.charAt(eIndex - 1) != ';')) {
					continue;
				}
				final int subXIndex = input.indexOf("X");
				final int subYIndex = input.indexOf("Y");
				final int fieldId;
				if ((subYIndex < 0) || (subYIndex >= subXIndex)) {
					if (subXIndex < 0) {
						if (lastFieldId == 0) {
							rowStartCount++;
						}
					} else if (flipMode && input.contains("Y") && (input.equals(kInput))) {
						eIndex = Math.min(subYIndex, eIndex);
					}

				}

				fieldId = getFieldId(input, lastFieldId, kInput, subXIndex, eIndex);
				if ((fieldId - 1) >= dataNames.length) {
					dataNames = Arrays.copyOf(dataNames, fieldId);
				}
				setDataField(dataNames, kInput, eIndex, fieldId);
				lastFieldId = fieldId;
				continue;
			}
			// if( rowStartCount == 2)
			// System.out.println(Arrays.toString(dataNames));
			if (input.contains("X1;") || ((!input.equals(kInput)) && input.endsWith("X1"))) {
				currentUnit = setDataTableField(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
				setCurrentUnitField(input, currentUnit, flipMode, dataNames, kInput);
			}
		}

		reader.close();
	}

	private static int getFieldId(String input, int lastFieldId, String kInput, int subXIndex, int eIndex) {
		final int fieldId;
		if (subXIndex < 0) {
			fieldId = lastFieldId + 1;
		} else {
			final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
			fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
		}
		return fieldId;
	}

	private static void setDataField(String[] dataNames, String kInput, int eIndex, int fieldId) {
		final int quotationIndex = kInput.indexOf("\"");
		if (quotationIndex == -1) {
			dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
		} else {
			dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
		}
	}

	private static Element setDataTableField(DataTable dataTable, Element currentUnit, String kInput) {
		final int start = kInput.indexOf("\"") + 1;
		final int end = kInput.lastIndexOf("\"");
		if ((start - 1) != end) {
			final String newKey = kInput.substring(start, end);
			currentUnit = dataTable.get(newKey);
			if (currentUnit == null) {
				currentUnit = new Element(newKey, dataTable);
				dataTable.put(newKey, currentUnit);
			}
		}
		return currentUnit;
	}

	private static void setCurrentUnitField(String input, Element currentUnit, boolean flipMode, String[] dataNames, String kInput) {
		final int subXIndex = input.indexOf("X");
		int eIndex = kInput.indexOf("K");
		if (flipMode && kInput.contains("Y")) {
			eIndex = Math.min(kInput.indexOf("Y"), eIndex);
		}
		final int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
		final int fieldId = (subXIndex == -1) || (subXIndex > fieldIdEndIndex) ? 1 : Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
		String fieldValue = kInput.substring(eIndex + 1);
		if ((fieldValue.length() > 1) && fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
			fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
		}
		if (dataNames[fieldId - 1] != null) {
			currentUnit.setField(dataNames[fieldId - 1], fieldValue);
		}
	}

	private static String getkInput(BufferedReader reader, String input) throws IOException {
		final String kInput;
		if (input.startsWith("F;")) {
			kInput = reader.readLine();
			if (DEBUG) {
				System.out.println(kInput);
			}
		} else {
			kInput = input;
		}
		return kInput;
	}


	private void ugg(DataTable dataTable, final InputStream txt){
		final BufferedReader r = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));
		ArrayList<String> htmlTableStrings = new ArrayList<>();
		ArrayList<String> htmlTableCells = new ArrayList<>();
		r.lines().forEach(l -> {
			if (l.startsWith("C;X1;Y")) {
				if (0 < htmlTableCells.size()){
//					htmlTableStrings.add(getHtmlTableRow(combineStrings(htmlTableCells)));
					htmlTableCells.clear();
				}
				String[] split = l.split(";K");
//				htmlTableCells.add(getHtmlTableCell(split[0].split("Y")[1]));
//				htmlTableCells.add(getHtmlTableCell(split[1]));
			} else if (l.matches("C;X\\d+;K.+")){
//				htmlTableCells.add(getHtmlTableCell(l.split(";K")[1]));
			}
		});
	}

	public static void readSLK2(DataTable dataTable, final InputStream txt) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));

		String input = "";
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
			readNormal(dataTable, reader, colCount);
		} else {
			rowCount = Integer.parseInt(input.substring(yIndex, input.lastIndexOf(";")));
			colCount = Integer.parseInt(input.substring(xIndex, yIndex - 2));
			flipMode = true;
			readFlipped(dataTable, reader, null, colCount);
		}

		reader.close();
	}

	private static void readNormal(DataTable dataTable, BufferedReader reader, int colCount) throws IOException {
		Element currentUnit = null;
		String input;
		int rowStartCount = 0;
		String[] dataNames = new String[colCount];

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
			String kInput = getkInput(reader, input);
			if (rowStartCount <= 1) {
				int entryStartIndex = kInput.indexOf("K");
				if ((entryStartIndex == -1) || (kInput.charAt(entryStartIndex - 1) != ';')) {
					continue;
				}

				int subXIndex = input.indexOf("X");
				int subYIndex = input.indexOf("Y");
				if (subXIndex < 0) {
					if (lastFieldId == 0) {
						rowStartCount++;
					}
				}

				int fieldId = getfieldId(input, lastFieldId, kInput, subXIndex, entryStartIndex);
				if ((fieldId - 1) >= dataNames.length) {
					dataNames = Arrays.copyOf(dataNames, fieldId);
				}

				setDataField(dataNames, kInput, entryStartIndex, fieldId);
				lastFieldId = fieldId;
				continue;
			}
			if (input.contains("X1;") || (!input.equals(kInput) && input.endsWith("X1"))) {
				currentUnit = setDataTableField(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
				int subXIndex = input.indexOf("X");
				int eIndex = kInput.indexOf("K");
				int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
				int fieldId = (subXIndex == -1) || (subXIndex > fieldIdEndIndex) ? 1 : Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
				String fieldValue = kInput.substring(eIndex + 1);
				if ((fieldValue.length() > 1) && fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
					fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
				}
				if (dataNames[fieldId - 1] != null) {
					currentUnit.setField(dataNames[fieldId - 1], fieldValue);
				}
			}
		}
	}
	private static void readFlipped(DataTable dataTable, BufferedReader reader, Element currentUnit1, int colCount) throws IOException {
		Element currentUnit = null;
		String input;
		int rowStartCount = 0;
		String[] dataNames = new String[colCount];
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
			String kInput = getkInput(reader, input);
			if (rowStartCount <= 1) {
				int entryStartIndex = kInput.indexOf("K");
				if ((entryStartIndex == -1) || (kInput.charAt(entryStartIndex - 1) != ';')) {
					continue;
				}

				int subXIndex = input.indexOf("X");
				int subYIndex = input.indexOf("Y");
				if (subXIndex < 0) {
					if (lastFieldId == 0) {
						rowStartCount++;
					}
				} else if(!((subYIndex >= 0) && (subYIndex < subXIndex))
						&& input.contains("Y") && input.equals(kInput)){
					entryStartIndex = Math.min(subYIndex, entryStartIndex);
				}

				int fieldId = getfieldId(input, lastFieldId, kInput, subXIndex, entryStartIndex);
				if ((fieldId - 1) >= dataNames.length) {
					dataNames = Arrays.copyOf(dataNames, fieldId);
				}

				setDataField(dataNames, kInput, entryStartIndex, fieldId);
				lastFieldId = fieldId;
				continue;
			}
			if (input.contains("X1;") || (!input.equals(kInput) && input.endsWith("X1"))) {
				currentUnit = setDataTableField(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
				int subXIndex = input.indexOf("X");
				int eIndex = kInput.indexOf("K");
				if (kInput.contains("Y")) {
					eIndex = Math.min(kInput.indexOf("Y"), eIndex);
				}
				int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
				int fieldId = (subXIndex == -1) || (subXIndex > fieldIdEndIndex) ? 1 : Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
				String fieldValue = kInput.substring(eIndex + 1);
				if ((fieldValue.length() > 1) && fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
					fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
				}
				if (dataNames[fieldId - 1] != null) {
					currentUnit.setField(dataNames[fieldId - 1], fieldValue);
				}
			}
		}
	}

	private static int getfieldId(String input, int lastFieldId, String kInput, int subXIndex, int eIndex) {
		int fieldId;
		if (0 <= subXIndex) {
			int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
			fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));;
		} else {
			fieldId = lastFieldId + 1;
		}
		return fieldId;
	}


	public static DataTable getDataTable(String filePath) {
		final DataTable unitMetaData = new DataTable();
		try {
			readSLK(unitMetaData, filePath);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

}
