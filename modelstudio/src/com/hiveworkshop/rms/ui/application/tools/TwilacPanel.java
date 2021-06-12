package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SnapCloseVertsAction;
import com.hiveworkshop.rms.editor.actions.mesh.WeldVertsAction;
import com.hiveworkshop.rms.editor.actions.nodes.BakeAndRebindAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TwilacPanel extends JPanel {
	public TwilacPanel() {
		super(new MigLayout("", "", ""));
		JButton geosetsUneditable = new JButton("Geoset Uneditable");
		geosetsUneditable.addActionListener(e -> ProgramGlobals.getCurrentModelPanel().getModelView().setGeosetsEditable(false));
		add(geosetsUneditable, "wrap");
		JButton geosetsEditable = new JButton("Geoset Editable");
		geosetsEditable.addActionListener(e -> ProgramGlobals.getCurrentModelPanel().getModelView().setGeosetsEditable(true));
		add(geosetsEditable, "wrap");

		JButton hideVerts = new JButton("Hide Selected Verts");
		hideVerts.addActionListener(e -> ProgramGlobals.getCurrentModelPanel().getModelView().hideVertices(ProgramGlobals.getCurrentModelPanel().getModelView().getSelectedVertices()));
		add(hideVerts, "wrap");

		JButton unHideVerts = new JButton("Unhide Verts");
		unHideVerts.addActionListener(e -> ProgramGlobals.getCurrentModelPanel().getModelView().unHideVertices());
		add(unHideVerts, "wrap");

		JButton bakeAndRebindToNull = new JButton("BakeAndRebindToNull");
		bakeAndRebindToNull.addActionListener(e -> rebindToNull());
		add(bakeAndRebindToNull, "wrap");

		JButton snapCloseVerts = new JButton("Snap Close Verts");
		snapCloseVerts.addActionListener(e ->
				ProgramGlobals.getCurrentModelPanel().getUndoManager().pushAction(new SnapCloseVertsAction(ProgramGlobals.getCurrentModelPanel().getModelView().getSelectedVertices(), 1, ProgramGlobals.getCurrentModelPanel().getModelStructureChangeListener()).redo()));
		add(snapCloseVerts, "wrap");

		JButton weldCloseVerts = new JButton("Weld Close Verts");
		weldCloseVerts.addActionListener(e ->
				ProgramGlobals.getCurrentModelPanel().getUndoManager().pushAction(new WeldVertsAction(ProgramGlobals.getCurrentModelPanel().getModelView().getSelectedVertices(), 1, ProgramGlobals.getCurrentModelPanel().getModelStructureChangeListener()).redo()));
		add(weldCloseVerts, "wrap");

		JButton button = new JButton("button");
		button.addActionListener(e -> ProgramGlobals.getCurrentModelPanel().getModelView().setGeosetsEditable(true));
		add(button, "wrap");


	}

	public void makeGeosetUneditable() {
		ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		modelView.setGeosetsEditable(true);
	}

	public void rebindToNull() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		ModelView modelView = modelPanel.getModelView();
		List<UndoAction> rebindActions = new ArrayList<>();
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			System.out.println("rebinding " + idObject.getName());
			UndoAction action = new BakeAndRebindAction(idObject, null, modelPanel.getModelHandler());
			rebindActions.add(action);
		}
		modelPanel.getUndoManager().pushAction(new CompoundAction("Baked and changed Parent", rebindActions, () -> modelPanel.getModelStructureChangeListener().nodesUpdated()).redo());
	}

	//	public void snapClose(){
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		UndoAction action = new SnapCloseVertsAction(modelPanel.getModelView().getSelectedVertices(), 1);
//		modelPanel.getUndoManager().pushAction(action.redo());
//		modelPanel.getUndoManager().pushAction(new SnapCloseVertsAction(ProgramGlobals.getCurrentModelPanel().getModelView().getSelectedVertices(), 1).redo());
//	}
	public static void showPopup(JComponent parent) {
		FramePopup.show(new TwilacPanel(), parent, "Twilac's new tools");
	}

	public static void showPopup() {
		FramePopup.show(new TwilacPanel(), ProgramGlobals.getMainPanel(), "Twilac's new tools");
	}
}
