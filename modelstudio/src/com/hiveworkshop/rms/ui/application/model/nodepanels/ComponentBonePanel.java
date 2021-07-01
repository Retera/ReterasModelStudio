package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.tools.RemoveBoneFromGeoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ComponentBonePanel extends ComponentIdObjectPanel<Bone> {

	public ComponentBonePanel(ModelHandler modelHandler) {
		super(modelHandler);
		JButton removeGeoBindings = new JButton("Remove from all vertices");
		removeGeoBindings.addActionListener(e -> removeGeoBindings());
		topPanel.add(removeGeoBindings);
	}

	private void removeGeoBindings() {
		List<GeosetVertex> vertexList = new ArrayList<>();
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			List<GeosetVertex> vertices = geoset.getBoneMap().get(idObject);
			if (vertices != null) {
				vertexList.addAll(vertices);
			}
		}
		UndoAction action = new RemoveBoneFromGeoAction(vertexList, idObject);
		modelHandler.getUndoManager().pushAction(action.redo());
	}
}
