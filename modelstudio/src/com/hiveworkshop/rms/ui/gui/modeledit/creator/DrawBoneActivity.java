package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class DrawBoneActivity implements ModelEditorViewportActivity {

	private Point lastMousePoint;
	private final ProgramPreferences preferences;
	private ModelEditor modelEditor;
	private final UndoManager undoActionListener;
	private final ModelView modelView;
	private SelectionView selectionView;
	private final ModelElementRenderer modelElementRenderer;
	private final ViewportListener viewportListener;
	private final ModelHandler modelHandler;
	ModelEditorManager modelEditorManager;

	public DrawBoneActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager, ViewportListener viewportListener) {
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;
		this.preferences = ProgramGlobals.getPrefs();
		this.undoActionListener = modelHandler.getUndoManager();
		this.modelEditor = modelEditorManager.getModelEditor();
		this.modelView = modelHandler.getModelView();
		this.selectionView = modelEditorManager.getSelectionView();
		this.viewportListener = viewportListener;
		modelElementRenderer = new ModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
	}

	@Override
	public void onSelectionChanged(SelectionView newSelection) {
		selectionView = newSelection;
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	@Override
	public void viewportChanged(CursorManager cursorManager) { }

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
			DrawBoneAction drawBoneAction = new DrawBoneAction(modelView, modelEditorManager.getStructureChangeListener(), bone);
			drawBoneAction.redo();


			undoActionListener.pushAction(drawBoneAction);
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(null, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String getNumberName(String name, int number) {
		return name + String.format("%3s", number).replace(' ', '0');
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) { }

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) { }

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (!isAnimated) {
			modelElementRenderer.reset(g, coordinateSystem, modelHandler.getRenderModel(), false);
//			selectionView.renderSelection(modelElementRenderer, coordinateSystem, modelView);
			g.setColor(preferences.getVertexColor());
			if (lastMousePoint != null) {
				g.fillRect(lastMousePoint.x, lastMousePoint.y, 3, 3);
			}
		}
	}

	@Override
	public boolean isEditing() {
		return false;
	}

}
