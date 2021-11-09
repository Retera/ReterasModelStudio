package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.addactions.DrawBoneAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class DrawBoneActivity extends ViewportActivity {

	private Point lastMousePoint;
	private final ViewportListener viewportListener;

	public DrawBoneActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager, ViewportListener viewportListener) {
		super(modelHandler, modelEditorManager);
		this.viewportListener = viewportListener;
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		Vec3 worldPressLocation = new Vec3(0, 0, 0);
		worldPressLocation.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(e.getX()));
		worldPressLocation.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(e.getY()));
		worldPressLocation.setCoord(coordinateSystem.getUnusedXYZ(), 0);
		try {
			Viewport viewport = viewportListener.getViewport();
			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();

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
			DrawBoneAction drawBoneAction = new DrawBoneAction(modelView.getModel(), ModelStructureChangeListener.changeListener, bone);
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
			g.setColor(preferences.getVertexColor());
			if (lastMousePoint != null) {
				g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
			}
		}
	}

}
