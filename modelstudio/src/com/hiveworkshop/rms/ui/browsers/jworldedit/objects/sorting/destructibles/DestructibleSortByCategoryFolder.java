package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.destructibles;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameDoodadComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public final class DestructibleSortByCategoryFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final Comparator<MutableGameObject> NAME_COMPARATOR = new MutableGameDoodadComparator();
	private static final War3ID DEST_CATEGORY = War3ID.fromString("bcat");
	private final Map<String, BottomLevelCategoryFolder> itemClassToTreeNode;
	private final List<BottomLevelCategoryFolder> itemClassesList;

	public DestructibleSortByCategoryFolder(final String displayName) {
		super(displayName);
		final DataTable unitEditorData = DataTable.getWorldEditorData();
		final Element itemClasses = unitEditorData.get("DestructibleCategories");
		itemClassToTreeNode = new LinkedHashMap<>();
		itemClassesList = new ArrayList<>();
		for (final String key : itemClasses.keySet()) {
			final BottomLevelCategoryFolder classFolder = new BottomLevelCategoryFolder(
					WEString.getString(itemClasses.getField(key).split(",")[0]), NAME_COMPARATOR);
			itemClassToTreeNode.put(key, classFolder);
			itemClassesList.add(classFolder);
		}
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		final String itemClass = object.getFieldAsString(DEST_CATEGORY, 0);
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
