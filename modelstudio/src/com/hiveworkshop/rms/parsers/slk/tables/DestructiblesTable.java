package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class DestructiblesTable extends DataTable {
	String[] sklDatafiles = {"Units\\DestructableData.slk"};
	String[] txtFiles = {"Units\\DestructableSkin.txt"};

	public DestructiblesTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
