package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.parsers.slk.WarcraftObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.UnitComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RaceData {
	List<WarcraftObject> units = new ArrayList<>();
	List<WarcraftObject> heroes = new ArrayList<>();
	List<WarcraftObject> buildings = new ArrayList<>();
	List<WarcraftObject> buildingsUprooted = new ArrayList<>();
	List<WarcraftObject> special = new ArrayList<>();

	void sort() {
		final Comparator<WarcraftObject> unitComp = new UnitComparator();

		units.sort(unitComp);
		heroes.sort(unitComp);
		buildings.sort(unitComp);
		buildingsUprooted.sort(unitComp);
		special.sort(unitComp);
	}
}
