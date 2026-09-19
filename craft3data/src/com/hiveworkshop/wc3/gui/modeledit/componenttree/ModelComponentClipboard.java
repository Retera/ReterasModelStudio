package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import com.hiveworkshop.wc3.mdl.EditableModel;

/**
 * In-process clipboard for Model tab components.
 * <p>
 * It remembers the original component and the model it came from; every paste
 * makes a fresh deep copy through {@link ModelComponentCopier}, so pasting
 * twice yields two independent components and pasting into another open model
 * remaps references by name.
 */
public final class ModelComponentClipboard {
	private static Object item;
	private static ComponentKind kind;
	private static EditableModel sourceModel;

	private ModelComponentClipboard() {
	}

	public static void set(final Object component, final EditableModel source) {
		item = component;
		kind = ComponentKind.of(component);
		sourceModel = source;
	}

	public static boolean isEmpty() {
		return item == null;
	}

	public static Object getItem() {
		return item;
	}

	public static ComponentKind getKind() {
		return kind;
	}

	public static EditableModel getSourceModel() {
		return sourceModel;
	}

	/** Forget the component if it was the given one (after it is deleted). */
	public static void forgetIfSame(final Object component) {
		if (item == component) {
			item = null;
			kind = null;
			sourceModel = null;
		}
	}
}
