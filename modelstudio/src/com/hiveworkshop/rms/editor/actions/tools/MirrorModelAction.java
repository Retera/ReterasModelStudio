package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

public class MirrorModelAction implements UndoAction {
//	private char[] DIMENSION_NAMES = { 'Z', 'X', 'Y' };
	private char[] DIMENSION_NAMES = { 'X', 'Y', 'Z' };
	private List<GeosetVertex> selection;
	private final List<IdObject> idObjects;
	private final byte mirrorDim;
	private final MoveDimension moveDimension;
	private final Vec3 center;
	private final Vec3 axis;

	public MirrorModelAction(Collection<GeosetVertex> selection, Collection<IdObject> idObjects, Vec3 axis, Vec3 center) {
		this.center = center;
		this.selection = new ArrayList<>(selection);
		this.idObjects = new ArrayList<>(idObjects);
		moveDimension = MoveDimension.getByAxis(axis);
		this.mirrorDim = 0;
		this.axis = axis;
	}

	@Override
	public UndoAction undo() {
		doMirror();
		return this;
	}

	@Override
	public UndoAction redo() {
		doMirror();
		return this;
	}

	private void doMirror() {
		Vec3 mirrorMul = new Vec3(1,1,1).addScaled(axis, -2); // sets one axis to -1
		Vec4 mirrorMulTang = mirrorMul.getVec4(1);
		Vec3 center = new Vec3(this.center).multiply(axis).scale(2); // extracts center coord for one axis
		// center is scaled by 2 and added last instead of v.sub(center).multiply(mirrorMul).add(center)
		for (GeosetVertex vert : selection) {
			vert.multiply(mirrorMul).add(center);
			Vec3 normal = vert.getNormal();
			if (normal != null) {
				// Flip normals, preserve lighting!
				normal.multiply(mirrorMul).negate();
				// this will inverse back if they correctly choose to flip
				// faces, otherwise we're making an inside out model now
			}
			if (vert.getTangent() != null) {
				// TODO doesn't support flip yet
				vert.getTangent().multiply(mirrorMulTang);
			}
		}
		for (IdObject obj : idObjects) {
			obj.getPivotPoint().multiply(mirrorMul).add(center);
			flipOver(mirrorMul, obj.getAnimFlags());
		}
	}



	public void flipOver(Vec3 mirrorMul, Collection<AnimFlag<?>> animFlags) {
		for (AnimFlag<?> timeline : animFlags) {
			flipOver(mirrorMul, timeline);
		}
	}

	public <T> void flipOver(Vec3 mirrorMul, AnimFlag<T> timeline) {
		Map<Sequence, TreeMap<Integer, Entry<T>>> sequenceMap = timeline.getAnimMap();
		for (Sequence anim : sequenceMap.keySet()) {
			TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
			Collection<Entry<T>> entries = entryMap.values();
			if (timeline instanceof QuatAnimFlag && timeline.getName().equals(MdlUtils.TOKEN_ROTATION)) {
				// Rotation
				for (Entry<T> entry : entries) {
					flipQuat(mirrorMul, (Quat) entry.getValue());
					flipQuat(mirrorMul, (Quat) entry.getInTan());
					flipQuat(mirrorMul, (Quat) entry.getOutTan());
				}
			} else if (timeline instanceof Vec3AnimFlag && timeline.getName().equals(MdlUtils.TOKEN_TRANSLATION)) {
				// Translation
				for (Entry<T> entry : entries) {
					flipVec3(mirrorMul, (Vec3) entry.getValue());
					flipVec3(mirrorMul, (Vec3) entry.getInTan());
					flipVec3(mirrorMul, (Vec3) entry.getOutTan());
				}
			}
		}
	}

	public void flipVec3(Vec3 mirrorMul, Vec3 value) {
		if(value != null){
			value.multiply(mirrorMul);
		}
	}

	private void flipQuat(Vec3 mirrorMul, Quat quat) {
		if(quat != null && mirrorMul != null){
			Vec4 axis4 = new Vec4(mirrorMul, -1);
			Vec4 axisMul = new Vec4(1,1,1,1).multiply(axis4);
			quat.multiply(axisMul);
		}

	}

	@Override
	public String actionName() {
		return "mirror " + moveDimension.name();
	}

}
