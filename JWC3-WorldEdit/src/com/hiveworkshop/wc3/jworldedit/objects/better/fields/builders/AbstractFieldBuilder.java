package com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.jworldedit.objects.better.EditorFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.SingleFieldFactory;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public abstract class AbstractFieldBuilder implements EditorFieldBuilder {
	protected final SingleFieldFactory singleFieldFactory;
	protected final WorldEditorDataType worldEditorDataType;

	public AbstractFieldBuilder(final SingleFieldFactory singleFieldFactory,
			final WorldEditorDataType worldEditorDataType) {
		this.singleFieldFactory = singleFieldFactory;
		this.worldEditorDataType = worldEditorDataType;
	}

	@Override
	public final List<EditableOnscreenObjectField> buildFields(final ObjectData metaData,
			final MutableGameObject gameObject) {
		final List<EditableOnscreenObjectField> fields = new ArrayList<>();
		for (final String key : metaData.keySet()) {
			final GameObject metaDataField = metaData.get(key);
			final War3ID metaKey = War3ID.fromString(key);
			if (includeField(gameObject, metaDataField, metaKey)) {
				makeAndAddFields(fields, metaKey, metaDataField, gameObject, metaData);
			}
		}
		return fields;
	}

	protected abstract void makeAndAddFields(List<EditableOnscreenObjectField> fields, War3ID metaKey,
			GameObject metaDataField, MutableGameObject gameObject, final ObjectData metaData);

	protected abstract boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey);
}
