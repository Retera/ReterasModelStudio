package com.hiveworkshop.wc3.mdlx;

import java.io.IOException;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class ParseEveryModel {

	public static void main(final String[] args) {
		int parsed = 0;
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();
		for (final String str : mergedListfile) {
			if (str.toLowerCase().endsWith(".mdx")) {
				System.err.println(str);
				try {
					final MdxModel loadModel = MdxUtils
							.loadModel(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream(str)));
					parsed++;
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.err.println("parsed " + parsed + " successfully");
	}

}
