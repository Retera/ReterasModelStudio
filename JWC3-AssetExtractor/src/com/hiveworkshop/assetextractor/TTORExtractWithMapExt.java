package com.hiveworkshop.assetextractor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.units.DataTable;

public class TTORExtractWithMapExt {

	public static void main(final String[] args) {
		final Path ttorPath = Paths.get("C:\\Users\\Eric\\Documents\\Warcraft\\Modding\\ttor\\ttor.mpq");
		final MapAssetExtractor mapAssetExtractor = new MapAssetExtractor(ttorPath);
		// final WarcraftData unitData = mapAssetExtractor.getUnitData();
		// for (final String key : unitData.keySet()) {
		// mapAssetExtractor.extractObject(key,
		// Paths.get("C:\\Users\\Eric\\Documents\\Warcraft\\Modding\\ttor\\output"),
		// new AssetExtractorSettings(false, 0));
		// }
		final List<DataTable> tables = new ArrayList<>();
		tables.add(DataTable.get());
		tables.add(DataTable.getBuffs());
		tables.add(DataTable.getDestructables());
		tables.add(DataTable.getDoodads());
		tables.add(DataTable.getGinters());
		tables.add(DataTable.getItems());
		tables.add(DataTable.getSpawns());
		tables.add(DataTable.getSplats());
		tables.add(DataTable.getTerrain());
		for (final DataTable table : tables) {
			for (final String unitId : table.keySet()) {
				final AssetSourceObject assetSourceObject = new AssetSourceObject(unitId);
				assetSourceObject.extract(mapAssetExtractor.getCodebase(), table,
						Paths.get("C:\\Users\\Eric\\Documents\\Warcraft\\Modding\\ttor\\output"),
						new AssetExtractorSettings(false, 0));
			}
		}
	}

}
