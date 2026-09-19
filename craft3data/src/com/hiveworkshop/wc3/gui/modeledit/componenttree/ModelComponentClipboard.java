package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hiveworkshop.wc3.mdl.EditableModel;

/**
 * In-process clipboard for Model tab components.
 * <p>
 * It remembers the original components and the model they came from; every
 * paste makes fresh deep copies through {@link ModelComponentCopier}, so
 * pasting twice yields two independent sets and pasting into another open
 * model remaps references by name. A cut component stays on the clipboard
 * after it is deleted, because the copy is made from the detached original.
 */
public final class ModelComponentClipboard {
	private static List<Object> items = Collections.emptyList();
	private static EditableModel sourceModel;

	private ModelComponentClipboard() {
	}

	public static void set(final Object component, final EditableModel source) {
		set(Collections.singletonList(component), source);
	}

	public static void set(final List<Object> components, final EditableModel source) {
		items = new ArrayList<>(components);
		sourceModel = source;
	}

	public static boolean isEmpty() {
		return items.isEmpty();
	}

	/** The first component; use {@link #getItems()} for the whole selection. */
	public static Object getItem() {
		return items.isEmpty() ? null : items.get(0);
	}

	public static List<Object> getItems() {
		return Collections.unmodifiableList(items);
	}

	public static ComponentKind getKind() {
		return items.isEmpty() ? null : ComponentKind.of(items.get(0));
	}

	public static EditableModel getSourceModel() {
		return sourceModel;
	}

	public static void clear() {
		items = Collections.emptyList();
		sourceModel = null;
	}
}
