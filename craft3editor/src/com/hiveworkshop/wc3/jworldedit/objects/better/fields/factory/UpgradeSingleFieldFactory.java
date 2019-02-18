package com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory;

import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class UpgradeSingleFieldFactory extends AbstractSingleFieldFactory {
	private final Map<String, GameObject> effectIDToUpgradeEffect = new HashMap<>();

	public UpgradeSingleFieldFactory(final ObjectData upgradeEffectMetaData) {
		for (final String notEffectId : upgradeEffectMetaData.keySet()) {
			final GameObject upgradeEffect = upgradeEffectMetaData.get(notEffectId);
			effectIDToUpgradeEffect.put(upgradeEffect.getField("effectID") + upgradeEffect.getField("dataType"),
					upgradeEffect);
		}
	}

	@Override
	protected String getDisplayName(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		final String defaultDisplayName = LevelsSingleFieldFactory.INSTANCE.getDisplayName(metaData, metaKey, level,
				gameObject);
		final GameObject metaDataField = metaData.get(metaKey.toString());
		final String effectType = metaDataField.getField("effectType");
		if ("Base".equalsIgnoreCase(effectType) || "Mod".equalsIgnoreCase(effectType)
				|| "Code".equalsIgnoreCase(effectType)) {
			final GameObject upgradeEffect = effectIDToUpgradeEffect
					.get(gameObject.getFieldAsString(War3ID.fromString("gef" + metaDataField.getId().charAt(3)), 0)
							+ metaDataField.getField("effectType"));
			final String displayNameOfSubMetaField = upgradeEffect == null ? "WESTRING_ERROR_BADTRIGVAL"
					: upgradeEffect.getField("displayName");
			return String.format(defaultDisplayName, WEString.getString(displayNameOfSubMetaField));
		}
		return defaultDisplayName;
	}

	@Override
	protected String getDisplayPrefix(final ObjectData metaData, final War3ID metaKey, final int level,
			final MutableGameObject gameObject) {
		return LevelsSingleFieldFactory.INSTANCE.getDisplayPrefix(metaData, metaKey, level, gameObject);
	}

}
