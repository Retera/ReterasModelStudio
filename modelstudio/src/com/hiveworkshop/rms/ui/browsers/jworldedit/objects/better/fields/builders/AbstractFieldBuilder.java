package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.EditorFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.SingleFieldFactory;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

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
