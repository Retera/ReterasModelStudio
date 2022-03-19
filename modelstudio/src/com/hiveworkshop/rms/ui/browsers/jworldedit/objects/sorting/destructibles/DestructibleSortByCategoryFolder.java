package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.destructibles;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameDoodadComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.TreeNode;
import java.util.*;

public final class DestructibleSortByCategoryFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final Comparator<MutableGameObject> NAME_COMPARATOR = new MutableGameDoodadComparator();
	private static final War3ID DEST_CATEGORY = War3ID.fromString("bcat");
	private final Map<String, BottomLevelCategoryFolder> itemClassToTreeNode = new LinkedHashMap<>();
	private final List<BottomLevelCategoryFolder> itemClassesList = new ArrayList<>();

	public DestructibleSortByCategoryFolder(String displayName) {
		super(displayName);
		DataTable unitEditorData = DataTableHolder.getWorldEditorData();
		Element itemClasses = unitEditorData.get("DestructibleCategories");
		for (String key : itemClasses.keySet()) {
			BottomLevelCategoryFolder classFolder = new BottomLevelCategoryFolder(WEString.getString(itemClasses.getField(key).split(",")[0]), NAME_COMPARATOR);
			itemClassToTreeNode.put(key, classFolder);
			itemClassesList.add(classFolder);
		}
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String itemClass = object.getFieldAsString(DEST_CATEGORY, 0);
		if (!itemClassToTreeNode.containsKey(itemClass)) {
			return itemClassesList.get(itemClassesList.size() - 1);
		}
		return itemClassToTreeNode.get(itemClass);
	}

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
//		return itemClassesList.indexOf(childNode);

		if (childNode != null) {
			return itemClassesList.indexOf(childNode);
		}
		return -1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
//		return itemClassesList.indexOf(childNode);

		if (childNode != null) {
			return itemClassesList.indexOf(childNode);
		}
		return -1;
	}
}
