package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.SingleFieldFactory;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public final class BasicEditorFieldBuilder extends AbstractNoLevelsFieldBuilder {
	public BasicEditorFieldBuilder(final SingleFieldFactory singleFieldFactory,
			final WorldEditorDataType worldEditorDataType) {
		super(singleFieldFactory, worldEditorDataType);
	}

	@Override
	protected boolean includeField(final MutableGameObject gameObject, final GameObject metaDataField,
			final War3ID metaKey) {
		return true;
	}

}
