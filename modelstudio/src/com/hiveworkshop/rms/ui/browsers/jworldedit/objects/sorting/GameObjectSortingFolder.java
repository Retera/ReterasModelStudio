package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;

public interface GameObjectSortingFolder {
	SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker treeModel);
}
