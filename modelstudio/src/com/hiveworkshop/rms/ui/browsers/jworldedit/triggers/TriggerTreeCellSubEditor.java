package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui.TriggerTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerEnvironment;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class TriggerTreeCellSubEditor extends JTextField implements TreeCellEditor {
	private final List<CellEditorListener> listeners = new ArrayList<>();
	private final TriggerEnvironment triggerEnvironment;
	private JTree tree;
	private Object startingValue;

	public TriggerTreeCellSubEditor(final TriggerEnvironment triggerEnvironment) {
		this.triggerEnvironment = triggerEnvironment;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	@Override
	public Object getCellEditorValue() {
		return getText();
	}

	@Override
	public boolean isCellEditable(final EventObject anEvent) {
		return (anEvent == null) || ((anEvent instanceof MouseEvent) && (((MouseEvent) anEvent).isMetaDown()));
	}

	@Override
	public boolean shouldSelectCell(final EventObject anEvent) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		if (startingValue instanceof TriggerTreeNode) {
			final String value = getText();
			for (final char c : value.toCharArray()) {
				if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != ' ') {
					return false;
				}
			}
			for (final TriggerCategory category : triggerEnvironment.getCategories()) {
				for (final Trigger trigger : category.getTriggers()) {
					if (trigger != startingValue && trigger.getName().equals(value)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void cancelCellEditing() {
		setText(startingValue.toString());
	}

	@Override
	public void addCellEditorListener(final CellEditorListener l) {
		listeners.add(l);
	}

	@Override
	public void removeCellEditorListener(final CellEditorListener l) {
		listeners.remove(l);
	}

	@Override
	public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected,
			final boolean expanded, final boolean leaf, final int row) {
		System.out.println(value);
		setText(value.toString());
		startingValue = value;
		return this;
	}

	private void fireEditingStopped(final ChangeEvent changeEvent) {
		for (final CellEditorListener listener : listeners) {
			listener.editingStopped(changeEvent);
		}
	}

}
