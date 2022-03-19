package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

import java.util.HashMap;
import java.util.Map;

public class UpgradesFieldBuilder extends AbstractLevelsFieldBuilder {
	private static final War3ID UPGRADE_MAX_LEVEL_FIELD = War3ID.fromString("glvl");
	private final ObjectData upgradeEffectMetaData = StandardObjectData.getStandardUpgradeEffectMeta();
	private final Map<String, GameObject> effectIDToUpgradeEffect = new HashMap<>();

	public UpgradesFieldBuilder(ObjectData upgradeEffectMetaData) {
//		super(new UpgradeSingleFieldFactory(upgradeEffectMetaData, WorldEditorDataType.UPGRADES), UPGRADE_MAX_LEVEL_FIELD);
		super(WorldEditorDataType.UPGRADES, UPGRADE_MAX_LEVEL_FIELD);
//		this.upgradeEffectMetaData = upgradeEffectMetaData;
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

	@Override
	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		String defaultDisplayName = LevelsSingleFieldFactory.INSTANCE.getDisplayName(metaData, metaKey, level, gameObject);
		String defaultDisplayName = getDisplayName2(metaData, metaKey, level, gameObject);
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
		String prefix = "";
		if (level > 0) {
			String westring = WEString.getString("WESTRING_AEVAL_LVL");
			prefix = String.format(westring, level) + " - " + prefix;
		}
		return prefix;
	}

	protected String getDisplayName2(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
		GameObject metaDataField = metaData.get(metaKey.toString());
		String category = metaDataField.getField("category");
		String prefix = categoryName(category) + " - ";
		String displayName = metaDataField.getField("displayName");
		return prefix + WEString.getString(displayName);
	}

}
