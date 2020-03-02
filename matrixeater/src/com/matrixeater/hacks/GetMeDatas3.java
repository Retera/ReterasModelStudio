package com.matrixeater.hacks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas3 {

	public static void main(final String[] args) {
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();

		try (final InputStream footman = new FileInputStream(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReforgedArtProject\\UndeadArthas3.mdx")) {
			try {

				final MDL model = new MDL(MdxUtils.loadModel(new BlizzardDataInputStream(footman)));
				int badGuys = 0;
				int goodGuys = 0;
				for (final Geoset geo : model.getGeosets()) {
					final ArrayList<GeosetVertex> vertices = geo.getVertices();
					for (final GeosetVertex gv : vertices) {
						int sumWeight = 0;
						for (int i = 0; i < 4; i++) {
							sumWeight += gv.getSkinBoneWeights()[i];
						}
						if (sumWeight == 255) {
							goodGuys++;
						} else {
							badGuys++;
						}
					}
				}
				System.out.println(badGuys + ", " + goodGuys);
//			model.printTo(new File(
//					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReforgedArtProject\\UndeadArthasAutoOut.mdx"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

}
