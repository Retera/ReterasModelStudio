package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

public class ObjectShellListCellRenderer extends AbstractSnapshottingListCellRenderer2D<IdObjectShell<?>> {
	boolean showParent = false;
	IdObjectShell<?> selectedObject;
	boolean showClass = false;

	public ObjectShellListCellRenderer() {
		super(null, null);
	}

	public ObjectShellListCellRenderer(EditableModel model, EditableModel other) {
		super(model, other);
	}

	public ObjectShellListCellRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public ObjectShellListCellRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public ObjectShellListCellRenderer setSelectedObjectShell(IdObjectShell<?> objectShell) {
		selectedObject = objectShell;
		return this;
	}

	@Override
	protected boolean isFromDonating(IdObjectShell<?> value) {
		if (value != null) {
			return value.isFromDonating();
		}
		return false;
	}

	@Override
	protected boolean isFromReceiving(IdObjectShell<?> value) {
		if (value != null) {
			return !value.isFromDonating();
		}
		return false;
	}

	@Override
	protected IdObjectShell<?> valueToType(Object value) {
		return (IdObjectShell<?>) value;
	}

	@Override
	protected Vec3 getRenderVertex(IdObjectShell<?> value) {
		return value.getIdObject().getPivotPoint();
	}

	@Override
	protected boolean contains(EditableModel model, IdObjectShell<?> object) {
		if (model != null) {
			return model.contains(object.getIdObject());
		}
		return false;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);
//		setIcon(ImportPanel.redIcon);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		if (value instanceof IdObjectShell) {
			setText(((IdObjectShell<?>) value).toString(showClass, showParent));
			if (!((IdObjectShell<?>) value).getShouldImport()) {
				bg = Vec3.getProd(bg, otherOwnerBgCol).normalize().scale(160);
				fg = Vec3.getProd(bg, otherOwnerFgCol).normalize().scale(60);
			}
		} else {
			setText(value.toString());
		}

		if (isSel) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}

		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());

		return this;
	}
}
