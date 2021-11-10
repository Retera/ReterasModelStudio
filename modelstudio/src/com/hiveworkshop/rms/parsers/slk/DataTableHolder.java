package com.hiveworkshop.rms.parsers.slk;

public class DataTableHolder {
	static DataTable theTable;
	static DataTable spawnTable;
	static DataTable splatTable;
	static DataTable terrainTable;
	static DataTable ginterTable;
	static DataTable unitEditorDataTable;
	static DataTable buffTable;
	static DataTable itemTable;
	static DataTable theTableDestructibles;
	static DataTable theTableDoodads;

	public static void dropCache() {
		theTable = null;
		spawnTable = null;
		splatTable = null;
		terrainTable = null;
		ginterTable = null;
		buffTable = null;
		itemTable = null;
		theTableDestructibles = null;
		theTableDoodads = null;
	}

	public static DataTable get() {
		if (theTable == null) {
			theTable = new DataTable();
			theTable.loadDefaults();
		}
		return theTable;
	}
}
