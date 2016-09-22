package com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta;

import com.hiveworkshop.wc3.resources.WEString;

public enum Category {
	STATS("stats"), DATA("data"), ABILITY("abil"), COMBAT("combat"), ART("art"), TEXT("text"), SOUND("sound"), TECHTREE("tech"), MOVEMENT("move"), PATHING("path"), EDITOR("editor");

	private final String codeName;
	Category(final String codeName) {
		this.codeName = codeName;
	}
	public String getDisplayName() {
		return categoryName(codeName);
	}
	public String getCodeName() {
		return codeName;
	}
	public static Category fromCodeName(final String name) {
		for(final Category cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("category does not exist: " + name);
	}
	private static String categoryName(final String cat) {
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
