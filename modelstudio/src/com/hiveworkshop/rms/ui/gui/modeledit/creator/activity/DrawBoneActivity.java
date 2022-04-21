package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.addactions.DrawBoneAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class DrawBoneActivity extends ViewportActivity {

	private Point lastMousePoint;

	public DrawBoneActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		Vec3 worldPressLocation = new Vec3(0, 0, 0);
		worldPressLocation.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		worldPressLocation.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		worldPressLocation.setCoord(coordinateSystem.getUnusedXYZ(), 0);
		try {
			Set<String> allBoneNames = new HashSet<>();
			for (IdObject object : modelHandler.getModel().getIdObjects()) {
				allBoneNames.add(object.getName());
			}
			int nameNumber = 1;
			while (allBoneNames.contains(getNumberName("Bone", nameNumber))) {
				nameNumber++;
			}
			Bone bone = new Bone(getNumberName("Bone", nameNumber));
			bone.setPivotPoint(new Vec3(worldPressLocation));
			DrawBoneAction drawBoneAction = new DrawBoneAction(modelHandler.getModel(), ModelStructureChangeListener.changeListener, bone);
			drawBoneAction.redo();


			undoManager.pushAction(drawBoneAction);
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String getNumberName(String name, int number) {
		return name + String.format("%3s", number).replace(' ', '0');
	}

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		lastMousePoint = e.getPoint();
	}

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
//			g.setColor(preferences.getVertexColor());
			g.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX));
			if (lastMousePoint != null) {
				g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
			}
		}
	}


	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		Vec3 worldPressLocation = new Vec3(e.getX(), e.getY(), 0).transform(viewProjectionMatrix);
		try {
			Set<String> allBoneNames = new HashSet<>();
			for (IdObject object : modelView.getModel().getIdObjects()) {
				allBoneNames.add(object.getName());
			}
			int nameNumber = 1;
			while (allBoneNames.contains(getNumberName("Bone", nameNumber))) {
				nameNumber++;
			}
			Bone bone = new Bone(getNumberName("Bone", nameNumber));
			bone.setPivotPoint(new Vec3(worldPressLocation));
			DrawBoneAction drawBoneAction = new DrawBoneAction(modelHandler.getModel(), ModelStructureChangeListener.changeListener, bone);
			drawBoneAction.redo();


			undoManager.pushAction(drawBoneAction);
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		lastMousePoint = e.getPoint();
	}

}
