package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public final class BasicSingleFieldFactory extends AbstractSingleFieldFactory {
	public static final BasicSingleFieldFactory INSTANCE = new BasicSingleFieldFactory();

	public BasicSingleFieldFactory() {

	}

	public BasicSingleFieldFactory(WorldEditorDataType worldEditorDataType) {
		this.worldEditorDataType = worldEditorDataType;
	}

	@Override
	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		String category = metaDataFieldObject.getField("category");
		String prefix = categoryName(category) + " - ";
		String displayName = metaDataFieldObject.getField("displayName");
		return prefix + WEString.getString(displayName);
	}

	@Override
	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		return "";
	}
}
