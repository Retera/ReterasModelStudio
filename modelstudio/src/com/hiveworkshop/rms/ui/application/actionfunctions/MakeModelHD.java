package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public class MakeModelHD extends ActionFunction {
	public MakeModelHD(){
		super(TextKey.SD_TO_HD, MakeModelHD::makeItHD);
	}

	public static void makeItHD() {
		ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
		if(currentModelPanel != null){
			EditableModel model = currentModelPanel.getModel();

			for (Geoset geo : model.getGeosets()) {
				geo.makeHd();
			}
			for (Material m : model.getMaterials()) {
				m.makeHD();
			}
		}
	}


	public static void makeItHD2(EditableModel model) {
		for (Geoset geo : model.getGeosets()) {
			List<GeosetVertex> vertices = geo.getVertices();
			for (GeosetVertex gv : vertices) {
				Vec3 normal = gv.getNormal();
				if (normal != null) {
					gv.initV900();
					gv.setTangent(normal, 1);
				}
				int bones = Math.min(4, gv.getBones().size());
				short weight = (short) (255 / bones);
				for (int i = 0; i < bones; i++) {
					if (i == 0) {
						gv.setSkinBone(gv.getBones().get(i), (short) (weight + (255 % bones)), i);
					} else {
						gv.setSkinBone(gv.getBones().get(i), weight, i);

					}
				}
			}
		}
		for (Material m : model.getMaterials()) {
			m.makeHD();
		}
	}
}
