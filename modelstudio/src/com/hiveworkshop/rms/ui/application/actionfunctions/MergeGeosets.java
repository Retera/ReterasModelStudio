package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.MergeGeosetsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import com.hiveworkshop.rms.util.uiFactories.FontHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class MergeGeosets extends ActionFunction {
	public MergeGeosets() {
		super(TextKey.MERGE_GEOSETS, MergeGeosets::mergeGeosetActionRes, "control T");
	}
	public static void mergeGeosetActionRes(ModelHandler modelHandler) {
		if (modelHandler != null) {
			MergeGeosetsPanel geosetChoosingPanel = new MergeGeosetsPanel(modelHandler);
			JScrollPane scrollPane = new JScrollPane(geosetChoosingPanel);
			scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);

			int option = JOptionPane.showConfirmDialog(
					ProgramGlobals.getMainPanel(),
					scrollPane,
					"Merge Geoset into Geoset", JOptionPane.OK_CANCEL_OPTION);

			if (option == JOptionPane.OK_OPTION) {
				Geoset recGeoset = geosetChoosingPanel.getRecGeo();
				Set<Geoset> donGeos = geosetChoosingPanel.getDonGeos();
				if (recGeoset == null) {
					recGeoset = donGeos.stream().findFirst().orElse(null);
				}
				donGeos.remove(recGeoset);
				if (!donGeos.isEmpty()) {
					merge(modelHandler, recGeoset, donGeos);
				}
			}
		}
	}

	private static void merge(ModelHandler modelHandler, Geoset recGeo, Collection<Geoset> donGeos) {
		List<UndoAction> undoActions = new ArrayList<>();
		int removedGeos = 0;
		EditableModel model = modelHandler.getModel();
		for (Geoset geoset : donGeos) {
			undoActions.add(new MergeGeosetsAction(recGeo, geoset, model.getGeosetId(geoset) - removedGeos, model, null));
			removedGeos++;
		}
		String donGeoName = donGeos.size() == 1 ?
				("Geoset #" + model.getGeosetId(donGeos.stream().findFirst().get()))
				: (donGeos.size() + " Geosets");
		String actionName = "Merge " + donGeoName + " into Geoset #" + model.getGeosetId(recGeo);
		modelHandler.getUndoManager().pushAction(new CompoundAction(actionName, undoActions,
				ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
	}

	private static class MergeGeosetsPanel extends JPanel {
		private final Map<Geoset, JRadioButton> radioButtonMap;
		private final TreeSet<Geoset> donGeos;
		private Geoset recGeo;

		MergeGeosetsPanel(ModelHandler modelHandler) {
			super(new MigLayout("ins 0, fill", "[grow][grow]", "[grow]"));
			EditableModel model = modelHandler.getModel();

			SmartButtonGroup recGeosetGroup = new SmartButtonGroup("Destination");
			recGeosetGroup.addPanelConst("hidemode 3");

			JPanel donGeoPanel = new JPanel(new MigLayout(""));
			donGeoPanel.setBorder(BorderFactory.createTitledBorder("Geosets to merge"));
			donGeos = new TreeSet<>(Comparator.comparingInt(model::getGeosetId));
			radioButtonMap = new LinkedHashMap<>();

			for (Geoset geoset : model.getGeosets()) {
				JRadioButton radioButton = recGeosetGroup.addJRadioButton(geoset.getName(), e -> recGeo = geoset);
				radioButton.setVisible(false);
				radioButtonMap.put(geoset, radioButton);

				JCheckBox comp = CheckBox.create(geoset.getName(), false, b -> setImp(geoset, b));
				if (geoset.getMaterial() != null) {
					String tooltipText = "Material: #" + model.computeMaterialID(geoset.getMaterial()) + " " + geoset.getMaterial().getName();
					radioButton.setToolTipText(tooltipText);
					comp.setToolTipText(tooltipText);
				}
				donGeoPanel.add(comp, "wrap");
			}

			JLabel tempArrowLabel = new JLabel("\u2794");
			FontHelper.set(tempArrowLabel, Font.BOLD, 20f);

			add(donGeoPanel, "growx");
			add(recGeosetGroup.getButtonPanel(), "growx, wrap");
		}

		Geoset getRecGeo() {
			return recGeo;
		}

		Set<Geoset> getDonGeos() {
			return donGeos;
		}

		private void setImp(Geoset geoset, boolean imp) {
			JRadioButton radioButton = radioButtonMap.get(geoset);
			radioButton.setVisible(imp);
			if (imp) {
				donGeos.add(geoset);
			} else {
				donGeos.remove(geoset);
				recGeo = recGeo == geoset ? null : recGeo;
			}
			if (recGeo == null && !donGeos.isEmpty()) {
				recGeo = donGeos.first();
				radioButtonMap.get(recGeo).setSelected(true);
			}
			repaint();
		}
	}
}
