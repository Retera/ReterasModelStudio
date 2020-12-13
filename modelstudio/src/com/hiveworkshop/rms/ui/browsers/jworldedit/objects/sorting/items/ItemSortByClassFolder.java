package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.items;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public final class ItemSortByClassFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID ITEM_CLASS_FIELD = War3ID.fromString("icla");
	private static final Comparator<MutableGameObject> ITEM_NAME_COMPARATOR = Comparator.comparing(MutableGameObject::getName);
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
