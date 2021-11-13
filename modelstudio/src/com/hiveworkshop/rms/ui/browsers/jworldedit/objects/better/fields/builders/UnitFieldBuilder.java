package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public class UnitFieldBuilder extends AbstractNoLevelsFieldBuilder {
	private static final War3ID IS_A_BUILDING = War3ID.fromString("ubdg");

	public UnitFieldBuilder() {
		super(WorldEditorDataType.UNITS);
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		return metaDataField.getFieldValue("useUnit") > 0
				|| (gameObject.getFieldAsBoolean(IS_A_BUILDING, 0) && metaDataField.getFieldValue("useBuilding") > 0)
				|| (Character.isUpperCase(gameObject.getAlias().charAt(0))
				&& metaDataField.getFieldValue("useHero") > 0);
	}

}
