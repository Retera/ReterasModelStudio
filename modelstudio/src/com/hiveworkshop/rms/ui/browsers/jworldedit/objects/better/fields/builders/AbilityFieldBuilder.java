package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.LevelsSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;

public class AbilityFieldBuilder extends AbstractLevelsFieldBuilder {
	private static final War3ID ABILITY_LEVEL_FIELD = War3ID.fromString("alev");
	private static final War3ID HERO_ABILITY_FIELD = War3ID.fromString("aher");
	private static final War3ID ITEM_ABILITY_FIELD = War3ID.fromString("aite");

	public AbilityFieldBuilder() {
		super(LevelsSingleFieldFactory.INSTANCE, WorldEditorDataType.ABILITIES, ABILITY_LEVEL_FIELD);
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		boolean heroAbility = gameObject.getFieldAsBoolean(HERO_ABILITY_FIELD, 0);
		boolean itemAbility = gameObject.getFieldAsBoolean(ITEM_ABILITY_FIELD, 0);
		String useSpecific = metaDataField.getField("useSpecific");
		String notAllowed = metaDataField.getField("notSpecific"); //specificallyNotAllowedAbilityIds

		String codeStringValue = gameObject.getCode().asStringValue();

		if ((0 >= useSpecific.length() || Arrays.asList(useSpecific.split(",")).contains(codeStringValue))
				&& (0 >= notAllowed.length() || !Arrays.asList(notAllowed.split(",")).contains(codeStringValue))) {
			boolean useHero = heroAbility && !itemAbility && metaDataField.getFieldValue("useHero") == 1;
			boolean useUnit = !heroAbility && !itemAbility && metaDataField.getFieldValue("useUnit") == 1;
			boolean useItem = itemAbility && metaDataField.getFieldValue("useItem") == 1;

			return (useHero || useUnit || useItem);
		} else {
			return false;
		}
	}

}
