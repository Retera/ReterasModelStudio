package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class BuffsTable extends DataTable {
	String[] sklDatafiles = {"Units\\AbilityBuffData.slk"};
	String[] txtFiles = {
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
			"Units\\ItemAbilityStrings.txt"};

	public BuffsTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
