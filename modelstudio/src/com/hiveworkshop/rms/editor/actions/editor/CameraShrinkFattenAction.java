package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class CameraShrinkFattenAction extends AbstractShrinkFattenAction {
	private final ModelStructureChangeListener changeListener;
	private final ArrayList<CameraNode> selectedCameraNodes;
	private final ArrayList<Vec3> opgPosVertices;
	private final ArrayList<Vec3> normals;

	public CameraShrinkFattenAction(ModelView modelView, float amount, boolean scaleApart) {
		this(modelView.getSelectedCameraNodes(), amount, scaleApart);
	}
	public CameraShrinkFattenAction(Collection<CameraNode> selectedCameraNodes, float amount, boolean scaleApart) {
		this(selectedCameraNodes, amount, scaleApart, null);
	}
	public CameraShrinkFattenAction(Collection<CameraNode> selectedCameraNodes, float amount, boolean scaleApart, ModelStructureChangeListener changeListener) {
		this.amount = amount;
		this.changeListener = changeListener;
		this.selectedCameraNodes = new ArrayList<>(selectedCameraNodes.size());
		for (CameraNode cameraNode : selectedCameraNodes) {
			if (scaleApart || cameraNode instanceof CameraNode.SourceNode || !selectedCameraNodes.contains(cameraNode.getParent().getSourceNode())) {
				this.selectedCameraNodes.add(cameraNode);
			}
		}
		this.opgPosVertices = new ArrayList<>(selectedCameraNodes.size());
		this.normals = new ArrayList<>(selectedCameraNodes.size());

		this.selectedCameraNodes.forEach(cameraNode -> opgPosVertices.add(new Vec3(cameraNode.getPosition())));
		this.selectedCameraNodes.forEach(cameraNode -> normals.add(getNormal(cameraNode)));

	}

	private Vec3 getNormal(CameraNode cameraNode) {
		Camera parent = cameraNode.getParent();
		if (cameraNode instanceof CameraNode.SourceNode) {
			return new Vec3(cameraNode.getPosition()).sub(parent.getTargetNode().getPivotPoint()).normalize();
		} else {
			return new Vec3(cameraNode.getPosition()).sub(parent.getSourceNode().getPivotPoint()).normalize();
		}
	}

	@Override
	public CameraShrinkFattenAction undo() {
		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			selectedCameraNodes.get(i).setPivotPoint(opgPosVertices.get(i));
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public CameraShrinkFattenAction redo() {
		rawScale(amount);

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return (amount < 0 ? "Shrink by " : "Fatten by ") + String.format(Locale.US, "%3.1f", amount);
	}


	public CameraShrinkFattenAction setAmount(float amount) {
		this.amount = amount;
		rawSetScale(amount);
		return this;
	}


	public CameraShrinkFattenAction updateAmount(float deltaAmount) {
		this.amount += deltaAmount;
		rawScale(deltaAmount);
		return this;
	}

	protected void rawScale(float amountDelta) {
		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			selectedCameraNodes.get(i).getPivotPoint().addScaled(normals.get(i), amountDelta);
		}
	}

	protected void rawSetScale(float amount) {
		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			selectedCameraNodes.get(i).getPivotPoint().set(opgPosVertices.get(i)).addScaled(normals.get(i), amount);
		}
	}

}
