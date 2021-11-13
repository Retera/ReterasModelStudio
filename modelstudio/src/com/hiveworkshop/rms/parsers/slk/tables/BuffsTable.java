package com.hiveworkshop.rms.parsers.slk.tables;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableUtils;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.parsers.slk.StringKey;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BuffsTable extends DataTable {
	Map<StringKey, Element> dataTable = new LinkedHashMap<>();

	public BuffsTable() {
		loadBuffs();
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

	public void loadBuffs() {
		try {
			DataTableUtils.readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\AbilityBuffData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CampaignAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\CommonAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\HumanAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NeutralAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\NightElfAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\OrcAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\UndeadAbilityStrings.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityFunc.txt"));
		DataTableUtils.readTXT(this, GameDataFileSystem.getDefault().getResourceAsStream("Units\\ItemAbilityStrings.txt"));
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
