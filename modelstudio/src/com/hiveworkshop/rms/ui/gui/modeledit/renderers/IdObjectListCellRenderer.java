package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.util.AbstractObject2DThumbnailListCellRenderer;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class IdObjectListCellRenderer extends AbstractObject2DThumbnailListCellRenderer<IdObject> {
	boolean showParent = false;
	IdObject selectedObject;
	boolean showClass = false;
	Set<IdObject> invalidObjects;

	public IdObjectListCellRenderer() {
		super(null, null);
	}

	public IdObjectListCellRenderer(EditableModel model, EditableModel other) {
		super(model, other);
	}

	public IdObjectListCellRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public IdObjectListCellRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public IdObjectListCellRenderer setSelectedObjectShell(IdObject objectShell) {
		selectedObject = objectShell;
		return this;
	}

	public IdObjectListCellRenderer setInvalidObjects(Set<IdObject> invalidObjects) {
		this.invalidObjects = invalidObjects;
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);
//		setIcon(ImportPanel.redIcon);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		if (value instanceof IdObject) {
			if(invalidObjects != null && invalidObjects.contains(value)){
				bg = otherOwnerBgCol;
				fg = otherOwnerFgCol;
			}
			String name = ((IdObject) value).getName();
			String stringToReturn = "";
//			if (modelName != null && !modelName.equals("")) {
//				stringToReturn += modelName + ": ";
//			}
			if (showClass) {
				stringToReturn += "(" + value.getClass().getSimpleName() + ") ";
			}
			stringToReturn += name;
			if (showParent) {
				if (((IdObject) value).getParent() == null) {
					stringToReturn += "; (no parent)";
				} else {
					stringToReturn += "; " + ((IdObject) value).getParent().getName();
				}
			}
			setText(stringToReturn);
		} else if (value == null) {
			setText("None");
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
