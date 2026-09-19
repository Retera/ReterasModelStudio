package com.hiveworkshop.wc3.gui.modeledit.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;

import net.miginfocom.swing.MigLayout;

/**
 * Two column form used by every Add wizard, plus the shared controls (node
 * chooser, xyz row, spinners).
 */
public class WizardForm extends JPanel {
	public WizardForm() {
		super(new MigLayout("fillx, wrap 2, insets 8", "[right]8[grow,fill,300::]", ""));
	}

	public <T extends JComponent> T row(final String label, final T component) {
		add(new JLabel(label));
		add(component, "growx");
		return component;
	}

	public <T extends JComponent> T row(final String label, final T component, final String constraints) {
		add(new JLabel(label));
		add(component, constraints);
		return component;
	}

	public <T extends JComponent> T wide(final T component) {
		add(component, "span 2, growx");
		return component;
	}

	public <T extends JComponent> T wide(final T component, final String constraints) {
		add(component, constraints);
		return component;
	}

	public JTextField text(final String label, final String initial) {
		return row(label, new JTextField(initial, 24));
	}

	public JCheckBox check(final String label, final boolean initial) {
		final JCheckBox box = new JCheckBox(label, initial);
		add(box, "span 2, alignx left");
		return box;
	}

	public JSpinner intSpinner(final String label, final int value, final int min, final int max, final int step) {
		return row(label, new JSpinner(new SpinnerNumberModel(value, min, max, step)), "wmax 160, alignx left");
	}

	public JSpinner doubleSpinner(final String label, final double value, final double step) {
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, -1.0e9, 1.0e9, step));
		return row(label, spinner, "wmax 160, alignx left");
	}

	/** Three spinners on one row; read back with {@link #vertex(JSpinner[])}. */
	public JSpinner[] xyz(final String label, final Vertex initial) {
		final JPanel rowPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill][grow,fill][grow,fill]", ""));
		final JSpinner[] spinners = new JSpinner[3];
		final double[] values = { initial == null ? 0 : initial.x, initial == null ? 0 : initial.y,
				initial == null ? 0 : initial.z };
		for (int i = 0; i < 3; i++) {
			spinners[i] = new JSpinner(new SpinnerNumberModel(values[i], -1.0e9, 1.0e9, 1.0));
			rowPanel.add(spinners[i]);
		}
		row(label, rowPanel);
		return spinners;
	}

	public static Vertex vertex(final JSpinner[] spinners) {
		return new Vertex(number(spinners[0]), number(spinners[1]), number(spinners[2]));
	}

	public static double number(final JSpinner spinner) {
		return ((Number) spinner.getValue()).doubleValue();
	}

	public static int integer(final JSpinner spinner) {
		return ((Number) spinner.getValue()).intValue();
	}

	/** Combo of every node in the model with a leading "(none)" entry. */
	public JComboBox<IdObject> nodeChooser(final String label, final EditableModel model, final IdObject initial) {
		final List<IdObject> options = new ArrayList<>();
		options.add(null);
		options.addAll(model.getIdObjects());
		final JComboBox<IdObject> combo = new JComboBox<>(options.toArray(new IdObject[0]));
		combo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				final String text = value == null ? "(none)"
						: value.getClass().getSimpleName() + " \"" + ((IdObject) value).getName() + "\"";
				return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
			}
		});
		combo.setSelectedItem(initial);
		combo.setMaximumRowCount(20);
		return row(label, combo);
	}

	public static JComboBox<String> editableCombo(final String initial, final String... options) {
		final JComboBox<String> combo = new JComboBox<>(options);
		combo.setEditable(true);
		combo.setSelectedItem(initial);
		combo.setMaximumRowCount(20);
		return combo;
	}

	public static String textOf(final JComboBox<String> combo) {
		final Object item = combo.getEditor().getItem();
		return item == null ? "" : item.toString().trim();
	}

	public boolean showOkCancel(final Component parent, final String title) {
		return JOptionPane.showConfirmDialog(parent, this, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
	}
}
