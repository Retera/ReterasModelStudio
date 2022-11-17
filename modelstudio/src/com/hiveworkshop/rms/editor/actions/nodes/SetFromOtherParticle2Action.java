package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;

public class SetFromOtherParticle2Action implements UndoAction {
	ParticleEmitter2 emitter;
	ParticleEmitter2 old;
	ParticleEmitter2 other;

	/**
	 * Sets every setting except name, parent, objectId and AnimFlags (timelines) to the same as for other
	 **/
	public SetFromOtherParticle2Action(ParticleEmitter2 emitter, ParticleEmitter2 other) {
		this.emitter = emitter;
		this.other = other;
		old = emitter.copy();
	}

	@Override
	public UndoAction undo() {
		setSettings(old);
		return this;
	}

	@Override
	public UndoAction redo() {
		setSettings(other);
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Emitter" + emitter.getName();
	}

	private void setSettings(ParticleEmitter2 emitter2) {
		emitter.setDontInheritTranslation(emitter2.getDontInheritTranslation());
		emitter.setDontInheritRotation(emitter2.getDontInheritRotation());
		emitter.setDontInheritScaling(emitter2.getDontInheritScaling());
		emitter.setBillboarded(emitter2.getBillboarded());
		emitter.setBillboardLockX(emitter2.getBillboardLockX());
		emitter.setBillboardLockY(emitter2.getBillboardLockY());
		emitter.setBillboardLockZ(emitter2.getBillboardLockZ());
		emitter.setPivotPoint(emitter2.getPivotPoint());
		if (emitter2.getBindPose() != null) {
			emitter.setBindPose(emitter2.getBindPose().clone());
		}


		emitter.setFilterMode(emitter2.getFilterMode());
		emitter.setHeadOrTail(emitter2.getHeadOrTail());
		emitter.setUnshaded(emitter2.getUnshaded());
		emitter.setSortPrimsFarZ(emitter2.getSortPrimsFarZ());
		emitter.setLineEmitter(emitter2.getLineEmitter());
		emitter.setUnfogged(emitter2.getUnfogged());
		emitter.setModelSpace(emitter2.getModelSpace());
		emitter.setXYQuad(emitter2.getXYQuad());
		emitter.setSquirt(emitter2.getSquirt());

		emitter.setSpeed(emitter2.getSpeed());
		emitter.setVariation(emitter2.getVariation());
		emitter.setLatitude(emitter2.getLatitude());
		emitter.setGravity(emitter2.getGravity());
		emitter.setEmissionRate(emitter2.getEmissionRate());
		emitter.setWidth(emitter2.getWidth());
		emitter.setLength(emitter2.getLength());
		emitter.setLifeSpan(emitter2.getLifeSpan());
		emitter.setTailLength(emitter2.getTailLength());
		emitter.setTime(emitter2.getTime());
		emitter.setRows(emitter2.getRows());
		emitter.setColumns(emitter2.getColumns());
		emitter.setReplaceableId(emitter2.getReplaceableId());
		emitter.setPriorityPlane(emitter2.getPriorityPlane());

		emitter.setSegmentColor(0, emitter2.getSegmentColor(0));
		emitter.setSegmentColor(1, emitter2.getSegmentColor(1));
		emitter.setSegmentColor(2, emitter2.getSegmentColor(2));
		emitter.setAlpha(emitter2.getAlpha());
		emitter.setParticleScaling(emitter2.getParticleScaling());
		emitter.setHeadUVAnim(emitter2.getHeadUVAnim());
		emitter.setHeadDecayUVAnim(emitter2.getHeadDecayUVAnim());
		emitter.setTailUVAnim(emitter2.getTailUVAnim());
		emitter.setTailDecayUVAnim(emitter2.getTailDecayUVAnim());

		emitter.setTexture(emitter2.getTexture());
	}
}
