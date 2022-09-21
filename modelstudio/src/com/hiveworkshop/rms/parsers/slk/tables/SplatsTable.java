package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class SplatsTable extends DataTable {
	String[] sklDatafiles = {"Splats\\SplatData.slk", "Splats\\UberSplatData.slk"};
	String[] txtFiles = {};

	public SplatsTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
