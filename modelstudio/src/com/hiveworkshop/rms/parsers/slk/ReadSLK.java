package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReadSLK {
	private final boolean DEBUG = false;

	Map<String, Map<String, String>> keyMap1 = new HashMap<>();
	Map<String, Map<String, String>> keyMap2 = new HashMap<>();
	List<List<String>> tableStringsXY = new ArrayList<>();
	List<List<String>> tableDescriptionsXY = new ArrayList<>();


	public void readSLK(DataTable dataTable, final File f) {
		try {
			readSLK(dataTable, new FileInputStream(f));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public static void main(String[] args){
//		String l = "C;X15;Y1;K\"category\"";
//		String l = "C;X36;AMScipione:\u001B :Starting stock after stockStart finishes. No effect if stockStart is 0. \u001B :Respects stockMax.";
//		String l = "C;X2;K\"Crown of Kings +5\"";
		String l = "\"Crown of Kings +5\"";
//		String l = "8";
//		String s1 = l.replaceAll(".*;X", "");
//		String s1 = l.replaceAll("((?<=;X\\d)(?<=\\d)*;.*)|(.*;X)", "");
//		String s1 = l.replaceAll("((?<=;X(\\d+));.*)|(.*;X)", "");
//		String s1 = l.replaceAll("((?<=^[^xX]*;X(\\d+));[^xX]*)|([^xX]*;X)|(^[^xX]*$)", "");
//		String s1 = l.replaceAll("((?<=^([^Xx]*)(;X(\\d+)));.*)|(^[^xX]*$)|(^[^Xx]*;X)", "");
//		String s1 = l.replaceAll("((?<=^([^Xx]{0,20})(;X(\\d{0,20})));.*)", "");

//		String s1 = l.replaceAll("((?<=^([^Xx]{0,20})(;X(\\d{0,20})));.*)|(^[^xX]*$)|(^[^Xx]*;X)", "");
//		System.out.println("replaceAll: \"" + s1 + "\"");
//		String[] split = l.split("[^X]*;X");
//		String[] split = l.split(";X");

//		String[] split = l.split("^C(;[XY]\\d+){0,2};");
//		System.out.println("\"" + l + "\", split into: " + split.length);
//		for(String s : split){
//			System.out.println("\"" + s + "\"");
//		}
		String s1 = l.replaceAll("(^[^\"]*\")|(\"[^\"]*$)", "");
		System.out.println("replaceAll: '" + s1 + "'");

		String ugg = "C;X2;K\"effectID\"";
		String[] split = ugg.split("^C(;[XY]\\d+){0,2};K", 2);
		for(String s : split){
			System.out.println("split:  " + "\"" + s + "\"");
		}
		System.out.println("string after: '" + getStringAfter2(ugg, "^C(;[XY]\\d+){0,2};K") + "'");

	}
	private static String getStringAfter2(String s, String c){
		String[] split = s.split(c, 2);
		if(1<split.length){
			return split[1];
		}
		return "";
	}

	private int getNum(String s, String c){
		String[] split = s.split(c);
		if(1<split.length){
			String s1 = split[1].split(";")[0];
			if(s1.matches("\\d+")){
				return Integer.parseInt(s1);
			}
		}
		return -1;
	}
	private int getNum(String s, String c, int fallback){
		String[] split = s.split(c);
		if(1<split.length){
			String s1 = split[1].split(";")[0];
			if(s1.matches("\\d+")){
				return Integer.parseInt(s1);
			}
		}
		return fallback;
	}
	private String getStringAfter(String s, String c){
		String[] split = s.split(c, 2);
		if(1<split.length){
			return split[1];
		}
		return "";
	}

	public void readSLK(DataTable dataTable, final InputStream txt) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));
		final int[] tableCoords = {-1, -1};
		final Boolean[] yBeforeX = new Boolean[1];
		reader.lines().forEach(l -> {
			tableCoords[1] = getNum(l, ";Y", tableCoords[1]);
			tableCoords[0] = getNum(l, ";X", tableCoords[0]);
			int x = tableCoords[0];
			int y = tableCoords[1];
//			System.out.println("'" + l + "', x: " + x + ", y: " + y);
			if(yBeforeX[0] == null && l.matches("^C(;[XY]\\d+){2}.*")){
				yBeforeX[0] = l.matches("^C(;Y\\d+)(;X\\d+).*");
				System.out.println("in Y before X: " + yBeforeX[0]);
			}
			if(l.matches("^C(;[XY]\\d+){0,2};K.*")){
				readValueField(l, x, y);
			} else if(l.matches("^C(;[XY]\\d+){0,2};A.*")){
				readTableDecription(l, x, y);
			}
		});
		reader.close();

		fillDataTable(dataTable, yBeforeX);
	}

	private void fillDataTable(DataTable dataTable, Boolean[] yBeforeX) {
		if(yBeforeX[0] == null || !yBeforeX[0]){
			// field names
			List<String> fieldNames = tableStringsXY.get(0);
			fieldNames.replaceAll(s -> s.replaceAll("(^[^\"]*\")|(\"[^\"]*$)", ""));

			for(int unitIndex = 1; unitIndex<tableStringsXY.size(); unitIndex++){
				// create unit
				List<String> x_list = tableStringsXY.get(unitIndex);
//				System.out.println("unitIndex: " + unitIndex + ", values: " + x_list.size());
				if(!x_list.isEmpty()){
					String unitName = x_list.get(0);
					String newKey = unitName.replaceAll("(^[^\"]*\")|(\"[^\"]*$)", "");

					Element currentUnit = dataTable.get(newKey);
					if(unitIndex<2){
						System.out.println("current unit: " + currentUnit);
					}
					if(currentUnit == null){
						currentUnit = new Element(newKey, dataTable);
						dataTable.put(currentUnit);
						if(unitIndex<2){
							System.out.println("current unit2: " + currentUnit);
						}
					}
//					Element currentUnit = dataTable.computeIfAbsent(newKey);

					if(currentUnit != null){
						for(int fieldIndex = 1; fieldIndex<x_list.size(); fieldIndex++){
							String fieldName = fieldNames.get(fieldIndex);
							String fieldString = x_list.get(fieldIndex);
							if(fieldName != null){
								String fieldValue = fieldString.replaceAll("(^[^\"]*\")|(\"[^\"]*$)", "");
								currentUnit.setField(fieldName, fieldValue);
								if(unitIndex<2){
									System.out.println(currentUnit.id + ": setting field " + fieldIndex +" '" + fieldName + "' to '" + fieldValue + "'");
								}
							}
						}

					}
				}

			}
		}
	}

	private void readValueField(String l, int x, int y) {
		List<String> x_list = null;
		if (y <= tableStringsXY.size()) {
			x_list = tableStringsXY.get(y - 1);
		} else if ((y -1) == tableStringsXY.size()){
			x_list = new ArrayList<>();
			tableStringsXY.add(x_list);
		}
//		if(x < 10 && y < 10){
//			if(x_list != null){
//				System.out.println("value: '" + l + "', x_list: " + x_list.size() + ", x: " + x + ", y: " + y);
//			} else {
//				System.out.println("value: '" + l + "', x_list: " + null);
//			}
//		}
		if(x_list != null && x-1 == x_list.size()){
			String field = getStringAfter(l, "^C(;[XY]\\d+){0,2};K");
			if(!field.isEmpty()){
				x_list.add(field);
			}
		}
	}

	private void readTableDecription(String l, int x, int y) {
		List<String> x_list = null;
		if (y <= tableDescriptionsXY.size()) {
			x_list = tableDescriptionsXY.get(y - 1);
		} else if ((y -1) == tableDescriptionsXY.size()){
			x_list = new ArrayList<>();
			tableDescriptionsXY.add(x_list);
		}
		System.out.println("description: " + l + ", x_list: " + x_list);
		if(x_list != null){
			String desc = getStringAfter(l, "^C(;[XY]\\d+){0,2};A");
			if(!desc.isEmpty()){
				if(x == x_list.size()){
					x_list.add(desc);
				} else if (x == x_list.size()-1){
					x_list.set(x -1, x_list.get(x -1) + desc);
				}
			}
		}
	}


	private void setData2(ReaderTracker tracker, String input, Element currentUnit, String[] dataNames, String kInput) {
		int eIndex = kInput.indexOf("K");
		if (tracker.flipMode && kInput.contains("Y")) {
			eIndex = Math.min(kInput.indexOf("Y"), eIndex);
		}
		int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
		int subXIndex = input.indexOf("X");
		int fieldId = (subXIndex == -1) || (subXIndex > fieldIdEndIndex) ? 1 : Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));

		String fieldValue = kInput.substring(eIndex + 1);
		if ((fieldValue.length() > 1) && fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
			fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
		}

		if (dataNames[fieldId - 1] != null) {
			currentUnit.setField(dataNames[fieldId - 1], fieldValue);
		}
	}
	public void readSLK1(DataTable dataTable, final InputStream txt) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, StandardCharsets.UTF_8));
		ReaderTracker tracker = new ReaderTracker();
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
		countColRows(tracker, input);
		tracker.rowStartCount = 0;
		String[] dataNames = new String[tracker.colCount];

		tracker.col = 0;
		tracker.lastFieldId = 0;
		while ((input = reader.readLine()) != null) {
			debugLog(input);
			if (input.startsWith("E")) {
				break;
			}
			if (input.startsWith("O;")) {
				continue;
			}
			incrementRowCol(tracker, input);
			String kInput = getkInput(reader, input);
			if (tracker.rowStartCount <= 1) {
				dataNames = getFieldNames(tracker, input, dataNames, kInput);
			} else if (input.contains("X1;") || ((!input.equals(kInput)) && input.endsWith("X1"))) {
				currentUnit = getElement(dataTable, currentUnit, kInput);
			} else if (kInput.contains("K")) {
				setData(tracker, input, currentUnit, dataNames, kInput);
			}
		}

		reader.close();
	}

	private void countColRows(ReaderTracker tracker, String input) {
		tracker.yIndex = input.indexOf("Y") + 1;
		tracker.xIndex = input.indexOf("X") + 1;
		tracker.colCount = 0;
		tracker.rowCount = 0;
		if (tracker.xIndex > tracker.yIndex) {
			tracker.colCount = Integer.parseInt(input.substring(tracker.xIndex, input.lastIndexOf(";")));
			tracker.rowCount = Integer.parseInt(input.substring(tracker.yIndex, tracker.xIndex - 2));
			tracker.flipMode = false;
		} else {
			tracker.rowCount = Integer.parseInt(input.substring(tracker.yIndex, input.lastIndexOf(";")));
			tracker.colCount = Integer.parseInt(input.substring(tracker.xIndex, tracker.yIndex - 2));
			tracker.flipMode = true;
		}
	}

	private String getkInput(BufferedReader reader, String input) throws IOException {
		String kInput;
		if (input.startsWith("F;")) {
			kInput = reader.readLine();
			debugLog(kInput);
		} else {
			kInput = input;
		}
		return kInput;
	}

	private void incrementRowCol(ReaderTracker tracker, String input) {
		if (input.contains("X1;")) {
			tracker.rowStartCount++;
			tracker.col = 0;
		} else {
			tracker.col++;
		}
	}

	private void setData(ReaderTracker tracker, String input, Element currentUnit, String[] dataNames, String kInput) {
		int eIndex = kInput.indexOf("K");
		if (tracker.flipMode && kInput.contains("Y")) {
			eIndex = Math.min(kInput.indexOf("Y"), eIndex);
		}
		int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
		int subXIndex = input.indexOf("X");
		int fieldId = (subXIndex == -1) || (subXIndex > fieldIdEndIndex) ? 1 : Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));

		String fieldValue = kInput.substring(eIndex + 1);
		if ((fieldValue.length() > 1) && fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
			fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
		}

		if (dataNames[fieldId - 1] != null) {
			currentUnit.setField(dataNames[fieldId - 1], fieldValue);
		}
	}

	private Element getElement(DataTable dataTable, Element currentUnit, String kInput) {
		int start = kInput.indexOf("\"") + 1;
		int end = kInput.lastIndexOf("\"");
		if ((start - 1) != end) {
			String newKey = kInput.substring(start, end);
			currentUnit = dataTable.get(newKey);
			if (currentUnit == null) {
				currentUnit = new Element(newKey, dataTable);
				dataTable.put(newKey, currentUnit);
			}
		}
		return currentUnit;
	}

	private String[] getFieldNames(ReaderTracker tracker, String input, String[] dataNames, String kInput) {
		if(kInput.contains(";K")){
			int fieldId;

			int entryStartIndex = kInput.indexOf("K");
			int subXIndex = input.indexOf("X");
			if (subXIndex < 0) {
				if (tracker.lastFieldId == 0) {
					tracker.rowStartCount++;
				}
				fieldId = tracker.lastFieldId + 1;
			} else {
				int subYIndex = input.indexOf("Y");
				if (!(subYIndex >= 0 && subYIndex < subXIndex) && tracker.flipMode && input.contains("Y") && input.equals(kInput)) {
					entryStartIndex = Math.min(subYIndex, entryStartIndex);
				}
				int fieldIdEndIndex = !kInput.equals(input) ? input.length() : entryStartIndex - 1;
				fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
			}


			if ((fieldId - 1) >= dataNames.length) {
				dataNames = Arrays.copyOf(dataNames, fieldId);
			}
			if (kInput.contains("\"")) {
				// is string field
				dataNames[fieldId - 1] = kInput.substring(kInput.indexOf("\"") + 1, kInput.lastIndexOf("\""));
			} else {
				// is value field
				dataNames[fieldId - 1] = kInput.substring(entryStartIndex + 1);
			}
			tracker.lastFieldId = fieldId;
		}
		return dataNames;
	}
	private String[] getStrings1(ReaderTracker tracker, String input, String[] dataNames, String kInput) {
		int subXIndex = input.indexOf("X");
		int subYIndex = input.indexOf("Y");
		if ((subYIndex >= 0) && (subYIndex < subXIndex)) {
			int eIndex = kInput.indexOf("K");
			if ((eIndex == -1) || (kInput.charAt(eIndex - 1) != ';')) {
				return dataNames;
			}
			int fieldId;
			if (subXIndex < 0) {
				if (tracker.lastFieldId == 0) {
					tracker.rowStartCount++;
				}
				fieldId = tracker.lastFieldId + 1;
			} else {
				int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
				fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
			}

			int quotationIndex = kInput.indexOf("\"");
			if ((fieldId - 1) >= dataNames.length) {
				dataNames = Arrays.copyOf(dataNames, fieldId);
			}
			if (quotationIndex == -1) {
				dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
			} else {
				dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
			}
			tracker.lastFieldId = fieldId;
		} else {
			int eIndex = kInput.indexOf("K");
			if ((eIndex == -1) || (kInput.charAt(eIndex - 1) != ';')) {
				return dataNames;
			}
			int fieldId;
			if (subXIndex < 0) {
				if (tracker.lastFieldId == 0) {
					tracker.rowStartCount++;
				}
				fieldId = tracker.lastFieldId + 1;
			} else {
				if (tracker.flipMode && input.contains("Y") && (input.equals(kInput))) {
					eIndex = Math.min(subYIndex, eIndex);
				}
				int fieldIdEndIndex = !kInput.equals(input) ? input.length() : eIndex - 1;
				fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
			}

			int quotationIndex = kInput.indexOf("\"");
			if ((fieldId - 1) >= dataNames.length) {
				dataNames = Arrays.copyOf(dataNames, fieldId);
			}
			if (quotationIndex == -1) {
				dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
			} else {
				dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
			}
			tracker.lastFieldId = fieldId;
		}
		return dataNames;
	}

	public static DataTable getDataTable(String filePath) {
		final DataTable unitMetaData = new DataTable();
		try {
//			readSLK(unitMetaData, filePath);
			System.out.println("reading slk: '" + filePath + "'");
			new ReadSLK().readSLK(unitMetaData, GameDataFileSystem.getDefault().getResourceAsStream(filePath));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public void readSLK(DataTable dataTable, String filepath) throws IOException {
		readSLK(dataTable, GameDataFileSystem.getDefault().getResourceAsStream(filepath));
	}

	private static class ReaderTracker {
		int yIndex;
		int xIndex;
		int colCount;
		int rowCount = 0;
		boolean flipMode;
		int rowStartCount ;
		String[] dataNames;
		int col = 0;
		int lastFieldId = 0;
	}

	private void debugLog(String s){
		if(DEBUG){
			System.out.println(s);
		}
	}
}
