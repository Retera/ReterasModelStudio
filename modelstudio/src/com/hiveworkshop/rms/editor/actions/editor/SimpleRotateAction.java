package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public final class SimpleRotateAction implements UndoAction {
	private final ModelView modelView;
	private final Vec3 center;
	private final Quat rot;


	public SimpleRotateAction(ModelView modelView, Vec3 center, double radians, Vec3 axis) {
		this.modelView = modelView;
		this.center = center;
		rot = new Quat().setFromAxisAngle(axis, (float) radians);
	}

	@Override
	public UndoAction undo() {
		Quat temp = new Quat().set(rot).invertRotation();
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, temp);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.rotate(center, rot);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	private static Vec3 getUnusedAxis(byte portFirstXYZ, byte portSecondXYZ) {
		byte unusedXYZ = getUnusedXYZ(portFirstXYZ, portSecondXYZ);
		return switch (unusedXYZ) {
			case 0 -> new Vec3(1, 0, 0);
			case 1 -> new Vec3(0, -1, 0);
			default -> new Vec3(0, 0, -1);
		};
	}

	private static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}
}
