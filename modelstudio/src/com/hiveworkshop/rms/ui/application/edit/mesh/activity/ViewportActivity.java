package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ViewportActivity implements SelectionListener {

	protected Consumer<Cursor> cursorManager;
	protected ModelEditor modelEditor;
	protected ModelHandler modelHandler;
	protected UndoManager undoManager;
	protected ModelView modelView;
	protected ProgramPreferences preferences;
//	protected final Vec2 mouseStartPoint = new Vec2();
//	protected final Vec2 lastMousePoint = new Vec2();
//	protected final Vec2 lastDragPoint = new Vec2();
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
		this.cursorManager = cursorManager;
	}

	public abstract void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem);

	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
	}


	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}

	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
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
		EditableModel model = modelHandler.getModel();

		if (!modelView.isEditable(solidWhiteGeoset)) {
			List<UndoAction> undoActions = new ArrayList<>();
			undoActions.add(new AddGeosetAction(solidWhiteGeoset, model, null));
			if (!model.contains(solidWhiteMaterial)) {
				undoActions.add(new AddMaterialAction(solidWhiteMaterial, model, null));
			}
			return new CompoundAction("Add geoset", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated);
		}


		return null;
	}

	protected Vec2 getPoint(MouseEvent e) {
		Component component = e.getComponent();
		float xRatio = (2.0f * (float) e.getX() / (float) component.getWidth()) - 1.0f;
		float yRatio = 1.0f - (2.0f * (float) e.getY() / (float) component.getHeight());
		return new Vec2(xRatio, yRatio);
	}

}
