package com.hiveworkshop.wc3.units;

import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;

public class DataTableFileGenerator {

	public static void main(final String[] args) {
		final WarcraftData standardUnits = StandardObjectData.getStandardUnits();
		System.out.println(standardUnits.get("opeo").getName());
	}

}
