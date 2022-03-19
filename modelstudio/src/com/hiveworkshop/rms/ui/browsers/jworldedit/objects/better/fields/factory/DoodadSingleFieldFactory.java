package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public final class DoodadSingleFieldFactory extends AbstractSingleFieldFactory {
	public static final DoodadSingleFieldFactory INSTANCE = new DoodadSingleFieldFactory(WorldEditorDataType.DOODADS);

	public DoodadSingleFieldFactory() {

	}

	public DoodadSingleFieldFactory(WorldEditorDataType worldEditorDataType) {
		this.worldEditorDataType = worldEditorDataType;
	}

	@Override
	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		String category = metaDataFieldObject.getField("category");
		String prefix = categoryName(category) + " - ";

		String subPrefix = getSubPrefix(level);
		String displayName = metaDataFieldObject.getField("displayName");
		return prefix + subPrefix + WEString.getString(displayName);
	}

	private String getSubPrefix(int level) {
		String subPrefix = "";
		if (level > 0) {
			String westring_deval_var = WEString.getString("WESTRING_DEVAL_VAR");
			subPrefix += String.format(westring_deval_var, level) + " - ";
		}
		return subPrefix;
	}

	@Override
	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		return "";
	}
}
