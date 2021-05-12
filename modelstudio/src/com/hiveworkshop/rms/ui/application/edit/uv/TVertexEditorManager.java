package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.faces.FaceSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex.GeosetVertexSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.FaceTVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.GeosetVertexTVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public final class TVertexEditorManager {
	private final ModelView modelView;
	private TVertexEditor modelEditor;
	private final TVertexViewportSelectionHandlerImpl viewportSelectionHandler;
	private final TVertexEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private final RenderModel renderModel;
	private NodeAnimationSelectionManager nodeAnimationSelectionManager;
	private final ModelStructureChangeListener structureChangeListener;
	public static boolean MOVE_LINKED;

	public TVertexEditorManager(ModelView modelView,
	                            ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                            TVertexEditorChangeListener modelEditorChangeListener,
	                            SelectionListener selectionListener,
	                            RenderModel renderModel,
	                            ModelStructureChangeListener structureChangeListener) {
		this.modelView = modelView;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.selectionListener = selectionListener;
		this.renderModel = renderModel;
		this.structureChangeListener = structureChangeListener;
		viewportSelectionHandler = new TVertexViewportSelectionHandlerImpl(modeButtonGroup, null);
		setSelectionItemType(TVertexSelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(final TVertexSelectionItemTypes selectionMode) {
		final Collection<? extends Vec3> lastSelectedVertices;
		if (selectionView != null) {

			lastSelectedVertices = selectionView.getSelectedVertices();
		} else {
			lastSelectedVertices = null;
		}
		switch (selectionMode) {
			case FACE -> {
				final FaceSelectionManager selectionManager = new FaceSelectionManager(modelView);
				final FaceTVertexEditor faceModelEditor = new FaceTVertexEditor(modelView, selectionManager, structureChangeListener);
				modelEditor = faceModelEditor;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.editorChanged(modelEditor);
				selectionView = selectionManager;
				selectionListener.onSelectionChanged(selectionView);
				nodeAnimationSelectionManager = null;
			}
			case VERTEX -> {
				final GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager(modelView);
				final GeosetVertexTVertexEditor geosetVertexModelEditor = new GeosetVertexTVertexEditor(modelView, selectionManager, structureChangeListener);
				modelEditor = geosetVertexModelEditor;
				if (lastSelectedVertices != null) {
					modelEditor.selectByVertices(lastSelectedVertices);
				}
				viewportSelectionHandler.setSelectingEventHandler(modelEditor);
				modelEditorChangeListener.editorChanged(modelEditor);
				selectionView = selectionManager;
				selectionListener.onSelectionChanged(selectionView);
				nodeAnimationSelectionManager = null;
			}
		}
	}

	public TVertexEditor getModelEditor() {
		return modelEditor;
	}

	public TVertexViewportSelectionHandlerImpl getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public SelectionView getSelectionView() {
		return selectionView;
	}

	public NodeAnimationSelectionManager getNodeAnimationSelectionManager() {
		return nodeAnimationSelectionManager;
	}
}
