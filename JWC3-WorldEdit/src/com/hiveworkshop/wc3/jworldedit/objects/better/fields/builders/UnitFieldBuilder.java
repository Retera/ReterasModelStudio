package com.hiveworkshop.wc3.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory.BasicSingleFieldFactory;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class UnitFieldBuilder extends AbstractNoLevelsFieldBuilder {
	private static final War3ID IS_A_BUILDING = War3ID.fromString("ubdg");

	public UnitFieldBuilder() {
		super(BasicSingleFieldFactory.INSTANCE, WorldEditorDataType.UNITS);
	}

	@Override
	protected boolean includeField(final MutableGameObject gameObject, final GameObject metaDataField,
			final War3ID metaKey) {
		return metaDataField.getFieldValue("useUnit") > 0
				|| (gameObject.getFieldAsBoolean(IS_A_BUILDING, 0) && metaDataField.getFieldValue("useBuilding") > 0)
				|| (Character.isUpperCase(gameObject.getAlias().charAt(0))
						&& metaDataField.getFieldValue("useHero") > 0);
	}

}
