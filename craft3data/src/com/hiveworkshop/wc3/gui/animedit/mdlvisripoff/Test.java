package com.hiveworkshop.wc3.gui.animedit.mdlvisripoff;

import com.etheller.warsmash.parsers.mdlx.InterpolationType;
import com.hiveworkshop.wc3.mdl.AnimFlag;

public class Test {
	public static void main(final String[] args) {
		final AnimFlag vis = AnimFlag.createEmpty2018("Alpha", InterpolationType.DONT_INTERP, null);
		vis.addEntry(100, 0.5);
		vis.addEntry(200, 0.7);
		vis.addEntry(300, 0.8);
		final int ceilIndex = vis.ceilIndex(200);
		System.out.println(ceilIndex);
	}
}
