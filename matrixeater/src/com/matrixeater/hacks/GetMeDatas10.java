package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas10 {

	public static void main(final String[] args) {

		try (final InputStream footman = new FileInputStream(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Requests\\Wazzz\\WispGrove50.mdx")) {
			try {

				final EditableModel model = new EditableModel(MdxUtils.loadModel(new BlizzardDataInputStream(footman)));

				IdObject bad = null;
				for (final IdObject obj : model.getIdObjects()) {
					if (obj.getName().startsWith("canopy_08")) {
						bad = obj;
					}
				}

				for (final Geoset g : model.getGeosets()) {
					final Set<GeosetVertex> toDelete = new HashSet<>();
					for (final GeosetVertex gv : g.getVertices()) {
						if (gv.getSkinBones() != null) {
							for (final Bone b : gv.getSkinBones()) {
								if (b == bad) {
									toDelete.add(gv);
								}
							}
						}
					}
					for (final GeosetVertex gv : toDelete) {
						g.remove(gv);
						for (final Triangle t : gv.getTriangles()) {
							g.remove(t);
						}
					}
				}

				model.printTo(new File(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Requests\\Wazzz\\WispGrove51.mdx"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void fix(final AnimFlag visibilityAnimation, final Animation deathSequence) {
		final int startOfDeath = deathSequence.getStart();
		final int endOfDeath = deathSequence.getEnd();
		if (visibilityAnimation != null) {
			final int floorIndexOfStart = visibilityAnimation.floorIndex(startOfDeath);
			if (floorIndexOfStart < visibilityAnimation.floorIndex(endOfDeath)) {
				if (visibilityAnimation.getTimes().get(floorIndexOfStart) < startOfDeath) {
					visibilityAnimation.addEntry(startOfDeath, visibilityAnimation.getEntry(floorIndexOfStart).value);
				}
			}
		}
	}

}
