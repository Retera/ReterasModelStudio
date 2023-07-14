package com.hiveworkshop.rms.parsers.simsstuff.smarter;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.simsstuff.SimsBone;
import com.hiveworkshop.rms.parsers.simsstuff.SimsSkeleton;
import com.hiveworkshop.rms.parsers.simsstuff.plain.SimModel2;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Generator {
	String name;
	String texture;
	Animation stand = new Animation("Stand", 0, 500);
	List<Bone> bones = new ArrayList<>();
	List<Bone> blendBones = new ArrayList<>();
	List<GeosetVertex> blendVerts = new ArrayList<>();
	List<GeosetVertex> realVerts = new ArrayList<>();
	List<Triangle> faces = new ArrayList<>();

	public Generator(SimModel2 simModel2) {
		name = simModel2.getName();
		texture = simModel2.getTexture();
		for (String boneName: simModel2.getBoneNames()) {
			bones.add(new Bone(boneName));
		}

//		for (int[] blendData : simModel2.getBlendWeights()) {
//			int blendDatum = blendData[1];
//			Bone blendVert = new Bone("BlendVert_" + blendDatum);
//			blendVert.setPivotPoint(new Vec3(simModel2.getRealVerts()[blendDatum][0]));
//			blendBones.add(blendVert);
//		}

		for (int i = 0; i < simModel2.getRealVertsCount(); i++) {
			float[][] vertPosNorm = simModel2.getRealVerts()[i];
			Vec3 pos = new Vec3(vertPosNorm[0]);
			Vec3 norm = new Vec3(vertPosNorm[1]);
			GeosetVertex vert = new GeosetVertex(pos, norm);
			vert.scale(20f);
			vert.initV900();
			vert.setSkinBoneWeights(new short[]{(short) 255, (short) 0, (short) 0, (short) 0});
//			vert.initSkinBones();
			vert.addTVertex(new Vec2(simModel2.getTexVerts()[i]));
			realVerts.add(vert);
		}

		Bone tempBone = new Bone("temp");
		for (int i = 0; i < simModel2.getBlendVertCount(); i++) {
			int[] blendData = simModel2.getBlendWeights()[i];
			int blendDatum = blendData[1];
			short weight = (short) (255f * blendData[1]/(float)0x8000);
			Bone blendBone = new Bone("BlendVert_" + blendData[0]);
			GeosetVertex realVert = realVerts.get(blendData[0]);
//			realVert.initV900();
//			realVert.initSkinBones();
			realVert.setSkinBone(tempBone, (short) (255 - weight), 0);
			realVert.setSkinBone(blendBone, weight, 1);
			blendBone.setPivotPoint(realVert);
			blendBones.add(blendBone);
			float[][] blendVertPosNorm = simModel2.getBlendVerts()[i];
			Vec3 pos = new Vec3(blendVertPosNorm[0]);
			Vec3 norm = new Vec3(blendVertPosNorm[1]);
			GeosetVertex blendVert = new GeosetVertex(pos, norm);
			blendVert.scale(20f);
			blendVert.initV900();
			blendVert.setSkinBoneWeights(new short[]{(short) 255, (short) 0, (short) 0, (short) 0});
//			blendVert.initSkinBones();
			blendVert.addTVertex(new Vec2());
			Vec3AnimFlag timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
			timeline.addEntry(0, pos.scale(20f).sub(realVert), stand);
			blendBone.add(timeline);
			blendVerts.add(blendVert);
		}

		for (int[] binding : simModel2.getBindings()) {
			Bone b = bones.get(binding[0]);
			for (int j = binding[1]; j < binding[1] + binding[2]; j++) {
				GeosetVertex vertex = realVerts.get(j);
				if (vertex.getSkinBones() != null && vertex.getSkinBones()[1] != null) {
					vertex.setSkinBone(b, 0);
				} else {
					vertex.initSkinBones();
					vertex.setSkinBone(b, (short) 255, 0);
				}
			}
			for (int j = binding[3]; j < binding[3] + binding[4]; j++) {
				GeosetVertex vertex = blendVerts.get(j);
				vertex.initSkinBones();
				vertex.setSkinBone(b, (short) 255, 0);
				Bone bone = blendBones.get(j);
				bone.setParent(b);
				bone.setName("Blend_" + b.getName() + "_" + j);
			}
		}

		for (int[] face : simModel2.getFaces()) {
			Triangle triangle = new Triangle(realVerts.get(face[0]), realVerts.get(face[1]), realVerts.get(face[2]));
			faces.add(triangle);
		}
	}
	public Generator(SimModel2 simModel2, boolean ugg) {
		name = simModel2.getName();
		texture = simModel2.getTexture();
		for (String boneName: simModel2.getBoneNames()) {
			bones.add(new Bone(boneName));
		}

//		for (int[] blendData : simModel2.getBlendWeights()) {
//			int blendDatum = blendData[1];
//			Bone blendVert = new Bone("BlendVert_" + blendDatum);
//			blendVert.setPivotPoint(new Vec3(simModel2.getRealVerts()[blendDatum][0]));
//			blendBones.add(blendVert);
//		}

		for (int i = 0; i < simModel2.getRealVertsCount(); i++) {
			float[][] vertPosNorm = simModel2.getRealVerts()[i];
			Vec3 pos = new Vec3(vertPosNorm[0]);
			Vec3 norm = new Vec3(vertPosNorm[1]);
			GeosetVertex vert = new GeosetVertex(pos, norm);
			vert.scale(20f);
			vert.initV900();
			vert.setSkinBoneWeights(new short[]{(short) 255, (short) 0, (short) 0, (short) 0});
//			vert.initSkinBones();
			vert.addTVertex(new Vec2(simModel2.getTexVerts()[i]));
			realVerts.add(vert);
		}

//		Bone tempBone = new Bone("temp");
//		for (int i = 0; i < simModel2.getBlendVertCount(); i++) {
//			int[] blendData = simModel2.getBlendWeights()[i];
//			short weight = (short) (255f * blendData[1]/(float)0x8000);
//			Bone blendBone = new Bone("BlendVert_" + blendData[0]);
//			GeosetVertex realVert = realVerts.get(blendData[0]);
////			realVert.initV900();
////			realVert.initSkinBones();
//			realVert.setSkinBone(tempBone, (short) (255 - weight), 0);
//			realVert.setSkinBone(blendBone, weight, 1);
//			blendBone.setPivotPoint(realVert);
//			blendBones.add(blendBone);
//			float[][] blendVertPosNorm = simModel2.getBlendVerts()[i];
//			Vec3 pos = new Vec3(blendVertPosNorm[0]);
//			Vec3 norm = new Vec3(blendVertPosNorm[1]);
//			GeosetVertex blendVert = new GeosetVertex(pos, norm);
//			blendVert.scale(20f);
//			blendVert.initV900();
//			blendVert.setSkinBoneWeights(new short[]{(short) 255, (short) 0, (short) 0, (short) 0});
////			blendVert.initSkinBones();
//			blendVert.addTVertex(new Vec2());
//			Vec3AnimFlag timeline = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
//			timeline.addEntry(0, pos.scale(20f).sub(realVert), stand);
//			blendBone.add(timeline);
//			blendVerts.add(blendVert);
//		}

		// Need to apply *bindpose* before adding blendWeights
		for (int[] binding : simModel2.getBindings()) {
			Bone b = bones.get(binding[0]);
			for (int j = binding[1]; j < binding[1] + binding[2]; j++) {
				GeosetVertex vertex = realVerts.get(j);
				if (vertex.getSkinBones() != null && vertex.getSkinBones()[1] != null) {
					vertex.setSkinBone(b, vertex.getSkinBones()[0].getWeight(), 0);
				} else {
					vertex.setSkinBone(b, (short) 255, 0);
				}
			}
			for (int j = binding[3]; j < binding[3] + binding[4]; j++) {
				int[] blendData = simModel2.getBlendWeights()[j];
				short weight = (short) (255f * blendData[1]/(float)0x8000);
				GeosetVertex realVert = realVerts.get(blendData[0]);
				realVert.setSkinBone((short)(255 - weight), 0);
				realVert.setSkinBone(b, weight, 1);
			}
		}

		for (int[] face : simModel2.getFaces()) {
			Triangle triangle = new Triangle(realVerts.get(face[0]), realVerts.get(face[1]), realVerts.get(face[2]));
			faces.add(triangle);
		}
	}

	public Generator initSkeleton(SimsSkeleton skeleton) {
		for (Bone bone : bones) {
			SimsBone skelBone = skeleton.getBone(bone.getName());
			if (skelBone != null) {
				Vec3AnimFlag transl = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
				transl.addEntry(0, skelBone.getPos(), stand);
				bone.add(transl);
				QuatAnimFlag rot = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
				rot.addEntry(0, skelBone.getQuat(), stand);
				bone.add(rot);
				String parName = skelBone.getParName();
				if(!parName.equals("NULL")){
					for (Bone b : bones) {
						if (b.getName().equals(parName)){
							bone.setParent(b);
							break;
						}
					}
				}
			}
		}
		return this;
	}

	public EditableModel getModel(){
		EditableModel model = new EditableModel(name);
		model.setFormatVersion(1000);
		model.add(stand);

		String homeProfile = System.getProperty("user.home");
		model.setFileRef(new File(homeProfile + "\\Documents\\" + name + ".mdl"));

		bones.forEach(model::add);
		blendBones.forEach(model::add);
		model.sortIdObjects();

		model.setExtents(new ExtLog());
		Bitmap bitmap = new Bitmap(texture + ".bmp");
		model.add(bitmap);
		Material material = new Material(new Layer(bitmap));
		model.add(material);

		if (!realVerts.isEmpty()) {
			Geoset geoset = new Geoset();
			geoset.setMaterial(material);
			realVerts.forEach(v -> v.setGeoset(geoset));
			faces.forEach(f -> f.setGeoset(geoset));
			geoset.addVerticies(realVerts);
			geoset.addTriangles(faces);
			model.add(geoset);
		}

		if (!blendVerts.isEmpty()) {
			Geoset geoset = new Geoset();
			geoset.setMaterial(material);
			blendVerts.forEach(v -> v.setGeoset(geoset));
			geoset.addVerticies(blendVerts);
			model.add(geoset);
		}

		return model;
	}
}
