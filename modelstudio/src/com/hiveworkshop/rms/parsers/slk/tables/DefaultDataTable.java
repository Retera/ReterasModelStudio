package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableUtils;
import com.hiveworkshop.rms.parsers.slk.StringKey;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DefaultDataTable extends DataTable {
	//	Map<StringKey, Element> dataTable = new LinkedHashMap<>();
	String[] sklDatafiles = {
			"Units\\UnitUI.slk",
			"Units\\AbilityData.slk",
			"Units\\UnitData.slk",
			"Units\\UnitAbilities.slk",
			"Units\\UnitBalance.slk",
			"Units\\UnitWeapons.slk",
			"Units\\UpgradeData.slk"};
	String[] txtFiles = {
			"Units\\CampaignUnitFunc.txt",
			"Units\\CampaignUnitStrings.txt",
			"Units\\HumanUnitFunc.txt",
			"Units\\HumanUnitStrings.txt",
			"Units\\NeutralUnitFunc.txt",
			"Units\\NeutralUnitStrings.txt",
			"Units\\NightElfUnitFunc.txt",
			"Units\\NightElfUnitStrings.txt",
			"Units\\OrcUnitFunc.txt",
			"Units\\OrcUnitStrings.txt",
			"Units\\UndeadUnitFunc.txt",
			"Units\\UndeadUnitStrings.txt",

			"Units\\CampaignUpgradeFunc.txt",
			"Units\\CampaignUpgradeStrings.txt",
			"Units\\HumanUpgradeFunc.txt",
			"Units\\HumanUpgradeStrings.txt",
			"Units\\NeutralUpgradeFunc.txt",
			"Units\\NeutralUpgradeStrings.txt",
			"Units\\NightElfUpgradeFunc.txt",
			"Units\\NightElfUpgradeStrings.txt",
			"Units\\OrcUpgradeFunc.txt",
			"Units\\OrcUpgradeStrings.txt",
			"Units\\UndeadUpgradeFunc.txt",
			"Units\\UndeadUpgradeStrings.txt",

			"Units\\CampaignAbilityFunc.txt",
			"Units\\CampaignAbilityStrings.txt",
			"Units\\CommonAbilityFunc.txt",
			"Units\\CommonAbilityStrings.txt",
			"Units\\HumanAbilityFunc.txt",
			"Units\\HumanAbilityStrings.txt",
			"Units\\NeutralAbilityFunc.txt",
			"Units\\NeutralAbilityStrings.txt",
			"Units\\NightElfAbilityFunc.txt",
			"Units\\NightElfAbilityStrings.txt",
			"Units\\OrcAbilityFunc.txt",
			"Units\\OrcAbilityStrings.txt",
			"Units\\UndeadAbilityFunc.txt",
			"Units\\UndeadAbilityStrings.txt",
			"Units\\ItemAbilityFunc.txt",
			"Units\\ItemAbilityStrings.txt",
			"Units\\ItemStrings.txt",
			"Units\\UnitSkin.txt"};

	public DefaultDataTable() {
//		loadDefaults();
		loadStuff(sklDatafiles, txtFiles, false);
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


	public void loadDefaults() {
		try {
			for (String sklData : sklDatafiles) {
				DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream(sklData));
			}
			for (String txt : txtFiles) {
				DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream(txt), false);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	public void loadDefaults1() {
		try {
			// DataTableUtils.readSLK(MpqNativeCodebase.get().getGameFile("Units\\AbilityBuffData.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitUI.slk"));
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\AbilityData.slk"));
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
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitSkin.txt"));

	}

//	@Override
//	public Element get(final String id) {
//		return dataTable.get(new StringKey(id));
//	}
//
//	@Override
//	public void setValue(final String id, final String field, final String value) {
//		get(id).setField(field, value);
//	}
//
//	public void put(final String id, final Element e) {
//		dataTable.put(new StringKey(id), e);
//	}
}
