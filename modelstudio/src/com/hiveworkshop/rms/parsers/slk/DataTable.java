package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DataTable implements ObjectData {
	Map<StringKey, Element> dataTable = new LinkedHashMap<>();

	public DataTable() {

	}

	@Override
	public Set<String> keySet() {
		Set<String> outputKeySet = new HashSet<>();
		Set<StringKey> internalKeySet = dataTable.keySet();
		for (StringKey key : internalKeySet) {
			outputKeySet.add(key.getString());
		}
		return outputKeySet;
	}

	public void loadDestructibles() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\DestructableData.slk"));
			final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Units\\DestructableSkin.txt");
			if (unitSkin != null) {
				DataTableUtils.readTXT(this, unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDoodads() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Doodads\\Doodads.slk"));
			final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Doodads\\DoodadSkins.txt");
			if (unitSkin != null) {
				DataTableUtils.readTXT(this, unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadItems() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemStrings.txt"));
	}

	public void loadBuffs() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\AbilityBuffData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
	}

	public void loadSpawns() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Splats\\SpawnData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadSplats() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Splats\\SplatData.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Splats\\UberSplatData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadTerrain() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("TerrainArt\\Terrain.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadGinters() {
		try {
			DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("UI\\war3skins.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadUnitEditorData() {
		try {
			DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("UI\\UnitEditorData.txt"), true);
			DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDefaults() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitUI.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\AbilityData.slk"));
			// DataTableUtils.readSLK(MpqNativeCodebase.get().getGameFile("Units\\AbilityBuffData.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitData.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitAbilities.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitBalance.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitWeapons.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UpgradeData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUnitFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUnitStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUnitFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUnitStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUnitFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUnitStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUnitFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUnitStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUnitFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUnitStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUnitFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUnitStrings.txt"));

		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUpgradeFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignUpgradeStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUpgradeFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanUpgradeStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUpgradeFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralUpgradeStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUpgradeFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfUpgradeStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUpgradeFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcUpgradeStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUpgradeFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadUpgradeStrings.txt"));

		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemStrings.txt"));
		final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitSkin.txt");
		if (unitSkin != null) {
			DataTableUtils.readTXT(this, unitSkin);
		}
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
