package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class DoodadFieldBuilder extends AbstractLevelsFieldBuilder {
	private static final War3ID DOODAD_VARIATIONS_FIELD = War3ID.fromString("dvar");

	public DoodadFieldBuilder() {
		super(WorldEditorDataType.DOODADS, DOODAD_VARIATIONS_FIELD);
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		return true;
	}

	@Override
	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		GameObject metaDataField = metaData.get(metaKey.toString());
		String category = metaDataField.getField("category");
		String prefix = categoryName(category) + " - ";

		String subPrefix = getSubPrefix(level);
		String displayName = metaDataField.getField("displayName");
		return prefix + subPrefix + WEString.getString(displayName);
	}

	private String getSubPrefix(int level) {
		String subPrefix = "";
		if (level > 0) {
			String westring = WEString.getString("WESTRING_DEVAL_VAR");
			subPrefix += String.format(westring, level) + " - ";
		}
		return subPrefix;
	}

	@Override
	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		return "";
	}

}
