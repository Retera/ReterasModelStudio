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
//import java.util.Arrays;
//import java.util.List;
//
//public class AbilityFieldBuilder extends AbstractFieldBuilder {
//	protected final War3ID levelField;
//
//	public AbilityFieldBuilder() {
//		super(WorldEditorDataType.ABILITIES);
//		this.levelField = WE_Field.ABIL_LEVLE.getId();
//	}
//
//	@Override
//	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
//		String useSpecific = metaDataField.getField("useSpecific");
//		String notAllowed = metaDataField.getField("notSpecific"); //specificallyNotAllowedAbilityIds
//
//		String codeStringValue = gameObject.getCode().asStringValue();
//
//		boolean doUseSpecific = 0 >= useSpecific.length() || Arrays.asList(useSpecific.split(",")).contains(codeStringValue);
//		boolean isAllowed = 0 >= notAllowed.length() || !Arrays.asList(notAllowed.split(",")).contains(codeStringValue);
//		if (doUseSpecific && isAllowed) {
//			boolean heroAbility = gameObject.getFieldAsBoolean(WE_Field.ABIL_IS_HERO_ABIL.getId(), 0);
//			boolean itemAbility = gameObject.getFieldAsBoolean(WE_Field.ABIL_IS_ITEM_ABIL.getId(), 0);
//			boolean useHero = heroAbility && !itemAbility && metaDataField.getFieldValue("useHero") == 1;
//			boolean useUnit = !heroAbility && !itemAbility && metaDataField.getFieldValue("useUnit") == 1;
//			boolean useItem = itemAbility && metaDataField.getFieldValue("useItem") == 1;
//
//			return (useHero || useUnit || useItem);
//		} else {
//			return false;
//		}
//	}
//	@Override
//	protected void makeAndAddFields(List<AbstractObjectField> fields, War3ID metaKey,
//	                                GameObject metaDataField, MutableGameObject gameObject, ObjectData metaData) {
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
//		GameObject metaDataField = metaData.get(metaKey.toString());
//		String category = metaDataField.getField("category");
//		String prefix = categoryName(category) + " - ";
//		String displayName = metaDataField.getField("displayName");
//		return prefix + WEString.getString(displayName);
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
//}
