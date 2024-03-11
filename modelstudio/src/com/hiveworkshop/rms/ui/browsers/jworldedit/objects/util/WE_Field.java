package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util;

import com.hiveworkshop.rms.util.War3ID;

public enum WE_Field {
	DESTR_CATEGORY ("bcat"),
	DESTR_FILE("bfil"),
	DESTR_VARIATIONS("bvar"),
	DOODAD_CAT("dcat"),
	DOODAD_FILE("dfil"),
	DOODAD_VARIATIONS_FIELD("dvar"),
	MODEL_FILE("umdl"),
	UNIT_FILE("umdl"),
	UNIT_NAME("unam"),
	UNITS_ICON("uico"),
	INTERFACE_ICON("uico"),
	UNIT_PROJECTILE_1("ua1m"),
	UNIT_PROJECTILE_2("ua2m"),
	UNIT_RACE("urac"),
	UNIT_LEVEL("ulev"),
	UNIT_IS_BUILDING("ubdg"),
	UNIT_CATEGORIZE_SPECIAL("uspe"),
	UNIT_CATEGORIZE_CAMPAIGN("ucam"),
	UNIT_DISPLAY_AS_NEUTRAL_HOSTILE("uhos"),
	UPGRADES_ICON("gar1"),
	UPGRADE_MAX_LEVEL("glvl"),
	UPGR_RACE("grac"),
	ABILITIES_ICON("aart"),
	ABIL_IS_ITEM_ABIL("aite"),
	ABIL_IS_HERO_ABIL("aher"),
	ABIL_RACE("arac"),
	ABIL_LEVLE("alev"),
	ITEM_ICON("iico"),
	ITEM_CLASS("icla"),
	BUFF_RACE("frac"),
	BUFFS_EFFECTS_ICON("fart"),
	BUFF_EDITOR_NAME("fnam"),
	IS_EFFECT("feff"),

	;
	final String string;
	final War3ID id;
	WE_Field(String string) {
		this.string = string;
		this.id = War3ID.fromString(string);
	}

	public String getString() {
		return string;
	}

	public War3ID getId() {
		return id;
	}
}
