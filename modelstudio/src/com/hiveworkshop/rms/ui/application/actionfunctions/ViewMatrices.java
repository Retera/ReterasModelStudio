package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.InfoPopup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ViewMatrices extends ActionFunction {

	public ViewMatrices(){
		super(TextKey.VIEW_MATRICES, ViewMatrices::viewMatrices);
	}

	public static void viewMatrices(ModelHandler modelHandler) {
		InfoPopup.show(ProgramGlobals.getMainPanel(), getSelectedMatricesDescription(modelHandler.getModelView().getSelectedVertices()));
	}

	public static String getSelectedMatricesDescription(Collection<GeosetVertex> selection) {
		List<Bone> boneRefs = new ArrayList<>();
		for (GeosetVertex gv : selection) {
			for (Bone b : gv.getBones()) {
				if (!boneRefs.contains(b)) {
					boneRefs.add(b);
				}
			}
		}
		StringBuilder boneList = new StringBuilder();
		for (int i = 0; i < boneRefs.size(); i++) {
			if (i == (boneRefs.size() - 2)) {
				boneList.append(boneRefs.get(i).getName()).append(" and ");
			} else if (i == (boneRefs.size() - 1)) {
				boneList.append(boneRefs.get(i).getName());
			} else {
				boneList.append(boneRefs.get(i).getName()).append(", ");
			}
		}
		if (boneRefs.size() == 0) {
			boneList = new StringBuilder("Nothing was selected that was attached to any bones.");
		}
		return boneList.toString();
	}
}
