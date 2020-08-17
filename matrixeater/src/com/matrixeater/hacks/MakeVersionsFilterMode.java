package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Normal;

public class MakeVersionsFilterMode {

	public static void main(final String[] args) throws IOException {
		final File sourceFile = new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReteraCubes\\Work\\ReteraCube.mdx");
		final FilterMode[] values = FilterMode.values();
		for (final FilterMode filterMode : values) {
			final EditableModel model = MdxUtils.loadEditableModel(sourceFile);
			final Geoset geoset = model.getGeoset(0);
			for (final GeosetVertex gv : geoset.getVertices()) {
				double u = 0, v = 0;
				final Normal normal = gv.getNormal();
				int xk = 0;
				for (byte c = 0; c < 3; c++) {
					final double coord = normal.getCoord(c);
					if (coord == 0) {
						final double newval = (Math.signum(gv.getCoord(c)) + 1) / 2.0;
						if (xk == 0) {
							u = newval;
						} else {
							v = newval;
						}
						xk++;
					}
				}
				gv.getTVertex(0).x = u;
				gv.getTVertex(0).y = v;
			}
			model.getMaterial(0).getLayers().get(0).setFilterMode(filterMode);
			model.getTexture(0).setPath("Textures\\Doodads0.blp");
			MdxUtils.saveEditableModel(model, new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReteraCubes\\Work\\ReteraCube_"
							+ filterMode.getMdlText() + ".mdx"));
		}

	}

}
