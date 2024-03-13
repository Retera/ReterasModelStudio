package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.tools.ModelIconHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.IdObjectListCellRenderer;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BoneChainMapWizard {
	private final String[] typeStrings = new String[]{"bone", "geo", "helper", "node"};
	private final Helper prototype = new Helper("Prototype prototype");
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private final JComponent parent;
	private final EditableModel mapToModel;
	private final EditableModel mapFromModel;
//	private final LinkedHashMap<IdObject, IdObject> boneChainMap = new LinkedHashMap<>();
//	private final NodeDepthMap nodeDepthMap = new NodeDepthMap(0);
	private int currDepth = 0;
	private int depth = 10000;

	private final JPanel panel = new JPanel(new MigLayout("ins 0"));
	private final JButton backButton = new JButton("BACK");
	private final JButton nextButton = new JButton("NEXT");
	private final JPanel objectMappingPanel = new JPanel(new MigLayout(""));

	NodeMapHelper nodeMapHelper = new NodeMapHelper();
//	private boolean presentParent;
//	private boolean checkHelperChilds;
//	private boolean isGeometryMode;
//	private boolean allowTopLevelMapping;
//	private boolean autoValidateChain = true;

//	private Set<Class<?>> classSet;

	private final IdObjectListCellRenderer mapToModelRenderer;


	public BoneChainMapWizard(JComponent parent, EditableModel mapToModel, EditableModel mapFromModel) {
		this.parent = parent;
		this.mapToModel = mapToModel;
		this.mapFromModel = mapFromModel;

		mapToModelRenderer = new IdObjectListCellRenderer(mapToModel, null);

		JScrollPane scrollPane = new JScrollPane(objectMappingPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		JPanel matchPanelWrapper = new JPanel(new MigLayout("fill, ins 0, gap 0", "", "[grow][]"));
		matchPanelWrapper.setPreferredSize(ScreenInfo.getSmallWindow());
		matchPanelWrapper.add(scrollPane, "spanx, growx, growy, wrap");
		matchPanelWrapper.add(backButton, "");
		matchPanelWrapper.add(nextButton, "");
		nextButton.addActionListener(e -> nextDepth());
		backButton.addActionListener(e -> privDepth());
		matchPanelWrapper.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		panel.add(matchPanelWrapper, "spanx, growx, growy, wrap");
	}

	protected Map<IdObject, IdObject> getChainMap(Bone mapToBone, Bone mapFromBone, int depth, boolean presentParent) {
		if (depth != -1) this.depth = depth;
//		this.presentParent = presentParent;
		nodeMapHelper.setPresentParent(presentParent);
		nodeMapHelper.setLink(currDepth, mapFromBone, mapToBone);
		nextDepth();
		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("done!");
		nodeMapHelper.fillChainMap(this.depth);
		return nodeMapHelper.getBoneChainMap();
	}
	protected Map<IdObject, IdObject> getChainMap2(IdObject mapToBone, IdObject mapFromBone, int depth, boolean presentParent) {
		if (depth != -1) this.depth = depth;
//		this.presentParent = presentParent;
		nodeMapHelper.setPresentParent(presentParent);
		nodeMapHelper.setLink(currDepth, mapFromBone, mapToBone);
		nextDepth();
		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("done!");
		nodeMapHelper.fillChainMap(this.depth);
		return nodeMapHelper.getBoneChainMap();
	}

	public BoneChainMapWizard editMapping(IdObject mapToBone, IdObject mapFromBone, int depth, boolean presentParent) {
		this.depth = depth != -1 ? depth : 1000;
//		currDepth = allowTopLevelMapping ? -1 : 0;
//		allowTopLevelMapping = false;
//		this.presentParent = presentParent;
		currDepth = 0;
		nodeMapHelper.setAllowTopLevelMapping(true);
		nodeMapHelper.setPresentParent(presentParent);
		if (mapFromBone != null && nodeMapHelper.isNodeMapped(mapFromBone)) {
			nodeMapHelper.setLink(currDepth, mapFromBone, mapToBone);
		}
		nextDepth();
		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("done!");
		nodeMapHelper.fillChainMap(this.depth);
		return this;
	}


	public JPanel getEditMappingPanel(int depth, boolean presentParent, boolean doPrefill) {
		this.depth = depth != -1 ? depth : 1000;
//		allowTopLevelMapping = true;
//		this.presentParent = presentParent;
		nodeMapHelper.setAllowTopLevelMapping(true);
		nodeMapHelper.setPresentParent(presentParent);

		if (doPrefill) {
			nodeMapHelper.prefillMap(0, this.depth, getAllTopLevelNodes(mapFromModel), getAllTopLevelNodes(mapToModel));
			currDepth = 0;
			prefillMap();
		}

		currDepth = -1;
		nextDepth();
		return panel;
	}

	protected Map<IdObject, IdObject> getChainMap() {
		return nodeMapHelper.getBoneChainMap();
	}
	public Map<IdObject, IdObject> fillAndGetChainMap() {
		return nodeMapHelper.fillAndGetChainMap(depth);
	}

	private CollapsablePanel getSubMappingPanel(Map<IdObject, List<IdObject>> nodeToPosNodes, String title) {
		JPanel matchingPanel = new JPanel(new MigLayout("ins 0"));

		for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
			List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);
			TwiComboBox<IdObject> boneChooserBox = getShellTwiComboBox(mapFromNode, posMapToNodes);
			matchingPanel.add(new JLabel(iconHandler.getImageIcon(mapFromNode, mapFromModel)));
			matchingPanel.add(new JLabel(mapFromNode.getName()));
			matchingPanel.add(boneChooserBox, "wrap");
		}
		return new CollapsablePanel(title, matchingPanel);
	}
	private void prefillMap() {
		if (currDepth == 0) {
			List<IdObject> mapFromNodes = getAllTopLevelNodes(mapFromModel);
			List<IdObject> mapToNodes = getAllTopLevelNodes(mapToModel);
			nodeMapHelper.prefillCurrDepth(mapFromNodes, mapToNodes, currDepth);
//			System.out.println("added Top Level Nodes");
		}
		while (currDepth<depth) {
			Map<IdObject, IdObject> boneChainSubMap = nodeMapHelper.getBoneChainMap(currDepth);
			currDepth++;
			if (boneChainSubMap != null) {
				for (IdObject mapFromIdObject : boneChainSubMap.keySet()) {
					IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
//					if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)) {
					if (mapToIdObject != null) {
						nodeMapHelper.prefillCurrDepth(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes(), currDepth);

					}
				}
			} else {
				break;
			}
		}

	}

	private void nextDepth() {
		Map<IdObject, IdObject> boneChainSubMap = nodeMapHelper.getBoneChainMap(currDepth);
		objectMappingPanel.removeAll();
		currDepth++;
		nextButton.setEnabled(currDepth<depth);
		backButton.setEnabled(currDepth >= (nodeMapHelper.isAllowTopLevelMapping() ? 1 : 2));
		if (currDepth == 0) {
			mapTopNodes();
		} else {
			mapChildNodes(boneChainSubMap);
		}
		if (objectMappingPanel.getComponentCount() == 0) {
			nextButton.setEnabled(false);
			objectMappingPanel.add(new JLabel("No nodes found at depth " + currDepth));
		}
		panel.revalidate();
		panel.repaint();
	}

	private void mapTopNodes() {
		Map<IdObject, List<IdObject>> nodeToPosNodes = nodeMapHelper.getCandidateListMap(getAllTopLevelNodes(mapToModel), getAllTopLevelNodes(mapFromModel), currDepth);
		CollapsablePanel parentPanel = getSubMappingPanel(nodeToPosNodes, "Map Top Level Nodes");
		if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
			objectMappingPanel.add(parentPanel, "wrap");
		}
	}

	private void mapChildNodes(Map<IdObject, IdObject> boneChainSubMap) {
		if (currDepth < depth && boneChainSubMap != null) {
			for (IdObject mapFromIdObject : boneChainSubMap.keySet()) {
				IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
				if (mapToIdObject != null) {
					Map<IdObject, List<IdObject>> nodeToPosNodes = nodeMapHelper.getCandidateListMap(mapToIdObject.getChildrenNodes(), mapFromIdObject.getChildrenNodes(), currDepth);
					CollapsablePanel parentPanel = getSubMappingPanel(nodeToPosNodes, "Map children of " + mapFromIdObject.getName());
					if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
						objectMappingPanel.add(parentPanel, "wrap");
					}
				}
			}
		}

	}


	private void privDepth() {
		System.out.println("back!");
		currDepth -= 2;
		nextDepth();
	}

	private TwiComboBox<IdObject> getShellTwiComboBox(IdObject mapFromNode, List<IdObject> posMapToNodes) {

		TwiComboBox<IdObject> comboBox = new TwiComboBox<>(posMapToNodes, prototype);

		comboBox.setRenderer(mapToModelRenderer);
		comboBox.addOnSelectItemListener(toNode -> nodeMapHelper.setLink(currDepth, mapFromNode, toNode));

		if (nodeMapHelper.hasMappedNode(currDepth, mapFromNode)) {
			IdObject currToNode = nodeMapHelper.getMappedNode(currDepth, mapFromNode);
			comboBox.selectOrFirstWithListener(currToNode);
		} else if (posMapToNodes.size() == 2) {
			comboBox.setSelectedIndex(1);
		} else {
			comboBox.selectOrFirstWithListener(nodeMapHelper.findBetsMatch(mapFromNode, posMapToNodes));
		}
		return comboBox;
	}


	private List<IdObject> getAllTopLevelNodes(EditableModel model) {
		return model.getIdObjects().stream().filter(node -> node.getParent() == null).collect(Collectors.toList());
	}


	public BoneChainMapWizard setIsGeometryMode(boolean isGeometryMode) {
		nodeMapHelper.setIsGeometryMode(isGeometryMode);
		return this;
	}

	public BoneChainMapWizard setCheckHelperBones(boolean checkHelperChilds) {
		nodeMapHelper.setCheckHelperBones(checkHelperChilds);
		return this;
	}
	public BoneChainMapWizard setClassSet(Set<Class<?>> classSet) {
		nodeMapHelper.setClassSet(classSet);
		return this;
	}
}