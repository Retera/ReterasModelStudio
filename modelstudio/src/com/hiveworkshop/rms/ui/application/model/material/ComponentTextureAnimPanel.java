package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.TwiTextEditor.FlagPanel;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentTextureAnimPanel extends ComponentPanel<TextureAnim> {

	private final JPanel topPanel;
	private final FlagPanel<Vec3> transPanel;
	private final FlagPanel<Vec3> scalePanel;
	private final FlagPanel<Quat> rotPanel;

	public ComponentTextureAnimPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);
		setLayout(new MigLayout("fillx, gap 0", "[grow]", "[]"));

		transPanel = new FlagPanel<>(MdlUtils.TOKEN_TRANSLATION, this::parseVec3, new Vec3(0,0,0), modelHandler);
		scalePanel = new FlagPanel<>(MdlUtils.TOKEN_SCALING, this::parseVec3, new Vec3(1,1,1), modelHandler);
		rotPanel = new FlagPanel<>(MdlUtils.TOKEN_ROTATION, this::parseQuat, new Quat(0,0,0, 1), modelHandler);

		topPanel = new JPanel(new MigLayout());
		topPanel.add(transPanel, "wrap");
		topPanel.add(scalePanel, "wrap");
		topPanel.add(rotPanel, "wrap");
		add(topPanel);
	}

	private Vec3 parseVec3(String s) {
		return Vec3.parseVec3(ValueParserUtil.getString(3,s));
	}
	private Quat parseQuat(String s) {
		return Quat.parseQuat(ValueParserUtil.getString(4,s));
	}

	@Override
	public ComponentPanel<TextureAnim> setSelectedItem(TextureAnim itemToSelect) {
		selectedItem = itemToSelect;

//		topPanel.removeAll();
//		for (AnimFlag<?> animFlag : selectedItem.getAnimFlags()) {
//			if (animFlag instanceof Vec3AnimFlag) {
//				if (animFlag.getName().equals(MdlUtils.TOKEN_TRANSLATION)) {
//					transPanel.update(selectedItem, (Vec3AnimFlag) animFlag, new Vec3(0, 0, 0));
//					topPanel.add(transPanel);
//				}
//				if (animFlag.getName().equals(MdlUtils.TOKEN_SCALING)) {
//					scalePanel.update(selectedItem, (Vec3AnimFlag) animFlag, new Vec3(1, 1, 1));
//					topPanel.add(scalePanel);
//				}
//			} else if (animFlag instanceof QuatAnimFlag) {
//				rotPanel.update(selectedItem, (QuatAnimFlag) animFlag, new Quat(0, 0, 0, 1));
//				topPanel.add(rotPanel);
//			}
//		}
		transPanel.update(selectedItem, (Vec3AnimFlag) selectedItem.find(MdlUtils.TOKEN_TRANSLATION), new Vec3(0, 0, 0));
		scalePanel.update(selectedItem, (Vec3AnimFlag) selectedItem.find(MdlUtils.TOKEN_SCALING), new Vec3(1, 1, 1));
		rotPanel.update(selectedItem, (QuatAnimFlag) selectedItem.find(MdlUtils.TOKEN_ROTATION), new Quat(0, 0, 0, 1));
		revalidate();
		repaint();
		return this;
	}
}
