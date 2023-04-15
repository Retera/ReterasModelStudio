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


	public BoneChainMapWizard(JComponent parent, EditableModel mapToModel, EditableModel mapFromModel){
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

	protected Map<IdObject, IdObject> getChainMap(Bone mapToBone, Bone mapFromBone, int depth, boolean presentParent){
		if(depth != -1) this.depth = depth;
//		this.presentParent = presentParent;
		nodeMapHelper.setPresentParent(presentParent);
		nodeMapHelper.setLink(currDepth, mapFromBone, mapToBone);
		nextDepth();
		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("done!");
		nodeMapHelper.fillChainMap(this.depth);
		return nodeMapHelper.getBoneChainMap();
	}
	protected Map<IdObject, IdObject> getChainMap2(IdObject mapToBone, IdObject mapFromBone, int depth, boolean presentParent){
		if(depth != -1) this.depth = depth;
//		this.presentParent = presentParent;
		nodeMapHelper.setPresentParent(presentParent);
		nodeMapHelper.setLink(currDepth, mapFromBone, mapToBone);
		nextDepth();
		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("done!");
		nodeMapHelper.fillChainMap(this.depth);
		return nodeMapHelper.getBoneChainMap();
	}

	public BoneChainMapWizard editMapping(Bone mapToBone, Bone mapFromBone, int depth, boolean presentParent){
		this.depth = depth != -1 ? depth : 1000;
//		currDepth = allowTopLevelMapping ? -1 : 0;
//		allowTopLevelMapping = false;
//		this.presentParent = presentParent;
		currDepth = 0;
		nodeMapHelper.setAllowTopLevelMapping(true);
		nodeMapHelper.setPresentParent(presentParent);
		if(mapFromBone != null && nodeMapHelper.isNodeMapped(mapFromBone)){
			nodeMapHelper.setLink(currDepth, mapFromBone, mapToBone);
		}
		nextDepth();
		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("done!");
		nodeMapHelper.fillChainMap(this.depth);
		return this;
	}

//	public BoneChainMapWizard editMapping(int depth, boolean presentParent){
//		this.depth = depth != -1 ? depth : 1000;
//		allowTopLevelMapping = true;
//		currDepth = -1;
//		this.presentParent = presentParent;
//		nextDepth();
//		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
//		System.out.println("done!");
//		fillChainMap();
//		return this;
//	}

	public JPanel getEditMappingPanel(int depth, boolean presentParent, boolean doPrefill){
		this.depth = depth != -1 ? depth : 1000;
//		allowTopLevelMapping = true;
//		this.presentParent = presentParent;
		nodeMapHelper.setAllowTopLevelMapping(true);
		nodeMapHelper.setPresentParent(presentParent);

		if(doPrefill){
			nodeMapHelper.prefillMap(0, this.depth, getAllTopLevelNodes(mapFromModel), getAllTopLevelNodes(mapToModel));
			currDepth = 0;
			prefillMap();
		}

		currDepth = -1;
		nextDepth();
		return panel;
	}

//	private void fillChainMap() {
//		for (int i = 0; i < this.depth; i++){
//			LinkedHashMap<IdObject, IdObject> chainMap = nodeDepthMap.getBoneChainMap(i);
//			if(chainMap != null){
//				boneChainMap.putAll(chainMap);
//				System.out.println("added " + chainMap.size() + " mappings");
//			} else {
//				break;
//			}
//		}
//		System.out.println(boneChainMap.size() + " mappings found!");
//	}

	protected Map<IdObject, IdObject> getChainMap(){
		return nodeMapHelper.getBoneChainMap();
	}
	public Map<IdObject, IdObject> fillAndGetChainMap(){
//		fillChainMap();
		return nodeMapHelper.fillAndGetChainMap(depth);
	}

//	private CollapsablePanel getSubMappingPanel(List<IdObject> mapFromChilds, List<IdObject> mapToChilds, String title) {
//		JPanel matchingPanel = new JPanel(new MigLayout("ins 0"));
//		Map<IdObject, List<IdObject>> nodeToPosNodes = nodeMapHelper.getCandidateListMap(mapToChilds, mapFromChilds, currDepth);
//
//		for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
//			List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);
//			TwiComboBox<IdObject> boneChooserBox = getShellTwiComboBox(mapFromNode, posMapToNodes);
//			matchingPanel.add(new JLabel(iconHandler.getImageIcon(mapFromNode, mapFromModel)));
//			matchingPanel.add(new JLabel(mapFromNode.getName()));
//			matchingPanel.add(boneChooserBox, "wrap");
//		}
//		return new CollapsablePanel(title, matchingPanel);
//	}

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
		if(currDepth == 0){
			List<IdObject> mapFromNodes = getAllTopLevelNodes(mapFromModel);
			List<IdObject> mapToNodes = getAllTopLevelNodes(mapToModel);
			nodeMapHelper.prefillCurrDepth(mapFromNodes, mapToNodes, currDepth);
//			System.out.println("added Top Level Nodes");
		}
		while (currDepth<depth) {
			Map<IdObject, IdObject> boneChainSubMap = nodeMapHelper.getBoneChainMap(currDepth);
			currDepth++;
			if (boneChainSubMap != null){
				for(IdObject mapFromIdObject : boneChainSubMap.keySet()){
					IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
//					if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)){
					if (mapToIdObject != null){
						nodeMapHelper.prefillCurrDepth(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes(), currDepth);

					}
				}
			} else {
				break;
			}
		}

	}



//	private void prefillCurrDepth(List<IdObject> mapFromChilds, List<IdObject> mapToChilds) {
//		Map<IdObject, List<IdObject>> nodeToPosNodes = getNodeToPosNodes(mapToChilds, mapFromChilds);
//
//		for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
//			List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);
//
//			IdObject betsMatch = posMapToNodes.size() == 2 ? posMapToNodes.get(1) : findBetsMatch(mapFromNode, posMapToNodes);
//
//			nodeDepthMap.setLink(currDepth, mapFromNode, betsMatch);
//
//			String bestName = betsMatch == null ? "None" : betsMatch.getName();
//			System.out.println("mapped " + mapFromNode.getName() + " to " + bestName);
//		}
//	}

//	private void prefillCurrDepth(Map<IdObject, List<IdObject>> nodeToPosNodes) {
//		for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
//			List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);
//
//			IdObject betsMatch = posMapToNodes.size() == 2 ? posMapToNodes.get(1) : findBetsMatch(mapFromNode, posMapToNodes);
//
//			nodeDepthMap.setLink(currDepth, mapFromNode, betsMatch);
//
//			String bestName = betsMatch == null ? "None" : betsMatch.getName();
//			System.out.println("mapped " + mapFromNode.getName() + " to " + bestName);
//		}
//	}

	private void nextDepth() {
		Map<IdObject, IdObject> boneChainSubMap = nodeMapHelper.getBoneChainMap(currDepth);
		objectMappingPanel.removeAll();
		currDepth++;
		nextButton.setEnabled(currDepth<depth);
//		backButton.setEnabled(currDepth >= (allowTopLevelMapping ? 1 : 2));
		backButton.setEnabled(currDepth >= (nodeMapHelper.isAllowTopLevelMapping() ? 1 : 2));
		if(currDepth == 0){
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
//		List<IdObject> mapFromNodes = getAllTopLevelNodes(mapFromModel);
//		List<IdObject> mapToNodes = getAllTopLevelNodes(mapToModel);
//		CollapsablePanel parentPanel = getSubMappingPanel(mapFromNodes, mapToNodes, "Map Top Level Nodes");
//		if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
//			objectMappingPanel.add(parentPanel, "wrap");
//		}
	}

	private void mapChildNodes(Map<IdObject, IdObject> boneChainSubMap) {
		if (currDepth < depth && boneChainSubMap != null){
			for(IdObject mapFromIdObject : boneChainSubMap.keySet()){
				IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
				if (mapToIdObject != null){
					Map<IdObject, List<IdObject>> nodeToPosNodes = nodeMapHelper.getCandidateListMap(mapToIdObject.getChildrenNodes(), mapFromIdObject.getChildrenNodes(), currDepth);
					CollapsablePanel parentPanel = getSubMappingPanel(nodeToPosNodes, "Map children of " + mapFromIdObject.getName());
					if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
						objectMappingPanel.add(parentPanel, "wrap");
					}
				}
			}
		}
//		if (currDepth < depth && boneChainSubMap != null){
//			for(IdObject mapFromIdObject : boneChainSubMap.keySet()){
//				IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
////				if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)){
//				if (mapToIdObject != null){
//					CollapsablePanel parentPanel = getSubMappingPanel(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes(), "Map children of " + mapFromIdObject.getName());
//					if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
//						objectMappingPanel.add(parentPanel, "wrap");
//					}
//				}
//			}
//		}
	}

//	private void ugg() {
//		Map<IdObject, IdObject> boneChainSubMap = nodeDepthMap.getBoneChainMap(currDepth);
//		currDepth++;
//
//		if(currDepth == 0){
//			Map<IdObject, List<IdObject>> nodeToPosNodes = nodeMapHelper.getCandidateListMap(getAllTopLevelNodes(mapToModel), getAllTopLevelNodes(mapFromModel), currDepth);
//			CollapsablePanel parentPanel = getSubMappingPanel(nodeToPosNodes, "Map Top Level Nodes");
////			CollapsablePanel parentPanel = getSubMappingPanel(getAllTopLevelNodes(mapFromModel), getAllTopLevelNodes(mapToModel), "Map Top Level Nodes");
//			if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
//				objectMappingPanel.add(parentPanel, "wrap");
//			}
//		} else {
//			if (currDepth < depth && boneChainSubMap != null){
//				for(IdObject mapFromIdObject : boneChainSubMap.keySet()){
//					IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
//	//				if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)){
//					if (mapToIdObject != null){
//
//						Map<IdObject, List<IdObject>> nodeToPosNodes = nodeMapHelper.getCandidateListMap(mapToIdObject.getChildrenNodes(), mapFromIdObject.getChildrenNodes(), currDepth);
////						CollapsablePanel parentPanel = getSubMappingPanel(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes(), "Map children of " + mapFromIdObject.getName());
//						CollapsablePanel parentPanel = getSubMappingPanel(nodeToPosNodes, "Map children of " + mapFromIdObject.getName());
//						if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
//							objectMappingPanel.add(parentPanel, "wrap");
//						}
//					}
//				}
//			}
//		}
//	}

	private void privDepth() {
		System.out.println("back!");
//		nodeDepthMap.clear(currDepth);
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

	//////

	private List<IdObject> getAllTopLevelNodes(EditableModel model){
		return model.getIdObjects().stream().filter(node -> node.getParent() == null).collect(Collectors.toList());
	}

//	private Map<IdObject, List<IdObject>> getCandidateListMap(List<IdObject> idObjectsForComboBox, List<IdObject> idObjectsForPanel) {
//		Map<IdObject, List<IdObject>> nodeToPosNodes = new HashMap<>();
//
//		if(!idObjectsForComboBox.isEmpty()) {
//			for (IdObject idObjectForPanel : idObjectsForPanel) {
//				if(classSet == null || classSet.contains(idObjectForPanel.getClass())){
//					nodeToPosNodes.put(idObjectForPanel, getValidNodesList(idObjectForPanel, idObjectsForComboBox, currDepth));
//				}
//			}
//		}
//		return nodeToPosNodes;
//	}
//
//	private List<IdObject> getValidNodesList(IdObject idObjectDest, List<IdObject> idObjectsForComboBox, int currDepth) {
//		List<IdObject> validObjects = new ArrayList<>();
//		validObjects.add(null);
//
//		if (presentParent && !idObjectsForComboBox.isEmpty() && idObjectsForComboBox.get(0).getParent() != null) {
//			validObjects.add(idObjectsForComboBox.get(0).getParent());
//		}
//
//		idObjectsForComboBox.stream()
//				.filter(idObject -> isBones(idObject, idObjectDest) || sameClass(idObjectDest, idObject))
//				.forEach(validObjects::add);
//
//		if(checkHelperChilds){
//			validObjects.addAll(fetchSuitibleChildBones(idObjectDest, idObjectsForComboBox));
//		}
//
//
//		if(nodeDepthMap.hasMappedNode(currDepth, idObjectDest)){
//			IdObject mappedNode = nodeDepthMap.getMappedNode(currDepth, idObjectDest);
//			if(autoValidateChain && !validObjects.contains(mappedNode)){
//				nodeDepthMap.removeLink(currDepth, idObjectDest);
//			}
//		}
//
//		return validObjects;
//	}
//
//
//
//	private List<IdObject> fetchSuitibleChildBones(IdObject idObject, List<IdObject> nodesToCheck){
//		List<IdObject> nodes =  new ArrayList<>();
//		if(idObject instanceof Bone || idObject instanceof  Helper){
//			for (IdObject node : nodesToCheck){
//				if(node instanceof Helper){
//					for(IdObject child : node.getChildrenNodes()){
//						if(child instanceof Bone){
//							nodes.add(child);
//						}
//					}
//				}
//			}
//		}
//
//		return nodes;
//	}

//	private IdObject findBetsMatch(IdObject objToMatch, List<IdObject> validObjects) {
//		IdObject sameNameObject = null;
//		int lastMatch = 20;
//		String matchName = objToMatch.getName();
//		for (IdObject idObject : validObjects) {
//			if(idObject != null){
//				String name = idObject.getName();
//				int comp = Math.abs(name.compareTo(matchName));
//				if (comp == 0
//						|| comp < lastMatch && isMatch(matchName, name)
//						|| sameNameObject == null && isSomeWhatClose(matchName, name)) {
//					sameNameObject = idObject;
//					lastMatch = comp;
//				}
//			}
//		}
//		return sameNameObject;
//	}
//
//
//	/**
//	 * splits the strings on "_" and checks if the sub-strings matches up till
//	 * min(a_split.length, b_split.length)
//	 */
//	private boolean isMatch(String destName, String name) {
//		String[] namsSplit = name.split("_");
//		String[] destSplit = destName.split("_");
//		for (int i = 0; i < namsSplit.length && i < destSplit.length; i++) {
//			if (!namsSplit[i].equals(destSplit[i])) {
//				return false;
//			}
//		}
//		return true;
//	}
//	private boolean isSomeWhatClose(String destName, String name) {
//		destName = replaceAllTypeStrings(destName);
//		name = replaceAllTypeStrings(name);
//
//		return name.startsWith(destName) || destName.startsWith(name);
//	}
//
//	private String replaceAllTypeStrings(String s){
//		s = s.toLowerCase();
//		for(String stupid : typeStrings){
//			s = s.replaceAll(stupid, "TEMP");
//		}
////		s = s.replaceAll("TEMP", "").replaceAll("_", "");
//		s = s.replaceAll("(TEMP)|_", "");
//		return s;
//	}
//
//	private boolean isValidCandidate(IdObject idObjectDest, IdObject idObject) {
////		if(isGeometryMode){
////			return sameClass(idObjectDest, idObject);
////		}
//		return isBones(idObject, idObjectDest) || sameClass(idObjectDest, idObject) || idObjectDest instanceof Helper && idObject instanceof Bone;
//	}
//
//	private boolean sameClass(IdObject idObjectDest, IdObject idObject) {
//		return idObject.getClass() == idObjectDest.getClass();
//	}

	protected boolean isBones(IdObject idObject1, IdObject idObject2) {
		System.out.println("is bones? " + idObject1 + ", " + idObject2 + " - " + ((idObject1 instanceof Bone || idObject1 instanceof Helper) && (idObject2 instanceof Bone || idObject2 instanceof Helper)));
		return (idObject1 instanceof Bone || idObject1 instanceof Helper) && (idObject2 instanceof Bone || idObject2 instanceof Helper);
	}

	public BoneChainMapWizard setAllowTopLevelMapping(boolean allowTopLevelMapping) {
		nodeMapHelper.setAllowTopLevelMapping(allowTopLevelMapping);
//		this.allowTopLevelMapping = allowTopLevelMapping;
		return this;
	}

	public BoneChainMapWizard setIsGeometryMode(boolean isGeometryMode) {
		nodeMapHelper.setIsGeometryMode(isGeometryMode);
//		this.isGeometryMode = isGeometryMode;
		return this;
	}

	public BoneChainMapWizard setCheckHelperBones(boolean checkHelperChilds) {
		nodeMapHelper.setCheckHelperBones(checkHelperChilds);
//		this.checkHelperChilds = checkHelperChilds;
		return this;
	}
	public BoneChainMapWizard setClassSet(Set<Class<?>> classSet) {
		nodeMapHelper.setClassSet(classSet);
//		this.classSet = classSet;
		return this;
	}
}
//package com.hiveworkshop.rms.ui.application.tools;
//
//import com.hiveworkshop.rms.editor.model.Bone;
//import com.hiveworkshop.rms.editor.model.EditableModel;
//import com.hiveworkshop.rms.editor.model.Helper;
//import com.hiveworkshop.rms.editor.model.IdObject;
//import com.hiveworkshop.rms.ui.gui.modeledit.renderers.IdObjectListCellRenderer;
//import com.hiveworkshop.rms.util.CollapsablePanel;
//import com.hiveworkshop.rms.util.ScreenInfo;
//import com.hiveworkshop.rms.util.TwiComboBox;
//import net.miginfocom.swing.MigLayout;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class BoneChainMapWizard {
//	private final String[] typeStrings = new String[]{"bone", "geo", "helper", "node"};
//	private final Helper prototype = new Helper("Prototype prototype");
//	private final ModelIconHandler iconHandler = new ModelIconHandler();
//	private final JComponent parent;
//	private final EditableModel mapToModel;
//	private final EditableModel mapFromModel;
//	private final LinkedHashMap<IdObject, IdObject> boneChainMap = new LinkedHashMap<>();
//	private final NodeDepthMap nodeDepthMap = new NodeDepthMap(0);
//	private int currDepth = 0;
//	private int depth = 10000;
//
//	private final JPanel panel = new JPanel(new MigLayout("ins 0"));
//	private final JButton backButton = new JButton("BACK");
//	private final JButton nextButton = new JButton("NEXT");
//	private final JPanel objectMappingPanel = new JPanel(new MigLayout(""));
//	private boolean presentParent;
//	private boolean checkHelperChilds;
//	private boolean isGeometryMode;
//	private boolean allowTopLevelMapping;
//	private boolean autoValidateChain = true;
//
//	private Set<Class<?>> classSet;
//
//	private final IdObjectListCellRenderer mapToModelRenderer;
//
//
//	public BoneChainMapWizard(JComponent parent, EditableModel mapToModel, EditableModel mapFromModel){
//		this.parent = parent;
//		this.mapToModel = mapToModel;
//		this.mapFromModel = mapFromModel;
//
//		mapToModelRenderer = new IdObjectListCellRenderer(mapToModel, null);
//
//		JScrollPane scrollPane = new JScrollPane(objectMappingPanel);
//		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//
//		JPanel matchPanelWrapper = new JPanel(new MigLayout("fill, ins 0, gap 0", "", "[grow][]"));
//		matchPanelWrapper.setPreferredSize(ScreenInfo.getSmallWindow());
//		matchPanelWrapper.add(scrollPane, "spanx, growx, growy, wrap");
//		matchPanelWrapper.add(backButton, "");
//		matchPanelWrapper.add(nextButton, "");
//		nextButton.addActionListener(e -> nextDepth());
//		backButton.addActionListener(e -> privDepth());
//		matchPanelWrapper.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//
//		panel.add(matchPanelWrapper, "spanx, growx, growy, wrap");
//	}
//
//	protected Map<IdObject, IdObject> getChainMap(Bone mapToBone, Bone mapFromBone, int depth, boolean presentParent){
//		if(depth != -1) this.depth = depth;
//		this.presentParent = presentParent;
//		nodeDepthMap.setLink(currDepth, mapFromBone, mapToBone);
//		nextDepth();
//		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
//		System.out.println("done!");
//		fillChainMap();
//		return boneChainMap;
//	}
//
//	public BoneChainMapWizard editMapping(Bone mapToBone, Bone mapFromBone, int depth, boolean presentParent){
//		this.depth = depth != -1 ? depth : 1000;
////		currDepth = allowTopLevelMapping ? -1 : 0;
//		allowTopLevelMapping = false;
//		currDepth = 0;
//		this.presentParent = presentParent;
//		if(mapFromBone != null && (nodeDepthMap.getBoneChainMap().isEmpty() || !nodeDepthMap.getBoneChainMap().containsKey(mapFromBone))){
//			nodeDepthMap.setLink(currDepth, mapFromBone, mapToBone);
//		}
//		nextDepth();
//		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
//		System.out.println("done!");
//		fillChainMap();
//		return this;
//	}
//
////	public BoneChainMapWizard editMapping(int depth, boolean presentParent){
////		this.depth = depth != -1 ? depth : 1000;
////		allowTopLevelMapping = true;
////		currDepth = -1;
////		this.presentParent = presentParent;
////		nextDepth();
////		JOptionPane.showConfirmDialog(parent, panel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
////		System.out.println("done!");
////		fillChainMap();
////		return this;
////	}
//
//	public JPanel getEditMappingPanel(int depth, boolean presentParent, boolean doPrefill){
//		this.depth = depth != -1 ? depth : 1000;
//		allowTopLevelMapping = true;
//		this.presentParent = presentParent;
//
//		if(doPrefill){
//			currDepth = 0;
//			prefillMap();
//		}
//
//		currDepth = -1;
//		nextDepth();
//		return panel;
//	}
//
//	private void fillChainMap() {
//		for (int i = 0; i < this.depth; i++){
//			LinkedHashMap<IdObject, IdObject> chainMap = nodeDepthMap.getBoneChainMap(i);
//			if(chainMap != null){
//				boneChainMap.putAll(chainMap);
//				System.out.println("added " + chainMap.size() + " mappings");
//			} else {
//				break;
//			}
//		}
//		System.out.println(boneChainMap.size() + " mappings found!");
//	}
//
//	protected Map<IdObject, IdObject> getChainMap(){
//		return boneChainMap;
//	}
//	public Map<IdObject, IdObject> fillAndGetChainMap(){
//		fillChainMap();
//		return boneChainMap;
//	}
//
//	private CollapsablePanel getSubMappingPanel(List<IdObject> mapFromChilds, List<IdObject> mapToChilds, String title) {
//		JPanel matchingPanel = new JPanel(new MigLayout("ins 0"));
//		if (!(mapFromChilds.isEmpty() || mapToChilds.isEmpty())) {
//			Map<IdObject, List<IdObject>> nodeToPosNodes = getNodeToPosNodes(mapToChilds, mapFromChilds);
//
//			for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
//				List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);
//				TwiComboBox<IdObject> boneChooserBox = getShellTwiComboBox(mapFromNode, posMapToNodes);
//				matchingPanel.add(new JLabel(iconHandler.getImageIcon(mapFromNode, mapFromModel)));
//				matchingPanel.add(new JLabel(mapFromNode.getName()));
//				matchingPanel.add(boneChooserBox, "wrap");
//			}
//
//		}
//		return new CollapsablePanel(title, matchingPanel);
//	}
//	private void prefillMap() {
//		if(currDepth == 0){
//			List<IdObject> mapFromNodes = getAllTopLevelNodes(mapFromModel);
//			List<IdObject> mapToNodes = getAllTopLevelNodes(mapToModel);
//			prefillCurrDepth(mapFromNodes, mapToNodes);
////			System.out.println("added Top Level Nodes");
//		}
//		while (currDepth<depth) {
//			Map<IdObject, IdObject> boneChainSubMap = nodeDepthMap.getBoneChainMap(currDepth);
//			currDepth++;
//			if (boneChainSubMap != null){
//				for(IdObject mapFromIdObject : boneChainSubMap.keySet()){
//					IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
//					if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)){
//						prefillCurrDepth(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes());
//
//					}
//				}
//			} else {
//				break;
//			}
//		}
//
//	}
//
//
//
//	private void prefillCurrDepth(List<IdObject> mapFromChilds, List<IdObject> mapToChilds) {
//		if (!(mapFromChilds.isEmpty() || mapToChilds.isEmpty())) {
//			Map<IdObject, List<IdObject>> nodeToPosNodes = getNodeToPosNodes(mapToChilds, mapFromChilds);
//
//			for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
//				List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);
//
//				IdObject betsMatch = posMapToNodes.size() == 2 ? posMapToNodes.get(1) : findBetsMatch(mapFromNode, posMapToNodes);
//
//				nodeDepthMap.setLink(currDepth, mapFromNode, betsMatch);
//
//				String bestName = betsMatch == null ? "None" : betsMatch.getName();
//				System.out.println("mapped " + mapFromNode.getName() + " to " + bestName);
//			}
//
//		}
//	}
//
//	private void nextDepth() {
//		Map<IdObject, IdObject> boneChainSubMap = nodeDepthMap.getBoneChainMap(currDepth);
//		objectMappingPanel.removeAll();
//		currDepth++;
//		nextButton.setEnabled(currDepth<depth);
//		backButton.setEnabled(currDepth >= (allowTopLevelMapping ? 1 : 2));
//		if(currDepth == 0){
//			mapTopNodes();
//		} else {
//			mapChildNodes(boneChainSubMap);
//		}
//		if (objectMappingPanel.getComponentCount() == 0) {
//			nextButton.setEnabled(false);
//			objectMappingPanel.add(new JLabel("No nodes found at depth " + currDepth));
//		}
//		panel.revalidate();
//		panel.repaint();
//	}
//
//	private void mapTopNodes() {
//		List<IdObject> mapFromNodes = getAllTopLevelNodes(mapFromModel);
//		List<IdObject> mapToNodes = getAllTopLevelNodes(mapToModel);
//		CollapsablePanel parentPanel = getSubMappingPanel(mapFromNodes, mapToNodes, "Map Top Level Nodes");
//		if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
//			objectMappingPanel.add(parentPanel, "wrap");
//		}
//	}
//
//	private void mapChildNodes(Map<IdObject, IdObject> boneChainSubMap) {
//
//		if (currDepth < depth && boneChainSubMap != null){
//			for(IdObject mapFromIdObject : boneChainSubMap.keySet()){
//				IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
////				if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)){
//				if (mapToIdObject != null){
//					CollapsablePanel parentPanel = getSubMappingPanel(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes(), "Map children of " + mapFromIdObject.getName());
//					if (parentPanel.getCollapsableContentPanel() != null && parentPanel.getCollapsableContentPanel().getComponentCount()!=0) {
//						objectMappingPanel.add(parentPanel, "wrap");
//					}
//				}
//			}
//		}
//	}
//
//	private void privDepth() {
//		System.out.println("back!");
////		nodeDepthMap.clear(currDepth);
//		currDepth -= 2;
//		nextDepth();
//	}
//
//	private List<IdObject> getAllTopLevelNodes(EditableModel model){
//		return model.getIdObjects().stream().filter(node -> node.getParent() == null).collect(Collectors.toList());
//	}
//
//	private TwiComboBox<IdObject> getShellTwiComboBox(IdObject mapFromNode, List<IdObject> posMapToNodes) {
//
//		TwiComboBox<IdObject> comboBox = new TwiComboBox<>(posMapToNodes, prototype);
//
//		comboBox.setRenderer(mapToModelRenderer);
//		comboBox.addOnSelectItemListener(toNode -> nodeDepthMap.setLink(currDepth, mapFromNode, toNode));
//
//		if (nodeDepthMap.hasMappedNode(currDepth, mapFromNode)) {
//			IdObject currToNode = nodeDepthMap.getMappedNode(currDepth, mapFromNode);
//			comboBox.selectOrFirstWithListener(currToNode);
//		} else if (posMapToNodes.size() == 2) {
//			comboBox.setSelectedIndex(1);
//		} else {
//			comboBox.selectOrFirstWithListener(findBetsMatch(mapFromNode, posMapToNodes));
//		}
//		return comboBox;
//	}
//
//	private Map<IdObject, List<IdObject>> getNodeToPosNodes(List<IdObject> idObjectsForComboBox, List<IdObject> idObjectsForPanel) {
//		Map<IdObject, List<IdObject>> nodeToPosNodes = new HashMap<>();
//
//		for (IdObject idObjectForPanel : idObjectsForPanel) {
//			if(classSet == null || classSet.contains(idObjectForPanel.getClass())){
//				nodeToPosNodes.put(idObjectForPanel, getValidNodesList(idObjectForPanel, idObjectsForComboBox));
//			}
//		}
//		return nodeToPosNodes;
//	}
//
//	private List<IdObject> getValidNodesList(IdObject idObjectDest, List<IdObject> idObjectsForComboBox) {
//		List<IdObject> validObjects = new ArrayList<>();
//		validObjects.add(null);
//
//		if (presentParent && !idObjectsForComboBox.isEmpty() && idObjectsForComboBox.get(0).getParent() != null) {
//			validObjects.add(idObjectsForComboBox.get(0).getParent());
//		}
//
//		idObjectsForComboBox.stream()
//				.filter(idObject -> isBones(idObject, idObjectDest) || sameClass(idObjectDest, idObject))
//				.forEach(validObjects::add);
//
//		if(checkHelperChilds){
//			validObjects.addAll(fetchSuitibleChildBones(idObjectDest, idObjectsForComboBox));
//		}
//
//
//		if(nodeDepthMap.hasMappedNode(currDepth, idObjectDest)){
//			IdObject mappedNode = nodeDepthMap.getMappedNode(currDepth, idObjectDest);
//			if(autoValidateChain && !validObjects.contains(mappedNode)){
//				nodeDepthMap.removeLink(currDepth, idObjectDest);
//			}
//		}
//
//		return validObjects;
//	}
//
//
//
//	private List<IdObject> fetchSuitibleChildBones(IdObject idObject, List<IdObject> nodesToCheck){
//		List<IdObject> nodes =  new ArrayList<>();
//		if(idObject instanceof Bone || idObject instanceof  Helper){
//			for (IdObject node : nodesToCheck){
//				if(node instanceof Helper){
//					for(IdObject child : node.getChildrenNodes()){
//						if(child instanceof Bone){
//							nodes.add(child);
//						}
//					}
//				}
//			}
//		}
//
//		return nodes;
//	}
//
//	private IdObject findBetsMatch(IdObject objToMatch, List<IdObject> validObjects) {
//		IdObject sameNameObject = null;
//		int lastMatch = 20;
//		String matchName = objToMatch.getName();
//		for (IdObject idObject : validObjects) {
//			if(idObject != null){
//				String name = idObject.getName();
//				int comp = Math.abs(name.compareTo(matchName));
//				if (comp == 0
//						|| comp < lastMatch && isMatch(matchName, name)
//						|| sameNameObject == null && isSomeWhatClose(matchName, name)) {
//					sameNameObject = idObject;
//					lastMatch = comp;
//				}
//			}
//		}
//		return sameNameObject;
//	}
//
//
//	/**
//	 * splits the strings on "_" and checks if the sub-strings matches up till
//	 * min(a_split.length, b_split.length)
//	 */
//	private boolean isMatch(String destName, String name) {
//		String[] namsSplit = name.split("_");
//		String[] destSplit = destName.split("_");
//		for (int i = 0; i < namsSplit.length && i < destSplit.length; i++) {
//			if (!namsSplit[i].equals(destSplit[i])) {
//				return false;
//			}
//		}
//		return true;
//	}
//	private boolean isSomeWhatClose(String destName, String name) {
//		destName = replaceAllTypeStrings(destName);
//		name = replaceAllTypeStrings(name);
//
//		return name.startsWith(destName) || destName.startsWith(name);
//	}
//
//	private String replaceAllTypeStrings(String s){
//		s = s.toLowerCase();
//		for(String stupid : typeStrings){
//			s = s.replaceAll(stupid, "TEMP");
//		}
////		s = s.replaceAll("TEMP", "").replaceAll("_", "");
//		s = s.replaceAll("(TEMP)|_", "");
//		return s;
//	}
//
//	private boolean isValidCandidate(IdObject idObjectDest, IdObject idObject) {
////		if(isGeometryMode){
////			return sameClass(idObjectDest, idObject);
////		}
//		return isBones(idObject, idObjectDest) || sameClass(idObjectDest, idObject) || idObjectDest instanceof Helper && idObject instanceof Bone;
//	}
//
//	private boolean sameClass(IdObject idObjectDest, IdObject idObject) {
//		return idObject.getClass() == idObjectDest.getClass();
//	}
//
//	protected boolean isBones(IdObject idObject1, IdObject idObject2) {
//		System.out.println("is bones? " + idObject1 + ", " + idObject2 + " - " + ((idObject1 instanceof Bone || idObject1 instanceof Helper) && (idObject2 instanceof Bone || idObject2 instanceof Helper)));
//		return (idObject1 instanceof Bone || idObject1 instanceof Helper) && (idObject2 instanceof Bone || idObject2 instanceof Helper);
//	}
//
//	public BoneChainMapWizard setAllowTopLevelMapping(boolean allowTopLevelMapping) {
//		this.allowTopLevelMapping = allowTopLevelMapping;
//		return this;
//	}
//
//	public BoneChainMapWizard setIsGeometryMode(boolean isGeometryMode) {
//		this.isGeometryMode = isGeometryMode;
//		return this;
//	}
//
//	public BoneChainMapWizard setCheckHelperBones(boolean checkHelperChilds) {
//		this.checkHelperChilds = checkHelperChilds;
//		return this;
//	}
//	public BoneChainMapWizard setClassSet(Set<Class<?>> classSet) {
//		this.classSet = classSet;
//		return this;
//	}
//}