package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class SpawnsTable extends DataTable {
	String[] sklDatafiles = {"Splats\\SpawnData.slk"};
	String[] txtFiles = {};

	public SpawnsTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
