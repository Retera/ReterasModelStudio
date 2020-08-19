package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
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
	public static enum TimeDoubles {
		HeightAbove, HeightBelow, Alpha, TextureSlot;
	}

	public static enum LoneDoubles {
		LifeSpan, Gravity;
	}

	public static enum LoneInts {
		EmissionRate, Rows, Columns, MaterialID;
	}

	static final String[] timeDoubleNames = { "HeightAbove", "HeightBelow", "Alpha", "TextureSlot" };
	double[] timeDoubleData = new double[timeDoubleNames.length];
	static final String[] loneDoubleNames = { "LifeSpan", "Gravity" };
	double[] loneDoubleData = new double[loneDoubleNames.length];
	static final String[] loneIntNames = { "EmissionRate", "Rows", "Columns", "MaterialID" };
	Material material;
	int[] loneIntData = new int[loneIntNames.length];
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

		x.timeDoubleData = timeDoubleData.clone();
		x.loneDoubleData = loneDoubleData.clone();
		x.loneIntData = loneIntData.clone();
		x.material = material;
		x.staticColor = new Vertex(staticColor);

		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
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
		return loneIntData[3];
	}

	public void setMaterialId(final int i) {
		loneIntData[3] = i;
	}

	public double getHeightAbove() {
		return timeDoubleData[TimeDoubles.HeightAbove.ordinal()];
	}

	public void setHeightAbove(final double heightAbove) {
		timeDoubleData[TimeDoubles.HeightAbove.ordinal()] = heightAbove;
	}

	public double getHeightBelow() {
		return timeDoubleData[TimeDoubles.HeightBelow.ordinal()];
	}

	public void setHeightBelow(final double heightBelow) {
		timeDoubleData[TimeDoubles.HeightBelow.ordinal()] = heightBelow;
	}

	public double getAlpha() {
		return timeDoubleData[TimeDoubles.Alpha.ordinal()];
	}

	public void setAlpha(final double alpha) {
		timeDoubleData[TimeDoubles.Alpha.ordinal()] = alpha;
	}

	public double getTextureSlot() {
		return timeDoubleData[TimeDoubles.TextureSlot.ordinal()];
	}

	public void setTextureSlot(final double textureSlot) {
		timeDoubleData[TimeDoubles.TextureSlot.ordinal()] = textureSlot;
	}

	public double getLifeSpan() {
		return loneDoubleData[LoneDoubles.LifeSpan.ordinal()];
	}

	public void setLifeSpan(final double lifeSpan) {
		loneDoubleData[LoneDoubles.LifeSpan.ordinal()] = lifeSpan;
	}

	public double getGravity() {
		return loneDoubleData[LoneDoubles.Gravity.ordinal()];
	}

	public void setGravity(final double gravity) {
		loneDoubleData[LoneDoubles.Gravity.ordinal()] = gravity;
	}

	public int getEmissionRate() {
		return loneIntData[LoneInts.EmissionRate.ordinal()];
	}

	public void setEmissionRate(final int emissionRate) {
		loneIntData[LoneInts.EmissionRate.ordinal()] = emissionRate;
	}

	public int getRows() {
		return loneIntData[LoneInts.Rows.ordinal()];
	}

	public void setRows(final int rows) {
		loneIntData[LoneInts.Rows.ordinal()] = rows;
	}

	public int getColumns() {
		return loneIntData[LoneInts.Columns.ordinal()];
	}

	public void setColumns(final int columns) {
		loneIntData[LoneInts.Columns.ordinal()] = columns;
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
	public List<String> getFlags() {
		return new ArrayList<>();// Current ribbon implementation uses no
									// flags!
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
