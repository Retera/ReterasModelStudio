package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.MergeGeosetsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class MergeGeosets extends ActionFunction {

	private static final int REC = 0;
	private static final int DON = 1;

	public MergeGeosets() {
		super(TextKey.MERGE_GEOSETS, () -> mergeGeosetActionRes2(), "control T");
	}

	public static void mergeGeosetActionRes2() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			JPanel geosetChoosingPanel = new JPanel(new MigLayout("ins 0"));

			geosetChoosingPanel.add(new JLabel("Recieving Geoset"));
			geosetChoosingPanel.add(new JLabel("Geoset to merge into recieving"), "wrap");

			SmartButtonGroup recGeosetGroup = new SmartButtonGroup();
			SmartButtonGroup donGeosetGroup = new SmartButtonGroup();
			Map<Integer, Geoset> geoMap = new HashMap<>();

			for (Geoset geoset : current.getGeosets()) {
				recGeosetGroup.addJRadioButton(geoset.getName(), e -> geoMap.put(REC, geoset));
				donGeosetGroup.addJRadioButton(geoset.getName(), e -> geoMap.put(DON, geoset));
			}

			geosetChoosingPanel.add(recGeosetGroup.getButtonPanel());
			geosetChoosingPanel.add(donGeosetGroup.getButtonPanel());

			JScrollPane scrollPane = new JScrollPane(geosetChoosingPanel);
			scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);

//			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), geosetChoosingPanel, "Merge Geoset into Geoset", JOptionPane.OK_CANCEL_OPTION);
			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), scrollPane, "Merge Geoset into Geoset", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION && geoMap.containsKey(REC) && geoMap.containsKey(DON) && geoMap.get(REC) != geoMap.get(DON)) {
				merge(modelPanel, geoMap);
			}
		}
	}

	private static void merge(ModelPanel modelPanel, Map<Integer, Geoset> geoMap) {
		ModelHandler modelHandler = modelPanel.getModelHandler();
		MergeGeosetsAction action = new MergeGeosetsAction(geoMap.get(REC), geoMap.get(DON), modelHandler.getModelView(), ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}
}
