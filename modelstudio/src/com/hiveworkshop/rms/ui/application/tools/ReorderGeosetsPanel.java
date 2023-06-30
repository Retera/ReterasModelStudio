package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.ChangeGeosetIndexAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListRenderer;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReorderGeosetsPanel extends JPanel {

	private final TwiList<Geoset> geosetList;
	private final ModelHandler modelHandler;
	private final EditableModel model ;

	public ReorderGeosetsPanel(ModelHandler modelHandler){
		super(new MigLayout("gap 0", "[grow][][grow]", "[grow][align center]"));
		this.modelHandler = modelHandler;
		model = modelHandler.getModel();
		geosetList = new TwiList<>(new ArrayList<>(model.getGeosets()));

		add(getAnimListPanel());
		add(getArrowPanel(),"wrap");
		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> applyOrder());
		add(apply, "wrap, spanx, align center");
	}

	public static void showPanel(JComponent parent, ModelHandler modelHandler) {
		ReorderGeosetsPanel geosetsPanel = new ReorderGeosetsPanel(modelHandler);
		FramePopup.show(geosetsPanel, parent, "Reorder Geosets");
	}


	private JPanel getArrowPanel() {
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0", "[]5", "[align center]16[align center]"));

		JButton moveUp = new JButton(RMSIcons.moveUpIcon);
		moveUp.addActionListener(e -> moveUp());
		arrowPanel.add(moveUp, "wrap");

		JButton moveDown = new JButton(RMSIcons.moveDownIcon);
		moveDown.addActionListener(e -> moveDown());
		arrowPanel.add(moveDown, "wrap");
		return arrowPanel;
	}

	private JPanel getAnimListPanel() {
		JPanel listPanel = new JPanel(new MigLayout("gap 0, ins 0", "[grow, align center]", "[][grow][]"));
		geosetList.setCellRenderer(new GeosetListRenderer(model, 64));
		JScrollPane geoPane = new JScrollPane(geosetList);
		geoPane.setPreferredSize(new Dimension(400, 500));
		listPanel.add(geoPane, "wrap");
		return listPanel;
	}


	private void moveDown() {
		final int[] indices = geosetList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			for (int i = indices.length - 1; 0 <= i; i--) {
				if(indices[i]+1<geosetList.listSize() && (indices[i] + 1 <= geosetList.listSize()-indices.length+i)){
					geosetList.moveElement(indices[i], 1);
					indices[i] += 1;
				}
			}
			geosetList.setSelectedIndices(indices);
		}
	}

	private void moveUp() {
		final int[] indices = geosetList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			for (int i = 0; i < indices.length; i++) {
				if(i <= indices[i] - 1) {
					geosetList.moveElement(indices[i], -1);
					indices[i] -= 1;
				}
			}
			geosetList.setSelectedIndices(indices);
		}
	}

	private void applyOrder() {
		List<UndoAction> undoActions = new ArrayList<>();
		for (int i = 0; i < geosetList.listSize(); i++) {
			Geoset geoset = geosetList.get(i);
			if (model.getGeosetId(geoset) != i) {
				undoActions.add(new ChangeGeosetIndexAction(model, geoset, i, null));
			}
		}
		if (!undoActions.isEmpty()) {
			UndoAction action = new CompoundAction("Reorder Geosets", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}
}
