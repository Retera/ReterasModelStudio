package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.parsers.slk.Element;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DoodadSortByCategoryFolder extends AbstractSortingFolderTreeNode {
	private static final String TAG_NAME = "doodClass";
	private final Map<String, BottomLevelCategoryFolder> objectClassToTreeNode = new LinkedHashMap<>();
	private final List<BottomLevelCategoryFolder> objectClassesList = new ArrayList<>();

	public DoodadSortByCategoryFolder(String displayName) {
		super(displayName);
		DataTable unitEditorData = DataTableHolder.getWorldEditorData();
		Element dataEntries = unitEditorData.get("DoodadCategories");
		System.out.println(displayName + " classes: " + dataEntries.keySet().size());
		for (String key : dataEntries.keySet()) {
			BottomLevelCategoryFolder classFolder = new BottomLevelCategoryFolder(WEString.getString(dataEntries.getField(key).split(",")[0]), this::compare);
			objectClassToTreeNode.put(key, classFolder);
			objectClassesList.add(classFolder);
		}
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String itemClass = object.getFieldAsString(WE_Field.DOODAD_CAT.getId(), 0);
		if (!objectClassToTreeNode.containsKey(itemClass)) {
			return objectClassesList.get(objectClassesList.size() - 1);
		}
		return objectClassToTreeNode.get(itemClass);
	}

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
		if (childNode instanceof BottomLevelCategoryFolder) {
			return objectClassesList.indexOf(childNode);
		}
		return -1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
		if (childNode instanceof BottomLevelCategoryFolder) {
			return objectClassesList.indexOf(childNode);
		}
		return -1;
	}

	protected War3ID getWar3ID() {
		return WE_Field.DOODAD_CAT.getId();
	}


	public int compare(final MutableGameObject a, final MutableGameObject b) {
		String a_slkTag = a.readSLKTag(TAG_NAME);
		String b_slkTag = b.readSLKTag(TAG_NAME);
		if (a_slkTag.equals("") && !b_slkTag.equals("")) {
			return 1;
		} else if (b_slkTag.equals("") && !a_slkTag.equals("")) {
			return -1;
		}
		final int comp1 = a_slkTag.compareTo(b_slkTag);
		if (comp1 == 0) {
			return a.getName().compareTo(b.getName());
		}
		return comp1;
	}
}
