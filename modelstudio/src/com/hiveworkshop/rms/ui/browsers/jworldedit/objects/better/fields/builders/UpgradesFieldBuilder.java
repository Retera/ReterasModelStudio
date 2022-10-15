//package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders;
//
//import com.hiveworkshop.rms.parsers.slk.GameObject;
//import com.hiveworkshop.rms.parsers.slk.ObjectData;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WE_STRING;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
//import com.hiveworkshop.rms.util.War3ID;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class UpgradesFieldBuilder extends AbstractFieldBuilder {
//	protected final War3ID levelField;
//	private final Map<String, GameObject> effectIDToUpgradeEffect = new HashMap<>();
//
//	public UpgradesFieldBuilder(ObjectData upgradeEffectMetaData) {
//		super(WorldEditorDataType.UPGRADES);
//		this.levelField = WE_Field.UPGRADE_MAX_LEVEL.getId();
//		for (String notEffectId : upgradeEffectMetaData.keySet()) {
//			GameObject upgradeEffect = upgradeEffectMetaData.get(notEffectId);
//			String key = upgradeEffect.getField("effectID") + upgradeEffect.getField("dataType");
//			effectIDToUpgradeEffect.put(key, upgradeEffect);
//		}
//	}
//
//	@Override
//	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
//		String effectType = metaDataField.getField("effectType");
//		if ("Base".equalsIgnoreCase(effectType)
//				|| "Mod".equalsIgnoreCase(effectType)
//				|| "Code".equalsIgnoreCase(effectType)) {
//			War3ID field = War3ID.fromString("gef" + metaDataField.getId().charAt(3));
//			String key = gameObject.getFieldAsString(field, 0) + metaDataField.getField("effectType");
//			return effectIDToUpgradeEffect.containsKey(key);
//		}
//		return true;
//	}
//
//	@Override
//	protected void makeAndAddFields(List<AbstractObjectField> fields, War3ID metaKey,
//	                                      GameObject metaDataField, MutableGameObject gameObject, ObjectData metaData) {
//		int repeatCount = metaDataField.getFieldValue("repeat");
//		int actualRepeatCount = gameObject.getFieldAsInteger(levelField, 0);
//		if (repeatCount >= 1 && actualRepeatCount > 1) {
//			for (int level = 1; level <= actualRepeatCount; level++) {
//				fields.add(create(gameObject, metaData, metaKey, level, true));
//			}
//		} else {
//			fields.add(create(gameObject, metaData, metaKey, repeatCount >= 1 ? 1 : 0, false));
//		}
//	}
//
//	@Override
//	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		String defaultDisplayName = getDisplayName2(metaData, metaKey, level, gameObject);
//		GameObject metaDataField = metaData.get(metaKey.toString());
//		String effectType = metaDataField.getField("effectType");
//		if ("Base".equalsIgnoreCase(effectType)
//				|| "Mod".equalsIgnoreCase(effectType)
//				|| "Code".equalsIgnoreCase(effectType)) {
//			War3ID gefField = War3ID.fromString("gef" + metaDataField.getId().charAt(3));
//			String fieldAsString = gameObject.getFieldAsString(gefField, 0);
//			GameObject upgradeEffect = effectIDToUpgradeEffect.get(fieldAsString + metaDataField.getField("effectType"));
//			String displayNameOfSubMetaField = upgradeEffect == null ? WE_STRING.WESTRING_ERROR_BADTRIGVAL : upgradeEffect.getField("displayName");
//			return String.format(defaultDisplayName, WEString.getString(displayNameOfSubMetaField));
//		}
//		return defaultDisplayName;
//	}
//
//	@Override
//	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		String prefix = "";
//		if (level > 0) {
//			String westring = WEString.getString(WE_STRING.WESTRING_AEVAL_LVL);
//			prefix = String.format(westring, level) + " - " + prefix;
//		}
//		return prefix;
//	}
//
//	protected String getDisplayName2(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		GameObject metaDataField = metaData.get(metaKey.toString());
//		String category = metaDataField.getField("category");
//		String prefix = categoryName(category) + " - ";
//		String displayName = metaDataField.getField("displayName");
//		return prefix + WEString.getString(displayName);
//	}
//
//}
