package com.hiveworkshop.wc3.jworldedit.objects.better.fields;

import java.awt.Component;

import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;

public interface EditableOnscreenObjectField {
	String getDisplayName(final MutableGameObject gameUnit);

	/* for sorting */
	String getSortName(final MutableGameObject gameUnit);

	/* for sorting */
	int getLevel();

	String getRawDataName();

	Object getValue(final MutableGameObject gameUnit);

	boolean hasEditedValue(MutableGameObject gameUnit);

	boolean popupEditor(MutableGameObject gameUnit, Component parent, boolean editRawData, boolean disableLimits);

	// void setValue(final MutableGameObject gameUnit, final Object value);

	public static String categoryName(final String cat) {
		switch (cat.toLowerCase()) {
		case "abil":
			return WEString.getString("WESTRING_OE_CAT_ABILITIES").replace("&", "");
		case "art":
			return WEString.getString("WESTRING_OE_CAT_ART").replace("&", "");
		case "combat":
			return WEString.getString("WESTRING_OE_CAT_COMBAT").replace("&", "");
		case "data":
			return WEString.getString("WESTRING_OE_CAT_DATA").replace("&", "");
		case "editor":
			return WEString.getString("WESTRING_OE_CAT_EDITOR").replace("&", "");
		case "move":
			return WEString.getString("WESTRING_OE_CAT_MOVEMENT").replace("&", "");
		case "path":
			return WEString.getString("WESTRING_OE_CAT_PATHING").replace("&", "");
		case "sound":
			return WEString.getString("WESTRING_OE_CAT_SOUND").replace("&", "");
		case "stats":
			return WEString.getString("WESTRING_OE_CAT_STATS").replace("&", "");
		case "tech":
			return WEString.getString("WESTRING_OE_CAT_TECHTREE").replace("&", "");
		case "text":
			return WEString.getString("WESTRING_OE_CAT_TEXT").replace("&", "");
		}
		return WEString.getString("WESTRING_UNKNOWN");
	}
}
