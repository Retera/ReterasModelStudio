package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.RecalculateTangentsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class RecalculateTangents extends ActionFunction {
	public RecalculateTangents() {
		super(TextKey.RECALC_TANGENTS, RecalculateTangents::recalculateTangents3);
	}

	public static void recalculateTangents3(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		Set<GeosetVertex> vertices = new HashSet<>();
		for (Geoset geoset : model.getGeosets()) {
			vertices.addAll(geoset.getVertices());
		}

		RecalculateTangentsAction recalculateTangentsAction = new RecalculateTangentsAction(vertices);
		modelHandler.getUndoManager().pushAction(recalculateTangentsAction.redo());


		int goodTangents = 0;
		int slightlyBadTangents = 0;
		int badTangents = 0;
		for (Geoset theMesh : model.getGeosets()) {
			for (GeosetVertex gv : theMesh.getVertices()) {
				double dotProduct = gv.getNormal().dot(gv.getTang().getVec3());
//				System.out.println("dotProduct: " + dotProduct + " ("+gv.getNormal() + "*" + gv.getTangent() + ") angle: " + gv.getNormal().degAngleTo(gv.getTang().getVec3()));
				if (Math.abs(dotProduct) <= 0.000001) {
					goodTangents += 1;
				} else if (Math.abs(dotProduct) <= 0.01) {
					slightlyBadTangents += 1;
				} else {
					badTangents += 1;
				}
			}
		}
		JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
				"Tangent generation completed." +
						"\nGood tangents: " + goodTangents + ", slightly bad tangents: " + slightlyBadTangents + ", bad tangents: " + badTangents + "" +
						"\nFound " + recalculateTangentsAction.getZeroAreaUVTris() + " uv triangles with no area");
	}

}
