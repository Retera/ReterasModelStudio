package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.mesh.MergeGeosetsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class MergeGeosetsPanel extends JPanel {

	public MergeGeosetsPanel(ModelPanel modelPanel) {
		EditableModel model = modelPanel.getModel();
		JPanel geosetChoosingPanel = new JPanel(new MigLayout("ins 0"));

		SmartButtonGroup donGeosetGroup = new SmartButtonGroup();
		SmartButtonGroup recGeosetGroup = new SmartButtonGroup();
		Map<Integer, Geoset> geoMap = new HashMap<>();

		for (Geoset geoset : model.getGeosets()) {
			donGeosetGroup.addJRadioButton(geoset.getName(), e -> geoMap.put(0, geoset));
			recGeosetGroup.addJRadioButton(geoset.getName(), e -> geoMap.put(1, geoset));
		}

		geosetChoosingPanel.add(donGeosetGroup.getButtonPanel());
		geosetChoosingPanel.add(recGeosetGroup.getButtonPanel(), "wrap");
//		JButton mergeButton = new JButton("Merge");
//		mergeButton.addActionListener(e -> merge(modelPanel, geoMap));
	}

	private void merge(ModelPanel modelPanel, Map<Integer, Geoset> geoMap) {
		ModelHandler modelHandler = modelPanel.getModelHandler();
		MergeGeosetsAction action = new MergeGeosetsAction(geoMap.get(0), geoMap.get(1), modelHandler.getModelView(), ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	public static void showPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			MergeGeosetsPanel mergeGeosetsPanel = new MergeGeosetsPanel(modelPanel);
			JScrollPane scrollPane = new JScrollPane(mergeGeosetsPanel);
			scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
//		FramePopup.show(animCopyPanel, parent, geosetAnim.getName());
			FramePopup.show(scrollPane, ProgramGlobals.getMainPanel(), "Merge Geoset into Geoset");

		}

	}

	public static void mergeGeosetActionRes2() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			JPanel geosetChoosingPanel = new JPanel(new MigLayout("ins 0"));

			SmartButtonGroup donGeosetGroup = new SmartButtonGroup();
			SmartButtonGroup recGeosetGroup = new SmartButtonGroup();
			Map<Integer, Geoset> geoMap = new HashMap<>();

			for (Geoset geoset : current.getGeosets()) {
				donGeosetGroup.addJRadioButton(geoset.getName(), e -> geoMap.put(0, geoset));
				recGeosetGroup.addJRadioButton(geoset.getName(), e -> geoMap.put(1, geoset));
			}

			geosetChoosingPanel.add(donGeosetGroup.getButtonPanel());
			geosetChoosingPanel.add(recGeosetGroup.getButtonPanel());

			JScrollPane scrollPane = new JScrollPane(geosetChoosingPanel);
			scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);

//			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), geosetChoosingPanel, "Merge Geoset into Geoset", JOptionPane.OK_CANCEL_OPTION);
			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), scrollPane, "Merge Geoset into Geoset", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION && geoMap.containsKey(0) && geoMap.containsKey(1) && geoMap.get(0) != geoMap.get(1)) {
				ModelHandler modelHandler = modelPanel.getModelHandler();
				MergeGeosetsAction action = new MergeGeosetsAction(geoMap.get(0), geoMap.get(1), modelHandler.getModelView(), ModelStructureChangeListener.changeListener);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}
}
