package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddCameraAction;
import com.hiveworkshop.rms.editor.actions.model.AddFaceEffectAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetMaterialShaderStringAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.AddBirthDeathSequences;
import com.hiveworkshop.rms.ui.application.AddSingleAnimationActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.tools.AddParticlePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class AddMenu extends JMenu {

	public AddMenu() {
		super("Add");
		setMnemonic(KeyEvent.VK_A);
		getAccessibleContext().setAccessibleDescription("Allows the user to add new components to the model.");

		add(AddParticlePanel.getParticleMenu());
		add(getAnimationMenu());

		add(createMenuItem("Material", KeyEvent.VK_M, e -> addNewMaterial()));
		add(createMenuItem("Attachment", KeyEvent.VK_T, e -> addIdObject(new Attachment("New Attachment"))));
		add(createMenuItem("Bone", KeyEvent.VK_B, e -> addIdObject(new Bone("New Bone"))));
		add(createMenuItem("Camera", KeyEvent.VK_C, e -> addCamera()));
		add(createMenuItem("CollisionShape", KeyEvent.VK_S, e -> addIdObject(new CollisionShape("New CollisionShape", MdlxCollisionShape.Type.SPHERE, 100, new Vec3()))));
		add(createMenuItem("EventObject", KeyEvent.VK_E, e -> addIdObject(new EventObject("New EventObject"))));
		add(createMenuItem("FaceFX", KeyEvent.VK_F, e -> addFaceFX()));
		add(createMenuItem("Helper", KeyEvent.VK_H, e -> addIdObject(new Helper("New Helper"))));
		add(createMenuItem("Light", KeyEvent.VK_L, e -> addIdObject(new Light("New Light"))));
	}

	private JMenu getAnimationMenu() {
		JMenu animationMenu = createMenu("Animation", KeyEvent.VK_A);

		animationMenu.add(createMenuItem("Empty", KeyEvent.VK_E, e -> AddSingleAnimationActions.addEmptyAnimation()));
		animationMenu.add(createMenuItem("GlobalSeq", KeyEvent.VK_G, e -> AddSingleAnimationActions.addGlobalSeq()));
		animationMenu.add(createMenuItem("TextureAnim", KeyEvent.VK_T, e -> AddSingleAnimationActions.addTextureAnim()));
		animationMenu.add(createMenuItem("Rising/Falling Birth/Death", KeyEvent.VK_R, e -> AddBirthDeathSequences.riseFallBirthActionRes()));
		animationMenu.add(getImportAnimMenu());

		return animationMenu;
	}

	private JMenu getImportAnimMenu() {
		JMenu singleAnimationMenu = createMenu("Import", KeyEvent.VK_I);
//		JMenu singleAnimationMenu = createMenu("From Source", KeyEvent.VK_I);
		singleAnimationMenu.add(createMenuItem("From File", KeyEvent.VK_F, e -> AddSingleAnimationActions.addAnimationFromFile()));
		singleAnimationMenu.add(createMenuItem("From Unit", KeyEvent.VK_U, e -> AddSingleAnimationActions.addAnimationFromUnit()));
		singleAnimationMenu.add(createMenuItem("From Model", KeyEvent.VK_M, e -> AddSingleAnimationActions.addAnimFromModel()));
		singleAnimationMenu.add(createMenuItem("From Object", KeyEvent.VK_O, e -> AddSingleAnimationActions.addAnimationFromObject()));
		return singleAnimationMenu;
	}

	private static void addIdObject(IdObject idObject) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			modelPanel.getModelHandler().getUndoManager().pushAction(new AddNodeAction(current, idObject, ModelStructureChangeListener.changeListener).redo());
		}
	}
	private static void addCamera() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			Camera camera = new Camera("New Camera");
			camera.setPosition(new Vec3(100, 0, 90));
			camera.setTargetPosition(new Vec3(0, 0, 80));
			modelPanel.getModelHandler().getUndoManager().pushAction(new AddCameraAction(current, camera, ModelStructureChangeListener.changeListener).redo());
		}
	}
	private static void addFaceFX() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			FaceEffect faceEffect = new FaceEffect("Node", "");
			modelPanel.getModelHandler().getUndoManager().pushAction(new AddFaceEffectAction(faceEffect, current, ModelStructureChangeListener.changeListener).redo());
		}
	}

	public static void addNewMaterial() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel model = modelPanel.getModel();
			Bitmap texture;
			if (model.getTextures().isEmpty()) {
				String path = model.getFormatVersion() == 1000 ? "Textures\\White.dds" : "Textures\\White.blp";
				texture = new Bitmap(path);
			} else {
				texture = model.getTexture(0);
			}

			Material material = new Material(new Layer(texture));

			if (model.getFormatVersion() == 1000) {
				new SetMaterialShaderStringAction(model, material, "Shader_HD_DefaultUnit", null).redo();
			}
			UndoAction action = new AddMaterialAction(material, model, ModelStructureChangeListener.changeListener);
			modelPanel.getModelHandler().getUndoManager().pushAction(action.redo());
		}
	}
}