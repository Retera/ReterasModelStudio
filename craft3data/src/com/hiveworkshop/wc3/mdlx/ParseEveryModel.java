package com.hiveworkshop.wc3.mdlx;

import java.io.IOException;
import java.util.Set;

import com.etheller.warsmash.parsers.mdlx.MdlxGeoset;
import com.etheller.warsmash.parsers.mdlx.MdlxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class ParseEveryModel {

	public static void main(final String[] args) {
		int parsed = 0;
		final Set<String> mergedListfile = MpqCodebase.get().getMergedListfile();
		int maxVertexGroup = 0;
		for (final String str : mergedListfile) {
			if (str.toLowerCase().endsWith(".mdx")) {
//				System.err.println(str);
				try {
					final MdlxModel loadModel = MdxUtils.loadMdlx(MpqCodebase.get().getResourceAsStream(str));
					if (loadModel != null) {
						for (final MdlxGeoset geosetChunk : loadModel.geosets) {
							for (final long matrixGroup : geosetChunk.matrixGroups) {
								if (matrixGroup > maxVertexGroup) {
									maxVertexGroup = (int)matrixGroup;
								}
							}
						}
					}
					parsed++;
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.err.println("parsed " + parsed + " successfully");
		System.out.println("Max was: " + maxVertexGroup);
	}

}
