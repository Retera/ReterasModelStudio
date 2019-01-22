package com.hiveworkshop.wc3.jworldedit.objects.sorting;

import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;

public interface GameObjectSortingFolder {
	SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker treeModel);
}
