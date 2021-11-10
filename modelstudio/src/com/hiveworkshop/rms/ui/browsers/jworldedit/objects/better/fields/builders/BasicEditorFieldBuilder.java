package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.AbstractSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public final class BasicEditorFieldBuilder extends AbstractNoLevelsFieldBuilder {
	public BasicEditorFieldBuilder(AbstractSingleFieldFactory singleFieldFactory, WorldEditorDataType worldEditorDataType) {
		super(singleFieldFactory, worldEditorDataType);
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		return true;
	}

}
