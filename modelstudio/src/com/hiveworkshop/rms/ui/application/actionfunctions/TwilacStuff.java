package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.editor.actions.mesh.SnapCloseVertsAction;
import com.hiveworkshop.rms.editor.actions.nodes.BakeAndRebindAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParentAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetPivotAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.tools.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TwilacStuff {

	private static class BakeAndRebindToNull extends TwiFunction{
		public BakeAndRebindToNull() {
			super("BakeAndRebindToNull", BakeAndRebindToNull::rebindToNull);
		}

		private static void rebindToNull(ModelHandler modelHandler) {
			ModelView modelView = modelHandler.getModelView();
			List<UndoAction> rebindActions = new ArrayList<>();
			for (IdObject idObject : modelView.getSelectedIdObjects()) {
				System.out.println("rebinding " + idObject.getName());
				UndoAction action = new BakeAndRebindAction(idObject, null, modelHandler);
				rebindActions.add(action);
			}
			modelHandler.getUndoManager().pushAction(new CompoundAction("Baked and changed Parent", rebindActions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
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
			super("Import Model Part", ImportModelPart::doStuff);
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
			super("ImportModelSubAnim", ImportModelSubAnim::doStuff);
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

	private static class MergeBoneHelpers extends TwiFunction {

		public MergeBoneHelpers() {
			super("Merge unnecessary Helpers with Child Bones", MergeBoneHelpers::doStuff);
		}

		private static void doStuff(ModelHandler modelHandler) {
			EditableModel model = modelHandler.getModel();
			Set<Bone> bonesWOMotion = new HashSet<>();
			model.getBones().stream()
					.filter(b -> b.getAnimFlags().isEmpty() && b.getChildrenNodes().isEmpty() && b.getParent() instanceof Helper)
					.forEach(bonesWOMotion::add);

			List<UndoAction> undoActions = new ArrayList<>();
			List<IdObject> nodesToRemove = new ArrayList<>();

			for (Bone bone : bonesWOMotion){
				IdObject parent = bone.getParent();
				undoActions.add(new SetPivotAction(bone, parent.getPivotPoint(), null));
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
//						child.setParent(bone);
					}
				}
				undoActions.add(new SetParentAction(bone, parent.getParent(), null));
				nodesToRemove.add(parent);
//				bone.setParent(parent.getParent());
			}
			undoActions.add(new DeleteNodesAction(nodesToRemove, null, model));
			modelHandler.getUndoManager().pushAction(
					new CompoundAction("Merge unnececary Helpers", undoActions,
							ModelStructureChangeListener.changeListener::nodesUpdated)
							.redo());
		}
	}
	private static class LinearizeSelected extends TwiFunction{

		public LinearizeSelected() {
			super("Linearize Animations For Selected", LinearizeSelected::linearizeAnimations);
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

	public static JMenuItem getMergeBoneHelpersMenuItem() {
		return new MergeBoneHelpers().getMenuItem();
	}

	public static JMenuItem getLinearizeSelectedMenuItem() {
		return new LinearizeSelected().getMenuItem();
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
}
