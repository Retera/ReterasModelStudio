//package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;
//
//import com.hiveworkshop.rms.parsers.slk.GameObject;
//import com.hiveworkshop.rms.parsers.slk.ObjectData;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
//import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
//import com.hiveworkshop.rms.util.War3ID;
//
//public final class LevelsSingleFieldFactory extends AbstractSingleFieldFactory {
//	public static final LevelsSingleFieldFactory INSTANCE = new LevelsSingleFieldFactory();
//
//	public LevelsSingleFieldFactory() {
//
//	}
//
//	public LevelsSingleFieldFactory(WorldEditorDataType worldEditorDataType) {
//		this.worldEditorDataType = worldEditorDataType;
//	}
//
//	@Override
//	protected String getDisplayName(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		GameObject metaDataFieldObject = metaData.get(metaKey.toString());
//		String category = metaDataFieldObject.getField("category");
//		String prefix = categoryName(category) + " - ";
//		String displayName = metaDataFieldObject.getField("displayName");
//		return prefix + WEString.getString(displayName);
//	}
//
//	@Override
//	protected String getDisplayPrefix(ObjectData metaData, War3ID metaKey, int level, MutableGameObject gameObject) {
//		String prefix = "";
//		if (level > 0) {
//			String westring_aeval_lvl = WEString.getString("WESTRING_AEVAL_LVL");
//			prefix = String.format(westring_aeval_lvl, level) + " - " + prefix;
//		}
//		return prefix;
//	}
//}
