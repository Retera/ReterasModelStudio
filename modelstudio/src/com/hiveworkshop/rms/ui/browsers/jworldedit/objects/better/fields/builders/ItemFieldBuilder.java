package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.BasicSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class ItemFieldBuilder extends AbstractNoLevelsFieldBuilder {
	public ItemFieldBuilder() {
		super(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.ITEM);
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		return metaDataField.getFieldValue("useItem") > 0;
	}

}
