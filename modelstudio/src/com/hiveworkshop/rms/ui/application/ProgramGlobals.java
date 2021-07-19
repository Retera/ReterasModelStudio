package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.sound.SoundMappings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ProgramGlobals {
	private static final SaveProfile profile;
	private static final ProgramPreferences prefs;
	private static final List<ModelPanel> modelPanels;
	private static ModelPanel currentModelPanel;
	private static final MainPanel mainPanel;
	private static final JToolBar toolbar;
	private static final UndoHandler undoHandler;
	private static final SoundMappings soundMappings;

	private static final ToolbarButtonGroup2<SelectionItemTypes> selectionItemTypeGroup;
	private static final ToolbarButtonGroup2<SelectionMode> selectionModeGroup;
	private static final ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup;


	static {
		profile = SaveProfile.get();
		prefs = profile.getPreferences();
		modelPanels = new ArrayList<>();
		undoHandler = new UndoHandler();

		toolbar = ToolBar.createJToolBar(null);

		selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionItemTypes.values());
		selectionItemTypeGroup.setActiveButton(SelectionItemTypes.VERTEX);
		selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
		selectionModeGroup.setActiveButton(SelectionMode.SELECT);
		actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorActionType3.values());
		actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);


		mainPanel = new MainPanel(toolbar);
		soundMappings = new SoundMappings();

//		selectionItemTypeGroup.addToolbarButtonListener(mainPanel::selectionItemTypeGroupActionRes);
		selectionItemTypeGroup.addToolbarButtonListener(ProgramGlobals::setSelectionItemType);
//		actionTypeGroup.addToolbarButtonListener(newType -> mainPanel.mainPanelLinkActions.changeTransformMode(newType, mainPanel));
		actionTypeGroup.addToolbarButtonListener(ProgramGlobals::setEditorActionType);
	}

	public static MainPanel getMainPanel() {
		return mainPanel;
	}

	public static UndoHandler getUndoHandler() {
		return undoHandler;
	}

	public static ModelPanel getCurrentModelPanel() {
		return currentModelPanel;
	}

	public static void setCurrentModelPanel(ModelPanel currentModelPanel) {
		ProgramGlobals.currentModelPanel = currentModelPanel;
		if (!modelPanels.contains(currentModelPanel) && currentModelPanel != null) {
			modelPanels.add(currentModelPanel);
		}
		if (currentModelPanel != null) {
			selectionItemTypeGroup.setActiveButton(currentModelPanel.getSelectionType());
			actionTypeGroup.setActiveButton(currentModelPanel.getEditorActionType());
		}
	}

	public static List<ModelPanel> getModelPanels() {
		return modelPanels;
	}

	public static void removeModelPanel(ModelPanel modelPanel) {
		if (currentModelPanel == modelPanel) {
			currentModelPanel = null;
		}
		modelPanels.remove(modelPanel);
	}

	public static void addModelPanel(ModelPanel modelPanel) {
		modelPanels.add(modelPanel);
	}

	public static SaveProfile getProfile() {
		return profile;
	}

	public static ProgramPreferences getPrefs() {
		return prefs;
	}

	public static SoundMappings getSoundMappings() {
		return soundMappings;
	}

	public static void setUggUgg(){
//		selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
//		selectionModeGroup.setActiveButton(SelectionMode.SELECT);
//
//		toolbar.addSeparator();
//
//		selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionItemTypes.values());
//		selectionItemTypeGroup.setActiveButton(SelectionItemTypes.VERTEX);
//
//		toolbar.addSeparator();
//
//		actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorActionType3.values());
//		actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);
	}

	private static void setSelectionItemType(SelectionItemTypes selectionItemTypes){
		if(currentModelPanel != null){
			currentModelPanel.setSelectionType(selectionItemTypes);
		}
	}

	private static void setEditorActionType(ModelEditorActionType3 modelEditorActionType3){
		if(currentModelPanel != null){
			currentModelPanel.setEditorActionType(modelEditorActionType3);
		}
	}

	private static void setSelectionMode(SelectionMode selectionMode){
		if(currentModelPanel != null){

		}
		selectionModeGroup.setActiveButton(selectionMode);
	}
	public static void setSelectionModeButton(SelectionMode selectionMode){
		selectionModeGroup.setActiveButton(selectionMode);
	}

	public static void setSelectionTypeButton(SelectionItemTypes selectionItemTypes){
		selectionItemTypeGroup.setActiveButton(selectionItemTypes);
	}
	public static void setEditorActionTypeButton(ModelEditorActionType3 modelEditorActionType3){
		actionTypeGroup.setActiveButton(modelEditorActionType3);
	}

	public static SelectionMode getSelectionMode(){
		return selectionModeGroup.getActiveButtonType();
	}

	public static SelectionItemTypes getSelectionItemType(){
		return selectionItemTypeGroup.getActiveButtonType();
	}

	public static ModelEditorActionType3 getEditorActionType(){
		return actionTypeGroup.getActiveButtonType();
	}

	public static ToolbarButtonGroup2<ModelEditorActionType3> getActionTypeGroup() {
		return actionTypeGroup;
	}
}
