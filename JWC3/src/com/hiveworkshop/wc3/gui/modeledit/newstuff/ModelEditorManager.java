package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.Collection;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.NodeAnimationModelEditor;
import com.hiveworkshop.wc3.gui.animedit.NodeAnimationSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class ModelEditorManager {
	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private ModelEditor modelEditor;
	private final ViewportSelectionHandlerImpl viewportSelectionHandler;
	private final ModelEditorChangeListener modelEditorChangeListener;
	private SelectionView selectionView;
	private final SelectionListener selectionListener;
	private final RenderModel renderModel;
	private NodeAnimationSelectionManager nodeAnimationSelectionManager;
	private final ModelStructureChangeListener structureChangeListener;

	public ModelEditorManager(final ModelView model, final ProgramPreferences programPreferences,
			final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final ModelEditorChangeListener modelEditorChangeListener, final SelectionListener selectionListener,
			final RenderModel renderModel, final ModelStructureChangeListener structureChangeListener) {
		this.model = model;
		this.modelEditorChangeListener = modelEditorChangeListener;
		this.programPreferences = programPreferences;
		this.selectionListener = selectionListener;
		this.renderModel = renderModel;
		this.structureChangeListener = structureChangeListener;
		viewportSelectionHandler = new ViewportSelectionHandlerImpl(modeButtonGroup, null);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(final SelectionItemTypes selectionMode) {
		final Collection<? extends Vertex> lastSelectedVertices;
		if (selectionView != null) {

			lastSelectedVertices = selectionView.getSelectedVertices();
		} else {
			lastSelectedVertices = null;
		}
		switch (selectionMode) {
		case FACE: {
			final FaceSelectionManager selectionManager = new FaceSelectionManager();
			final PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
			final ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
			modelEditorNotifier.subscribe(
					new FaceModelEditor(model, programPreferences, selectionManager, structureChangeListener));
			modelEditorNotifier.subscribe(new PivotPointModelEditor(model, programPreferences, pivotSelectionManager,
					structureChangeListener));
			modelEditor = modelEditorNotifier;
			if (lastSelectedVertices != null) {
				modelEditor.selectByVertices(lastSelectedVertices);
			}
			viewportSelectionHandler.setSelectingEventHandler(modelEditor);
			modelEditorChangeListener.modelEditorChanged(modelEditor);
			selectionView = new MultiPartSelectionView(
					ListView.Util.<SelectionView> of(selectionManager, pivotSelectionManager));
			selectionListener.onSelectionChanged(selectionView);
			nodeAnimationSelectionManager = null;
			break;
		}
		case GROUP: {
			final VertexGroupSelectionManager selectionManager = new VertexGroupSelectionManager();
			final PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
			final ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
			modelEditorNotifier.subscribe(
					new VertexGroupModelEditor(model, programPreferences, selectionManager, structureChangeListener));
			modelEditorNotifier.subscribe(new PivotPointModelEditor(model, programPreferences, pivotSelectionManager,
					structureChangeListener));
			modelEditor = modelEditorNotifier;
			if (lastSelectedVertices != null) {
				modelEditor.selectByVertices(lastSelectedVertices);
			}
			viewportSelectionHandler.setSelectingEventHandler(modelEditor);
			modelEditorChangeListener.modelEditorChanged(modelEditor);
			selectionView = new MultiPartSelectionView(
					ListView.Util.<SelectionView> of(selectionManager, pivotSelectionManager));
			selectionListener.onSelectionChanged(selectionView);
			nodeAnimationSelectionManager = null;
			break;
		}
		case ANIMATE: {
			nodeAnimationSelectionManager = new NodeAnimationSelectionManager(renderModel);
			final NodeAnimationModelEditor nodeAnimationModelEditor = new NodeAnimationModelEditor(model,
					programPreferences, nodeAnimationSelectionManager, renderModel, structureChangeListener);
			modelEditor = nodeAnimationModelEditor;
			if (lastSelectedVertices != null) {
				modelEditor.selectByVertices(lastSelectedVertices);
			}
			viewportSelectionHandler.setSelectingEventHandler(modelEditor);
			modelEditorChangeListener.modelEditorChanged(modelEditor);
			selectionView = nodeAnimationSelectionManager;
			selectionListener.onSelectionChanged(selectionView);
			break;
		}
		default:
		case VERTEX: {
			final GeosetVertexSelectionManager selectionManager = new GeosetVertexSelectionManager();
			final PivotPointSelectionManager pivotSelectionManager = new PivotPointSelectionManager();
			final ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
			modelEditorNotifier.subscribe(
					new GeosetVertexModelEditor(model, programPreferences, selectionManager, structureChangeListener));
			modelEditorNotifier.subscribe(new PivotPointModelEditor(model, programPreferences, pivotSelectionManager,
					structureChangeListener));
			modelEditor = modelEditorNotifier;
			if (lastSelectedVertices != null) {
				modelEditor.selectByVertices(lastSelectedVertices);
			}
			viewportSelectionHandler.setSelectingEventHandler(modelEditor);
			modelEditorChangeListener.modelEditorChanged(modelEditor);
			selectionView = new MultiPartSelectionView(
					ListView.Util.<SelectionView> of(selectionManager, pivotSelectionManager));
			selectionListener.onSelectionChanged(selectionView);
			nodeAnimationSelectionManager = null;
			break;
		}
		}
	}

	public ModelEditor getModelEditor() {
		return modelEditor;
	}

	public ViewportSelectionHandlerImpl getViewportSelectionHandler() {
		return viewportSelectionHandler;
	}

	public SelectionView getSelectionView() {
		return selectionView;
	}

	public NodeAnimationSelectionManager getNodeAnimationSelectionManager() {
		return nodeAnimationSelectionManager;
	}
}
