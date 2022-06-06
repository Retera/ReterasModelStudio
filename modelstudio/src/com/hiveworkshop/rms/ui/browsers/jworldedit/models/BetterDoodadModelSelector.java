package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import com.hiveworkshop.rms.util.War3ID;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;


public class BetterDoodadModelSelector extends BetterSelector {
	private JComboBox<Integer> variantBox;
	private ArrayList<Integer> variants;

	public BetterDoodadModelSelector(UnitEditorSettings unitEditorSettings) {
		super(new DoodadTabTreeBrowserBuilder(), unitEditorSettings, "dfil", "dvar");
	}

	protected JPanel getRightPanel() {
		JPanel rightPanel = new JPanel(new MigLayout("fill, ins 0", "", ""));
		rightPanel.add(perspDisplayPanel, "growx, growy, wrap");
		variants = new ArrayList<>();
		variantBox = new JComboBox<>(new TwiComboBoxModel<>(variants));
		variantBox.setPrototypeDisplayValue(10000000);
		variantBox.addItemListener(this::chooseVariant);
		variantBox.setEditable(false);
		rightPanel.add(variantBox);
		return rightPanel;
	}

	protected void loadUnitPreview() {
		variants.clear();
		int numberOfVariations = currentUnit.getFieldAsInteger(War3ID.fromString("dvar"), 0);
		for (int i = 0; i < numberOfVariations; i++) {
			variants.add(i + 1);
		}
		variantBox.setEnabled(numberOfVariations > 1);
		if(variantBox.getItemCount()>0){
			variantBox.setSelectedIndex(0);
			openModel(getFilePath(currentUnit, 0), currentUnit.getName());
		}
	}

	protected void chooseVariant(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && currentUnit != null) {
			String filepath = getFilePath(currentUnit, ((Integer) e.getItem()) - 1);
			String gameObjectName = currentUnit.getName();
			openModel(filepath, gameObjectName);
		}
	}
	public String getCurrentFilePath() {
		if(currentUnit != null){
			int variant = variantBox.isEnabled() ? variantBox.getSelectedIndex() : 0;
			return getFilePath(currentUnit, variant);
		} else {
			return null;
		}
	}
}
