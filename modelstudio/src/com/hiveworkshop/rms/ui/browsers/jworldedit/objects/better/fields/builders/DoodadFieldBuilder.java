package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.DoodadSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class DoodadFieldBuilder extends AbstractLevelsFieldBuilder {
	private static final War3ID DOODAD_VARIATIONS_FIELD = War3ID.fromString("dvar");

	public DoodadFieldBuilder() {
		super(DoodadSingleFieldFactory.INSTANCE, WorldEditorDataType.DOODADS, DOODAD_VARIATIONS_FIELD);
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		return true;
	}

}
