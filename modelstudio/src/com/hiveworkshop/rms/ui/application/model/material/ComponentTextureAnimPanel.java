package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.QuatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.Vec3ValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentTextureAnimPanel extends ComponentPanel<TextureAnim> {

	private final JPanel topPanel;
	private Vec3ValuePanel transPanel;
	private Vec3ValuePanel scalePanel;
	private QuatValuePanel rotPanel;

	public ComponentTextureAnimPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fillx, gap 0", "[grow]", "[]"));

		transPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_TRANSLATION);
		scalePanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_SCALING);
		rotPanel = new QuatValuePanel(modelHandler, MdlUtils.TOKEN_ROTATION);

		topPanel = new JPanel(new MigLayout());
		add(topPanel);
	}

	@Override
	public ComponentPanel<TextureAnim> setSelectedItem(TextureAnim itemToSelect) {
		selectedItem = itemToSelect;

		topPanel.removeAll();
		for (AnimFlag<?> animFlag : selectedItem.getAnimFlags()) {
			topPanel.add(new JLabel(animFlag.getName()), "wrap");
			if (animFlag instanceof Vec3AnimFlag) {
				if (animFlag.getName().equals(MdlUtils.TOKEN_TRANSLATION)) {
					transPanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) animFlag, selectedItem, MdlUtils.TOKEN_TRANSLATION);
					topPanel.add(transPanel);
				}
				if (animFlag.getName().equals(MdlUtils.TOKEN_SCALING)) {
					scalePanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) animFlag, selectedItem, MdlUtils.TOKEN_SCALING);
					topPanel.add(scalePanel);
				}
			} else if (animFlag instanceof QuatAnimFlag) {
				rotPanel.reloadNewValue(new Quat(0, 0, 0, 1), (QuatAnimFlag) animFlag, selectedItem, MdlUtils.TOKEN_ROTATION);
				topPanel.add(rotPanel);
			}
		}
		revalidate();
		repaint();
		return this;
	}
}
