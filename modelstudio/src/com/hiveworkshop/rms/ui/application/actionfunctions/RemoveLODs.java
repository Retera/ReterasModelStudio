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
import java.util.ArrayList;
import java.util.List;

public class RemoveLODs extends ActionFunction {
	public RemoveLODs(){
		super(TextKey.REMOVE_LODS, () -> removeLoDs());
	}

	public static void removeLoDs() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new JLabel("LoD to remove"));
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, -2, 10, 1));
			panel.add(spinner, "wrap");
			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, "Remove LoDs", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				removeLoDGeoset(modelPanel.getModelHandler(), (int) spinner.getValue());
			}
		}
		ProgramGlobals.getMainPanel().repaint();
	}

	public static void removeLoDGeoset(ModelHandler modelHandler, int lodToRemove) {
		EditableModel model = modelHandler.getModel();
		List<Geoset> lodGeosToRemove = new ArrayList<>();
		for (Geoset geo : model.getGeosets()) {
			if (geo.getLevelOfDetail() == lodToRemove) {
				lodGeosToRemove.add(geo);
			}
		}
		if (model.getGeosets().size() > lodGeosToRemove.size()) {
			DeleteGeosetAction deleteGeosetAction = new DeleteGeosetAction(model, lodGeosToRemove, ModelStructureChangeListener.changeListener);
			CompoundAction deletActions = new CompoundAction("Delete LoD=" + lodToRemove + " geosets", deleteGeosetAction);
			modelHandler.getUndoManager().pushAction(deletActions.redo());
		}
	}
}
