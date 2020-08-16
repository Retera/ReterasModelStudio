package com.matrixeater.hacks;

import java.io.IOException;
import java.util.Collection;

import com.hiveworkshop.wc3.mdx.MaterialChunk;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

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
				try (BlizzardDataInputStream stream = new BlizzardDataInputStream(
						mpqCodebase.getResourceAsStream(entry))) {
					final MdxModel model = MdxUtils.loadModel(stream);
					if ((model != null) && (model.materialChunk != null) && (model.materialChunk.material != null)) {
						for (final MaterialChunk.Material material : model.materialChunk.material) {
							if ("Shader_HD_DefaultUnit".equals(material.shader)) {
								final int layers = material.layerChunk.layer.length;
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
