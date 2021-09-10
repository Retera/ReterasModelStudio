package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ImportGeoset extends ActionFunction {
	public ImportGeoset(){
		super(TextKey.IMP_GEOSET_INTO_GEOSET, () -> mergeGeosetActionRes());
	}

	public static void mergeGeosetActionRes() {
		FileDialog fileDialog = new FileDialog();
//
		EditableModel current = ProgramGlobals.getCurrentModelPanel().getModel();
		EditableModel geoSource = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);

		if (geoSource != null) {
			boolean going = true;
			Geoset host = null;
			while (going) {
				String s = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
						"Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
				try {
					int x = Integer.parseInt(s);
					if ((x >= 1) && (x <= current.getGeosetsSize())) {
						host = current.getGeoset(x - 1);
						going = false;
					}
				} catch (final NumberFormatException ignored) {

				}
			}
			Geoset newGeoset = null;
			going = true;
			while (going) {
				String s = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(), "Geoset to Import: (1 to " + geoSource.getGeosetsSize() + ")");
				try {
					int x = Integer.parseInt(s);
					if ((x >= 1) && x <= geoSource.getGeosetsSize()) {
						newGeoset = geoSource.getGeoset(x - 1);
						going = false;
					}
				} catch (final NumberFormatException ignored) {

				}
			}
			updateToObjects(newGeoset, current);
			System.out.println("putting " + newGeoset.numUVLayers() + " into a nice " + host.numUVLayers());
			for (int i = 0; i < newGeoset.numVerteces(); i++) {
				GeosetVertex ver = newGeoset.getVertex(i);
				host.add(ver);
				ver.setGeoset(host);// geoset = host;
				// for( int z = 0; z < host.n.numUVLayers(); z++ ){
				// host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));}
			}
			for (int i = 0; i < newGeoset.numTriangles(); i++) {
				Triangle tri = newGeoset.getTriangle(i);
				host.add(tri);
				tri.setGeoRef(host);
			}
		}
	}

	public static void updateToObjects(Geoset geoset, final EditableModel model) {
		// upload the temporary UVLayer and Matrix objects into the vertices themselves
		System.out.println(geoset + ", " + model.getName());
		Map<String, IdObject> nameBoneMap = new HashMap<>();
		for(Bone bone : model.getBones()){
			nameBoneMap.put(bone.getName(), bone);
		}
		Map<IdObject, IdObject> boneToBoneMap = new HashMap<>();
		geoset.reMakeMatrixList();
		for (final Matrix m : geoset.getMatrices()) {
			System.out.println(m.size());
			for (Bone bone : m.getBones()){
				boneToBoneMap.put(bone, nameBoneMap.get(bone.getName()));
			}
		}

		for (GeosetVertex gv : geoset.getVertices()) {
			if (!(gv.getMatrix().getBones().isEmpty() && ModelUtils.isTangentAndSkinSupported(model.getFormatVersion()))) {
				gv.getMatrix().replaceBones(boneToBoneMap);
			}
//			for (final Triangle triangle : geoset.getTriangles()) {
//				if (triangle.containsRef(gv)) {
//					gv.addTriangle(triangle);
//				}
//				triangle.setGeoset(geoset);
//			}
			gv.setGeoset(geoset);
		}
		geoset.setParentModel(model);
	}
}
