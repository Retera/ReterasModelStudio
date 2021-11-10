package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameDoodadComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public final class SortByDoodadCategoryFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final Comparator<MutableGameObject> NAME_COMPARATOR = new MutableGameDoodadComparator();
	private final Map<String, BottomLevelCategoryFolder> objectClassToTreeNode = new LinkedHashMap<>();
	private final List<BottomLevelCategoryFolder> objectClassesList = new ArrayList<>();
	private final War3ID metaDataField;

	public SortByDoodadCategoryFolder(String displayName, String categoryName, War3ID metaDataField) {
		super(displayName);
		this.metaDataField = metaDataField;
		DataTable unitEditorData = DataTableHolder.getWorldEditorData();
		Element itemClasses = unitEditorData.get(categoryName);
		for (String key : itemClasses.keySet()) {
			BottomLevelCategoryFolder classFolder = new BottomLevelCategoryFolder(WEString.getString(itemClasses.getField(key).split(",")[0]), NAME_COMPARATOR);
			objectClassToTreeNode.put(key, classFolder);
			objectClassesList.add(classFolder);
		}
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String itemClass = object.getFieldAsString(metaDataField, 0);
		if (!objectClassToTreeNode.containsKey(itemClass)) {
			return objectClassesList.get(objectClassesList.size() - 1);
		}
		return objectClassToTreeNode.get(itemClass);
	}

	@Override
	public int getSortIndex(DefaultMutableTreeNode childNode) {
		return objectClassesList.indexOf(childNode);
	}
}
