package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RemoveUnusedPanel extends JPanel {

	boolean keepParents = false;
	boolean keepParentsOfObjectNodes = false;

	boolean preserveBindJoint = true;
	String bindJntName = "hd_anim_bind_jnt";
	Map<Class<? extends IdObject>, Boolean> doMap = new LinkedHashMap<>();
	{
		doMap.put(Bone.class, true);
		doMap.put(Light.class, true);
		doMap.put(Helper.class, true);
		doMap.put(ParticleEmitter.class, true);
		doMap.put(ParticleEmitter2.class, true);
		doMap.put(ParticleEmitterPopcorn.class, true);
		doMap.put(RibbonEmitter.class, true);
		doMap.put(EventObject.class, true);
	}

	public RemoveUnusedPanel(){
		super(new MigLayout());
		add(new JLabel("Choose types to weed"), "wrap");

		for (Class<? extends IdObject> nodeClass : doMap.keySet()) {
			add(CheckBox.create(nodeClass.getSimpleName(), doMap.get(nodeClass), b -> doMap.put(nodeClass, b)), "wrap");
		}

		JCheckBox preserve = CheckBox.create("Preserve", preserveBindJoint, b -> preserveBindJoint = b);
		preserve.setToolTipText("Don't remove this node or it's children even if unused. Use this to ensure nodes used by FaceFX is kept.");
		add(preserve, "split");
		add(new TwiTextField(bindJntName, 12, s -> bindJntName = s), "wrap");

		JCheckBox keep_parents = CheckBox.create("Keep parents of used nodes", keepParents, b -> keepParents = b);
		keep_parents.setToolTipText("Keep unused node if it has any used child node. This will preserve node hierarchy.");
		add(keep_parents, "wrap");
	}

	public Map<Class<? extends IdObject>, Boolean> getDoMap() {
		return doMap;
	}

	public String getBindJntName() {
		return preserveBindJoint ? bindJntName : null;
	}

	public boolean keepParents() {
		return keepParents;
	}
}
