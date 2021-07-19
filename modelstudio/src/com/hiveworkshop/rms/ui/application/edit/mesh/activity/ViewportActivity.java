package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public abstract class ViewportActivity implements SelectionListener {

	protected ModelEditor modelEditor;
	protected ModelHandler modelHandler;
	protected UndoManager undoManager;
	protected ModelView modelView;
	protected ProgramPreferences preferences;
	protected Vec2 mouseStart;
	protected Point lastMousePoint;
	protected AbstractSelectionManager selectionManager;
	protected ViewportListener viewportListener;
	protected AbstractModelEditorManager modelEditorManager;

	public ViewportActivity(ModelHandler modelHandler, AbstractModelEditorManager modelEditorManager) {
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		this.undoManager = modelHandler.getUndoManager();
		this.modelEditorManager = modelEditorManager;
		this.modelEditor = modelEditorManager.getModelEditor();
		this.selectionManager = modelEditorManager.getSelectionView();

		this.preferences = ProgramGlobals.getPrefs();
	}

	@Override
	public void onSelectionChanged(AbstractSelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	public void viewportChanged(CursorManager cursorManager) {
	}

	public abstract void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem);

	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
	}


	public void mousePressed(MouseEvent e, CameraHandler cameraHandler) {
	}

	public void mouseReleased(MouseEvent e, CameraHandler cameraHandler) {
	}

	public void mouseMoved(MouseEvent e, CameraHandler cameraHandler) {
	}

	public void mouseDragged(MouseEvent e, CameraHandler cameraHandler) {
	}


	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
	}

	public boolean isEditing() {
		return false;
	}


	public Geoset getSolidWhiteGeoset(Material solidWhiteMaterial) {
		List<Geoset> geosets = modelView.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (Geoset geoset : geosets) {
			if (geoset.getMaterial().equals(solidWhiteMaterial)) {
				solidWhiteGeoset = geoset;
				break;
			}
		}

		if (solidWhiteGeoset == null) {
			solidWhiteGeoset = new Geoset();
			solidWhiteGeoset.setMaterial(solidWhiteMaterial);
		}
		return solidWhiteGeoset;
	}
}
