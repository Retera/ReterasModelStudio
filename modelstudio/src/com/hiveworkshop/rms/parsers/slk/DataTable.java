package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataTable implements ObjectData {
	private static final boolean DEBUG = false;
	static DataTable theTable;
	static DataTable spawnTable;
	static DataTable splatTable;
	static DataTable terrainTable;
	static DataTable ginterTable;
	static DataTable unitEditorDataTable;
	static DataTable buffTable;
	static DataTable itemTable;
	static DataTable theTableDestructibles;
	static DataTable theTableDoodads;

	public static void dropCache() {
		theTable = null;
		spawnTable = null;
		splatTable = null;
		terrainTable = null;
		ginterTable = null;
		buffTable = null;
		itemTable = null;
		theTableDestructibles = null;
		theTableDoodads = null;
	}

	public static DataTable get() {
		if (theTable == null) {
			theTable = new DataTable();
			theTable.loadDefaults();
		}
		return theTable;
	}

	public static DataTable getDoodads() {
		if (theTableDoodads == null) {
			theTableDoodads = new DataTable();
			theTableDoodads.loadDoodads();
		}
		return theTableDoodads;
	}

	public static DataTable getDestructables() {
		if (theTableDestructibles == null) {
			theTableDestructibles = new DataTable();
			theTableDestructibles.loadDestructibles();
		}
		return theTableDestructibles;
	}

	public static DataTable getItems() {
		if (itemTable == null) {
			itemTable = new DataTable();
			itemTable.loadItems();
		}
		return itemTable;
	}

	public static DataTable getBuffs() {
		if (buffTable == null) {
			buffTable = new DataTable();
			buffTable.loadBuffs();
		}
		return buffTable;
	}

	public static DataTable getSpawns() {
		if (spawnTable == null) {
			spawnTable = new DataTable();
			spawnTable.loadSpawns();
		}
		return spawnTable;
	}

	public static DataTable getSplats() {
		if (splatTable == null) {
			splatTable = new DataTable();
			splatTable.loadSplats();
		}
		return splatTable;
	}

	public static DataTable getTerrain() {
		if (terrainTable == null) {
			terrainTable = new DataTable();
			terrainTable.loadTerrain();
		}
		return terrainTable;
	}

	public static DataTable getGinters() {
		if (ginterTable == null) {
			ginterTable = new DataTable();
			ginterTable.loadGinters();
		}
		return ginterTable;
	}

	public static DataTable getWorldEditorData() {
		if (unitEditorDataTable == null) {
			unitEditorDataTable = new DataTable();
			unitEditorDataTable.loadUnitEditorData();
		}
		return unitEditorDataTable;
	}

	Map<StringKey, Element> dataTable = new LinkedHashMap<>();

	public DataTable() {

	}

	@Override
	public Set<String> keySet() {
		final Set<String> outputKeySet = new HashSet<>();
		final Set<StringKey> internalKeySet = dataTable.keySet();
		for (final StringKey key : internalKeySet) {
			outputKeySet.add(key.getString());
		}
		return outputKeySet;
	}

	public void loadDestructibles() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\DestructableData.slk"));
			final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Units\\DestructableSkin.txt");
			if (unitSkin != null) {
				readTXT(unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDoodads() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Doodads\\Doodads.slk"));
			final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Doodads\\DoodadSkins.txt");
			if (unitSkin != null) {
				readTXT(unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadItems() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemStrings.txt"));
	}

	public void loadBuffs() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\AbilityBuffData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
	}

	public void loadSpawns() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Splats\\SpawnData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadSplats() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Splats\\SplatData.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Splats\\UberSplatData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadTerrain() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("TerrainArt\\Terrain.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadGinters() {
		try {
			readTXT(GameDataFileSystem.getDefault().getResourceAsStream("UI\\war3skins.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadUnitEditorData() {
		try {
			readTXT(GameDataFileSystem.getDefault().getResourceAsStream("UI\\UnitEditorData.txt"), true);
			readTXT(GameDataFileSystem.getDefault().getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDefaults() {
		try {
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitUI.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\AbilityData.slk"));
			// readSLK(MpqNativeCodebase.get().getGameFile("Units\\AbilityBuffData.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitData.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitAbilities.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitBalance.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitWeapons.slk"));
			readSLK(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UpgradeData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUnitFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUnitStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUnitFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUnitStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUnitFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUnitStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUnitFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUnitStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUnitFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUnitStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUnitFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUnitStrings.txt"));

		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUpgradeFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUpgradeStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUpgradeFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUpgradeStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUpgradeFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUpgradeStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUpgradeFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUpgradeStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUpgradeFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUpgradeStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUpgradeFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUpgradeStrings.txt"));

		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
		readTXT(GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemStrings.txt"));
		final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitSkin.txt");
		if (unitSkin != null) {
			readTXT(unitSkin);
		}
	}


	public void readTXT(final InputStream inputStream) {
		try {
			readTXT(inputStream, false);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void readTXT(final File f) {
		readTXT(f, false);
	}

	public void readTXT(final File f, final boolean canProduce) {
		try {
			readTXT(new FileInputStream(f), canProduce);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void readSLK(final File f) {
		try {
			readSLK(new FileInputStream(f));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void readTXT(final InputStream txt, final boolean canProduce) throws IOException {
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
				currentUnit = dataTable.get(new StringKey(newKey));
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
					currentUnit = new Element(newKey, this);
					if (canProduce) {
						currentUnit = new LMUnit(newKey, this);
						dataTable.put(new StringKey(newKey), currentUnit);
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

	public void readSLK(final InputStream txt) throws IOException {
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
					currentUnit = dataTable.get(new StringKey(newKey));
					if (currentUnit == null) {
						currentUnit = new Element(newKey, this);
						dataTable.put(new StringKey(newKey), currentUnit);
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

	@Override
	public Element get(final String id) {
		return dataTable.get(new StringKey(id));
	}

	@Override
	public void setValue(final String id, final String field, final String value) {
		get(id).setField(field, value);
	}

	public void put(final String id, final Element e) {
		dataTable.put(new StringKey(id), e);
	}

	// public Unit getFallyWorker() {
	// return dataTable.get("h02Z");
	// }
	//
	// public Unit getFallyWorker2() {
	// return dataTable.get("h03P");
	// }
	//
	// public Unit getTribeWorker() {
	// return dataTable.get("opeo");
	// }
	//
	// public Unit getTideWorker() {
	// return dataTable.get("ewsp");
	// }
	//
	// public Unit getVoidWorker() {
	// return dataTable.get("e007");
	// }
	//
	// public Unit getElfWorker() {
	// return dataTable.get("e000");
	// }
	//
	// public Unit getHumanWorker() {
	// return dataTable.get("h001");
	// }
	//
	// public Unit getOrcWorker() {
	// return dataTable.get("o000");
	// }
	//
	// public Unit getUndeadWorker() {
	// return dataTable.get("u001");
	// }

	// public static void main(String [] args) {
	// UnitDataTable table = new UnitDataTable();
	// table.loadDefaults();
	// Unit villager = table.get("h02Z");
	// System.out.println(villager.getField("Name")+ " can build: ");
	// System.out.println(villager.builds());
	//
	// System.out.println();
	//
	// Unit townSquare = table.get("owtw");
	// System.out.println(townSquare.getField("Name")+ " trains: ");
	// System.out.println(townSquare.trains());
	//
	// System.out.println(townSquare.getField("Name")+ " upgrades: ");
	// System.out.println(townSquare.upgrades());
	//
	// System.out.println(townSquare.getField("Name")+ " researches: ");
	// System.out.println(townSquare.researches());
	//
	// System.out.println(townSquare.getField("Name")+ " stats: ");
	// for( String field: townSquare.fields.keySet() ) {
	// System.out.println(field +": "+townSquare.getField(field));
	// }
	//// System.out.println(townSquare.getField("goldcost"));
	//// System.out.println(townSquare.getField("lumbercost"));
	//// System.out.println(townSquare.getField("fmade"));
	//// System.out.println(townSquare.getField("fmade"));
	//
	// List<Unit> abils = table.getTideWorker().abilities();
	// System.out.println(abils);
	// for( Unit abil: abils ) {
	// System.out.println(abil.getUnitId());
	// }
	// }
}
