package com.hiveworkshop.assetextractor;

import java.nio.file.Paths;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.resources.Resources;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.UnitOptionPanel;

public class DotaMain {

	public static void main(final String[] args) {

		try (MapAssetExtractor extractor = new MapAssetExtractor(Paths.get(args[0]))) {
			UnitOptionPanel.dropRaceCache();
			DataTable.dropCache();
			ModelOptionPanel.dropCache();
			WEString.dropCache();
			Resources.dropCache();
			BLPHandler.get().dropCache();
			final WarcraftData unitData = extractor.getUnitData();
			for (final String key : unitData.keySet()) {
				extractor.extractObject(key, Paths.get(args[1]), new AssetExtractorSettings(false, 3));
			}
			extractor.close();
		} catch (final Throwable exc) {
			exc.printStackTrace();
		}
	}

}
