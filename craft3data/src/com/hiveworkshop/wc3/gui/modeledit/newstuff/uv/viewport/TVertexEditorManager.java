package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.viewport;

import java.util.Collection;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.NodeAnimationSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.FaceSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.FaceTVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.GeosetVertexTVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.selection.TVertexSelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

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
		final Collection<? extends Vertex> lastSelectedVertices;
		if (selectionView != null) {

			lastSelectedVertices = selectionView.getSelectedVertices();
		} else {
			lastSelectedVertices = null;
		}
		switch (selectionMode) {
		case FACE: {
			final FaceSelectionManager selectionManager = new FaceSelectionManager();
			final FaceTVertexEditor faceModelEditor = new FaceTVertexEditor(model, programPreferences, selectionManager,
					structureChangeListener);
			modelEditor = faceModelEditor;
			if (lastSelectedVertices != null) {
				modelEditor.selectByVertices(lastSelectedVertices);
			}
			viewportSelectionHandler.setSelectingEventHandler(modelEditor);
			modelEditorChangeListener.editorChanged(modelEditor);
			selectionView = selectionManager;
			selectionListener.onSelectionChanged(selectionView);
			nodeAnimationSelectionManager = null;
			break;
		}
		default:
		case VERTEX: {
			final GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager();
			final GeosetVertexTVertexEditor geosetVertexModelEditor = new GeosetVertexTVertexEditor(model,
					programPreferences, selectionManager, structureChangeListener);
			modelEditor = geosetVertexModelEditor;
			if (lastSelectedVertices != null) {
				modelEditor.selectByVertices(lastSelectedVertices);
			}
			viewportSelectionHandler.setSelectingEventHandler(modelEditor);
			modelEditorChangeListener.editorChanged(modelEditor);
			selectionView = selectionManager;
			selectionListener.onSelectionChanged(selectionView);
			nodeAnimationSelectionManager = null;
			break;
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
