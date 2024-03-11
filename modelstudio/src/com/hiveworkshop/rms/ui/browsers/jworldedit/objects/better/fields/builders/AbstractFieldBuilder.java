package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFieldBuilder {
	protected final WorldEditorDataType worldEditorDataType;

	public AbstractFieldBuilder(WorldEditorDataType worldEditorDataType) {
		this.worldEditorDataType = worldEditorDataType;
	}

	public final List<AbstractObjectField> buildFields(ObjectData metaData, MutableGameObject gameObject) {
		List<AbstractObjectField> fields = new ArrayList<>();

		for (String key : metaData.keySet()) {
			GameObject metaDataField = metaData.get(key);
			War3ID metaKey = War3ID.fromString(key);

			if (includeField(gameObject, metaDataField)) {
//				makeAndAddFields(fields, metaKey, metaDataField, gameObject, metaData);
				fields.addAll(makeFields(metaKey, metaDataField, gameObject, metaData));
			}
		}
		return fields;
	}

	protected abstract List<AbstractObjectField> makeFields(War3ID metaKey, GameObject metaDataField, MutableGameObject gameObject, ObjectData metaData);

	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField) {
		return true;
	}

	public AbstractObjectField create(MutableGameObject gameObject, ObjectData metaData, War3ID metaKey, int level, boolean hasMoreThanOneLevel) {
//		int level = 0;
//		boolean hasMoreThanOneLevel = false;
		GameObject metaField = metaData.get(metaKey.toString());

		int displayLevel = hasMoreThanOneLevel ? level : 0;
		String displayName = getDisplayName(metaField, displayLevel, gameObject);
		String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

		String displayPrefix = getDisplayPrefix(displayLevel);
		String prefixedDispName = displayPrefix + displayName;

		return getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);
	}
	public AbstractObjectField create(GameObject metaField, MutableGameObject gameObject, War3ID metaKey, int level, boolean hasMoreThanOneLevel) {
		int displayLevel = hasMoreThanOneLevel ? level : 0;
		String displayName = getDisplayName(metaField, displayLevel, gameObject);
		String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

		String displayPrefix = getDisplayPrefix(displayLevel);
		String prefixedDispName = displayPrefix + displayName;

		return getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);
	}
	public AbstractObjectField create1(MutableGameObject gameObject, ObjectData metaData, War3ID metaKey, int level, boolean hasMoreThanOneLevel) {
		GameObject metaField = metaData.get(metaKey.toString());

		int displayLevel = hasMoreThanOneLevel ? level : 0;
		String displayName = getDisplayName(metaField, displayLevel, gameObject);
		String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

		String displayPrefix = getDisplayPrefix(displayLevel);
		String prefixedDispName = displayPrefix + displayName;

		return getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);
	}

	protected AbstractObjectField getObjectField(War3ID metaKey, int level, boolean hasMoreThanOneLevel, GameObject metaField, String displayName, String rawDataName, String prefixedDispName) {
		return switch (metaField.getField("type")) {
			case "attackBits", "teamColor", "deathType", "versionFlags", "channelFlags", "channelType",
					"int" -> new IntegerObjectField(prefixedDispName, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "real", "unreal" -> new FloatObjectField(prefixedDispName, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "bool" -> new BooleanObjectField(prefixedDispName, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "unitRace" -> new GameEnumObjectField(prefixedDispName, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			default -> new StringObjectField(prefixedDispName, displayName, rawDataName, hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
		};
	}

	protected abstract String getDisplayName(GameObject metaDataField, int level, MutableGameObject gameObject);

	protected String getDisplayPrefix(int level) {
		return "";
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
