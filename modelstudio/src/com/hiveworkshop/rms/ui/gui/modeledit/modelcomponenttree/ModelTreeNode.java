package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.function.Supplier;

public class ModelTreeNode <T> extends DefaultMutableTreeNode {
	ChoosableDisplayElement<T> displayElement;

	public ModelTreeNode(DisplayElementType type, ModelView modelView, T item) {
		this(new ChoosableDisplayElement<>(type, modelView, item, null, -1));
	}

	public ModelTreeNode(DisplayElementType type, ModelView modelView, T item, int id) {
		this(new ChoosableDisplayElement<>(type, modelView, item, null, id));
	}

	public ModelTreeNode(DisplayElementType type, ModelView modelView, T item, Supplier<String> namingFunk) {
		this(new ChoosableDisplayElement<>(type, modelView, item, namingFunk, -1));
	}

	public ModelTreeNode(DisplayElementType type, ModelView modelView, T item, Supplier<String> namingFunk, int id) {
		this(new ChoosableDisplayElement<>(type, modelView, item, namingFunk, id));
	}

	public ModelTreeNode(ChoosableDisplayElement<T> displayElement) {
		super(displayElement);
		this.displayElement = displayElement;
	}

	public ChoosableDisplayElement<T> getDisplayElement() {
		return displayElement;
	}


	public boolean hasSameItem(ModelTreeNode<?> other) {
		return displayElement.hasSameItem(other.displayElement);
	}

	public ImageIcon getIcon(boolean expanded) {
		return displayElement.getIcon(expanded);
	}

	public T getItem() {
		return displayElement.getItem();
	}


	public ModelTreeNode<T> select(ComponentsPanel componentsPanel) {
		displayElement.select(componentsPanel);
		return this;
	}

	public ModelTreeNode<?> findChildWithSameItem(ModelTreeNode<?> other) {
		if (other != null) {
			for (int i = 0; i < getChildCount(); i++){
				TreeNode treeNode = getChildAt(i);
				if (treeNode instanceof ModelTreeNode<?> node && node.hasSameItem(other)) {
					return node;
				}
			}
		}
		return null;
	}

	public ModelTreeNode<?> findChildWithSameItem(ChoosableDisplayElement<?> other) {
		if (other != null) {
			for (int i = 0; i < getChildCount(); i++){
				TreeNode treeNode = getChildAt(i);
				if (treeNode instanceof ModelTreeNode<?> node && node.displayElement.hasSameItem(other)) {
					return node;
				}
			}
		}
		return null;
	}
}
