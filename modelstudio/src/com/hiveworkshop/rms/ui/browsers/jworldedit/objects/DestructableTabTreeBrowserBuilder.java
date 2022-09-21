package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.BasicEditorFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableDestructibleData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.destructibles.DestructibleSortByCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;

public class DestructableTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public DestructableTabTreeBrowserBuilder(){
		unitData = new MutableDestructibleData();
		editorFieldBuilder = new BasicEditorFieldBuilder(WorldEditorDataType.DESTRUCTIBLES);
		objectTabTreeBrowserBuilder = this;
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("DEST", "Dest");
		customUnitPopupRunner = () -> {};
	}


	protected void setNewUnitData(){
		unitData = new MutableDestructibleData();
	}
	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new DestructibleSortByCategoryFolder(WEString.getString("WESTRING_BE_STANDARDDESTS")),
				new DestructibleSortByCategoryFolder(WEString.getString("WESTRING_BE_CUSTOMDESTS")));
	}
}
