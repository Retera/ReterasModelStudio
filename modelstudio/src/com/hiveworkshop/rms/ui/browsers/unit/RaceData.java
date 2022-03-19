package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RaceData {
	List<GameObject> units = new ArrayList<>();
	List<GameObject> heroes = new ArrayList<>();
	List<GameObject> buildings = new ArrayList<>();
	List<GameObject> buildingsUprooted = new ArrayList<>();
	List<GameObject> special = new ArrayList<>();

	void sort() {
		Collections.sort(units);
		Collections.sort(heroes);
		Collections.sort(buildings);
		Collections.sort(buildingsUprooted);
		Collections.sort(special);
	}

	protected void addUnitToCorrectList(GameObject unit, int sortGroupId) {
		switch (sortGroupId) {
			case 0 -> units.add(unit);
			case 1 -> heroes.add(unit);
			case 2 -> buildings.add(unit);
			case 3 -> {
				buildingsUprooted.add(unit);
				buildings.add(unit);
			}
			case 4 -> special.add(unit);
		}
	}
}
