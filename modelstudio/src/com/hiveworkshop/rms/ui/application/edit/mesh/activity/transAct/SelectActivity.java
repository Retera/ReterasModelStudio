package com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SelectActivity extends ViewportActivity {
	protected boolean isActing = false;

	public SelectActivity(ModelHandler modelHandler,
	                      AbstractModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	private void finnishAction(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj, boolean wasCanceled) {
		if (isActing) {
			Vec2 mouseEnd = getPoint(e);
			Vec2 min = new Vec2(mouseStartPoint).minimize(mouseEnd);
			Vec2 max = new Vec2(mouseStartPoint).maximize(mouseEnd);
			UndoAction undoAction = selectRegion(e, min, max, viewBox, sizeAdj);
			if (undoAction != null) {
				undoAction.redo();
			}
			if (wasCanceled && undoAction != null) {
				undoAction.undo();
			} else if (undoAction != null) {
				undoManager.pushAction(undoAction);
			}
			mouseStartPoint.set(0,0);
			lastMousePoint.set(0,0);
			isActing = false;
		}
	}


	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}
	public void mousePressed(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj) {
		if (isActing) {
			finnishAction(e, viewBox, sizeAdj, true);
		}

		int modifiersEx = e.getModifiersEx();
		if (0 < (getSelect() & modifiersEx)) {
			isActing = true;
			mouseStartPoint.set(getPoint(e));
			lastMousePoint.set(mouseStartPoint);
			inverseViewProjectionMatrix.setIdentity().invert();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
	}
	public void mouseReleased(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj) {
		finnishAction(e, viewBox, sizeAdj, false);
	}

	@Override
	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		Vec2 mousePoint = getPoint(e);
//		if (!selectionManager.isEmpty() && widgetOffersEdit(mousePoint, inverseViewProjectionMatrix, e.getComponent(), selectionManager)) {
		if (!selectionManager.isEmpty()) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else if (selectionManager.selectableUnderCursor(mousePoint, inverseViewProjectionMatrix, sizeAdj)) {
			cursorManager.accept(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			cursorManager.accept(null);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (isActing) {
			lastMousePoint.set(getPoint(e));
		}
	}

	@Override
	public void render(Graphics2D graphics, AbstractCamera coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (isActing) {
			Component component = coordinateSystem.getComponent();
			tempVec2.set(mouseStartPoint).minimize(lastMousePoint);
			double minX = (1+tempVec2.x)/2.0 * component.getWidth() ;
			double maxY = (1-tempVec2.y)/2.0 * component.getHeight();
			tempVec2.set(mouseStartPoint).maximize(lastMousePoint);
			double maxX = (1 + tempVec2.x)/2.0 * component.getWidth() ;
			double minY = (1 - tempVec2.y)/2.0 * component.getHeight();
			double sizeX = maxX - minX;
			double sizeY = maxY - minY;
			graphics.setColor(ProgramGlobals.getPrefs().getSelectColor());
			graphics.drawRect((int) minX, (int) minY, (int) sizeX, (int) sizeY);
		}
	}

	@Override
	public boolean isEditing() {
		return isActing;
	}

	protected ModelEditor getModelEditor() {
		return modelEditor;
	}


	public void render(Graphics2D graphics,
	                   CoordinateSystem coordinateSystem,
	                   SelectionManager selectionManager) {
	}

	public UndoAction selectRegion(MouseEvent e, Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		SelectionMode tempSelectMode = getTempSelectMode(e);

		return selectionManager.selectStuff(min, max, tempSelectMode, viewPortAntiRotMat, sizeAdj);
	}

	public UndoAction selectRegion(MouseEvent e, Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		SelectionMode tempSelectMode = getTempSelectMode(e);

		return selectionManager.selectStuff(min, max, tempSelectMode, viewBox, sizeAdj);
	}

	private SelectionMode getTempSelectMode(MouseEvent e) {
		SelectionMode tempSelectMode;

		int modBut = e.getModifiersEx();

		if (modBut == getAddSel() || ProgramGlobals.getSelectionMode() == SelectionMode.ADD && modBut != getRemSel()) {
			tempSelectMode = SelectionMode.ADD;
		} else if (modBut == getRemSel() || ProgramGlobals.getSelectionMode() == SelectionMode.DESELECT) {
			tempSelectMode = SelectionMode.DESELECT;
		} else {
			tempSelectMode = SelectionMode.SELECT;
		}
		return tempSelectMode;
	}
}
