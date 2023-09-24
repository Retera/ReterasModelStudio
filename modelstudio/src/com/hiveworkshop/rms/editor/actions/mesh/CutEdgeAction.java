package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

public class CutEdgeAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Geoset geoset;
	private final GeosetVertex cutPoint;
	private final List<Triangle> orgTris = new ArrayList<>();
	private final List<Triangle> newTris = new ArrayList<>();

	public CutEdgeAction(GeosetVertex v1, GeosetVertex v2, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = v1.getGeoset();

		for (Triangle triangle : v1.getTriangles()) {
			if (triangle.containsRef(v2)) {
				orgTris.add(triangle);
			}
		}

		this.cutPoint = getVertex(v1, v2);

		for (Triangle triangle : orgTris) {
//			Triangle tri1 = new Triangle(triangle.get(0), triangle.get(1), triangle.get(2));
			Triangle tri1 = new Triangle(geoset);
			tri1.setVerts(triangle.getVerts());
			tri1.replace(v2, cutPoint);
			newTris.add(tri1);
			Triangle tri2 = new Triangle(geoset);
			tri2.setVerts(triangle.getVerts());
			tri2.replace(v1, cutPoint);
			newTris.add(tri2);
		}

	}

	private GeosetVertex getVertex(GeosetVertex v1, GeosetVertex v2) {
		Vec3 newPos = new Vec3(v1).add(v2).scale(.5f);
		Vec3 newNorm = new Vec3(v1.getNormal()).add(v2.getNormal()).normalize();
		if (newNorm.length() < .5) {
			newNorm.set(v1.getNormal());
		}

		GeosetVertex v3 = new GeosetVertex(newPos, newNorm);
		v3.setGeoset(v1.getGeoset());
		if (v1.getTangent() != null) {
			Vec4 newTang = new Vec4(v1.getTang());
			if (v2.getTangent() != null) {
				newTang.add(newTang).scale(.5f);
				newTang.normalizeAsV3();

				if (newTang.length() < .5) {
					newTang.set(v1.getTang());
				} else if (Math.abs(newTang.w) < .9 || 1.1 < Math.abs(newTang.w)) {
					newTang.w = v1.getTangent().w;
				}
			}
		}

		for (int i = 0; i < v1.getTverts().size(); i++) {
			v3.addTVertex(new Vec2(v1.getTVertex(i)).add(v2.getTVertex(i)).scale(.5f));
		}

		if (v1.getSkinBones() != null) {
			Map<Bone, Integer> boneMap = new HashMap<>();
			for (SkinBone skinBone : v1.getSkinBones()) {
				if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0) {
					boneMap.put(skinBone.getBone(), boneMap.getOrDefault(skinBone.getBone(), 0) + skinBone.getWeight());
				}
			}
			for (SkinBone skinBone : v2.getSkinBones()) {
				if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0) {
					boneMap.put(skinBone.getBone(), boneMap.getOrDefault(skinBone.getBone(), 0) + skinBone.getWeight());
				}
			}
			List<SkinBone> allSkinBones = new ArrayList<>();
			for (Bone bone : boneMap.keySet()) {
				allSkinBones.add(new SkinBone(boneMap.get(bone).shortValue(), bone));
			}
			allSkinBones.sort(Comparator.comparingInt(SkinBone::getWeight));

			v3.initSkinBones();

			for (int i = 0; i < 4 && i < allSkinBones.size(); i++) {
				v3.setSkinBone(allSkinBones.get(i), i);
			}
			v3.normalizeBoneWeights();

		} else {
			Set<Bone> allBones = new LinkedHashSet<>(v1.getBones());
			allBones.addAll(v2.getBones());
			v3.addBoneAttachments(allBones);
		}
		return v3;
	}


	@Override
	public CutEdgeAction undo() {
		for (Triangle triangle : newTris) {
			geoset.remove(triangle.removeFromVerts());
		}
		for (Triangle triangle : orgTris) {
			geoset.add(triangle.addToVerts());
		}
		geoset.remove(cutPoint);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public CutEdgeAction redo() {
		geoset.add(cutPoint);
		for (Triangle triangle : orgTris) {
			geoset.remove(triangle.removeFromVerts());
		}
		for (Triangle triangle : newTris) {
			geoset.add(triangle.addToVerts());
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Cut Edge Action";
	}
}
