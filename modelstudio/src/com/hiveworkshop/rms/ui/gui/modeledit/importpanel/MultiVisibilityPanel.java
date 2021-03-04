package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

class MultiVisibilityPanel extends VisibilityPanel implements ChangeListener {
	boolean oldVal = true;
	ImportPanel impPanel;

	public MultiVisibilityPanel(final DefaultComboBoxModel<Object> oldSources, final DefaultComboBoxModel<Object> newSources,
	                            final VisShellBoxCellRenderer renderer) {
		setLayout(new MigLayout("gap 0"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		oldAnimsLabel = new JLabel("Existing animation visibility from: ");

		oldSourcesBox = new JComboBox<>(oldSources);
		oldSourcesBox.setEditable(false);
		oldSourcesBox.setMaximumSize(new Dimension(1000, 25));
		oldSourcesBox.setRenderer(renderer);
		oldSourcesBox.addItemListener(e -> getImportPanel().mht.setVisGroupItemOld(oldSourcesBox.getSelectedItem()));

		newAnimsLabel = new JLabel("Imported animation visibility from: ");

		newSourcesBox = new JComboBox<>(newSources);
		newSourcesBox.setEditable(false);
		newSourcesBox.setMaximumSize(new Dimension(1000, 25));
		newSourcesBox.setRenderer(renderer);
		newSourcesBox.addItemListener(e -> getImportPanel().mht.setVisGroupItemNew(newSourcesBox.getSelectedItem()));

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);
		favorOld.addChangeListener(this);

		add(title, "cell 0 0, spanx, align center, wrap");
		add(oldAnimsLabel, "cell 0 1");
		add(oldSourcesBox, "cell 1 1");
		add(newAnimsLabel, "cell 0 2");
		add(newSourcesBox, "cell 1 2");
		add(favorOld, "cell 0 3");
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (favorOld.isSelected() != oldVal) {
			getImportPanel().mht.setVisGroupSelected(favorOld.isSelected());
			oldVal = favorOld.isSelected();
		}
	}

	public void setMultipleOld() {
		oldSourcesBox.setEditable(true);
		oldSourcesBox.setSelectedItem("Multiple selected");
		oldSourcesBox.setEditable(false);
	}

	public void setMultipleNew() {
		newSourcesBox.setEditable(true);
		newSourcesBox.setSelectedItem("Multiple selected");
		newSourcesBox.setEditable(false);
	}

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while ((temp != null) && (temp.getClass() != ImportPanel.class)) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}
