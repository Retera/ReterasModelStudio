package com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders;

import java.util.List;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.SingleFieldFactory;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public abstract class AbstractLevelsFieldBuilder extends AbstractFieldBuilder {
	private final War3ID levelField;

	public AbstractLevelsFieldBuilder(final SingleFieldFactory singleFieldFactory,
			final WorldEditorDataType worldEditorDataType, final War3ID levelField) {
		super(singleFieldFactory, worldEditorDataType);
		this.levelField = levelField;
	}

	@Override
	protected final void makeAndAddFields(final List<EditableOnscreenObjectField> fields, final War3ID metaKey,
			final GameObject metaDataField, final MutableGameObject gameObject, final ObjectData metaData) {
		final int repeatCount = metaDataField.getFieldValue("repeat");
		final int actualRepeatCount = gameObject.getFieldAsInteger(levelField, 0);
		if (repeatCount >= 2 && actualRepeatCount > 1) {
			for (int level = 1; level <= actualRepeatCount; level++) {
				fields.add(singleFieldFactory.create(gameObject, metaData, metaKey, level, worldEditorDataType));
			}
		} else {
			fields.add(singleFieldFactory.create(gameObject, metaData, metaKey, 0, worldEditorDataType));
		}
	}
}
