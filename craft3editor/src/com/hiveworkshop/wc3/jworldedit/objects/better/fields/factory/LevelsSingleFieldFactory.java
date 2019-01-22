package com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class LevelsSingleFieldFactory extends AbstractSingleFieldFactory {
	public static final LevelsSingleFieldFactory INSTANCE = new LevelsSingleFieldFactory();

	@Override
	protected String getDisplayName(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		final GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		String prefix = EditableOnscreenObjectField.categoryName(metaDataFieldObject.getField("category")) + " - ";
		if (level > 0) {
			prefix = String.format(WEString.getString("WESTRING_AEVAL_LVL"), level) + " - " + prefix;
		}
		return prefix + WEString.getString(metaDataFieldObject.getField("displayName"));
	}
}
