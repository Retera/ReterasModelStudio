package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameDoodadComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

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
