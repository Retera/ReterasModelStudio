//package com.hiveworkshop.rms.editor.actions.selection;
//
//import com.hiveworkshop.rms.editor.actions.UndoAction;
//import com.hiveworkshop.rms.ui.gui.modeledit.listener.EditabilityToggleHandler;
//
//public final class MakeNotEditableAction implements UndoAction {
//	private final EditabilityToggleHandler editabilityToggleHandler;
//	private final Runnable truncateSelectionRunnable;
//	private final Runnable unTruncateSelectionRunnable;
//	private final Runnable refreshGUIRunnable;
//
//	public MakeNotEditableAction(EditabilityToggleHandler editabilityToggleHandler,
//	                             Runnable truncateSelectionRunnable,
//	                             Runnable unTruncateSelectionRunnable,
//	                             Runnable refreshGUIRunnable) {
//		this.editabilityToggleHandler = editabilityToggleHandler;
//		this.truncateSelectionRunnable = truncateSelectionRunnable;
//		this.unTruncateSelectionRunnable = unTruncateSelectionRunnable;
//		this.refreshGUIRunnable = refreshGUIRunnable;
//	}
//
//	@Override
//	public UndoAction undo() {
//		editabilityToggleHandler.makeEditable();
//		unTruncateSelectionRunnable.run();
//		refreshGUIRunnable.run();
//		return this;
//	}
//
//	@Override
//	public UndoAction redo() {
//		editabilityToggleHandler.makeNotEditable();
//		truncateSelectionRunnable.run();
//		refreshGUIRunnable.run();
//		return this;
//	}
//
//	@Override
//	public String actionName() {
//		return "toggle visibility";
//	}
//
//}