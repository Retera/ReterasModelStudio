package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory.UpgradeSingleFieldFactory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.HashMap;
import java.util.Map;

public class UpgradesFieldBuilder extends AbstractLevelsFieldBuilder {
	private static final War3ID UPGRADE_MAX_LEVEL_FIELD = War3ID.fromString("glvl");
	private final ObjectData upgradeEffectMetaData;
	private final Map<String, GameObject> effectIDToUpgradeEffect = new HashMap<>();

	public UpgradesFieldBuilder(ObjectData upgradeEffectMetaData) {
		super(new UpgradeSingleFieldFactory(upgradeEffectMetaData), WorldEditorDataType.UPGRADES, UPGRADE_MAX_LEVEL_FIELD);
		this.upgradeEffectMetaData = upgradeEffectMetaData;

		for (String notEffectId : upgradeEffectMetaData.keySet()) {
			GameObject upgradeEffect = upgradeEffectMetaData.get(notEffectId);
			String key = upgradeEffect.getField("effectID") + upgradeEffect.getField("dataType");
			effectIDToUpgradeEffect.put(key, upgradeEffect);
		}
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
		String effectType = metaDataField.getField("effectType");
		if ("Base".equalsIgnoreCase(effectType)
				|| "Mod".equalsIgnoreCase(effectType)
				|| "Code".equalsIgnoreCase(effectType)) {
			War3ID field = War3ID.fromString("gef" + metaDataField.getId().charAt(3));
			String key = gameObject.getFieldAsString(field, 0) + metaDataField.getField("effectType");
			return effectIDToUpgradeEffect.containsKey(key);
		}
		return true;
	}

}
