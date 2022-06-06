package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.UpgradesFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableUpgradeData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.upgrades.UpgradeSortByRaceFolder;

public class UpgradeTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public UpgradeTabTreeBrowserBuilder(){
		unitData = new MutableUpgradeData();
		editorFieldBuilder = new UpgradesFieldBuilder(StandardObjectData.getStandardUpgradeEffectMeta());
		objectTabTreeBrowserBuilder = this;
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("UPGR", "Upgr");
		customUnitPopupRunner = () -> {};
	}

	protected void setNewUnitData(){
		unitData = new MutableUpgradeData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_STANDARDUPGRS")),
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_CUSTOMUPGRS")));
	}
}
