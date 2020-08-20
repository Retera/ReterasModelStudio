package com.hiveworkshop.wc3.mdl;

import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxRibbonEmitter;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * RibbonEmitter class, these are the things most people would think of as a
 * particle emitter, I think. Blizzard favored use of these over
 * ParticleEmitters and I do too simply because I so often recycle data and
 * there are more of these to use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class RibbonEmitter extends IdObject {
	double heightAbove;
	double heightBelow;
	double alpha;
	double textureSlot;
	double lifeSpan = 0;
	double gravity = 0;
	int emissionRate = 0;
	int rows = 0;
	int columns = 0;
	int materialID = 0;
	Material material;
	Vertex staticColor;

	private RibbonEmitter() {

	}

	public RibbonEmitter(final String name) {
		this.name = name;
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
		setStaticColor(new Vertex(MdlxUtils.flipRGBtoBGR(emitter.color)));
		setLifeSpan(emitter.lifeSpan);
		setEmissionRate((int)emitter.emissionRate);
		setRows((int)emitter.rows);
		setColumns((int)emitter.columns);
		setMaterialId(emitter.materialId);
		setGravity(emitter.gravity);
	}

	public MdlxRibbonEmitter toMdlx() {
		MdlxRibbonEmitter emitter = new MdlxRibbonEmitter();

		objectToMdlx(emitter);

		emitter.textureSlot = (long)getTextureSlot();
		emitter.heightAbove = (float)getHeightAbove();
		emitter.heightBelow = (float)getHeightBelow();
		emitter.alpha = (float)getAlpha();
		emitter.color = MdlxUtils.flipRGBtoBGR(getStaticColor().toFloatArray());
		emitter.lifeSpan = (float)getLifeSpan();
		emitter.emissionRate = getEmissionRate();
		emitter.rows = getRows();
		emitter.columns = getColumns();
		emitter.materialId = getMaterialId();
		emitter.gravity = (float)getGravity();

		return emitter;
	}

	@Override
	public IdObject copy() {
		final RibbonEmitter x = new RibbonEmitter();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.heightAbove = heightAbove;
		x.heightBelow = heightBelow;
		x.alpha = alpha;
		x.textureSlot = textureSlot;
		x.lifeSpan = lifeSpan;
		x.gravity = gravity;
		x.emissionRate = emissionRate;
		x.rows = rows;
		x.columns = columns;
		x.materialID = materialID;
		x.material = material;
		x.staticColor = new Vertex(staticColor);
		x.addAll(getAnimFlags());
		return x;
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

	public Vertex getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vertex staticColor) {
		this.staticColor = staticColor;
	}

	@Override
	public void add(final String flag) {
		System.err.println("ERROR: RibbonEmitter given unknown flag: " + flag);
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
