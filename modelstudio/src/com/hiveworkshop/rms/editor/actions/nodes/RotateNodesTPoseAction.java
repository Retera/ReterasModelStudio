package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.KeyframeActionHelpers;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class RotateNodesTPoseAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final ModelStructureChangeListener changeListener;
	private final List<RotateNodeTPoseAction> rotNodeActions = new ArrayList<>();
	private double radians;
	private final Quat tempQuat = new Quat();
	private final Vec3 realAxis = new Vec3();
	private final Set<IdObject> topNodes = new LinkedHashSet<>();
	private final Set<IdObject> allNodes = new LinkedHashSet<>();
	private final BakeGeometryTransformAction geometryTransformAction;
	boolean indvOrigins;
	private String actionName;
	Vec3 center;
	public RotateNodesTPoseAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              Collection<CameraNode> camSelection,
	                              Collection<Geoset> geosets,
	                              Vec3 center, Vec3 axis, double radians,
	                              Mat4 rotMat,
	                              boolean preserveAnimations,
								  boolean indvOrigins,
	                              GlobalSeq globalSeq,
								  Collection<Sequence> sequences,
	                              ModelStructureChangeListener changeListener) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.changeListener = changeListener;
		this.radians = radians;
		this.indvOrigins = indvOrigins;
		Mat4 tempMat = new Mat4();
		tempMat.set(rotMat).invert();
		realAxis.set(axis).transform(tempMat, 1, true);
		this.center = center;

		topNodes.addAll(getTopNodes(nodeSelection));
		for (IdObject topNode : topNodes) {
			collectSortedSelected(nodeSelection, allNodes, topNode);
		}

		geometryTransformAction = new BakeGeometryTransformAction(allNodes, geosets, rotMat, null);

		if (radians != 0.0) {
			tempQuat.setFromAxisAngle(realAxis, (float) radians);
			geometryTransformAction.calculateTransform(Vec3.ZERO, tempQuat, center, allNodes);
		}

		for (IdObject node2 : allNodes) {
			Vec3 actionCenter = center == null ? node2.getPivotPoint() : center;
			if (preserveAnimations) {
				AnimFlag<Quat> rotTimeline = (QuatAnimFlag) KeyframeActionHelpers.getNewOrCopiedTimeline(node2, MdlUtils.TOKEN_ROTATION, new Quat(), globalSeq);
				if (rotTimeline != null) {
					KeyframeActionHelpers.ensureSequenceKFs(sequences, rotTimeline);
				}
				AnimFlag<Vec3> translTimeline;
				if (center != null && !actionCenter.equalLocs(node2.getPivotPoint())) {
					translTimeline = (Vec3AnimFlag) KeyframeActionHelpers.getNewOrCopiedTimeline(node2, MdlUtils.TOKEN_TRANSLATION, new Vec3(), globalSeq);
					if (translTimeline != null) {
						KeyframeActionHelpers.ensureSequenceKFs(sequences, translTimeline);
					}
				} else {
					translTimeline = null;
				}
				rotNodeActions.add(new RotateNodeTPoseAction(node2, realAxis, radians, actionCenter, rotMat, true, rotTimeline, translTimeline, null));
			} else {
				rotNodeActions.add(new RotateNodeTPoseAction(node2, realAxis, radians, actionCenter, rotMat, false, null, null, null));
			}
		}
		if (nodeSelection.size() == 1) {
			IdObject idObject = nodeSelection.stream().findFirst().orElse(null);
			actionName = "TPose Rotate " + idObject.getName();
		} else {
			actionName = "TPose Rotate " + nodeSelection.size() + " nodes";
		}
	}
	private List<IdObject> getTopNodes(Collection<IdObject> selection) {
		return selection.stream()
				.filter(idObject -> idObject.getParent() == null || !selection.contains(idObject.getParent()))
				.collect(Collectors.toList());
	}

	private void collectSortedSelected(Collection<IdObject> selection, Set<IdObject> sortedSelection, IdObject currNode){
		if (selection.contains(currNode)) {
			sortedSelection.add(currNode);
			for (IdObject child : currNode.getChildrenNodes()) {
				collectSortedSelected(selection, sortedSelection, child);
			}
		}
	}

	public RotateNodesTPoseAction doSetup() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		geometryTransformAction.doSetup();
		for (RotateNodeTPoseAction action : rotNodeActions) {
			action.doSetup();
		}
		return this;
	}

	public RotateNodesTPoseAction updateRotation(double radians){
		this.radians += radians;
		return updateRot(radians);
	}

	public RotateNodesTPoseAction setRotation(double radians) {
		double rotDiff = radians - this.radians;
		this.radians = radians;
		return updateRot(rotDiff);
	}

	private RotateNodesTPoseAction updateRot(double radians) {
		tempQuat.setFromAxisAngle(realAxis, (float) -radians);
		geometryTransformAction.updateTransform(Vec3.ZERO, tempQuat, center, allNodes);


		for (RotateNodeTPoseAction action : rotNodeActions) {
			action.updateRotation(radians);
		}
		return this;
	}

	@Override
	public RotateNodesTPoseAction undo() {
		geometryTransformAction.undo();
		
		for (RotateNodeTPoseAction action : rotNodeActions) {
			action.undo();
		}
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.undo();
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodesTPoseAction redo() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		for (RotateNodeTPoseAction action : rotNodeActions) {
			action.redo();
		}

		geometryTransformAction.redo();
		
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
