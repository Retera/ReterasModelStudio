package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public abstract class AbstractSingleFieldFactory {
	protected WorldEditorDataType worldEditorDataType;

	public final AbstractObjectField create(MutableGameObject gameObject, ObjectData metaData,
	                                        War3ID metaKey, int level, boolean hasMoreThanOneLevel) {
		GameObject metaField = metaData.get(metaKey.toString());

		int displayLevel = hasMoreThanOneLevel ? level : 0;
		String displayName = getDisplayName(metaData, metaKey, displayLevel, gameObject);
		String displayPrefix = getDisplayPrefix(metaData, metaKey, displayLevel, gameObject);
		String rawDataName = getRawDataName(metaData, metaKey, displayLevel);
		String metaDataType = metaField.getField("type");
		String displayName1 = displayPrefix + displayName;

		return switch (metaDataType) {
			case "attackBits", "teamColor", "deathType", "versionFlags", "channelFlags", "channelType", "int" -> new IntegerObjectField(displayName1, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "real", "unreal" -> new FloatObjectField(displayName1, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "bool" -> new BooleanObjectField(displayName1, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "unitRace" -> new GameEnumObjectField(displayName1, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField, "unitRace", "WESTRING_COD_TYPE_UNITRACE", StandardObjectData.getUnitEditorData());
//			case "string" -> new StringObjectField(displayName1, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			default -> new StringObjectField(displayName1, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
		};
	}

	protected abstract String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject);

	protected abstract String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject);

	private String getRawDataName(ObjectData metaData, War3ID metaKey, int level) {
		GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		return MutableObjectData.getEditorMetaDataDisplayKey(level, metaDataFieldObject);
	}

	public WorldEditorDataType getWorldEditorDataType() {
		return worldEditorDataType;
	}

	public static String categoryName(String cat) {
		return switch (cat.toLowerCase()) {
			case "abil" -> WEString.getString("WESTRING_OE_CAT_ABILITIES").replace("&", "");
			case "art" -> WEString.getString("WESTRING_OE_CAT_ART").replace("&", "");
			case "combat" -> WEString.getString("WESTRING_OE_CAT_COMBAT").replace("&", "");
			case "data" -> WEString.getString("WESTRING_OE_CAT_DATA").replace("&", "");
			case "editor" -> WEString.getString("WESTRING_OE_CAT_EDITOR").replace("&", "");
			case "move" -> WEString.getString("WESTRING_OE_CAT_MOVEMENT").replace("&", "");
			case "path" -> WEString.getString("WESTRING_OE_CAT_PATHING").replace("&", "");
			case "sound" -> WEString.getString("WESTRING_OE_CAT_SOUND").replace("&", "");
			case "stats" -> WEString.getString("WESTRING_OE_CAT_STATS").replace("&", "");
			case "tech" -> WEString.getString("WESTRING_OE_CAT_TECHTREE").replace("&", "");
			case "text" -> WEString.getString("WESTRING_OE_CAT_TEXT").replace("&", "");
			default -> WEString.getString("WESTRING_UNKNOWN");
		};
	}
}
