package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

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
