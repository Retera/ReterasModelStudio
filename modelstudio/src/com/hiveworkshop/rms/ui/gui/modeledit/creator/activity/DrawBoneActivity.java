package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class DrawBoneActivity extends ViewportActivity {
	float[] halfScreenXY;
	float halfScreenX;
	float halfScreenY;

	private ModelEditorActionType3 lastEditorType;

	private Point lastMousePointPoint;

	public DrawBoneActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	public DrawBoneActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager,
	                        ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager);
		this.lastEditorType = lastEditorType;
	}

	@Override
	public void render(Graphics2D g, AbstractCamera coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
//			g.setColor(preferences.getVertexColor());
			g.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX));
			if (lastMousePointPoint != null) {
				g.fillRect(lastMousePointPoint.x, lastMousePointPoint.y, 3, 3);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		Vec2 point = getPoint(e);
		mouseStartPoint.set(point);
		this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
		this.viewProjectionMatrix.set(viewProjectionMatrix);
		halfScreenXY = halfScreenXY();
		halfScreenX = halfScreenXY[0];
		halfScreenY = halfScreenXY[1];

		Set<String> allBoneNames = new HashSet<>();
		for (IdObject object : modelView.getModel().getIdObjects()) {
			allBoneNames.add(object.getName());
		}
		int nameNumber = 1;
		while (allBoneNames.contains(getNumberName("Bone", nameNumber))) {
			nameNumber++;
		}
		Bone bone = new Bone(getNumberName("Bone", nameNumber));
		bone.setPivotPoint(get3DPoint(mouseStartPoint));

		AddNodeAction addNodeAction = new AddNodeAction(modelHandler.getModel(), bone, changeListener);

		undoManager.pushAction(addNodeAction.redo());
		lastMousePoint.set(point);
		if (lastEditorType != null && !MouseEventHelpers.hasModifier(e.getModifiersEx(), MouseEvent.CTRL_DOWN_MASK)) {
			System.out.println("returning to prev action type!");
			ProgramGlobals.getCurrentModelPanel().setEditorActionType(lastEditorType);
		} else {
			System.out.println("keep draw vertices!");
		}
	}

	private static String getNumberName(String name, int number) {
		return name + String.format("%3s", number).replace(' ', '0');
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		lastMousePointPoint = e.getPoint();
	}

}
