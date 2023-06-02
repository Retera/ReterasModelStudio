package com.hiveworkshop.wc3.jworldedit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.util.CharInt;

public class TestMain {

	public static void main(final String[] args) {
		final int int1 = CharInt.toInt("SBLG");
		System.out.println(int1);

		final EditableModel myModel = new EditableModel("TestModel");
		myModel.add(new Animation("Stand", 333, 1333));
		final Bone myRoot = new Bone("Bone_Root");
		myModel.add(myRoot);
		AnimFlag translation = new AnimFlag("Translation");
		translation.setInterpType(InterpolationType.LINEAR);
		for (int frame = 0; frame <= 1000; frame += 100) {
			translation.addEntry(frame + 333, new Vertex(0, 0, Math.abs(frame - 500) / 500. * 128));
		}
		myRoot.add(translation);

		final Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
		final int nFaces = 42;
		final double ang = Math.PI * 2 / nFaces;
		for (int i = 0; i < nFaces; i++) {
			final double iAng = i * ang;
			final GeosetVertex vertex = new GeosetVertex(0, 0, 10, new Normal(0, 0, 1));
			final GeosetVertex vertex2 = new GeosetVertex(128 * Math.cos(iAng), 128 * Math.sin(iAng), 10,
					new Normal(0, 0, 1));
			final GeosetVertex vertex3 = new GeosetVertex(128 * Math.cos(iAng + ang), 128 * Math.sin(iAng + ang), 10,
					new Normal(0, 0, 1));
			vertex.getTverts().add(new TVertex(0, 0));
			vertex2.getTverts().add(new TVertex(0, 0));
			vertex3.getTverts().add(new TVertex(0, 0));
			geoset.add(vertex);
			geoset.add(vertex2);
			geoset.add(vertex3);
			final Triangle tri = new Triangle(vertex, vertex2, vertex3);
			vertex.getTriangles().add(tri);
			vertex2.getTriangles().add(tri);
			vertex3.getTriangles().add(tri);
			geoset.add(tri);

			final Bone generateBone = new Bone("SoftwareGen" + i);
			translation = new AnimFlag("Translation");
			translation.setInterpType(InterpolationType.LINEAR);
			for (int frame = 0; frame <= 1000; frame += 100) {
				translation.addEntry(frame + 333,
						new Vertex(0, 0, 64 + 64 * Math.cos(frame / 1000.0 * Math.PI * 2 + iAng)));
			}
			generateBone.add(translation);
			vertex2.addBoneAttachment((short) 255, generateBone);
			vertex3.addBoneAttachment((short) 255, generateBone);
			myModel.add(generateBone);

			vertex.addBoneAttachment((short) 255, generateBone);
		}
		myModel.add(geoset);

		myModel.printTo(new File("C:/users/micro/onedrive/documents/warcraft III/models/Generated43Matrices.mdx"),
				false);
//		for(int i = 0; i < )

		final EditableModel twoCloud = EditableModel
				.read(new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\TwoCloudB.mdx"));
		final Map<IdObject, Integer> objToCount = new HashMap<>();
		for (final Geoset g : twoCloud.getGeosets()) {
			for (final GeosetVertex gv : g.getVertices()) {
				for (final GeosetVertexBoneLink link : gv.getLinks()) {
					Integer integer = objToCount.get(link.bone);
					if (integer == null) {
						integer = 0;
					}
					objToCount.put(link.bone, integer + 1);
				}
			}
		}
		int max = 0;
		for (final IdObject obj : objToCount.keySet()) {
			final Integer count = objToCount.get(obj);
			if (count > max) {
				max = count;
			}
		}
		System.out.println(max);
	}

}
