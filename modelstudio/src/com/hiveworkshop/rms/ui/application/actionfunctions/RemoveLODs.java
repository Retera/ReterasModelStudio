package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class RemoveLODs extends ActionFunction {
	public RemoveLODs(){
		super(TextKey.REMOVE_LODS, RemoveLODs::removeLoDs);
	}

	public static void removeLoDs() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new JLabel("Choose LoDs to remove"), "wrap");
			EditableModel model = modelPanel.getModel();

			TreeSet<Integer> lodSet = new TreeSet<>();
			for(Geoset geoset : model.getGeosets()){
				lodSet.add(geoset.getLevelOfDetail());
			}

			TreeMap<Integer, Boolean> lodsToRemove = new TreeMap<>();
			for(int lod : lodSet){
				lodsToRemove.put(lod, false);
				JCheckBox lodBox = new JCheckBox("LoD " + lod);
				lodBox.addActionListener(e -> lodsToRemove.put(lod, lodBox.isSelected()));
				panel.add(lodBox, "wrap");
			}


			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, "Remove LoDs", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				removeLoDGeoset(modelPanel.getModelHandler(), lodsToRemove);
			}
		}
		ProgramGlobals.getMainPanel().repaint();
	}

	public static void removeLoDGeoset(ModelHandler modelHandler, Map<Integer, Boolean> lodsToRemove) {
		EditableModel model = modelHandler.getModel();
		List<Geoset> lodGeosToRemove = new ArrayList<>();
		for (Geoset geo : model.getGeosets()) {
			if (lodsToRemove.get(geo.getLevelOfDetail())){
				lodGeosToRemove.add(geo);
			}
		}
		if (model.getGeosets().size() > lodGeosToRemove.size()) {
			StringBuilder lods = new StringBuilder("[");
			for(Integer lod : lodsToRemove.keySet()){
				if(lodsToRemove.get(lod)){
					lods.append(lod).append(", ");
				}
			}
			lods.delete(lods.length()-2, lods.length()).append("]");
			DeleteGeosetAction deleteGeosetAction = new DeleteGeosetAction(model, lodGeosToRemove, ModelStructureChangeListener.changeListener);
			CompoundAction deletActions = new CompoundAction("Delete LoD=" + lods + " geosets", deleteGeosetAction);
			modelHandler.getUndoManager().pushAction(deletActions.redo());
		}
	}

}
