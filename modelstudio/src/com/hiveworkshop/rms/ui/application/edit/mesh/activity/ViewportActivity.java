package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
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
import java.util.function.Consumer;

public abstract class ViewportActivity implements SelectionListener {

	protected ModelEditor modelEditor;
	protected ModelHandler modelHandler;
	protected UndoManager undoManager;
	protected ModelView modelView;
	protected ProgramPreferences preferences;
	protected Vec2 mouseStart;
	protected Point lastMousePoint;
	protected AbstractSelectionManager selectionManager;
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

	public void viewportChanged(Consumer<Cursor> cursorManager) {
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


	protected UndoAction getAddAction(Material solidWhiteMaterial, Geoset solidWhiteGeoset) {
		if (!modelView.getModel().contains(solidWhiteMaterial) || !modelView.getModel().contains(solidWhiteGeoset) || !modelView.isEditable(solidWhiteGeoset)) {
			AddGeosetAction addGeosetAction = new AddGeosetAction(solidWhiteGeoset, modelView, null);
			if (!modelHandler.getModel().contains(solidWhiteMaterial)) {
				AddMaterialAction addMaterialAction = new AddMaterialAction(solidWhiteMaterial, modelHandler.getModel(), null);
				return new CompoundAction("Add geoset", ModelStructureChangeListener.changeListener::geosetsUpdated, addGeosetAction, addMaterialAction);
			} else {

				return new CompoundAction("Add geoset", ModelStructureChangeListener.changeListener::geosetsUpdated, addGeosetAction);
			}

		}
		return null;
	}

}
