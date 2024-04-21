package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;

public class SetFromOtherRibbonAction implements UndoAction {
	private final RibbonEmitter emitter;
	private final RibbonEmitter old;
	private final RibbonEmitter other;

	/**
	 * Sets every setting except name, parent and AnimFlags (timelines) to the same as for other
	 **/
	public SetFromOtherRibbonAction(RibbonEmitter emitter, RibbonEmitter other) {
		this.emitter = emitter;
		this.other = other;
		old = emitter.copy();
	}

	@Override
	public SetFromOtherRibbonAction undo() {
		setSettings(old);
		return this;
	}

	@Override
	public SetFromOtherRibbonAction redo() {
		setSettings(other);
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Emitter " + emitter.getName();
	}

	private void setSettings(RibbonEmitter emitter2) {
		emitter.setDontInheritTranslation(emitter2.getDontInheritTranslation());
		emitter.setDontInheritRotation(emitter2.getDontInheritRotation());
		emitter.setDontInheritScaling(emitter2.getDontInheritScaling());
		emitter.setBillboarded(emitter2.getBillboarded());
		emitter.setBillboardLockX(emitter2.getBillboardLockX());
		emitter.setBillboardLockY(emitter2.getBillboardLockY());
		emitter.setBillboardLockZ(emitter2.getBillboardLockZ());
		emitter.setPivotPoint(emitter2.getPivotPoint());
		emitter.setBindPoseM4(emitter2.getBindPoseM4());

		emitter.setHeightAbove(emitter2.getHeightAbove());
		emitter.setHeightBelow(emitter2.getHeightBelow());
		emitter.setAlpha(emitter2.getAlpha());
		emitter.setTextureSlot(emitter2.getTextureSlot());
		emitter.setLifeSpan(emitter2.getLifeSpan());
		emitter.setGravity(emitter2.getGravity());
		emitter.setEmissionRate(emitter2.getEmissionRate());
		emitter.setRows(emitter2.getRows());
		emitter.setColumns(emitter2.getColumns());
		emitter.setMaterial(emitter2.getMaterial());
		emitter.setStaticColor(emitter2.getStaticColor());
	}
}
