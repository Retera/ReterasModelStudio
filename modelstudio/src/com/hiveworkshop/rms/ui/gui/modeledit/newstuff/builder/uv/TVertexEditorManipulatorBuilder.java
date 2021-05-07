package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.Widget;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeListener;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.SelectManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public abstract class TVertexEditorManipulatorBuilder implements ManipulatorBuilder, TVertexEditorChangeListener {
	private final ViewportSelectionHandler viewportSelectionHandler;
	private final TVertexModelElementRenderer tVertexModelElementRenderer;
	private final ModelView modelView;
	private TVertexEditor modelEditor;
	protected Widget widget;

	public TVertexEditorManipulatorBuilder(ViewportSelectionHandler viewportSelectionHandler,
	                                       TVertexEditor modelEditor,
	                                       ModelView modelView) {
		this.viewportSelectionHandler = viewportSelectionHandler;
		this.modelEditor = modelEditor;
		this.modelView = modelView;
		tVertexModelElementRenderer = new TVertexModelElementRenderer(ProgramGlobals.getPrefs().getVertexSize());
	}

	@Override
	public void editorChanged(TVertexEditor newModelEditor) {
		modelEditor = newModelEditor;
	}

	protected final TVertexEditor getModelEditor() {
		return modelEditor;
	}

	@Override
	public final Cursor getCursorAt(int x, int y,
	                                CoordinateSystem coordinateSystem,
	                                SelectionView selectionView) {
		Vec2 mousePoint = new Vec2(x, y);
		if (!selectionView.isEmpty() && widgetOffersEdit(selectionView.getUVCenter(modelEditor.getUVLayerIndex()), mousePoint, coordinateSystem, selectionView)) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else if (viewportSelectionHandler.canSelectAt(mousePoint, coordinateSystem)) {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		return null;
	}

	@Override
	public final Manipulator buildActivityListener(int x, int y,
	                                               ButtonType clickedButton,
	                                               CoordinateSystem coordinateSystem,
	                                               SelectionView selectionView) {
		Vec2 mousePoint = new Vec2(x, y);
		if (clickedButton == ButtonType.RIGHT_MOUSE) {
			return createDefaultManipulator(selectionView.getUVCenter(modelEditor.getUVLayerIndex()), mousePoint, coordinateSystem, selectionView);
		} else {
			if (!selectionView.isEmpty()) {
				Manipulator manipulatorFromWidget = createManipulatorFromWidget(selectionView.getUVCenter(modelEditor.getUVLayerIndex()), mousePoint, coordinateSystem, selectionView);
				if (manipulatorFromWidget != null) {
					return manipulatorFromWidget;
				}
			}
			return new SelectManipulator(viewportSelectionHandler, coordinateSystem);
		}
	}

	@Override
	public final void render(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         SelectionView selectionView,
	                         boolean isAnimated) {
		if (!isAnimated) {
			selectionView.renderUVSelection(tVertexModelElementRenderer.reset(graphics, coordinateSystem), modelView, modelEditor.getUVLayerIndex());
			if (!selectionView.isEmpty()) {
				renderWidget(graphics, coordinateSystem, selectionView);
			}
		}
	}

	protected boolean widgetOffersEdit(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		MoveDimension directionByMouse = widget.getDirectionByMouse(mousePoint, coordinateSystem);
		widget.setMoveDirection(directionByMouse);
		return directionByMouse != MoveDimension.NONE;
	}

	protected abstract Manipulator createManipulatorFromWidget(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected abstract Manipulator createDefaultManipulator(Vec2 selectionCenter, Vec2 mousePoint, CoordinateSystem coordinateSystem, SelectionView selectionView);

	protected void renderWidget(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView) {
		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		widget.render(graphics, coordinateSystem);
	}
}
