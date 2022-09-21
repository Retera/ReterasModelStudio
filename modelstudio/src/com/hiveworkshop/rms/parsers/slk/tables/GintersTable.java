package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class GintersTable extends DataTable {
	String[] sklDatafiles = {};
	String[] txtFiles = {"UI\\war3skins.txt"};

	public GintersTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
