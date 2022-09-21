package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class ItemsTable extends DataTable {
	String[] sklDatafiles = {"Units\\ItemData.slk"};
	String[] txtFiles = {"Units\\ItemFunc.txt", "Units\\ItemStrings.txt"};

	public ItemsTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
