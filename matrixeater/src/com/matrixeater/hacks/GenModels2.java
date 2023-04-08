package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.ModelUtils;
import com.hiveworkshop.wc3.util.ModelUtils.Mesh;

public class GenModels2 {

	public static void main(final String[] args) {
		final File dest = new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Generated");
		dest.mkdir();
		final EditableModel model = new EditableModel("ParticleTest");
		final Geoset geoset = new Geoset();
		final Mesh planeMesh = ModelUtils.createPlane((byte) 1, (byte) 2, new Vertex(1, 0, 0), 0, -128, -128, 128, 128,
				1);
		geoset.getTriangles().addAll(planeMesh.getTriangles());
		geoset.getVertices().addAll(planeMesh.getVertices());
		final Bone dummy = new Bone("Bone_Root");
		for (final GeosetVertex gv : geoset.getVertices()) {
			gv.addBoneAttachment((short) 255, dummy);
		}
		model.add(dummy);

		model.add(geoset);
		final Bitmap cloudsTexture = new Bitmap("Textures\\clouds_anim1.blp");
		final Material material = new Material(new Layer("Additive", cloudsTexture));
		geoset.setMaterial(material);

		final Animation animation = new Animation("Stand", 333, 16333);
		model.add(animation);

		System.out.println(geoset.getVertices().size());
		final ArrayList<GeosetVertex> vertices = geoset.getVertices();
		for (int i = 0; i < vertices.size(); i++) {
			vertices.get(i).getTVertex(0).scale(0, 0, 1 / 4f, 1 / 4f);
			System.out.println(vertices.get(i).getTVertex(0));
		}
		final ArrayList<AnimFlag> flags = new ArrayList<>();
		final AnimFlag translationData = new AnimFlag("Translation");
		for (int i = 0; i < 16; i++) {
			translationData.addEntry(333 + (i * 1000), new Vertex((i % 4) * 0.25, (i / 4) * 0.25, 0));
		}
		flags.add(translationData);
		final TextureAnim textureAnim = new TextureAnim(flags);
		material.getLayers().get(0).setTextureAnim(textureAnim);

		model.printTo(new File(dest.getPath() + "\\ParticleTest.mdl"));
	}
}
