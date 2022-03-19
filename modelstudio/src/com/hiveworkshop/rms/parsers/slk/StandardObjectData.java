package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;

public class StandardObjectData {

	public static DataTable getStandardUpgradeEffectMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\UpgradeEffectMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getUnitEditorData() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readTXT(unitMetaData, source.getResourceAsStream("UI\\UnitEditorData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	private StandardObjectData() {
	}
}
