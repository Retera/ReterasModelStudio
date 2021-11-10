package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.AbstractSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.List;

public abstract class AbstractNoLevelsFieldBuilder extends AbstractFieldBuilder {
	public AbstractNoLevelsFieldBuilder(AbstractSingleFieldFactory singleFieldFactory, WorldEditorDataType worldEditorDataType) {
		super(singleFieldFactory, worldEditorDataType);
	}

	@Override
	protected void makeAndAddFields(List<AbstractObjectField> fields, War3ID metaKey,
	                                GameObject metaDataField, MutableGameObject gameObject, ObjectData metaData) {
		fields.add(singleFieldFactory.create(gameObject, metaData, metaKey, 0, worldEditorDataType, false));
	}

}
