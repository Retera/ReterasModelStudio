package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.BasicEditorFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableBuffData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs.BuffSortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;

public class BuffTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public BuffTabTreeBrowserBuilder(){
		unitData = new MutableBuffData();
		editorFieldBuilder = new BasicEditorFieldBuilder(WorldEditorDataType.BUFFS_EFFECTS);
		objectTabTreeBrowserBuilder = this;
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("BUFF", "Buff");
		customUnitPopupRunner = () -> {};
	}

	protected void setNewUnitData(){
		unitData = new MutableBuffData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_STANDARDBUFFS")),
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_CUSTOMBUFFS")));
	}
}
