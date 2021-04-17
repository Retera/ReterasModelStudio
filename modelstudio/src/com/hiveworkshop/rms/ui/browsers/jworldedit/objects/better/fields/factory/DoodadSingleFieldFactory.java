package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

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

	@Override
	protected String getDisplayPrefix(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		return "";
	}
}
