package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.mesh.BridgeEdgeAction;
import com.hiveworkshop.rms.editor.actions.mesh.SnapCloseVertsAction;
import com.hiveworkshop.rms.editor.actions.nodes.BakeAndRebindAction;
import com.hiveworkshop.rms.editor.actions.nodes.BakeAndRebindActionTwi2;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelFromFile;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.CrudeSelectionUVMask;
import com.hiveworkshop.rms.ui.application.tools.*;
import com.hiveworkshop.rms.ui.application.tools.shadereditors.BoneShaderEditPanel;
import com.hiveworkshop.rms.ui.application.tools.shadereditors.GridShaderEditPanel;
import com.hiveworkshop.rms.ui.application.tools.shadereditors.MeshShaderEditPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
	private static class ImportModelPart extends TwiFunction{

		public ImportModelPart() {
			super("Import Model Part By Bone Chain", ImportModelPart::doStuff);
		}
		private static void doStuff(ModelHandler modelHandler) {
			EditableModel donModel = ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null);
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
			EditableModel donModel = ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null);
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
			EditableModel donModel = ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null);
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
			EditableModel donModel = ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null);
			if (donModel != null) {
				SpliceGeosetPanel panel = new SpliceGeosetPanel(donModel, modelHandler);
				FramePopup.show(panel, ProgramGlobals.getMainPanel(), "Splice Geoset(s)");
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
				MeshShaderEditPanel.show(ProgramGlobals.getMainPanel(), modelHandler.getPreviewRenderModel().getBufferFiller());
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

	private static class GridShaderEditor extends TwiFunction {

		public GridShaderEditor() {
			super("Grid Shader Editor", GridShaderEditor::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			if(modelHandler != null && modelHandler.getRenderModel() != null){
				GridShaderEditPanel.show(ProgramGlobals.getMainPanel(), modelHandler.getRenderModel().getBufferFiller());
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

		private static void doStuff() {
			TextureCompositionPanel.showPanel(ProgramGlobals.getMainPanel());
		}
	}
	private static class LinearizeSelected extends TwiFunction{

		public LinearizeSelected() {
			super("Change Transform Interpolation For Selected Nodes", LinearizeSelected::linearizeAnimations);
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
					"Change Transform Interpolation For Selected Nodes", JOptionPane.OK_CANCEL_OPTION);
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

				UndoAction action = new CompoundAction("Set Interpolation To " + interpolationType, interpTypActions, ModelStructureChangeListener.changeListener::materialsListChanged);
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
	public static JMenuItem getTextShaderStuffNodeMenuItem() {
		return new NodeShaderEditor().getMenuItem();
	}
	public static JMenuItem getTextShaderStuffGridMenuItem() {
		return new GridShaderEditor().getMenuItem();
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
		public TwiFunction(String name, Runnable runnable) {
			this.name = name;
			menuItem = new JMenuItem(getAsAction(runnable));
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
		private AbstractAction getAsAction(Runnable runnable) {
			return new AbstractAction(name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					runnable.run();
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
