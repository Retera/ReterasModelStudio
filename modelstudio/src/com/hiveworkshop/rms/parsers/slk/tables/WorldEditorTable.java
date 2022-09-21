package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class WorldEditorTable extends DataTable {
	String[] sklDatafiles = {};
	String[] txtFiles = {"UI\\UnitEditorData.txt", "UI\\WorldEditData.txt"};

	public WorldEditorTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
