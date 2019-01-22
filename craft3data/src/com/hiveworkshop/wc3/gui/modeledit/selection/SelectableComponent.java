package com.hiveworkshop.wc3.gui.modeledit.selection;

public interface SelectableComponent {
	void visit(SelectableComponentVisitor visitor);
}
