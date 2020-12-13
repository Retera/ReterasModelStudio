package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

@Deprecated
public final class EditableOnscreenObjectFieldImpl {
	private final String cachedMetaKeyString;
	private final War3ID metaKey;
	private final int level;

	public EditableOnscreenObjectFieldImpl(final War3ID metaKey, final int level) {
		this.metaKey = metaKey;
		this.level = level;
		this.cachedMetaKeyString = metaKey.toString();
	}

	public String getDisplayName(final ObjectData metaData, final MutableGameObject gameUnit) {
		final GameObject metaDataFieldObject = metaData.get(cachedMetaKeyString);
		String prefix = EditableOnscreenObjectField.categoryName(metaDataFieldObject.getField("category")) + " - ";
		if (level > 0) {
			if (metaData.get("alev") != null || metaData.get("glvl") != null) {
				// abilities, TODO less hacky
				prefix = String.format(WEString.getString("WESTRING_AEVAL_LVL"), level) + " - " + prefix;
			} else if (metaData.get("dvar") != null) {
				// doodads
				prefix += String.format(WEString.getString("WESTRING_DEVAL_VAR"), level) + " - ";
			} else {
				prefix = level + " - " + prefix; // ??? this should never happen
			}
		}
		// TODO upgrade data, depends on current object data
		return prefix + WEString.getString(metaDataFieldObject.getField("displayName"));
	}

	public String getRawDataName(final ObjectData metaData) {
		final GameObject metaDataFieldObject = metaData.get(cachedMetaKeyString);
		return MutableObjectData.getEditorMetaDataDisplayKey(level, metaDataFieldObject);
	}

	public Object getValue(final ObjectData metaData, final MutableGameObject gameUnit) {
		final GameObject metaDataFieldObject = metaData.get(cachedMetaKeyString);
		final String metaDataType = metaDataFieldObject.getField("type");
		return switch (metaDataType) {
			case "int" -> gameUnit.getFieldAsInteger(metaKey, level);
			case "real", "unreal" -> gameUnit.getFieldAsFloat(metaKey, level);
			case "bool" -> gameUnit.getFieldAsBoolean(metaKey, level);
			case "string" -> gameUnit.getFieldAsString(metaKey, level);
			default -> gameUnit.getFieldAsString(metaKey, level);
		};
	}

	public void setValue(final ObjectData metaData, final MutableGameObject gameUnit, final Object value) {
		final GameObject metaDataFieldObject = metaData.get(cachedMetaKeyString);
		final String metaDataType = metaDataFieldObject.getField("type");
		switch (metaDataType) {
			case "int" -> gameUnit.setField(metaKey, level, ((Number) value).intValue());
			case "real", "unreal" -> gameUnit.setField(metaKey, level, ((Number) value).floatValue());
			case "bool" -> gameUnit.setField(metaKey, level, ((Boolean) value));
			case "string" -> gameUnit.setField(metaKey, level, value.toString());
		}
	}

}
