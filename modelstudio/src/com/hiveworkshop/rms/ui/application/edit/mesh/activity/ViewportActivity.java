package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class ViewportActivity implements SelectionListener, ModelEditorChangeListener {

	protected ModelEditor modelEditor;
	protected ModelHandler modelHandler;
	protected UndoManager undoManager;
	protected ModelView modelView;
	protected ProgramPreferences preferences;
	protected Point lastMousePoint;
	protected AbstractSelectionManager selectionManager;
	protected ModelElementRenderer modelElementRenderer;
	protected ViewportListener viewportListener;
	protected ModelEditorManager modelEditorManager;

	@Override
	public void onSelectionChanged(AbstractSelectionManager newSelection) {
		selectionManager = newSelection;
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	public void viewportChanged(CursorManager cursorManager) {
	}

	public abstract void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem);
	public abstract void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem);
	public abstract void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem);
	public abstract void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem);
	public abstract void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated);
	public abstract boolean isEditing();
}
