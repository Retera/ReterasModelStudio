package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class DoodadsTable extends DataTable {
	String[] sklDatafiles = {"Doodads\\Doodads.slk"};
	String[] txtFiles = {"Doodads\\DoodadSkins.txt"};

	public DoodadsTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
