package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;

/**
 * RibbonEmitter specific animation tags
 * 	KRHA - Ribbon emitter height above
 * 	KRHB - Ribbon emitter height below
 * 	KRAL - Ribbon emitter alpha
 * 	KRCO - Ribbon emitter color
 * 	KRTX - Ribbon emitter texture slot
 * 	KRVS - Ribbon emitter visibility
 */

public class RibbonEmitter extends EmitterIdObject {
	double heightAbove = 0;
	double heightBelow = 0;
	double alpha = 0;
	int textureSlot = 0; // which slot int rows*cols
	double lifeSpan = 0;
	double gravity = 0;
	int emissionRate = 0;
	int rows = 0;
	int columns = 0;
	Material material;
	Vec3 staticColor = new Vec3(1, 1, 1);

	//https://www.hiveworkshop.com/threads/ribbon-emitters-from-a-mesh-perspective.239816/
	public RibbonEmitter() {

	}

	@Override
	public int getBlendSrc() {
		return 0;
	}

	@Override
	public int getBlendDst() {
		return 0;
	}

	public RibbonEmitter(final String name) {
		this.name = name;
	}

	public RibbonEmitter(final RibbonEmitter emitter) {
		super(emitter);

		heightAbove = emitter.heightAbove;
		heightBelow = emitter.heightBelow;
		alpha = emitter.alpha;
		textureSlot = emitter.textureSlot;
		lifeSpan = emitter.lifeSpan;
		gravity = emitter.gravity;
		emissionRate = emitter.emissionRate;
		rows = emitter.rows;
		columns = emitter.columns;
		material = emitter.material;
		staticColor = new Vec3(emitter.staticColor);
	}

	@Override
	public RibbonEmitter copy() {
		return new RibbonEmitter(this);
	}

	public double getHeightAbove() {
		return heightAbove;
	}

	public void setHeightAbove(final double heightAbove) {
		this.heightAbove = heightAbove;
	}

	public double getHeightBelow() {
		return heightBelow;
	}

	public void setHeightBelow(final double heightBelow) {
		this.heightBelow = heightBelow;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(final double alpha) {
		this.alpha = alpha;
	}

	public int getTextureSlot() {
		return textureSlot;
	}

	public void setTextureSlot(final int textureSlot) {
		System.out.println("RE, textureSlot: " + textureSlot);
		if(textureSlot != 0){
			JOptionPane.showMessageDialog(null, "RE TextureSlot: " + textureSlot + "!!!");
		}
		this.textureSlot = textureSlot;
	}

	public double getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(final double lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public double getGravity() {
		return gravity;
	}

	public void setGravity(final double gravity) {
		this.gravity = gravity;
	}

	public int getEmissionRate() {
		return emissionRate;
	}

	public void setEmissionRate(final int emissionRate) {
		this.emissionRate = emissionRate;
	}

	public int getRows() {
		return rows;
	}

	@Override
	public int getCols() {
		return columns;
	}

	@Override
	public boolean isRibbonEmitter() {
		return true;
	}

	public void setRows(final int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(final int columns) {
		this.columns = columns;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(final Material material) {
		this.material = material;
	}

	public Vec3 getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vec3 staticColor) {
		this.staticColor = staticColor;
	}

	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}
}
