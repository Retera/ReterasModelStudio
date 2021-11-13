package com.hiveworkshop.rms.parsers.slk;

public class DataTableHolder {
	static DataTable theTable;
	static DataTable spawnTable;
	static DataTable splatTable;
	static DataTable terrainTable;
	static DataTable ginterTable;
	static DataTable buffTable;
	static DataTable itemTable;
	static DataTable theTableDestructibles;
	static DataTable theTableDoodads;
	static DataTable unitEditorDataTable;

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

	public static DataTable getDoodads() {
		if (theTableDoodads == null) {
			theTableDoodads = new DoodadsTable();
		}
		return theTableDoodads;
	}

	public static DataTable getDestructables() {
		if (theTableDestructibles == null) {
			theTableDestructibles = new DestructiblesTable();
		}
		return theTableDestructibles;
	}

	public static DataTable getItems() {
		if (itemTable == null) {
			itemTable = new ItemsTable();
		}
		return itemTable;
	}

	public static DataTable getBuffs() {
		if (buffTable == null) {
			buffTable = new BuffsTable();
		}
		return buffTable;
	}

	public static DataTable getSpawns() {
		if (spawnTable == null) {
			spawnTable = new SpawnsTable();
		}
		return spawnTable;
	}

	public static DataTable getSplats() {
		if (splatTable == null) {
			splatTable = new SplatsTable();
		}
		return splatTable;
	}

	public static DataTable getTerrain() {
		if (terrainTable == null) {
			terrainTable = new TerrainTable();
		}
		return terrainTable;
	}

	public static DataTable getGinters() {
		if (ginterTable == null) {
			ginterTable = new GintersTable();
		}
		return ginterTable;
	}

	public static DataTable getWorldEditorData() {
		if (unitEditorDataTable == null) {
			unitEditorDataTable = new DataTable();
			unitEditorDataTable.loadUnitEditorData();
		}
		return unitEditorDataTable;
	}
}
