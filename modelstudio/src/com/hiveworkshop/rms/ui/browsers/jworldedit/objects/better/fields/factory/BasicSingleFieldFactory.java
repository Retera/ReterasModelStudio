package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

public final class BasicSingleFieldFactory extends AbstractSingleFieldFactory {
	public static final BasicSingleFieldFactory INSTANCE = new BasicSingleFieldFactory();

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
		return "";
	}
}
