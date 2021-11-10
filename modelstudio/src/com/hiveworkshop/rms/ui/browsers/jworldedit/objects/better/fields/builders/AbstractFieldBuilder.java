package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.AbstractSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFieldBuilder {
	protected final AbstractSingleFieldFactory singleFieldFactory;
	protected final WorldEditorDataType worldEditorDataType;

	public AbstractFieldBuilder(AbstractSingleFieldFactory singleFieldFactory, WorldEditorDataType worldEditorDataType) {
		this.singleFieldFactory = singleFieldFactory;
		this.worldEditorDataType = worldEditorDataType;
	}

	public final List<AbstractObjectField> buildFields(ObjectData metaData, MutableGameObject gameObject) {
		List<AbstractObjectField> fields = new ArrayList<>();

		for (String key : metaData.keySet()) {
			GameObject metaDataField = metaData.get(key);
			War3ID metaKey = War3ID.fromString(key);

			if (includeField(gameObject, metaDataField, metaKey)) {
				makeAndAddFields(fields, metaKey, metaDataField, gameObject, metaData);
			}
		}
		return fields;
	}

	protected abstract void makeAndAddFields(List<AbstractObjectField> fields, War3ID metaKey, GameObject metaDataField,
	                                         MutableGameObject gameObject, ObjectData metaData);

	protected abstract boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey);
}
