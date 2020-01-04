package com.hiveworkshop.wc3.units;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

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
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\DestructableData.slk"));
			final InputStream unitSkin = MpqCodebase.get().getResourceAsStream("Units\\DestructableSkin.txt");
			if (unitSkin != null) {
				readTXT(unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDoodads() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("Doodads\\Doodads.slk"));
			final InputStream unitSkin = MpqCodebase.get().getResourceAsStream("Doodads\\DoodadSkins.txt");
			if (unitSkin != null) {
				readTXT(unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadItems() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\ItemData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemStrings.txt"));
	}

	public void loadBuffs() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\AbilityBuffData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
	}

	public void loadSpawns() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("Splats\\SpawnData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadSplats() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("Splats\\SplatData.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Splats\\UberSplatData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadTerrain() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("TerrainArt\\Terrain.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadGinters() {
		try {
			readTXT(MpqCodebase.get().getResourceAsStream("UI\\war3skins.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadUnitEditorData() {
		try {
			readTXT(MpqCodebase.get().getResourceAsStream("UI\\UnitEditorData.txt"), true);
			readTXT(MpqCodebase.get().getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDefaults() {
		try {
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\UnitUI.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\AbilityData.slk"));
			// readSLK(MpqNativeCodebase.get().getGameFile("Units\\AbilityBuffData.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\UnitData.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\UnitAbilities.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\UnitBalance.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\UnitWeapons.slk"));
			readSLK(MpqCodebase.get().getResourceAsStream("Units\\UpgradeData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignUnitFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignUnitStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanUnitFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanUnitStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralUnitFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralUnitStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfUnitFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfUnitStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcUnitFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcUnitStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadUnitFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadUnitStrings.txt"));

		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignUpgradeFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignUpgradeStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanUpgradeFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanUpgradeStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralUpgradeFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralUpgradeStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfUpgradeFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfUpgradeStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcUpgradeFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcUpgradeStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadUpgradeFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadUpgradeStrings.txt"));

		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
		readTXT(MpqCodebase.get().getResourceAsStream("Units\\ItemStrings.txt"));
		final InputStream unitSkin = MpqCodebase.get().getResourceAsStream("Units\\UnitSkin.txt");
		if (unitSkin != null) {
			readTXT(unitSkin);
		}
		// readTXT(MpqNativeCodebase.get().getGameFile("war3mapMisc.txt"));

		// Specific data edits for tech browser
		// Unit castleAge = dataTable.get("R035");
		// castleAge.setField("Name", "Castle Age");
		// get("h030").addResearches("R03Y");
		//
		//// getVoidWorker().addToList("n03A","Builds");
		// Unit empoweredNetherling = get("n03J");
		// empoweredNetherling.setField("Upgrade",empoweredNetherling.getField("Upgrade").replace("n03A","null"));
		// get("h02Y").setField("Name", "Shrine of the Æther");
		// get("h02H").setField("Name", "Æthergate");
		// get("h02G").setField("Name", "Vault of the Æther");
		// get("h02V").setField("Name", "Ætherstorm Tower");

		// //Loading of data
		// for( String unitid: dataTable.keySet() ) {
		// Unit u = dataTable.get(unitid);
		//// u.setField("Parents",u.getField("Requires"));
		// for( Unit req: u.requires() ) {
		// u.addParent(req.getUnitId());
		// }
		// }
		//
		// for( String unitid: dataTable.keySet() ) {
		// Unit u = dataTable.get(unitid);
		// for( Unit upgrade: u.upgrades() ) {
		// upgrade.addParent(u.getUnitId());
		// }
		// }
		//
		// for( String unitid: dataTable.keySet() ) {
		// Unit u = dataTable.get(unitid);
		// for( Unit upgrade: u.researches() ) {
		// upgrade.addParent(u.getUnitId());
		// }
		// }
		//
		// //Now calculate Children values
		//
		//
		//
		// for( String unitid: dataTable.keySet() ) {
		// Unit u = dataTable.get(unitid);
		// for( Unit upgrade: u.parents() ) {
		// upgrade.addChild(u.getUnitId());
		// }
		// for( Unit upgrade: u.requires() ) {
		// upgrade.addRequiredBy(u.getUnitId());
		// }
		// }
	}

	// public void updateListWithLevels(List<Unit> list, List<Integer> levels) {
	// for( int i = 0; i < levels.size() && i < list.size(); i++ ) {
	// int level = levels.get(i);
	// if( level == 2 && list.get(i).equals(get("R035")) ) {
	// list.set(i, get("R03Y"));
	// //Enforce that "Level 2 Castle Age" is considered
	// // to be "Golden Age"
	// }
	// }
	// }

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
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, "utf-8"));
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
				final String newKeyBase = newKey;
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
		final BufferedReader reader = new BufferedReader(new InputStreamReader(txt, "utf-8"));

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
		final String[] dataNames = new String[colCount];
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
			String kInput;
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
					final int fieldIdEndIndex = kInput != input ? input.length() : eIndex - 1;
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
					if (quotationIndex == -1) {
						dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
					} else {
						dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
					}
					lastFieldId = fieldId;
					continue;
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
						if (flipMode && input.contains("Y") && (input == kInput)) {
							eIndex = Math.min(subYIndex, eIndex);
						}
						final int fieldIdEndIndex = kInput != input ? input.length() : eIndex - 1;
						fieldId = Integer.parseInt(input.substring(subXIndex + 1, fieldIdEndIndex));
					}

					final int quotationIndex = kInput.indexOf("\"");
					if (quotationIndex == -1) {
						dataNames[fieldId - 1] = kInput.substring(eIndex + 1);
					} else {
						dataNames[fieldId - 1] = kInput.substring(quotationIndex + 1, kInput.lastIndexOf("\""));
					}
					lastFieldId = fieldId;
					continue;
				}
			}
			// if( rowStartCount == 2)
			// System.out.println(Arrays.toString(dataNames));
			if (input.contains("X1;") || ((input != kInput) && input.endsWith("X1"))) {
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
				final int fieldIdEndIndex = kInput != input ? input.length() : eIndex - 1;
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
