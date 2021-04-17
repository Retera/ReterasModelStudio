package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxRibbonEmitter;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

/**
 * RibbonEmitter class, these are the things most people would think of as a
 * particle emitter, I think. Blizzard favored use of these over
 * ParticleEmitters and I do too simply because I so often recycle data and
 * there are more of these to use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class RibbonEmitter extends IdObject {
	double heightAbove = 0;
	double heightBelow = 0;
	double alpha = 0;
	double textureSlot = 0;
	double lifeSpan = 0;
	double gravity = 0;
	int emissionRate = 0;
	int rows = 0;
	int columns = 0;
	int materialID = 0;
	Material material;
	Vec3 staticColor = new Vec3(1, 1, 1);

	public RibbonEmitter() {

	}

	public RibbonEmitter(final String name) {
		this.name = name;
	}

	public RibbonEmitter(final RibbonEmitter emitter) {
		copyObject(emitter);
		
		heightAbove = emitter.heightAbove;
		heightBelow = emitter.heightBelow;
		alpha = emitter.alpha;
		textureSlot = emitter.textureSlot;
		lifeSpan = emitter.lifeSpan;
		gravity = emitter.gravity;
		emissionRate = emitter.emissionRate;
		rows = emitter.rows;
		columns = emitter.columns;
		materialID = emitter.materialID;
		material = emitter.material;
		staticColor = new Vec3(emitter.staticColor);
	}

	public RibbonEmitter(final MdlxRibbonEmitter emitter) {
		if ((emitter.flags & 16384) != 16384) {
			System.err.println("MDX -> MDL error: A ribbon emitter '" + emitter.name
					+ "' not flagged as ribbon emitter in MDX!");
		}
		
		loadObject(emitter);

		setTextureSlot(emitter.textureSlot);
		setHeightAbove(emitter.heightAbove);
		setHeightBelow(emitter.heightBelow);
		setAlpha(emitter.alpha);
		setStaticColor(new Vec3(ModelUtils.flipRGBtoBGR(emitter.color)));
		setLifeSpan(emitter.lifeSpan);
		setEmissionRate((int) emitter.emissionRate);
		setRows((int) emitter.rows);
		setColumns((int) emitter.columns);
		setMaterialId(emitter.materialId);
		setGravity(emitter.gravity);
	}

	public MdlxRibbonEmitter toMdlx(EditableModel model) {
		final MdlxRibbonEmitter emitter = new MdlxRibbonEmitter();

		objectToMdlx(emitter, model);

		emitter.textureSlot = (long) getTextureSlot();
		emitter.heightAbove = (float) getHeightAbove();
		emitter.heightBelow = (float) getHeightBelow();
		emitter.alpha = (float) getAlpha();
		emitter.color = ModelUtils.flipRGBtoBGR(getStaticColor().toFloatArray());
		emitter.lifeSpan = (float) getLifeSpan();
		emitter.emissionRate = getEmissionRate();
		emitter.rows = getRows();
		emitter.columns = getColumns();
		emitter.materialId = getMaterialId();
		emitter.gravity = (float)getGravity();

		return emitter;
	}

	@Override
	public RibbonEmitter copy() {
		return new RibbonEmitter(this);
	}

	public void updateMaterialRef(final List<Material> mats) {
		if (getMaterialId() == -1) {
			material = null;
			return;
		}
		material = mats.get(getMaterialId());
	}

	public int getMaterialId() {
		return materialID;
	}

	public void setMaterialId(final int materialID) {
		this.materialID = materialID;
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

	public double getTextureSlot() {
		return textureSlot;
	}

	public void setTextureSlot(final double textureSlot) {
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
	public void apply(final IdObjectVisitor visitor) {
		visitor.ribbonEmitter(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}
}
