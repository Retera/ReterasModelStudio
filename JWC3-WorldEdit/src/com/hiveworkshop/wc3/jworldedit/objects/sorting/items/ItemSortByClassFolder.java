package com.hiveworkshop.wc3.jworldedit.objects.sorting.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class ItemSortByClassFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID ITEM_CLASS_FIELD = War3ID.fromString("icla");
	private static final Comparator<MutableGameObject> ITEM_NAME_COMPARATOR = new Comparator<MutableGameObject>() {
		@Override
		public int compare(final MutableGameObject o1, final MutableGameObject o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	private final Map<String, BottomLevelCategoryFolder> itemClassToTreeNode;
	private final List<BottomLevelCategoryFolder> itemClassesList;

	public ItemSortByClassFolder(final String displayName) {
		super(displayName);
		final DataTable unitEditorData = DataTable.getWorldEditorData();
		final Element itemClasses = unitEditorData.get("itemClass");
		final int numClasses = itemClasses.getFieldValue("NumValues");
		itemClassToTreeNode = new LinkedHashMap<>();
		itemClassesList = new ArrayList<>();
		for (int i = 0; i < numClasses; i++) {
			final String typeName = itemClasses.getField(String.format("%2d", i).replace(' ', '0'), 0);
			final String tag = itemClasses.getField(String.format("%2d", i).replace(' ', '0'), 1);
			final BottomLevelCategoryFolder classFolder = new BottomLevelCategoryFolder(WEString.getString(tag),
					ITEM_NAME_COMPARATOR);
			itemClassToTreeNode.put(typeName, classFolder);
			itemClassesList.add(classFolder);
		}
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		final String itemClass = object.getFieldAsString(ITEM_CLASS_FIELD, 0);
		if (!itemClassToTreeNode.containsKey(itemClass)) {
			return itemClassesList.get(itemClassesList.size() - 1);
		}
		return itemClassToTreeNode.get(itemClass);
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return itemClassesList.indexOf(childNode);
	}
}
