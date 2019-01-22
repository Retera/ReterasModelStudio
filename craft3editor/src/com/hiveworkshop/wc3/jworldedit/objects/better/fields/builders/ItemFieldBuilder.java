package com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.BasicSingleFieldFactory;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class ItemFieldBuilder extends AbstractNoLevelsFieldBuilder {
	public ItemFieldBuilder() {
		super(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.ITEM);
	}

	@Override
	protected boolean includeField(final MutableGameObject gameObject, final GameObject metaDataField,
			final War3ID metaKey) {
		return metaDataField.getFieldValue("useItem") > 0;
	}

}
