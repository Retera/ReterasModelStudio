package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.language.Translator;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.sound.SoundMappings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ProgramGlobals {
	private static final SaveProfile profile;
	private static final ProgramPreferences prefs;
	private static final KeyBindingPrefs keyBindingPrefs;
	private static final EditorColorPrefs editorColorPrefs;
	private static final List<ModelPanel> modelPanels;
	private static final RootWindowUgg rootWindowUgg;
	private static final MainPanel mainPanel;
	private static final JToolBar toolbar;
	private static final UndoHandler undoHandler;
	private static final SoundMappings soundMappings;
	private static final MenuBar menuBar;
	private static ModelPanel currentModelPanel;

	private static final Translator translator;
	private static final ViewportTransferHandler viewportTransferHandler = new ViewportTransferHandler();


	private static boolean cheatShift = false;
	private static boolean cheatAlt = false;
	private static boolean lockLayout = false;

	private static final ToolbarButtonGroup2<SelectionItemTypes> selectionItemTypeGroup;
	private static final ToolbarButtonGroup2<SelectionMode> selectionModeGroup;
	private static final ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup;


	static {
		System.out.println("  ~~~  Initializing Globals  ~~~  ");
		profile = SaveProfile.get();
		prefs = profile.getPreferences();
		translator = new Translator();
		keyBindingPrefs = prefs.getKeyBindingPrefs();
		editorColorPrefs = prefs.getEditorColorPrefs();
		modelPanels = new ArrayList<>();
		undoHandler = new UndoHandler();

		toolbar = new ToolBar();

		selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionItemTypes.values());
		selectionItemTypeGroup.setActiveButton(SelectionItemTypes.VERTEX);
		selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
		selectionModeGroup.setActiveButton(SelectionMode.SELECT);
		actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorActionType3.values());
		actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);

		rootWindowUgg = new RootWindowUgg(prefs.getViewMap());
		mainPanel = new MainPanel(toolbar, rootWindowUgg);
		soundMappings = new SoundMappings();

		selectionItemTypeGroup.addToolbarButtonListener(ProgramGlobals::setSelectionItemType);
		actionTypeGroup.addToolbarButtonListener(ProgramGlobals::setEditorActionType);

		menuBar = new MenuBar();
		System.out.println("  ~~~  Initializing Done  ~~~  ");
	}

//	static {
//		System.out.println("  ~~~  Initializing Globals  ~~~  ");
//		TimeLogger timeLogger = new TimeLogger().start();
////		long startTime = System.currentTimeMillis();
//		profile = SaveProfile.get();
//		timeLogger.log("SaveProfile");
//		prefs = profile.getPreferences();
//		timeLogger.log("Preferences");
//		translator = new Translator();
//		timeLogger.log("Translator");
//		keyBindingPrefs = prefs.getKeyBindingPrefs();
//		timeLogger.log("keyBindingPrefs");
//		editorColorPrefs = prefs.getEditorColorPrefs();
//		timeLogger.log("editorColorPrefs");
//		modelPanels = new ArrayList<>();
//		undoHandler = new UndoHandler();
//		timeLogger.log("UndoHandler");
//
//		toolbar = new ToolBar();
//		timeLogger.log("ToolBar");
//
//		selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionItemTypes.values());
//		selectionItemTypeGroup.setActiveButton(SelectionItemTypes.VERTEX);
//		timeLogger.log("selectionItemTypeGroup");
//		selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
//		selectionModeGroup.setActiveButton(SelectionMode.SELECT);
//		timeLogger.log("selectionModeGroup");
//		actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorActionType3.values());
//		actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);
//		timeLogger.log("actionTypeGroup");
//
//		rootWindowUgg = new RootWindowUgg(prefs.getViewMap());
//		timeLogger.log("RootWindowUgg");
//		mainPanel = new MainPanel(toolbar, rootWindowUgg);
//		timeLogger.log("MainPanel");
//		soundMappings = new SoundMappings();
//		timeLogger.log("SoundMappings");
//
//
//		selectionItemTypeGroup.addToolbarButtonListener(ProgramGlobals::setSelectionItemType);
//		actionTypeGroup.addToolbarButtonListener(ProgramGlobals::setEditorActionType);
//		timeLogger.log("Listeners");
//
//		menuBar = new MenuBar();
//		timeLogger.log("MenuBar");
//		timeLogger.print();
//
//		System.out.println("  ~~~  Initializing Done  ~~~  ");
//	}

	public static MainPanel getMainPanel() {
		return mainPanel;
	}

	public static RootWindowUgg getRootWindowUgg(){
		return rootWindowUgg;
	}

	public static UndoHandler getUndoHandler() {
		return undoHandler;
	}

	public static ModelPanel getCurrentModelPanel() {
		return currentModelPanel;
	}

	public static void setCurrentModelPanel(ModelPanel currentModelPanel) {
		ProgramGlobals.currentModelPanel = currentModelPanel;
		rootWindowUgg.getWindowHandler2().setModelPanel(currentModelPanel);
		if (!modelPanels.contains(currentModelPanel) && currentModelPanel != null) {
			modelPanels.add(currentModelPanel);
		}
		if (currentModelPanel != null) {
			selectionItemTypeGroup.setActiveButton(currentModelPanel.getSelectionType());
			actionTypeGroup.setActiveButton(currentModelPanel.getEditorActionType());
		}
		menuBar.setToolsMenuEnabled(currentModelPanel != null);
	}

	public static List<ModelPanel> getModelPanels() {
		return modelPanels;
	}

	public static void removeModelPanel(ModelPanel modelPanel) {
		if (currentModelPanel == modelPanel) {
			currentModelPanel = null;
		}
		modelPanels.remove(modelPanel);
		menuBar.removeModelPanel(modelPanel);
	}

	public static void addModelPanel(ModelPanel modelPanel) {
		if(modelPanel != null && !modelPanels.contains(modelPanel)){
			modelPanels.add(modelPanel);
			menuBar.addModelPanel(modelPanel);
		}
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

	public static void setCheatAlt(boolean cheatAlt) {
		ProgramGlobals.cheatAlt = cheatAlt;
	}

	public static void setCheatShift(boolean cheatShift) {
		ProgramGlobals.cheatShift = cheatShift;
	}

	public static boolean isCheatAlt() {
		return cheatAlt;
	}

	public static boolean isCheatShift() {
		return cheatShift;
	}

	public static boolean isLockLayout() {
//		System.out.println("isLockLayout: " + lockLayout);
		return lockLayout;
	}

	public static void setLockLayout(boolean lockLayout) {
//		System.out.println("setLockLayout: " + lockLayout);
		ProgramGlobals.lockLayout = lockLayout;
	}

	public static KeyBindingPrefs getKeyBindingPrefs() {
		return keyBindingPrefs;
	}

	public static EditorColorPrefs getEditorColorPrefs() {
		return editorColorPrefs;
	}

	public static Translator getTranslator() {
		return translator;
	}

	public static ViewportTransferHandler getViewportTransferHandler() {
		return viewportTransferHandler;
	}

	public static MenuBar getMenuBar() {
		return menuBar;
	}
}
