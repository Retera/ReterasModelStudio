package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.HashMap;
import java.util.Map;

public class UpgradeSingleFieldFactory extends AbstractSingleFieldFactory {
	private final Map<String, GameObject> effectIDToUpgradeEffect = new HashMap<>();

	public UpgradeSingleFieldFactory() {

	}

	public UpgradeSingleFieldFactory(WorldEditorDataType worldEditorDataType) {
		this.worldEditorDataType = worldEditorDataType;
	}

	public UpgradeSingleFieldFactory(ObjectData upgradeEffectMetaData) {
		for (String notEffectId : upgradeEffectMetaData.keySet()) {
			GameObject upgradeEffect = upgradeEffectMetaData.get(notEffectId);
			effectIDToUpgradeEffect.put(upgradeEffect.getField("effectID") + upgradeEffect.getField("dataType"), upgradeEffect);
		}
	}

	public UpgradeSingleFieldFactory(ObjectData upgradeEffectMetaData, WorldEditorDataType worldEditorDataType) {
		this.worldEditorDataType = worldEditorDataType;
		for (String notEffectId : upgradeEffectMetaData.keySet()) {
			GameObject upgradeEffect = upgradeEffectMetaData.get(notEffectId);
			effectIDToUpgradeEffect.put(upgradeEffect.getField("effectID") + upgradeEffect.getField("dataType"), upgradeEffect);
		}
	}

	@Override
	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		String defaultDisplayName = LevelsSingleFieldFactory.INSTANCE.getDisplayName(metaData, metaKey, level, gameObject);
		String defaultDisplayName = LevelsSingleFieldFactory.INSTANCE.getDisplayName(metaData, metaKey, level, gameObject);
		GameObject metaDataField = metaData.get(metaKey.toString());
		String effectType = metaDataField.getField("effectType");
		if ("Base".equalsIgnoreCase(effectType)
				|| "Mod".equalsIgnoreCase(effectType)
				|| "Code".equalsIgnoreCase(effectType)) {
			War3ID gefField = War3ID.fromString("gef" + metaDataField.getId().charAt(3));
			String fieldAsString = gameObject.getFieldAsString(gefField, 0);
			GameObject upgradeEffect = effectIDToUpgradeEffect.get(fieldAsString + metaDataField.getField("effectType"));
			String displayNameOfSubMetaField = upgradeEffect == null ? "WESTRING_ERROR_BADTRIGVAL" : upgradeEffect.getField("displayName");
			return String.format(defaultDisplayName, WEString.getString(displayNameOfSubMetaField));
		}
		return defaultDisplayName;
	}

	@Override
	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		return LevelsSingleFieldFactory.INSTANCE.getDisplayPrefix(metaData, metaKey, level, gameObject);
	}

}
