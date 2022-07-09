package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.editor.actions.mesh.BridgeEdgeAction;
import com.hiveworkshop.rms.editor.actions.mesh.SnapCloseVertsAction;
import com.hiveworkshop.rms.editor.actions.nodes.*;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.CrudeSelectionUVMask;
import com.hiveworkshop.rms.ui.application.tools.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.function.Consumer;

public class TwilacStuff {

	private static class BakeAndRebindToNull extends TwiFunction{
		public BakeAndRebindToNull() {
			super("Rebind Node With Baked Transforms", BakeAndRebindToNull::rebindToNull);
		}

		private static void rebindToNull(ModelHandler modelHandler) {
			ModelView modelView = modelHandler.getModelView();
			List<UndoAction> rebindActions = new ArrayList<>();

//			quickTestForPeasant(modelHandler, modelView);

			realFunc(modelHandler, modelView, rebindActions);
		}

		private static void realFunc(ModelHandler modelHandler, ModelView modelView, List<UndoAction> rebindActions) {
			if(!modelView.getSelectedIdObjects().isEmpty()){
				IdObject childObject;
				if (modelView.getSelectedIdObjects().size() == 1){
					childObject = modelView.getSelectedIdObjects().stream().findFirst().get();
				} else {
					childObject = new Helper("Temp");
				}
				IdObject newParent = new IdObjectChooser(modelHandler.getModel(), true).chooseObject(childObject, ProgramGlobals.getMainPanel());
				for (IdObject idObject : modelView.getSelectedIdObjects()) {
					System.out.println("rebinding " + idObject.getName());
					if(newParent != idObject.getParent()){
						UndoAction action = new BakeAndRebindAction(idObject, newParent, modelHandler);
						rebindActions.add(action);
					}
				}
				if(!rebindActions.isEmpty()){
					modelHandler.getUndoManager().pushAction(new CompoundAction("Baked and changed Parent", rebindActions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
				}
			}
		}

		private static void quickTestForPeasant(ModelHandler modelHandler, ModelView modelView) {
			IdObject bone_leg2_l = modelHandler.getModel().getObject("Bone_Leg2_L");
//			IdObject bone_hand_l = modelHandler.getModel().getObject("Bone_Hand_L");
			IdObject bone_hand_l = modelHandler.getModel().getObject("Bone_Leg1_R");
			if(bone_leg2_l != null){
				System.out.println("rebinding " + bone_leg2_l.getName());
//				UndoAction action = new BakeAndRebindAction(bone_leg2_l, null, modelHandler);
//				UndoAction action = new BakeAndRebindActionRenderM3(bone_leg2_l, null, modelHandler);
				UndoAction action = new BakeAndRebindActionTwi2(bone_leg2_l, bone_hand_l, modelHandler);
//				UndoAction action = new BakeAndRebindActionRenderMat1(bone_leg2_l, bone_hand_l, modelHandler);
//				UndoAction action = new BakeAndRebindActionRenderM(bone_leg2_l, bone_hand_l, modelHandler);
				modelHandler.getUndoManager().pushAction(action.redo());
				modelHandler.getUndoManager().pushAction(new SetSelectionUggAction(new SelectionBundle(Collections.singleton(bone_leg2_l)), modelView, ModelStructureChangeListener.changeListener).redo());
			}
		}
	}
	private static class SnapCloseVerts extends TwiFunction{
		public SnapCloseVerts() {
			super("Snap Close Verts", SnapCloseVerts::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			modelHandler.getUndoManager().pushAction(new SnapCloseVertsAction(modelHandler.getModelView().getSelectedVertices(), 1, ModelStructureChangeListener.changeListener).redo());
		}
	}
	private static class RenameBoneChain extends TwiFunction{
		public RenameBoneChain() {
			super("Rename Bone Chain", RenameBoneChain::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			RenameBoneChainPanel.show(ProgramGlobals.getMainPanel());
		}
	}
	private static class RenameNodes extends TwiFunction{
		public RenameNodes() {
			super("Rename Nodes", RenameNodes::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			RenameNodesPanel.show(ProgramGlobals.getMainPanel());
		}
	}
	private static class ReorderAnimations extends TwiFunction{

		public ReorderAnimations() {
			super("Reorder Animations", ReorderAnimations::doStuff);
		}
		private static void doStuff(ModelHandler modelHandler) {
			ReorderAnimationsPanel panel = new ReorderAnimationsPanel(modelHandler);
			FramePopup.show(panel, null, "Re-order Animations");
		}
	}
	private static class ImportModelPart extends TwiFunction{

		public ImportModelPart() {
			super("Import Model Part By Bone Chain", ImportModelPart::doStuff);
		}
		private static void doStuff(ModelHandler modelHandler) {
			FileDialog fileDialog = new FileDialog();
			EditableModel donModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
			if (donModel != null) {
				ImportModelPartPanel panel = new ImportModelPartPanel(donModel, modelHandler);
				FramePopup.show(panel, ProgramGlobals.getMainPanel(), "Import Model Part");
			}
		}
	}
	private static class ImportModelSubAnim extends TwiFunction{

		public ImportModelSubAnim() {
			super("Import Bone Chain Animation", ImportModelSubAnim::doStuff);
		}
		private static void doStuff(ModelHandler modelHandler) {
			FileDialog fileDialog = new FileDialog();
			EditableModel donModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
			if (donModel != null) {
				ImportBoneChainAnimationPanel panel = new ImportBoneChainAnimationPanel(donModel, modelHandler);
				FramePopup.show(panel, ProgramGlobals.getMainPanel(), "Import bone chain animation");
			}
		}
	}
	private static class SpliceSubMesh extends TwiFunction{

		public SpliceSubMesh() {
			super("Splice Mesh", SpliceSubMesh::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			FileDialog fileDialog = new FileDialog();
			EditableModel donModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
			if (donModel != null) {
				SpliceModelPartPanel panel = new SpliceModelPartPanel(donModel, modelHandler);
				FramePopup.show(panel, ProgramGlobals.getMainPanel(), "Splice mesh");
			}
		}
	}

	private static class SpliceGeoset extends TwiFunction {

		public SpliceGeoset() {
			super("Splice Geoset", SpliceGeoset::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			FileDialog fileDialog = new FileDialog();
			EditableModel donModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
			if (donModel != null) {
				SpliceGeosetPanel panel = new SpliceGeosetPanel(donModel, modelHandler);
				FramePopup.show(panel, ProgramGlobals.getMainPanel(), "Splice Geoset");
			}
		}
	}

	private static class BridgeEdgeStuff extends TwiFunction {

		public BridgeEdgeStuff() {
			super("Bridge Edges", BridgeEdgeStuff::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			Set<GeosetVertex> selectedVertices = modelHandler.getModelView().getSelectedVertices();
			modelHandler.getUndoManager().pushAction(new BridgeEdgeAction(selectedVertices, ModelStructureChangeListener.getModelStructureChangeListener()).redo());
		}
	}

	private static class SelectEdgeStuff extends TwiFunction {

		public SelectEdgeStuff() {
			super("Select Edge", SelectEdgeStuff::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			new SelectEdge(modelHandler);
		}
	}

	private static class MeshShaderEditor extends TwiFunction {

		public MeshShaderEditor() {
			super("Shader Editor", MeshShaderEditor::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			if(modelHandler != null && modelHandler.getPreviewRenderModel() != null){
				ShaderEditPanel.show(ProgramGlobals.getMainPanel(), modelHandler.getPreviewRenderModel().getBufferFiller());
			}
		}
	}

	private static class NodeShaderEditor extends TwiFunction {

		public NodeShaderEditor() {
			super("Node Shader Editor", NodeShaderEditor::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			if(modelHandler != null && modelHandler.getRenderModel() != null){
				BoneShaderEditPanel.show(ProgramGlobals.getMainPanel(), modelHandler.getRenderModel().getBufferFiller());
			}
		}
	}

	private static class DupeForAnimStuff extends TwiFunction {

		public DupeForAnimStuff() {
			super("Geoset Split Wizard", DupeForAnimStuff::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			if(modelHandler != null){
				DuplicateForAnimation.show(ProgramGlobals.getMainPanel(), modelHandler);
			}
		}
	}

	private static class GlobalTransfStuff extends TwiFunction {

		public GlobalTransfStuff() {
			super("Global Transform Wizard", GlobalTransfStuff::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			if(modelHandler != null){
				GlobalTransformPanel.show(ProgramGlobals.getMainPanel(), modelHandler);
			}
		}
	}

	private static class ExportUVMask extends TwiFunction {

		public ExportUVMask() {
			super("Export Selected As UV Mask", ExportUVMask::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			if(modelHandler != null){
				CrudeSelectionUVMask.saveImage(modelHandler.getModelView(), 1024, 1024);
			}
		}
	}

	private static class TextureComposition extends TwiFunction {

		public TextureComposition() {
			super("Open Texture Composition Panel", TextureComposition::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			TextureCompositionPanel.showPanel(ProgramGlobals.getMainPanel());
		}
	}

	private static class MergeBoneHelpers extends TwiFunction {

		public MergeBoneHelpers() {
			super("Merge unnecessary Helpers with Child Bones", MergeBoneHelpers::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			EditableModel model = modelHandler.getModel();
			ModelView modelView = modelHandler.getModelView();
			Set<Bone> bonesWOMotion = new HashSet<>();
			model.getBones().stream()
					.filter(b -> b.getAnimFlags().isEmpty() && modelView.isSelected(b) && b.getChildrenNodes().isEmpty() && b.getParent() instanceof Helper)
					.forEach(bonesWOMotion::add);

			List<UndoAction> undoActions = new ArrayList<>();
			List<IdObject> nodesToRemove = new ArrayList<>();
			Map<IdObject, IdObject> nodeToReplacement = new HashMap<>();
			Map<Bone, List<IdObject>> boneToChildren = new HashMap<>();

			for (Bone bone : bonesWOMotion){
				IdObject parent = bone.getParent();
				long count = parent.getChildrenNodes().stream().filter(idObject -> idObject instanceof Bone && bonesWOMotion.contains(idObject)).count();
				if(count == 1){
					nodesToRemove.add(parent);
					nodeToReplacement.put(parent, bone);
					System.out.println("moving " + bone.getName() + " to parent position");
					undoActions.add(new SetPivotAction(bone, new Vec3(parent.getPivotPoint()), null));


					ArrayList<AnimFlag<?>> animFlags = parent.getAnimFlags();
					ArrayList<AnimFlag<?>> animFlagCopies = new ArrayList<>();
					for(AnimFlag<?> animFlag : animFlags){
						animFlagCopies.add(animFlag.deepCopy());
					}
					undoActions.add(new ReplaceAnimFlagsAction(bone, animFlagCopies, null));

					List<IdObject> childList = new ArrayList<>(parent.getChildrenNodes());
					childList.remove(bone);
					boneToChildren.put(bone, childList);
				}
//				bone.setParent(parent.getParent());
			}
			System.out.println("\nfound " + bonesWOMotion.size() + " bones to merge and " + nodesToRemove.size() + " nodes to remove");

			undoActions.add(new RemoveSelectionUggAction(new SelectionBundle(nodesToRemove), modelView, null));
			undoActions.add(new DeleteNodesAction(nodesToRemove, null, model));

			for (Bone bone : bonesWOMotion){
				IdObject parent = bone.getParent();
				if(nodesToRemove.contains(parent)){
					IdObject newParent = parent.getParent();
					while (nodeToReplacement.containsKey(newParent)){
						newParent = nodeToReplacement.get(newParent);
					}
					String newParentName = newParent == null ? "NULL" : newParent.getName();
					System.out.println("setting parent of " + bone.getName() + " to " + newParentName);
					undoActions.add(new SetParentAction(bone, newParent, null));
				}
//				bone.setParent(parent.getParent());
			}
			for (Bone bone : boneToChildren.keySet()){
				System.out.println(bone.getClass().getSimpleName() + ": " + bone.getName() + ": stealing parents (" + bone.getParent().getClass().getSimpleName() + ": " + bone.getParent().getName() + ") children");
				List<IdObject> childList = boneToChildren.get(bone);
				for(IdObject child : childList){
					undoActions.add(new SetParentAction(child, bone, null));
				}
			}
			modelHandler.getUndoManager().pushAction(
					new CompoundAction("Merge unnecessary Helpers", undoActions,
							ModelStructureChangeListener.changeListener::nodesUpdated)
//							ModelStructureChangeListener.changeListener::geosetsUpdated)
							.redo());
		}
		private static void doStuff1(ModelHandler modelHandler) {
			EditableModel model = modelHandler.getModel();
			ModelView modelView = modelHandler.getModelView();
			Set<Bone> bonesWOMotion = new HashSet<>();
			model.getBones().stream()
					.filter(b -> b.getAnimFlags().isEmpty() && modelView.isSelected(b) && b.getChildrenNodes().isEmpty() && b.getParent() instanceof Helper)
					.forEach(bonesWOMotion::add);

			List<UndoAction> undoActions = new ArrayList<>();
			List<IdObject> nodesToRemove = new ArrayList<>();
			Map<IdObject, IdObject> nodeToReplacement = new HashMap<>();

			for (Bone bone : bonesWOMotion){
				IdObject parent = bone.getParent();
				long count = parent.getChildrenNodes().stream().filter(idObject -> idObject instanceof Bone && bonesWOMotion.contains(idObject)).count();
				if(count == 1){
//					System.out.println(bone.getName() + ", parent: " + parent.getName() + ", has " + parent.getChildrenNodes().size() + " siblings");
					nodesToRemove.add(parent);
//					nodeToReplacement.put(parent, nodeToReplacement.getOrDefault(bone, bone));
					nodeToReplacement.put(parent, bone);
				}
//				bone.setParent(parent.getParent());
			}
			System.out.println("\nfound " + bonesWOMotion.size() + " bones to merge and " + nodesToRemove.size() + " nodes to remove");
			for (Bone bone : bonesWOMotion){
				IdObject parent = bone.getParent();
				if(nodesToRemove.contains(parent)){
					IdObject newParent = nodeToReplacement.getOrDefault(parent.getParent(), parent.getParent());
					Vec3 newPivot = new Vec3(parent.getPivotPoint());
					System.out.println(bone.getName() + ", \tparent: " + parent.getName() + ", \thas " + parent.getChildrenNodes().size() + " siblings");

					undoActions.add(new SetPivotAction(bone, newPivot, null));
//				bone.setPivotPoint(parent.getPivotPoint());

					ArrayList<AnimFlag<?>> animFlags = parent.getAnimFlags();
//				bone.setAnimFlags(animFlags);
					ArrayList<AnimFlag<?>> animFlagCopies = new ArrayList<>();
					for(AnimFlag<?> animFlag : animFlags){
						animFlagCopies.add(animFlag.deepCopy());
					}
					undoActions.add(new ReplaceAnimFlagsAction(bone, animFlagCopies, null));

					List<IdObject> childList = new ArrayList<>(parent.getChildrenNodes());
					for(IdObject child : childList){
						if(child != bone){
							undoActions.add(new SetParentAction(child, bone, null));
						}
					}
					undoActions.add(new SetParentAction(bone, newParent, null));
				}
//				bone.setParent(parent.getParent());
			}
			undoActions.add(new RemoveSelectionUggAction(new SelectionBundle(nodesToRemove), modelView, null));
			undoActions.add(new DeleteNodesAction(nodesToRemove, null, model));
			modelHandler.getUndoManager().pushAction(
					new CompoundAction("Merge unnececary Helpers", undoActions,
							ModelStructureChangeListener.changeListener::nodesUpdated)
//							ModelStructureChangeListener.changeListener::geosetsUpdated)
							.redo());
		}
	}
	private static class LinearizeSelected extends TwiFunction{

		public LinearizeSelected() {
			super("Linearize Animations For Selected Nodes", LinearizeSelected::linearizeAnimations);
		}
		private static void doStuff(ModelHandler modelHandler) {
		}
		public static void linearizeAnimations(ModelHandler modelHandler) {
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new JLabel("Animation types to affect"), "wrap");
			JCheckBox translation = new JCheckBox("Translation");
			JCheckBox rotation = new JCheckBox("Rotation");
			JCheckBox scaling = new JCheckBox("Scaling");
			JCheckBox other = new JCheckBox("Other");
			panel.add(translation, "wrap");
			panel.add(rotation, "wrap");
			panel.add(scaling, "wrap");
			panel.add(other, "wrap");

//			panel.add(new JLabel("new interpolation type"), "wrap");
			SmartButtonGroup buttonGroup = new SmartButtonGroup("new interpolation type");
			buttonGroup.addJRadioButton("None", null);
			buttonGroup.addJRadioButton("Linear", null);
			buttonGroup.addJRadioButton("Hermite", null);
			buttonGroup.addJRadioButton("Bezier", null);
			buttonGroup.setSelectedIndex(1);
			panel.add(buttonGroup.getButtonPanel());

			final int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
					panel,
					"Linearize Animations For Selected", JOptionPane.OK_CANCEL_OPTION);
			if (x == JOptionPane.OK_OPTION) {
				InterpolationType interpolationType =
						switch (buttonGroup.getSelectedIndex()){
					case 0 -> InterpolationType.DONT_INTERP;
					case 2 -> InterpolationType.HERMITE;
					case 3 -> InterpolationType.BEZIER;
							default -> InterpolationType.LINEAR;
						};

				List<UndoAction> interpTypActions = new ArrayList<>();
				for(IdObject idObject : modelHandler.getModelView().getSelectedIdObjects()){
					for (final AnimFlag<?> flag : idObject.getAnimFlags()) {
						if(flag.getName().equals(MdlUtils.TOKEN_TRANSLATION) && translation.isSelected()
								|| flag.getName().equals(MdlUtils.TOKEN_ROTATION) && rotation.isSelected()
								|| flag.getName().equals(MdlUtils.TOKEN_SCALING) && scaling.isSelected()
								|| !flag.getName().equals(MdlUtils.TOKEN_TRANSLATION)
										&& !flag.getName().equals(MdlUtils.TOKEN_ROTATION)
										&& !flag.getName().equals(MdlUtils.TOKEN_SCALING)
										&& other.isSelected()
						){
							interpTypActions.add(new ChangeInterpTypeAction<>(flag, interpolationType, null));
						}
					}
				}

				UndoAction action = new CompoundAction("Liniarize Animations", interpTypActions, ModelStructureChangeListener.changeListener::materialsListChanged);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}
	private static class TempNone extends TwiFunction{

		public TempNone() {
			super("TempNone", TempNone::doStuff);
		}
		private static void doStuff(ModelHandler modelHandler) {
		}
	}

	public static JMenuItem getBakeAndRebindToNullMenuItem(){
		return new BakeAndRebindToNull().getMenuItem();
	}
	public static JMenuItem getSnapCloseVertsMenuItem(){
		return new SnapCloseVerts().getMenuItem();
	}
	public static JMenuItem getRenameBoneChainMenuItem(){
		return new RenameBoneChain().getMenuItem();
	}
	public static JMenuItem getRenameNodesMenuItem(){
		return new RenameNodes().getMenuItem();
	}
	public static JMenuItem getReorderAnimationsMenuItem(){
		return new ReorderAnimations().getMenuItem();
	}

	public static JMenuItem getImportModelPartMenuItem() {
		return new ImportModelPart().getMenuItem();
	}

	public static JMenuItem getImportModelSubAnimMenuItem() {
		return new ImportModelSubAnim().getMenuItem();
	}

	public static JMenuItem getSpliceSubMeshMenuItem() {
		return new SpliceSubMesh().getMenuItem();
	}

	public static JMenuItem getSpliceGeosetMenuItem() {
		return new SpliceGeoset().getMenuItem();
	}

	public static JMenuItem getBridgeEdgesMenuItem() {
		return new BridgeEdgeStuff().getMenuItem();
	}
	public static JMenuItem getSelectEdgeMenuItem() {
		return new SelectEdgeStuff().getMenuItem();
	}
	public static JMenuItem getTestShaderStuffMenuItem() {
		return new MeshShaderEditor().getMenuItem();
	}
	public static JMenuItem getTestShaderStuff2MenuItem() {
		return new NodeShaderEditor().getMenuItem();
	}
	public static JMenuItem getDupeForAnimStuffMenuItem() {
		return new DupeForAnimStuff().getMenuItem();
	}
	public static JMenuItem getGlobalTransfStuffMenuItem() {
		return new GlobalTransfStuff().getMenuItem();
	}
	public static JMenuItem getExportUVMaskMenuItem() {
		return new ExportUVMask().getMenuItem();
	}
	public static JMenuItem getTextureCompositionMenuItem() {
		return new TextureComposition().getMenuItem();
	}
	public static JMenuItem getMergeBoneHelpersMenuItem() {
		return new MergeBoneHelpers().getMenuItem();
	}

	public static JMenuItem getLinearizeSelectedMenuItem() {
		return new LinearizeSelected().getMenuItem();
	}

	public static JMenuItem getAddNewAttatchment() {
		return new AddNewAttatchment().getMenuItem();
	}
	public static JMenuItem getTempNoneMenuItem() {
		return new TempNone().getMenuItem();
	}

	private static class TwiFunction{
		public final String name;
		private final JMenuItem menuItem;

		public TwiFunction(String name, Consumer<ModelHandler> consumer) {
			this.name = name;
			menuItem = new JMenuItem(getAsAction(consumer));
		}

		public JMenuItem getMenuItem() {
			return menuItem;
		}

		private AbstractAction getAsAction(Consumer<ModelHandler> consumer) {
			return new AbstractAction(name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
					if(modelPanel != null){
						consumer.accept(modelPanel.getModelHandler());
					}
				}
			};
		}
	}

	private static class AddNewAttatchment  extends TwiFunction{
		public AddNewAttatchment() {
			super("add New Attachment", AddNewAttatchment::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			AddAttachmentsPanel panel = new AddAttachmentsPanel(modelHandler);
			JFrame jFrame = FramePopup.show(panel, null, "Rename Nodes");
			panel.setOnFinished(() -> jFrame.dispose());
			jFrame.setVisible(true);
//			modelHandler.getUndoManager().pushAction(new SnapCloseVertsAction(modelHandler.getModelView().getSelectedVertices(), 1, ModelStructureChangeListener.changeListener).redo());
		}
	}
}
