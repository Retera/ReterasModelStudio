package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DestructableTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;


public class BetterDestructibleModelSelector extends BetterSelector {
	private TwiComboBox<Integer> variantBox;
	private ArrayList<Integer> variants;
	private Integer variant = 0;

	public BetterDestructibleModelSelector(UnitEditorSettings unitEditorSettings) {
		super(new DestructableTabTreeBrowserBuilder(), unitEditorSettings, WE_Field.DESTR_FILE.getId(), WE_Field.DESTR_VARIATIONS.getId());
	}

	protected JPanel getRightPanel() {
		JPanel rightPanel = new JPanel(new MigLayout("fill, ins 0", "", ""));
		rightPanel.add(viewportPanel, "growx, growy, spanx, wrap");
		variants = new ArrayList<>();
		variantBox = new TwiComboBox<>(variants, 10000000);
		variantBox.addOnSelectItemListener(this::selectVariant);
		variantBox.addMouseWheelListener(e -> variantBox.incIndex(e.getWheelRotation()));
		rightPanel.add(variantBox);
		rightPanel.add(animationChooser);
		return rightPanel;
	}

	protected void loadUnitPreview() {
		variants.clear();
		int numberOfVariations = currentUnit.getFieldAsInteger(WE_Field.DESTR_VARIATIONS.getId(), 0);
		for (int i = 0; i < numberOfVariations; i++) {
			variants.add(i + 1);
		}
		variantBox.selectFirst();
		variantBox.setEnabled(1 < numberOfVariations);
		selectVariant(1);
	}

	protected void selectVariant(Integer selected) {
		variant = selected == null ? 0 : selected - 1;
		if (currentUnit != null) {
			String filepath = getFilePath(currentUnit, (variant));
			String gameObjectName = currentUnit.getName();
			openModel(filepath, gameObjectName);
		}
	}

	public String getCurrentFilePath() {
		if (currentUnit != null) {
			return getFilePath(currentUnit, variant);
		} else {
			return null;
		}
	}
}
