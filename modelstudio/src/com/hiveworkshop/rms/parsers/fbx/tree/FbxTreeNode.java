package com.hiveworkshop.rms.parsers.fbx.tree;

import java.util.ArrayList;
import java.util.List;

public class FbxTreeNode {
    public String name = "";
    public List<FbxTreeProperty<?>> properties = new ArrayList<>();
    public List<FbxTreeNode> children = new ArrayList<>();
}
