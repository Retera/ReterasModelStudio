package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParentAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class IdObjectTypeChanger {

	public static void toBone(IdObject idObject, ModelHandler modelHandler){

		doReplaceNode(idObject, new Bone(), modelHandler);
	}

	public static void doReplaceNode(IdObject oldNode, IdObject newNode, ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		UndoAction undoAction = getReplaceNodeAction(oldNode, newNode, model, changeListener::nodesUpdated);
		modelHandler.getUndoManager().pushAction(undoAction.redo());
	}

	public static void changeNodeType(IdObject idObject, ModelHandler modelHandler) {
		JPanel panel = new JPanel(new MigLayout("ins 0"));
		String typeName = idObject.getName() + " (" + idObject.getClass().getSimpleName() + ")";
		String title = "Change Type of " + typeName;

		NodeType[] values = NodeType.values();
		SmartButtonGroup buttonGroup = new SmartButtonGroup();
		for(NodeType type : values){
			buttonGroup.addJRadioButton(type.getName(), null);
		}
		int ordinal = NodeType.getType(idObject).ordinal();
		buttonGroup.setSelectedIndex(ordinal);
		buttonGroup.getButton(ordinal).setEnabled(false);

		panel.add(new JLabel("Choose new type for " + typeName), "wrap");

		if(idObject instanceof Bone && !(idObject instanceof Helper)){
			JLabel label = new JLabel("This bone will be removed from any matrix or skin it might be used in.");
			label.setFont(label.getFont().deriveFont(Font.ITALIC));
			panel.add(label, "wrap");
		}
		panel.add(buttonGroup.getButtonPanel());

		int change_type = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, title, JOptionPane.OK_CANCEL_OPTION);
		if(change_type == JOptionPane.OK_OPTION){
			IdObject newNode = values[buttonGroup.getSelectedIndex()].getNewNode();
			if(idObject.getClass() != newNode.getClass()){
				IdObjectTypeChanger.doReplaceNode(idObject, newNode, modelHandler);
			}
		}
	}

	public static UndoAction getReplaceNodeAction(IdObject oldNode, IdObject newNode, EditableModel model, Runnable nodesUpdated) {
		newNode.setName(oldNode.getName());
		newNode.setParent(oldNode.getParent());
		newNode.setPivotPoint(oldNode.getPivotPoint());
		if(oldNode.getBindPose() != null){
			newNode.setBindPose(oldNode.getBindPose().clone());
		}

		newNode.setBillboarded(oldNode.getBillboarded());
		newNode.setBillboardLockX(oldNode.getBillboardLockX());
		newNode.setBillboardLockY(oldNode.getBillboardLockY());
		newNode.setBillboardLockZ(oldNode.getBillboardLockZ());

		newNode.setDontInheritTranslation(oldNode.getDontInheritTranslation());
		newNode.setDontInheritScaling(oldNode.getDontInheritScaling());
		newNode.setDontInheritRotation(oldNode.getDontInheritRotation());

		for(AnimFlag<?> animFlag : oldNode.getAnimFlags()){
			newNode.add(animFlag.deepCopy());
		}

		List<UndoAction> undoActions = new ArrayList<>();
		undoActions.add(new DeleteNodesAction(oldNode, null, model));
		undoActions.add(new AddNodeAction(model, newNode, null));
		undoActions.add(new SetParentAction(oldNode.getChildrenNodes(), newNode, null));

		String actionName = "Turn " + oldNode.getName() + " into " + newNode.getClass().getSimpleName();
		return new CompoundAction(actionName, undoActions, nodesUpdated);
	}


	enum NodeType {
		ATTACHMENT("Attachment", () -> new Attachment(), Attachment.class),
		BONE("Bone", () -> new Bone(), Bone.class),
		COLLISION_SHAPE("CollisionShape", () -> new CollisionShape(), CollisionShape.class),
		EVENT_OBJECT("EventObject", () -> new EventObject(), EventObject.class),
		HELPER("Helper", () -> new Helper(), Helper.class),
		LIGHT("Light", () -> new Light(), Light.class),
		PARTICLE_EMITTER("ParticleEmitter", () -> new ParticleEmitter(), ParticleEmitter.class),
		PARTICLE_EMITTER2("ParticleEmitter2", () -> new ParticleEmitter2(), ParticleEmitter2.class),
		POPCORN_EMITTER("PopcornEmitter", () -> new ParticleEmitterPopcorn(), ParticleEmitterPopcorn.class),
		RIBBON_EMITTER("RibbonEmitter", () -> new RibbonEmitter(), RibbonEmitter.class);
		final String name;
		final Supplier<IdObject> nodeSupplier;
		final Class<? extends IdObject> nodeClass;

		NodeType(String name, Supplier<IdObject> nodeSupplier, Class<? extends IdObject> nodeClass){
			this.name = name;
			this.nodeSupplier = nodeSupplier;
			this.nodeClass = nodeClass;
		}

		public String getName() {
			return name;
		}
		public static NodeType getType(IdObject node) {
			for(NodeType nodeType : NodeType.values()){
				if(node.getClass() == nodeType.nodeClass){
					return nodeType;
				}
			}
			return HELPER;
		}

		public Supplier<IdObject> getNodeSupplier() {
			return nodeSupplier;
		}

		public IdObject getNewNode(){
			return nodeSupplier.get();
		}
	}
}
