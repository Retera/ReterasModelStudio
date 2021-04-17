package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public final class TVertexViewportSelectionHandlerImpl implements ViewportSelectionHandler {
    private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
    private TVertexEditor selectingEventHandler;

    public TVertexViewportSelectionHandlerImpl(final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
                                               final TVertexEditor selectingEventHandler) {
        this.modeButtonGroup = modeButtonGroup;
        this.selectingEventHandler = selectingEventHandler;
    }

    public void setSelectingEventHandler(final TVertexEditor selectingEventHandler) {
        this.selectingEventHandler = selectingEventHandler;
    }

    @Override
    public UndoAction selectRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
        return switch (modeButtonGroup.getActiveButtonType()) {
            case ADD -> selectingEventHandler.addSelectedRegion(region, coordinateSystem);
            case DESELECT -> selectingEventHandler.removeSelectedRegion(region, coordinateSystem);
            case SELECT -> selectingEventHandler.setSelectedRegion(region, coordinateSystem);
        };
    }

    @Override
    public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
        return selectingEventHandler.canSelectAt(point, axes);
    }

}