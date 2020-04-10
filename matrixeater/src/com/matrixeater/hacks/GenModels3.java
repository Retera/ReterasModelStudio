package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.util.ModelUtils;
import com.hiveworkshop.wc3.util.ModelUtils.Mesh;

public class GenModels3 {

	public static void main(final String[] args) {
		final File source = new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Requests\\Wazzz\\WispGrove43.mdx");
		final MDL model = MDL.read(source);
		final List<Particle> particles = new LinkedList<>();
		final Bone particleRoot = new Bone("Bone_ParticleRoot");
		final List<Geoset> geosets = new LinkedList<>();
		final int particleCount = 16;
		for (int i = 0; i < particleCount; i++) {
			geosets.add(new Geoset());
		}
		Animation existingMorph = null;
		for (final Animation anim : model.getAnims()) {
			if (anim.getName().equals("Morph")) {
				existingMorph = anim;
			}
		}
		for (final ParticleEmitter2 particle : model.sortedIdObjects(ParticleEmitter2.class)) {
			if (particle.getName().startsWith("BlizPart")) {
				model.remove(particle);
			}
		}
		final int start = existingMorph.getStart();
		final int end = existingMorph.getEnd();
		final int startUsed = start;
		final int endUsed = end;
		for (int i = 0; i < 100; i++) {
			final Particle part = new Particle();
			final Bone particleBone = new Bone("Bone_Particle" + i);
			particleBone.add("Billboarded");
			particleBone.setParent(particleRoot);
			final int leafSize = 6 + (int) (Math.random() * 3);
			final Mesh planeMesh = ModelUtils.createPlane((byte) 1, (byte) 2, new Vertex(1, 0, 0), 0, -leafSize,
					-leafSize, leafSize, leafSize, 1);
			part.bone = particleBone;
			part.particle2Dobj = planeMesh;
			particles.add(part);
			final Geoset geoset = geosets.get((int) (Math.random() * 16));
			geoset.getTriangles().addAll(planeMesh.getTriangles());
			geoset.getVertices().addAll(planeMesh.getVertices());
			final double randomAngle = Math.random() * Math.PI * 2;
			final double depth = 97 + (Math.random() * 16);
			final double x = Math.cos(randomAngle) * depth;
			final double y = Math.sin(randomAngle) * depth;
			final int z = 240;
			for (final GeosetVertex v : planeMesh.getVertices()) {
				v.x += x;
				v.y += y;
				v.z += z;
				v.addBoneAttachment(particleBone);
			}
			particleBone.setPivotPoint(new Vertex(x, y, z));

			final AnimFlag scalingData = new AnimFlag("Scaling");
			final AnimFlag translationData = new AnimFlag("Translation");
			final int inwardTravelTime = 7000;
			final int timeOfInwardBegin = startUsed
					+ (int) (Math.random() * (((endUsed - startUsed) * 0.7) - inwardTravelTime));
			if (timeOfInwardBegin != startUsed) {
				translationData.addEntry(startUsed, new Vertex(0, 0, 0), new Vertex(0, 0, 0), new Vertex(0, 0, 0));
				scalingData.addEntry(startUsed, new Vertex(0, 0, 0));
			}
			scalingData.addEntry(timeOfInwardBegin, new Vertex(1, 1, 1));
			translationData.addEntry(timeOfInwardBegin, new Vertex(0, 0, 0), new Vertex(0, 0, 0), new Vertex(0, 0, 0));
			final int timeOfInwardEnd = timeOfInwardBegin + inwardTravelTime;
			final int tornadoDepth = -100;
			translationData.addEntry(timeOfInwardEnd, new Vertex(-x, -y, tornadoDepth),
					new Vertex(-x, -y, tornadoDepth), new Vertex(-x, -y, tornadoDepth));
			if (timeOfInwardEnd != endUsed) {
				translationData.addEntry(endUsed, new Vertex(-x, -y, tornadoDepth), new Vertex(-x, -y, tornadoDepth),
						new Vertex(-x, -y, tornadoDepth));
				scalingData.addEntry(endUsed, new Vertex(0, 0, 0));
			}
			scalingData.addEntry(timeOfInwardEnd, new Vertex(0, 0, 0));
			translationData.setInterpType(InterpolationType.HERMITE);
			particleBone.add(translationData);
			particleBone.add(scalingData);

			model.add(particleBone);
		}
		model.add(particleRoot);
		for (final Geoset geoset : geosets) {
			model.add(geoset);
		}

		final Bitmap cloudsTexture = new Bitmap("Textures\\leaf4x4.blp");
		int geosetId = 0;
		for (final Geoset geoset : geosets) {
			final Material material = new Material(new Layer("Blend", cloudsTexture));
			material.setShaderString("Shader_SD_FixedFunction");
			geoset.setMaterial(material);
			System.out.println(geoset.getVertices().size());
			final ArrayList<GeosetVertex> vertices = geoset.getVertices();
			for (int i = 0; i < vertices.size(); i++) {
				vertices.get(i).getTVertex(0).scale(0, 0, 1 / 4f, 1 / 4f);
				System.out.println(vertices.get(i).getTVertex(0));
			}
			final ArrayList<AnimFlag> flags = new ArrayList<>();
			final AnimFlag translationData = new AnimFlag("Translation");
			for (int i = 0; i < 16; i++) {
				translationData.addEntry(0 + (i * 100),
						new Vertex(((i + geosetId) % 4) * 0.25, (((i + geosetId) % particleCount) / 4) * 0.25, 0));
			}
			translationData.setGlobSeq(1600);
			flags.add(translationData);
			final TextureAnim textureAnim = new TextureAnim(flags);
			material.getLayers().get(0).setTextureAnim(textureAnim);
			geosetId++;

			final AnimFlag alphaData = new AnimFlag("Alpha");
			for (final Animation anim : model.getAnims()) {
				if (anim == existingMorph) {
					alphaData.addEntry(anim.getStart(), 1);
				} else {
					alphaData.addEntry(anim.getStart(), 0);
				}
			}
			geoset.forceGetGeosetAnim().addAnimFlag(alphaData);
		}

		final AnimFlag spinData = new AnimFlag("Rotation");
		spinData.setInterpType(InterpolationType.LINEAR);
		for (int i = 0; i < 9; i++) {
			spinData.addEntry(startUsed + ((i * (endUsed - startUsed)) / 8),
					new QuaternionRotation(new Vertex(0, 0, 1), (Math.PI / 2) * i));
		}
		particleRoot.add(spinData);

		model.printTo(new File(source.getParentFile().getPath() + "\\Generated.mdx"));
	}

	private static class Particle {
		private Bone bone;
		private Mesh particle2Dobj;
	}
}
