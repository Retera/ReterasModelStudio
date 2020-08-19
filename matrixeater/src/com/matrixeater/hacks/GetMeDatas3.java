package com.matrixeater.hacks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class GetMeDatas3 {

	public static void main(final String[] args) {
		final Set<String> mergedListfile = MpqCodebase.get().getMergedListfile();

		try (final InputStream footman = new FileInputStream(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReforgedArtProject\\UndeadArthas3.mdx")) {
			try {

				final EditableModel model = new EditableModel(MdxUtils.loadMdlx(footman));
				int badGuys = 0;
				int goodGuys = 0;
				for (final Geoset geo : model.getGeosets()) {
					final List<GeosetVertex> vertices = geo.getVertices();
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
