package com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.DoodadSingleFieldFactory;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class DoodadFieldBuilder extends AbstractLevelsFieldBuilder {
	private static final War3ID DOODAD_VARIATIONS_FIELD = War3ID.fromString("dvar");

	public DoodadFieldBuilder() {
		super(DoodadSingleFieldFactory.INSTANCE, WorldEditorDataType.DOODADS, DOODAD_VARIATIONS_FIELD);
	}

	@Override
	protected boolean includeField(final MutableGameObject gameObject, final GameObject metaDataField,
			final War3ID metaKey) {
		return true;
	}

}
