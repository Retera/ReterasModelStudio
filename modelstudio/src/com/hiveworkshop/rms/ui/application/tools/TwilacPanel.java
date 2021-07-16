package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SnapCloseVertsAction;
import com.hiveworkshop.rms.editor.actions.mesh.WeldVertsAction;
import com.hiveworkshop.rms.editor.actions.nodes.BakeAndRebindAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectoinUgg;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
				ProgramGlobals.getCurrentModelPanel().getUndoManager().pushAction(new SnapCloseVertsAction(ProgramGlobals.getCurrentModelPanel().getModelView().getSelectedVertices(), 1, ModelStructureChangeListener.changeListener).redo()));
		add(snapCloseVerts, "wrap");

		JButton weldCloseVerts = new JButton("Weld Close Verts");
		weldCloseVerts.addActionListener(e ->
				ProgramGlobals.getCurrentModelPanel().getUndoManager().pushAction(new WeldVertsAction(ProgramGlobals.getCurrentModelPanel().getModelView().getSelectedVertices(), 1, ModelStructureChangeListener.changeListener).redo()));
		add(weldCloseVerts, "wrap");

		JButton renameBoneChain = new JButton("Rename Bone Chain");
		renameBoneChain.addActionListener(e -> RenameBoneChainPanel.show(ProgramGlobals.getMainPanel()));
		add(renameBoneChain, "wrap");

		JButton selectNodeGeometry = new JButton("selectNodeGeometry");
		selectNodeGeometry.addActionListener(e -> selectNodeGeometry());
		add(selectNodeGeometry, "wrap");

//		JButton editParticle = new JButton("editParticle");
//		editParticle.addActionListener(e -> viewParticlePanel());
//		add(editParticle, "wrap");

		JButton reorder_animations = new JButton("Reorder Animations");
		reorder_animations.addActionListener(e -> viewReOrderAnimsPanel());
		add(reorder_animations, "wrap");

		JButton button = new JButton("button");
		button.addActionListener(e -> button.setText(button.getText().equalsIgnoreCase("butt-on") ? "Butt-Off" : "Butt-On"));
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
		modelPanel.getUndoManager().pushAction(new CompoundAction("Baked and changed Parent", rebindActions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
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

	private void selectNodeGeometry() {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
			ModelView modelView = modelHandler.getModelView();

			Set<Bone> selectedBones = new HashSet<>();
			Set<GeosetVertex> vertexList = new HashSet<>();
			for (IdObject idObject : modelView.getSelectedIdObjects()) {
				if (idObject instanceof Bone) {
					selectedBones.add((Bone) idObject);
				}
			}
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Bone bone : selectedBones) {
					List<GeosetVertex> vertices = geoset.getBoneMap().get(bone);
					if (vertices != null) {
						vertexList.addAll(vertices);
					}
				}
			}
			if (!vertexList.isEmpty()) {
				UndoAction action = new SetSelectionUggAction(new SelectoinUgg(vertexList), modelView, "Select");
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}

	private void viewParticlePanel(){
		ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
		if (currentModelPanel != null) {
			List<ParticleEmitter2> particleEmitter2s = currentModelPanel.getModel().getParticleEmitter2s();
			if(!particleEmitter2s.isEmpty()) {
				ParticleEditPanel panel = new ParticleEditPanel(particleEmitter2s.get(particleEmitter2s.size()/2));
				FramePopup.show(panel, null, "Edit Particle2 Emitter");
			}
		}
	}
	private void viewReOrderAnimsPanel(){
		ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
		if (currentModelPanel != null) {
			ReorderAnimationsPanel panel = new ReorderAnimationsPanel(ProgramGlobals.getCurrentModelPanel().getModelHandler());
			FramePopup.show(panel, null, "Edit Particle2 Emitter");
		}
	}
}
