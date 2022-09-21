package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class UnitEditorTable extends DataTable {
	String[] sklDatafiles = {};
	String[] txtFiles = {"UI\\UnitEditorData.txt", "UI\\WorldEditData.txt"};

	public UnitEditorTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
