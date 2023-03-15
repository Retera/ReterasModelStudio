package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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
	private final BakeGeometryTransformAction geometryTransformAction;

	public RotateNodesTPoseAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              Collection<CameraNode> camSelection,
	                              Collection<Geoset> geosets,
	                              Vec3 center, Vec3 axis, double radians,
	                              Mat4 rotMat,
	                              ModelStructureChangeListener changeListener){
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.changeListener = changeListener;
		this.radians = radians;
		Mat4 tempMat = new Mat4();
		tempMat.set(rotMat).invert();
		realAxis.set(axis).transform(tempMat, 1, true);

		topNodes.addAll(getTopNodes(nodeSelection));

		geometryTransformAction = new BakeGeometryTransformAction(topNodes, geosets, rotMat, null);

		if(radians != 0.0){
			tempQuat.setFromAxisAngle(realAxis, (float) -radians);
			geometryTransformAction.calculateTransform(Vec3.ZERO, tempQuat, topNodes);
		}

		for (IdObject node2 : nodeSelection) {
			rotNodeActions.add(new RotateNodeTPoseAction(node2, realAxis, radians, center, rotMat, null));
		}
	}
	private List<IdObject> getTopNodes(Collection<IdObject> selection) {
		return selection.stream()
				.filter(idObject -> idObject.getParent() == null || !selection.contains(idObject.getParent()))
				.collect(Collectors.toList());
	}

	public RotateNodesTPoseAction doSetup() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		geometryTransformAction.doSetup();
		for(RotateNodeTPoseAction action : rotNodeActions){
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
		geometryTransformAction.updateTransform(Vec3.ZERO, tempQuat, topNodes);

		for(RotateNodeTPoseAction action : rotNodeActions){
			action.updateRotation(radians);
		}
		return this;
	}

	@Override
	public RotateNodesTPoseAction undo() {
		geometryTransformAction.undo();
		
		for(RotateNodeTPoseAction action : rotNodeActions){
			action.undo();
		}
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.undo();
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodesTPoseAction redo() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		for(RotateNodeTPoseAction action : rotNodeActions){
			action.redo();
		}

		geometryTransformAction.redo();
		
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "TPose Rotate " + "node.getName()";
	}
}
