package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class Manipulator {
	protected final ModelEditor modelEditor;
	protected final AbstractSelectionManager selectionManager;
	protected MoveDimension dir;
	protected final Mat4 inverseViewProjectionMatrix = new Mat4();
	protected final Vec2 activityStart = new Vec2();


	public Manipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		this.modelEditor = modelEditor;
		this.selectionManager = selectionManager;
		this.dir = dir;
	}

	public final void start(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		activityStart.set(mouseStart);
		onStart(e, mouseStart, dim1, dim2);
	}

	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
	}

	public abstract void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2);

	public abstract UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2);

	public final void start(MouseEvent e, Vec2 mouseStart, Mat4 viewPortAntiRotMat) {
		activityStart.set(mouseStart);
		onStart(e, mouseStart, viewPortAntiRotMat);
	}

	protected void onStart(MouseEvent e, Vec2 mouseStart, Mat4 viewPortAntiRotMat) {
		inverseViewProjectionMatrix.set(viewPortAntiRotMat).invert();
	}

	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortMat) {
	}

	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortMat, double sizeAdj) {
		return null;
	}


	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {
	}
}
