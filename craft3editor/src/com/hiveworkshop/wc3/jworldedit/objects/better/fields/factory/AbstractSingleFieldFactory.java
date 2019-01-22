package com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.BooleanObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.FloatObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.IntegerObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.StringObjectField;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public abstract class AbstractSingleFieldFactory implements SingleFieldFactory {
	@Override
	public final EditableOnscreenObjectField create(final MutableGameObject gameObject, final ObjectData metaData,
			final War3ID metaKey, final int level, final WorldEditorDataType worldEditorDataType,
			final boolean hasMoreThanOneLevel) {
		final GameObject metaField = metaData.get(metaKey.toString());

		final String displayName = getDisplayName(metaData, metaKey, hasMoreThanOneLevel ? level : 0, gameObject);
		final String rawDataName = getRawDataName(metaData, metaKey, hasMoreThanOneLevel ? level : 0);
		final String metaDataType = metaField.getField("type");
		switch (metaDataType) {
		case "attackBits":
		case "teamColor":
		case "deathType":
		case "versionFlags":
		case "channelFlags":
		case "channelType":
		case "int":
			return new IntegerObjectField(displayName, rawDataName, metaKey, level, worldEditorDataType, metaField);
		case "real":
		case "unreal":
			return new FloatObjectField(displayName, rawDataName, metaKey, level, worldEditorDataType, metaField);
		case "bool":
			return new BooleanObjectField(displayName, rawDataName, metaKey, level, worldEditorDataType, metaField);
		default:
		case "string":
			return new StringObjectField(displayName, rawDataName, metaKey, level, worldEditorDataType, metaField);
		}
	}

	protected abstract String getDisplayName(final ObjectData metaData, final War3ID metaKey, final int level,
			MutableGameObject gameObject);

	private String getRawDataName(final ObjectData metaData, final War3ID metaKey, final int level) {
		final GameObject metaDataFieldObject = metaData.get(metaKey.toString());
		return MutableObjectData.getEditorMetaDataDisplayKey(level, metaDataFieldObject);
	}
}
