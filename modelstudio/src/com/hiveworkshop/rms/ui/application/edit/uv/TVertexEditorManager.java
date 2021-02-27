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
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public final class TVertexEditorManager {
	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private TVertexEditor modelEditor;
	private final TVertexViewportSelectionHandlerImpl viewportSelectionHandler;
	private final TVertexEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private final RenderModel renderModel;
	private NodeAnimationSelectionManager nodeAnimationSelectionManager;
	private final ModelStructureChangeListener structureChangeListener;
	public static boolean MOVE_LINKED;

	public TVertexEditorManager(final ModelView model, final ProgramPreferences programPreferences,
			final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final TVertexEditorChangeListener modelEditorChangeListener, final SelectionListener selectionListener,
			final RenderModel renderModel, final ModelStructureChangeListener structureChangeListener) {
		this.model = model;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.programPreferences = programPreferences;
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
				final FaceSelectionManager selectionManager = new FaceSelectionManager();
				final FaceTVertexEditor faceModelEditor = new FaceTVertexEditor(model, programPreferences, selectionManager, structureChangeListener);
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
				final GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager();
				final GeosetVertexTVertexEditor geosetVertexModelEditor = new GeosetVertexTVertexEditor(model, programPreferences, selectionManager, structureChangeListener);
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
