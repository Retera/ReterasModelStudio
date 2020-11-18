package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class HeadFlipper {
	private static final File output = new File(
			"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\(6)UpsideDownGnollWood.w3m\\");

	public static void main(final String[] args) {
		int x = 0;
		final MpqCodebase mpqCodebase = MpqCodebase.get();
		for (final String path : mpqCodebase.getListfile()) {
			final String lowerCase = path.toLowerCase();
			if (lowerCase.startsWith("war3.w3mod\\units\\") || lowerCase.startsWith("war3.w3mod\\buildings\\")) {
				if (lowerCase.endsWith(".mdx")) {
					try (BlizzardDataInputStream stream = new BlizzardDataInputStream(
							mpqCodebase.getResourceAsStream(path))) {
						final EditableModel model = new EditableModel(MdxUtils.loadModel(stream));
						Bone headBone = null;
						for (final IdObject node : model.getIdObjects()) {
							if ("bone_head".equals(node.getName().toLowerCase()) && (node instanceof Bone)) {
								headBone = (Bone) node;
								break;
							}
						}
						if (headBone != null) {
							final Set<Triangle> trianglesNeedingFlip = new HashSet<>();
							for (final Geoset geo : model.getGeosets()) {
								for (final GeosetVertex geosetVertex : geo.getVertices()) {
									final List<Bone> boneAttachments = geosetVertex.getBoneAttachments();
									if ((boneAttachments != null) && boneAttachments.contains(headBone)) {
										final Vertex pivotPoint = headBone.getPivotPoint();
										final double dz = geosetVertex.z - pivotPoint.z;
										geosetVertex.z = pivotPoint.z - dz;
										final Normal normal = geosetVertex.getNormal();
										if (normal != null) {
											normal.z = -normal.z;
										}
										for (final Triangle tri : geosetVertex.getTriangles()) {
											trianglesNeedingFlip.add(tri);
										}
									}
								}
								for (final Triangle tri : trianglesNeedingFlip) {
									tri.flip(false);
								}
								trianglesNeedingFlip.clear();
							}
							final File outputMDX = new File(output.getPath() + path.substring("war3.w3mod".length()));
							outputMDX.getParentFile().mkdirs();
							model.printTo(outputMDX);
							x++;
							if ((x % 100) == 0) {
								System.out.println("Done with " + x + " ...");
							}
						}
					} catch (final IOException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}
}
