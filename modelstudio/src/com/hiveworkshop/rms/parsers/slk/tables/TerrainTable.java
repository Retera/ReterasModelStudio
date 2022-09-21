package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.parsers.slk.DataTable;

public class TerrainTable extends DataTable {
	private final String[] sklDatafiles = {"TerrainArt\\Terrain.slk"};
	private final String[] txtFiles = {};

	public TerrainTable() {
		loadStuff(sklDatafiles, txtFiles, true);
	}
}
