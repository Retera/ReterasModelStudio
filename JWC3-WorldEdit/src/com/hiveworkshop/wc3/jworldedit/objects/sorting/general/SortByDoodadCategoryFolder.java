package com.hiveworkshop.wc3.jworldedit.objects.sorting.general;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.MutableGameDoodadComparator;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class SortByDoodadCategoryFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final Comparator<MutableGameObject> NAME_COMPARATOR = new MutableGameDoodadComparator();
	private final Map<String, BottomLevelCategoryFolder> objectClassToTreeNode;
	private final List<BottomLevelCategoryFolder> objectClassesList;
	private final War3ID metaDataField;

	public SortByDoodadCategoryFolder(final String displayName, final String categoryName,
			final War3ID metaDataField) {
		super(displayName);
		this.metaDataField = metaDataField;
		final DataTable unitEditorData = DataTable.getWorldEditorData();
		final Element itemClasses = unitEditorData.get(categoryName);
		objectClassToTreeNode = new LinkedHashMap<>();
		objectClassesList = new ArrayList<>();
		for (final String key : itemClasses.keySet()) {
			final BottomLevelCategoryFolder classFolder = new BottomLevelCategoryFolder(
					WEString.getString(itemClasses.getField(key).split(",")[0]), NAME_COMPARATOR);
			objectClassToTreeNode.put(key, classFolder);
			objectClassesList.add(classFolder);
		}
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		final String itemClass = object.getFieldAsString(metaDataField, 0);
		if (!objectClassToTreeNode.containsKey(itemClass)) {
			return objectClassesList.get(objectClassesList.size() - 1);
		}
		return objectClassToTreeNode.get(itemClass);
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return objectClassesList.indexOf(childNode);
	}
}
