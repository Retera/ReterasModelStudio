package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

public final class LevelsSingleFieldFactory extends AbstractSingleFieldFactory {
	public static final LevelsSingleFieldFactory INSTANCE = new LevelsSingleFieldFactory();

	@Override
	protected String getDisplayName(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		final GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		final String prefix = EditableOnscreenObjectField.categoryName(metaDataFieldObject.getField("category"))
				+ " - ";
		return prefix + WEString.getString(metaDataFieldObject.getField("displayName"));
	}

	@Override
	protected String getDisplayPrefix(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		String prefix = "";
		if (level > 0) {
			prefix = String.format(WEString.getString("WESTRING_AEVAL_LVL"), level) + " - " + prefix;
		}
		return prefix;
	}
}
