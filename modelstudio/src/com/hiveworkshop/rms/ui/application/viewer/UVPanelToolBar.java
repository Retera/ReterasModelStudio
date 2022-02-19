package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UVPanelToolBar extends JToolBar{
	private TVertexEditorManager modelEditorManager;
	private ViewportActivityManager viewportActivityManager;
	ModelHandler modelHandler;

	private ToolbarButtonGroup2<SelectionItemTypes> selectionItemTypeGroup;
	private ToolbarButtonGroup2<SelectionMode> selectionModeGroup;
	private ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup;

	public UVPanelToolBar(){
		super(JToolBar.HORIZONTAL);
		setFloatable(false);
		addSeparator();

		add(ProgramGlobals.getUndoHandler().getUndoAction());
		add(ProgramGlobals.getUndoHandler().getRedoAction());

		addSeparator();

		selectionModeGroup = new ToolbarButtonGroup2<>(this, SelectionMode.values());
		selectionModeGroup.setActiveButton(SelectionMode.SELECT);
		selectionModeGroup.addToolbarButtonListener(ProgramGlobals::setSelectionModeButton);
		addSeparator();
		selectionItemTypeGroup = new ToolbarButtonGroup2<>(this, new SelectionItemTypes[] {SelectionItemTypes.VERTEX, SelectionItemTypes.FACE});
		selectionItemTypeGroup.setActiveButton(SelectionItemTypes.VERTEX);
		selectionItemTypeGroup.addToolbarButtonListener(this::setSetlectionType);
		addSeparator();
		actionTypeGroup = new ToolbarButtonGroup2<>(this, new ModelEditorActionType3[]{ModelEditorActionType3.TRANSLATION, ModelEditorActionType3.SCALING, ModelEditorActionType3.ROTATION});
		actionTypeGroup.addToolbarButtonListener(this::changeActivity);
		actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);

		add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(modelHandler != null){
					modelHandler.getUndoManager().pushAction(snapSelectedVertices().redo());
				}
			}
		});


		setMaximumSize(new Dimension(80000, 48));
	}

	public UndoAction snapSelectedVertices() {
		Collection<Vec2> selection = getTVertices(modelHandler.getModelView().getSelectedVertices(), 0);
		List<Vec2> oldLocations = new ArrayList<>();
		Vec2 cog = Vec2.centerOfGroup(selection);
		for (Vec2 vertex : selection) {
			oldLocations.add(new Vec2(vertex));
		}
		return new UVSnapAction(selection, oldLocations, cog);
	}

	public static Collection<Vec2> getTVertices(Collection<GeosetVertex> vertexSelection, int uvLayerIndex) {
		List<Vec2> tVertices = new ArrayList<>();
		for (GeosetVertex vertex : vertexSelection) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				tVertices.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		return tVertices;
	}


	private void setSetlectionType(SelectionItemTypes newType) {
		modelEditorManager.setSelectionItemType(newType);
		repaint();
	}
	public void changeActivity(ModelEditorActionType3 newType) {
		if (newType != null && modelEditorManager != null) {
			TVertexEditorManipulatorBuilder manipulatorBuilder = new TVertexEditorManipulatorBuilder(modelEditorManager, modelHandler, newType);
			ViewportActivity activity =  new MultiManipulatorActivity(manipulatorBuilder, modelHandler, modelEditorManager);
			viewportActivityManager.setCurrentActivity(activity);
		}
	}


	static JButton addToolbarIcon(JToolBar toolbar, String hooverText, String icon, Runnable function) {
		System.out.println("adding Toolbar button: " + icon + " (" + hooverText + ")");
		AbstractAction action = new AbstractAction(hooverText) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					function.run();
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		};

		JButton button = new JButton(RMSIcons.loadToolBarImageIcon(icon));
		button.setToolTipText(hooverText);
		button.addActionListener(action);
		toolbar.add(button);
		return button;
	}

	public UVPanelToolBar setModelHandler(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		viewportActivityManager = new ViewportActivityManager(null);

		ModelEditorChangeNotifier modelEditorChangeNotifier = new ModelEditorChangeNotifier();
		modelEditorChangeNotifier.subscribe(viewportActivityManager);

		modelEditorManager = new TVertexEditorManager(this.modelHandler, selectionModeGroup, modelEditorChangeNotifier, viewportActivityManager);
		updateItemGroupTypes();
		return this;
	}

	public UVPanelToolBar setItemGroupType(SelectionItemTypes selectionItemType) {
		this.selectionItemTypeGroup.setActiveButton(selectionItemType);
		return this;
	}

	public UVPanelToolBar setItemGroupType(SelectionMode selectionItemType) {
		this.selectionModeGroup.setActiveButton(selectionItemType);
		return this;
	}

	public UVPanelToolBar setItemGroupType(ModelEditorActionType3 selectionItemType) {
		this.actionTypeGroup.setActiveButton(selectionItemType);
		return this;
	}

	public UVPanelToolBar updateItemGroupTypes() {
		this.selectionItemTypeGroup.setActiveButton(ProgramGlobals.getSelectionItemType());
		this.selectionModeGroup.setActiveButton(ProgramGlobals.getSelectionMode());
		this.actionTypeGroup.setActiveButton(ProgramGlobals.getEditorActionType());
		return this;
	}

	public ToolbarButtonGroup2<SelectionItemTypes> getSelectionItemTypeGroup() {
		return selectionItemTypeGroup;
	}

	public ToolbarButtonGroup2<SelectionMode> getSelectionModeGroup() {
		return selectionModeGroup;
	}

	public ToolbarButtonGroup2<ModelEditorActionType3> getActionTypeGroup() {
		return actionTypeGroup;
	}

	public TVertexEditorManager getModelEditorManager() {
		return modelEditorManager;
	}

	public ViewportActivityManager getViewportActivityManager() {
		return viewportActivityManager;
	}
}
