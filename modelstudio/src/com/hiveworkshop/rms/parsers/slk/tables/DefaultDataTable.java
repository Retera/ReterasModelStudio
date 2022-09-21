package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;

public class DefaultDataTable extends DataTable {
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
		loadStuff(sklDatafiles, txtFiles, false);
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
		DataTableUtils.readTXT(this, "Units\\CampaignUnitFunc.txt");
		DataTableUtils.readTXT(this, "Units\\CampaignUnitStrings.txt");
		DataTableUtils.readTXT(this, "Units\\HumanUnitFunc.txt");
		DataTableUtils.readTXT(this, "Units\\HumanUnitStrings.txt");
		DataTableUtils.readTXT(this, "Units\\NeutralUnitFunc.txt");
		DataTableUtils.readTXT(this, "Units\\NeutralUnitStrings.txt");
		DataTableUtils.readTXT(this, "Units\\NightElfUnitFunc.txt");
		DataTableUtils.readTXT(this, "Units\\NightElfUnitStrings.txt");
		DataTableUtils.readTXT(this, "Units\\OrcUnitFunc.txt");
		DataTableUtils.readTXT(this, "Units\\OrcUnitStrings.txt");
		DataTableUtils.readTXT(this, "Units\\UndeadUnitFunc.txt");
		DataTableUtils.readTXT(this, "Units\\UndeadUnitStrings.txt");

		DataTableUtils.readTXT(this, "Units\\CampaignUpgradeFunc.txt");
		DataTableUtils.readTXT(this, "Units\\CampaignUpgradeStrings.txt");
		DataTableUtils.readTXT(this, "Units\\HumanUpgradeFunc.txt");
		DataTableUtils.readTXT(this, "Units\\HumanUpgradeStrings.txt");
		DataTableUtils.readTXT(this, "Units\\NeutralUpgradeFunc.txt");
		DataTableUtils.readTXT(this, "Units\\NeutralUpgradeStrings.txt");
		DataTableUtils.readTXT(this, "Units\\NightElfUpgradeFunc.txt");
		DataTableUtils.readTXT(this, "Units\\NightElfUpgradeStrings.txt");
		DataTableUtils.readTXT(this, "Units\\OrcUpgradeFunc.txt");
		DataTableUtils.readTXT(this, "Units\\OrcUpgradeStrings.txt");
		DataTableUtils.readTXT(this, "Units\\UndeadUpgradeFunc.txt");
		DataTableUtils.readTXT(this, "Units\\UndeadUpgradeStrings.txt");

		DataTableUtils.readTXT(this, "Units\\CampaignAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\CampaignAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\CommonAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\CommonAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\HumanAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\HumanAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\NeutralAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\NeutralAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\NightElfAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\NightElfAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\OrcAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\OrcAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\UndeadAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\UndeadAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\ItemAbilityFunc.txt");
		DataTableUtils.readTXT(this, "Units\\ItemAbilityStrings.txt");
		DataTableUtils.readTXT(this, "Units\\ItemStrings.txt");
		DataTableUtils.readTXT(this, "Units\\UnitSkin.txt");

	}
}
