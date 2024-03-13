package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class RemoveLODs extends ActionFunction {
	public RemoveLODs() {
		super(TextKey.REMOVE_LODS, RemoveLODs::removeLoDs);
	}

	public static void removeLoDs(ModelHandler modelHandler) {
		if (modelHandler != null) {
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new JLabel("Choose LoDs to remove"), "wrap");
			EditableModel model = modelHandler.getModel();

			TreeSet<Integer> lodSet = new TreeSet<>();
			for (Geoset geoset : model.getGeosets()) {
				lodSet.add(geoset.getLevelOfDetail());
			}

			TreeMap<Integer, Boolean> lodsToRemove = new TreeMap<>();
			for (int lod : lodSet) {
				lodsToRemove.put(lod, false);
				panel.add(CheckBox.create("LoD " + lod, b -> lodsToRemove.put(lod, b)), "wrap");
			}

			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, "Remove LoDs", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				removeLoDGeoset(modelHandler, lodsToRemove);
			}
		}
		ProgramGlobals.getMainPanel().repaint();
	}

	public static void removeLoDGeoset(ModelHandler modelHandler, Map<Integer, Boolean> lodsToRemove) {
		EditableModel model = modelHandler.getModel();
		List<Geoset> lodGeosToRemove = model.getGeosets().stream()
				.filter(g -> lodsToRemove.get(g.getLevelOfDetail()))
				.toList();

		int nRem = lodGeosToRemove.size();
		if (!lodGeosToRemove.isEmpty() && nRem < model.getGeosets().size()) {
			DeleteGeosetAction deleteGeosetAction = new DeleteGeosetAction(model, lodGeosToRemove, ModelStructureChangeListener.changeListener);

			String lods = getLodsString(lodsToRemove);
			String name = nRem == 1 ? deleteGeosetAction.actionName() + " (Lod=" + lods + ")" : "Delete " + nRem + " Geosets (LoD=[" + lods + "])";
			modelHandler.getUndoManager().pushAction(new CompoundAction(name, deleteGeosetAction).redo());
		}
	}

	private static String getLodsString(Map<Integer, Boolean> lodsToRemove) {
		String[] lods = lodsToRemove.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(e -> e.getKey().toString())
				.toArray(String[]::new);
		return String.join(", ", lods);
	}

}
