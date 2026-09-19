package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import com.hiveworkshop.wc3.mdl.IdObject;

/**
 * What a row of the Model tab tree refers to: a component, a group heading, or
 * neither (header, comment, root...).
 */
public final class ComponentRef {
	private final Object item;
	private final ComponentKind kind;
	private final String groupName;

	private ComponentRef(final Object item, final ComponentKind kind, final String groupName) {
		this.item = item;
		this.kind = kind;
		this.groupName = groupName;
	}

	public static ComponentRef item(final Object item) {
		final ComponentKind kind = ComponentKind.of(item);
		return new ComponentRef(item, kind, kind == null ? null : kind.getGroupName());
	}

	public static ComponentRef group(final String groupName) {
		return new ComponentRef(null, null, groupName);
	}

	public static ComponentRef none() {
		return new ComponentRef(null, null, null);
	}

	public Object getItem() {
		return item;
	}

	/** Null for group rows and unsupported rows. */
	public ComponentKind getKind() {
		return kind;
	}

	/** The group the row is, or belongs to. Null for unsupported rows. */
	public String getGroupName() {
		return groupName;
	}

	public boolean isComponent() {
		return kind != null;
	}

	public boolean isGroup() {
		return (item == null) && (groupName != null);
	}

	public boolean isNode() {
		return (kind != null) && kind.isNode();
	}

	public IdObject asNode() {
		return isNode() ? (IdObject) item : null;
	}

	/** True when a "New" or "Paste" on this row should create nodes. */
	public boolean isInNodesGroup() {
		return ComponentKind.NODES_GROUP.equals(groupName);
	}
}
