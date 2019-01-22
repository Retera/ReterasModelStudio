package com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders;

import java.util.List;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.SingleFieldFactory;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public abstract class AbstractNoLevelsFieldBuilder extends AbstractFieldBuilder {
	public AbstractNoLevelsFieldBuilder(final SingleFieldFactory singleFieldFactory,
			final WorldEditorDataType worldEditorDataType) {
		super(singleFieldFactory, worldEditorDataType);
	}

	@Override
	protected void makeAndAddFields(final List<EditableOnscreenObjectField> fields, final War3ID metaKey,
			final GameObject metaDataField, final MutableGameObject gameObject, final ObjectData metaData) {
		fields.add(singleFieldFactory.create(gameObject, metaData, metaKey, 0, worldEditorDataType, false));
	}

}
