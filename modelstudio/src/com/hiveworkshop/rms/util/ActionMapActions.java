//package com.hiveworkshop.rms.util;
//
//import com.hiveworkshop.rms.ui.application.MenuBarActions;
//import com.hiveworkshop.rms.ui.application.ProgramGlobals;
//import com.hiveworkshop.rms.ui.application.ScriptActions;
//import com.hiveworkshop.rms.ui.application.actionfunctions.*;
//import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
//import com.hiveworkshop.rms.ui.application.tools.KeyframeCopyPanel;
//import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
//import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public enum ActionMapActions {
//	UNDO("Undo",                                            () -> ProgramGlobals.getUndoHandler().undo()),
//	REDO("Redo",                                            () -> ProgramGlobals.getUndoHandler().redo()),
//	DELETE("Delete",                                        () -> Delete.deleteActionRes()),
//	CLONE_SELECTION("CloneSelection",                       () -> Duplicate.cloneActionRes()),
//	DUPLICATE("Duplicate",                                  () -> Duplicate.cloneActionRes()),
//
//	CUT("Cut",                                              e -> MenuBarActions.copyCutPast(e)),
//	COPY("Copy",                                            e -> MenuBarActions.copyCutPast(e)),
//	PASTE("Paste",                                          e -> MenuBarActions.copyCutPast(e)),
//
//
//	MAXIMIZE_FOCUSED_WINDOW("Maximize Focused Window",      () -> TempActionsForActionMap.maximizeFocusedWindow()),
//	NEXT_KEYFRAME("Jump to next keyframe",                  () -> TimeSkip.nextKeyframe()),
//	PREV_KEYFRAME("Jump to previous keyframe",              () -> TimeSkip.previousKeyframe()),
//	JUMP_ONE_FRAME_FW("Jump forward 1 frame",               () -> TimeSkip.jumpFrames(1)),
//	JUMP_TEN_FRAMES_FW("Jump forward 10 frames",            () -> TimeSkip.jumpFrames(10)),
//	JUMP_ONE_FRAME_BW("Jump back 1 frame",                  () -> TimeSkip.jumpFrames(-1)),
//	JUMP_TEN_FRAMES_BW("Jump back 10 frames",               () -> TimeSkip.jumpFrames(-10)),
//	PLAY_ACTION("Play/pause Animation",                     () -> TimeSkip.playAnimation()),
//
//	SET_TRANSL_MODE_ANIM("set_Transl_Mode_Anim",            () -> {if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.TRANSLATION);}),
//	SET_SCALING_MODE_ANIM("set_Scaling_Mode_Anim",          () -> {if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.SCALING);}),
//	SET_ROTATE_MODE_ANIM("set_Rotate_Mode_Anim",            () -> {if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.ROTATION);}),
//	SET_EXTRUDE_MODE_ANIM("set_Extrude_Mode_Anim",          () -> {if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.EXTRUDE);}),
//	SET_EXTEND_MODE_ANIM("set_Extend_Mode_Anim",            () -> {if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.EXTEND);}),
//
//	SELECTION_TYPE_ANIMATE("Selection_Type_ANIMATE",        () -> {if (!isTextField()) ProgramGlobals.setSelectionTypeButton(SelectionItemTypes.ANIMATE);}),
//	SELECTION_TYPE_VERTEX("Selection_Type_VERTEX",          () -> {if (!isTextField()) ProgramGlobals.setSelectionTypeButton(SelectionItemTypes.VERTEX);}),
//	SELECTION_TYPE_CLUSTER("Selection_Type_CLUSTER",        () -> {if (!isTextField()) ProgramGlobals.setSelectionTypeButton(SelectionItemTypes.CLUSTER);}),
//	SELECTION_TYPE_FACE("Selection_Type_FACE",              () -> {if (!isTextField()) ProgramGlobals.setSelectionTypeButton(SelectionItemTypes.FACE);}),
//	SELECTION_TYPE_GROUP("Selection_Type_GROUP",            () -> {if (!isTextField()) ProgramGlobals.setSelectionTypeButton(SelectionItemTypes.GROUP);}),
//
//	TOGGLE_WIREFRAME("Toggle wireframe",                    () -> {if (!isTextField())ProgramGlobals.getPrefs().setViewMode(ProgramGlobals.getPrefs().getViewMode() == 1 ? 0 : 1); }),
//	CREATE_FACE("Create face from selection",               () -> CreateFace.createFace()),
//	SHIFT_SELECT("shiftSelect",                             () -> Select.shiftSelectActionRes()),
//	ALT_SELECT("altSelect",                                 () -> Select.altSelectActionRes()),
//	UNSHIFT_SELECT("unShiftSelect",                         () -> Select.unShiftSelectActionRes()),
//	UNALT_SELECT("unAltSelect",                             () -> Select.unAltSelect()),
//	SELECT_ALL("Select All",                                () -> Select.selectAll()),
//	INVERT_SELECTION("Invert Selection",                    () -> Select.invertSelectActionRes()),
//	EXPAND_SELECTION("Expand Selection",                    () -> Select.expandSelection()),
//	RIG_ACTION("RigAction",                                 () -> RigSelection.rigActionRes()),
//	MERGE_GEOSETS("Merge Geosets",                          () -> MergeGeosets.mergeGeosetActionRes2()),
//
//	EXPORT_STATIC_MESH("Export Animated to Static Mesh",                    () -> ExportStaticMesh.exportAnimatedToStaticMesh()),
//	EXPORT_ANIMATED_PNG("Export Animated Frame PNG",                        () -> ExportViewportFrame.exportAnimatedFramePNG()),
//	COPY_KFS_BETWEEN_ANIMS("Copy Keyframes Between Animations",             () -> KeyframeCopyPanel.showPanel()),
//	BACK2BACK_ANIMATION("Create Back2Back Animation",                       () -> CombineAnimations.combineAnimations()),
//	SCALING_ANIM_LENGTHS("Change Animation Lengths by Scaling",             () -> ScaleAnimationLength.showPanel()),
//	ASSIGN_800("Assign FormatVersion 800",                                  () -> ProgramGlobals.getCurrentModelPanel().getModel().setFormatVersion(800)),
//	ASSIGN_1000("Assign FormatVersion 1000",                                () -> ProgramGlobals.getCurrentModelPanel().getModel().setFormatVersion(1000)),
//	SD_TO_HD("SD -> HD (highly experimental, requires 900 or 1000)",        () -> MakeModelHD.makeItHD()),
//	HD_TO_SD("HD -> SD (highly experimental, becomes 800)",                 () -> MakeModelSD.convertToV800(1)),
//	REMOVE_LODS("Remove LoDs (highly experimental)",                        () -> RemoveLODs.removeLoDs()),
//	RECALC_TANGENTS("Recalculate Tangents (requires 900 or 1000)",          () -> RecalculateTangents.recalculateTangents()),
//	IMP_GEOSET_INTO_GEOSET("Import Geoset into Existing Geoset",            () -> ImportGeoset.mergeGeosetActionRes()),
//	SMOOTH_VERTS("Twilac-Style SmoothVerts",                                () -> SmoothSelection.smoothSelection()),
//	EDIT_MODEL_COMPS("Edit/delete model components",                        () -> ScriptActions.openImportPanelWithEmpty()),
//	IMPORT_ANIM("Import Animation",                                         () -> new AnimationTransfer().showWindow()),
//
//	KEYBOARDKEY("KeyboardKey", () -> System.out.println("KeyboardKey")),
//	;
//
////	private void ugg(){
////		for (ActionMapActions mapActions : ActionMapActions.values()) {
////			actionMap.put(mapActions, mapActions.getAsAction());
////		}
////		inputMap.put(KeyStroke.getKeyStroke("control Z"), ActionMapActions.DELETE);
////	}
//
//	private static boolean isTextField() {
//		return focusedComponentNeedsTyping(getFocusedComponent());
//	}
//
//	private boolean isTextField2() {
//		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//		return (focusedComponent instanceof JTextArea)
//				|| (focusedComponent instanceof JTextField)
//				|| (focusedComponent instanceof JTextPane);
//	}
//
//	private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
//		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
//	}
//
//	private static Component getFocusedComponent() {
//		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//		return kfm.getFocusOwner();
//	}
//
//	private final String name;
//	Runnable runnable;
//	AbstractAction action;
//
//	ActionMapActions(String name, Runnable runnable){
//		this.name = name;
//		this.runnable = runnable;
//		action = getAsAction();
//	}
//	ActionMapActions(String name, ActionListener action){
//		this.name = name;
//		this.action = getAsAction1(action);
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	private AbstractAction getAsAction() {
//		return new AbstractAction(name) {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				runnable.run();
//			}
//		};
//	}
//	private AbstractAction getAsAction1(ActionListener action) {
//		return new AbstractAction(name) {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				action.actionPerformed(e);
//			}
//		};
//	}
//
//	public AbstractAction getAction() {
//		return action;
//	}
//}
