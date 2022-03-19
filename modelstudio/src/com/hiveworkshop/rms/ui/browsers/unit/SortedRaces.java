package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortedRaces {

	String[] raceKeys = {"human", "orc", "undead", "nightelf", "neutrals", "naga"};
	static Map<String, RaceData> sortedRaces;

	private final ObjectData unitData;
	private final ObjectData abilityData;

	public static void dropRaceCache() {
		sortedRaces = null;
	}

	public SortedRaces(ObjectData unitData, ObjectData abilityData){
		this.unitData = unitData;
		this.abilityData = abilityData;
		sortRaces();
	}

	public RaceData get(String s){
		if(sortedRaces == null){
			sortRaces();
		}
		return sortedRaces.get(s);
	}

	public void sortRaces() {
		sortedRaces = new HashMap<>();

		for (String raceKey : raceKeys) {
			sortedRaces.put(raceKey + "melee", new RaceData());
			sortedRaces.put(raceKey + "campaign", new RaceData());
			sortedRaces.put(raceKey + "custom", new RaceData());
			sortedRaces.put(raceKey + "hidden", new RaceData());
		}

		GameObject root = abilityData.get("Aroo");
		GameObject rootAncientProtector = abilityData.get("Aro2");
		GameObject rootAncients = abilityData.get("Aro1");

		for (String str : unitData.keySet()) {
			String strUpper = str.toUpperCase();
			if (strUpper.startsWith("B")
					|| strUpper.startsWith("R")
					|| strUpper.startsWith("A")
					|| strUpper.startsWith("S")
					|| strUpper.startsWith("X")
					|| strUpper.startsWith("M")
					|| strUpper.startsWith("HERO")) {
				continue;
			}

			GameObject unit = unitData.get(str);

			int sortGroupId = getSortGroupId(root, rootAncientProtector, rootAncients, unit);

			sortedRaces.get(getStoreKey(unit)).addUnitToCorrectList(unit, sortGroupId);
		}

		for (RaceData race : sortedRaces.values()) {
			race.sort();
		}
	}

	private String getStoreKey(GameObject unit) {
		String raceKey = getRaceKey(unit);
		if (unit.getField("JWC3_IS_CUSTOM_UNIT").startsWith("1")) {
			return raceKey + "custom";
		} else if (!unit.getField("inEditor").startsWith("1")) {
			return raceKey + "hidden";
		} else if (unit.getField("campaign").startsWith("1")) {
			return raceKey + "campaign";
		} else {
			return raceKey + "melee";
		}
	}

	public String getRaceKey(GameObject unit) {
		String race = unit.getField("race");
		for (String key : raceKeys) {
			if (race.equals(key)) {
				return key;
			}
		}

		return "neutrals";
	}

	private int getSortGroupId(GameObject root, GameObject rootAncientProtector, GameObject rootAncients, GameObject unit) {
		List<? extends GameObject> abilities = unit.getFieldAsList("abilList", abilityData);// .abilities();

		if (unit.getField("special").startsWith("1")) {
			return 4;
		} else if (unit.getId().length() > 1 && Character.isUpperCase(unit.getId().charAt(0))) {
			return 1;
		} else if (abilities.contains(root)
				|| abilities.contains(rootAncients)
				|| abilities.contains(rootAncientProtector)) {
			return 3;
		} else if (unit.getField("isbldg").startsWith("1")) {
			return 2;
		} else {
			return 0;
		}
	}
}
