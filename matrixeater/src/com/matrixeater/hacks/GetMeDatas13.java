package com.matrixeater.hacks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.etheller.warsmash.parsers.mdlx.MdlxMaterial;
import com.etheller.warsmash.parsers.mdlx.MdlxModel;

import com.hiveworkshop.wc3.mdx.MdxUtils;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class GetMeDatas13 {

	public static void main(final String[] args) {
		final int[] tallies = new int[10];
		int crazyOutliers = 0;
		final MpqCodebase mpqCodebase = MpqCodebase.get();
		final Collection<String> listfile = mpqCodebase.getListfile();
		int index = 0;
		final int listFileSize = listfile.size();
		for (final String entry : listfile) {
			if (entry.toLowerCase().endsWith(".mdx")) {
				try (InputStream stream = mpqCodebase.getResourceAsStream(entry)) {
					final MdlxModel model = MdxUtils.loadModel(stream);
					if (model != null) {
						for (final MdlxMaterial material : model.materials) {
							if ("Shader_HD_DefaultUnit".equals(material.shader)) {
								final int layers = material.layers.size();
								if (layers >= tallies.length) {
									crazyOutliers++;
								} else {
									tallies[layers]++;
								}
							}
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			index++;
			if ((index % 600) == 0) {
				System.out.println("Reading... " + index + "/" + listFileSize);
			}
		}
		for (int i = 0; i < tallies.length; i++) {
			System.out.println(i + ": " + tallies[i]);
		}
		System.out.println("Crazy outliers: " + crazyOutliers);
	}

}
