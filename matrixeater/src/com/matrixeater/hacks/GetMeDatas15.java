package com.matrixeater.hacks;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class GetMeDatas15 {
	public static void main(final String[] args) {
		final MpqCodebase mpqCodebase = MpqCodebase.get();
		final SetView<String> mergedListfile = mpqCodebase.getMergedListfile();
		for (final String item : mergedListfile) {
			if (item.toLowerCase().contains("knight")) {
				System.out.println(item);
			}
		}
	}
}
