package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.RecalculateTangentsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class RecalculateTangents extends ActionFunction {
	public RecalculateTangents() {
		super(TextKey.RECALC_TANGENTS, RecalculateTangents::recalculateTangents);
	}

	public static void recalculateTangents(ModelHandler modelHandler) {

		JPanel messagePanel = new JPanel(new MigLayout());
		messagePanel.add(new JLabel("Recalculate tangents for"), "wrap");
//		messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");

		SmartButtonGroup buttonGroup2 = new SmartButtonGroup();
		buttonGroup2.addJRadioButton("Selected vertices", null);
		buttonGroup2.addJRadioButton("Editable vertices", null);
		buttonGroup2.addJRadioButton("All vertices", null);
		buttonGroup2.setSelectedIndex(0);

		messagePanel.add(buttonGroup2.getButtonPanel(), "wrap");

		int userChoice = JOptionPane.showConfirmDialog(
				ProgramGlobals.getMainPanel(), messagePanel,
				"Recalculate Tangents",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (userChoice == JOptionPane.OK_OPTION) {
			ModelView modelView = modelHandler.getModelView();
			EditableModel model = modelHandler.getModel();


			Set<GeosetVertex> vertices = new HashSet<>();
			if (buttonGroup2.getSelectedIndex() == 0) {
				vertices.addAll(modelView.getSelectedVertices());
			} else if (buttonGroup2.getSelectedIndex() == 1) {
				for (Geoset geoset : modelView.getVisEdGeosets()) {
					vertices.addAll(geoset.getVertices());
				}
			} else {
				for (Geoset geoset : model.getGeosets()) {
					vertices.addAll(geoset.getVertices());
				}
			}


			RecalculateTangentsAction recalculateTangentsAction = new RecalculateTangentsAction(vertices);
			modelHandler.getUndoManager().pushAction(recalculateTangentsAction.redo());


			int goodTangents = 0;
			int slightlyBadTangents = 0;
			int badTangents = 0;
			for (GeosetVertex gv : vertices) {
				double dotProduct = gv.getNormal().dot(gv.getTang());
				if (Math.abs(dotProduct) <= 0.000001) {
					goodTangents += 1;
				} else if (Math.abs(dotProduct) <= 0.01) {
					slightlyBadTangents += 1;
				} else {
					badTangents += 1;
				}
			}
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"Tangent generation completed." +
							"\nGood tangents: " + goodTangents + ", slightly bad tangents: " + slightlyBadTangents + ", bad tangents: " + badTangents + "" +
							"\nFound " + recalculateTangentsAction.getZeroAreaUVTris() + " uv triangles with no area");
		}
		ProgramGlobals.getMainPanel().repaint();
	}
}
