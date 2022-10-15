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
//import java.util.List;
//
//public class DoodadFieldBuilder extends AbstractFieldBuilder {
//	protected final War3ID levelField;
//	public DoodadFieldBuilder() {
//		super(WorldEditorDataType.DOODADS);
//		this.levelField = WE_Field.DOODAD_VARIATIONS_FIELD.getId();
//	}
//
//	@Override
//	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField, War3ID metaKey) {
//		return true;
//	}
//
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
//	@Override
//	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		GameObject metaDataField = metaData.get(metaKey.toString());
//		String category = metaDataField.getField("category");
//		String prefix = categoryName(category) + " - ";
//
//		String subPrefix = getSubPrefix(level);
//		String displayName = metaDataField.getField("displayName");
//		return prefix + subPrefix + WEString.getString(displayName);
//	}
//
//	private String getSubPrefix(int level) {
//		String subPrefix = "";
//		if (level > 0) {
//			String westring = WEString.getString(WE_STRING.WESTRING_DEVAL_VAR);
//			subPrefix += String.format(westring, level) + " - ";
//		}
//		return subPrefix;
//	}
//
//	@Override
//	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		return "";
//	}
//
//}
