package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.*;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public abstract class AbstractSingleFieldFactory implements SingleFieldFactory {
	@Override
	public final EditableOnscreenObjectField create(MutableGameObject gameObject, ObjectData metaData,
	                                                War3ID metaKey, int level, WorldEditorDataType worldEditorDataType,
	                                                boolean hasMoreThanOneLevel) {
		GameObject metaField = metaData.get(metaKey.toString());

		String displayName = getDisplayName(metaData, metaKey, hasMoreThanOneLevel ? level : 0, gameObject);
		String displayPrefix = getDisplayPrefix(metaData, metaKey, hasMoreThanOneLevel ? level : 0, gameObject);
		String rawDataName = getRawDataName(metaData, metaKey, hasMoreThanOneLevel ? level : 0);
		String metaDataType = metaField.getField("type");
		String displayName1 = displayPrefix + displayName;
		return switch (metaDataType) {
			case "attackBits", "teamColor", "deathType", "versionFlags", "channelFlags", "channelType", "int" -> new IntegerObjectField(displayName1, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "real", "unreal" -> new FloatObjectField(displayName1, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "bool" -> new BooleanObjectField(displayName1, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "unitRace" -> new GameEnumObjectField(displayName1, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField,
					"unitRace", "WESTRING_COD_TYPE_UNITRACE",
					StandardObjectData.getUnitEditorData());
			case "string" -> new StringObjectField(displayName1, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			default -> new StringObjectField(displayName1, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
		};
	}

	protected abstract String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject);

	protected abstract String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject);

	private String getRawDataName(ObjectData metaData, War3ID metaKey, int level) {
		GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		return MutableObjectData.getEditorMetaDataDisplayKey(level, metaDataFieldObject);
	}
}
