package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.GeosetFactory;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

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
			GeosetFactory.updateToObjects(newGeoset, current);
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
}
