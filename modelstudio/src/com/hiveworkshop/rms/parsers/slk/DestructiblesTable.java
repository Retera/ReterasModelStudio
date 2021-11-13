package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DestructiblesTable extends DataTable {
	Map<StringKey, Element> dataTable = new LinkedHashMap<>();

	public DestructiblesTable() {
		loadDestructibles();
	}

	@Override
	public Set<String> keySet() {
		Set<String> outputKeySet = new HashSet<>();
		Set<StringKey> internalKeySet = dataTable.keySet();
		for (StringKey key : internalKeySet) {
			outputKeySet.add(key.getString());
		}
		return outputKeySet;
	}

	public void loadDestructibles() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\DestructableData.slk"));
			final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Units\\DestructableSkin.txt");
			if (unitSkin != null) {
				DataTableUtils.readTXT(this, unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	@Override
	public Element get(final String id) {
		return dataTable.get(new StringKey(id));
	}

	@Override
	public void setValue(final String id, final String field, final String value) {
		get(id).setField(field, value);
	}

	public void put(final String id, final Element e) {
		dataTable.put(new StringKey(id), e);
	}

}
