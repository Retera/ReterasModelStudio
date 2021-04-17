package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import java.util.List;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.SingleFieldFactory;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

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
