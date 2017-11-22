package com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class DoodadSingleFieldFactory extends AbstractSingleFieldFactory {
	public static final DoodadSingleFieldFactory INSTANCE = new DoodadSingleFieldFactory();

	@Override
	protected String getDisplayName(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		final GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		String prefix = EditableOnscreenObjectField.categoryName(metaDataFieldObject.getField("category")) + " - ";
		if (level > 0) {
			prefix += String.format(WEString.getString("WESTRING_DEVAL_VAR"), level) + " - ";
		}
		return prefix + WEString.getString(metaDataFieldObject.getField("displayName"));
	}
}
