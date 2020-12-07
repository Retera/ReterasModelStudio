package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.BooleanObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.FloatObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.GameEnumObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.IntegerObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.StringObjectField;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public abstract class AbstractSingleFieldFactory implements SingleFieldFactory {
	@Override
	public final EditableOnscreenObjectField create(final MutableGameObject gameObject, final ObjectData metaData,
			final War3ID metaKey, final int level, final WorldEditorDataType worldEditorDataType,
			final boolean hasMoreThanOneLevel) {
		final GameObject metaField = metaData.get(metaKey.toString());

		final String displayName = getDisplayName(metaData, metaKey, hasMoreThanOneLevel ? level : 0, gameObject);
		final String displayPrefix = getDisplayPrefix(metaData, metaKey, hasMoreThanOneLevel ? level : 0, gameObject);
		final String rawDataName = getRawDataName(metaData, metaKey, hasMoreThanOneLevel ? level : 0);
		final String metaDataType = metaField.getField("type");
		return switch (metaDataType) {
			case "attackBits", "teamColor", "deathType", "versionFlags", "channelFlags", "channelType", "int" ->
					new IntegerObjectField(displayPrefix + displayName, displayName, rawDataName,
							hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "real", "unreal" ->
					new FloatObjectField(displayPrefix + displayName, displayName, rawDataName,
							hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "bool" ->
					new BooleanObjectField(displayPrefix + displayName, displayName, rawDataName,
							hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			case "unitRace" ->
					new GameEnumObjectField(displayPrefix + displayName, displayName, rawDataName,
							hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField,
							"unitRace", "WESTRING_COD_TYPE_UNITRACE",
					StandardObjectData.getUnitEditorData());
			case "string" ->
					new StringObjectField(displayPrefix + displayName, displayName, rawDataName,
							hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
			default ->
					new StringObjectField(displayPrefix + displayName, displayName, rawDataName,
					hasMoreThanOneLevel, metaKey, level, worldEditorDataType, metaField);
		};
	}

	protected abstract String getDisplayName(final ObjectData metaData, final War3ID metaKey, final int level,
			MutableGameObject gameObject);

	protected abstract String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level,
			MutableGameObject gameObject);

	private String getRawDataName(final ObjectData metaData, final War3ID metaKey, final int level) {
		final GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		return MutableObjectData.getEditorMetaDataDisplayKey(level, metaDataFieldObject);
	}
}
