package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.AbstractFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;

public abstract class ObjectTabTreeBrowserBuilder extends AbstractFieldBuilder {
	protected MutableObjectData unitData;
	protected EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData;
	protected Runnable customUnitPopupRunner;

	public ObjectTabTreeBrowserBuilder(WorldEditorDataType worldEditorDataType) {
		super(worldEditorDataType);
	}

	public abstract TopLevelCategoryFolder build();

	public EditorTabCustomToolbarButtonData getEditorTabCustomToolbarButtonData() {
		return editorTabCustomToolbarButtonData;
	}

	public MutableObjectData getUnitData() {
		return unitData;
	}

	public MutableObjectData reloadAndGetUnitData() {
		setNewUnitData();
		return unitData;
	}

	protected abstract void setNewUnitData();

	public Runnable getCustomUnitPopupRunner() {
		return customUnitPopupRunner;
	}
}
