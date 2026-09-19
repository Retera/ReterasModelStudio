package com.hiveworkshop.wc3.pkb.sim;

import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;
import com.hiveworkshop.wc3.pkb.bind.LayerProgram;
import com.hiveworkshop.wc3.pkb.bind.LayerRenderer;

/**
 * The per-frame, per-renderer view of a layer's particles: parallel float
 * arrays per render slot (null when the effect does not bind that slot) and
 * an alive flag per particle.
 */
public final class RenderPacket {
	public static final int SLOT_POSITION = 0;
	public static final int SLOT_SIZE = 1;
	public static final int SLOT_ENABLED = 2;
	public static final int SLOT_ORIENTATION = 3;
	public static final int SLOT_AXIS0 = 4;
	public static final int SLOT_AXIS1 = 5;
	public static final int SLOT_ROTATION = 6;
	public static final int SLOT_COLOR = 7;
	public static final int SLOT_TEXTURE_ID = 8;
	public static final int SLOT_TEXTURE_U = 9;
	public static final int SLOT_CURSOR = 10;
	public static final int SLOT_COUNT = 11;

	public int layerIndex;
	public int rendererIndex;
	public LayerRenderer renderer;
	public int particleCount;
	public boolean[] alive = new boolean[0];
	public final float[][] slots = new float[SLOT_COUNT][];
	public final int[] components = new int[SLOT_COUNT];

	/** Names of the externals feeding each slot. */
	public static final class InputMap {
		public final String[] names = new String[SLOT_COUNT];
	}

	public static int componentsForSlot(final int slot, final int rendererClass) {
		switch (slot) {
		case SLOT_POSITION:
			return 3;
		case SLOT_SIZE:
			return rendererClass == LayerRenderer.CLASS_MESH ? 3 : 1;
		case SLOT_ENABLED:
			return 1;
		case SLOT_ORIENTATION:
			return 4;
		case SLOT_AXIS0:
		case SLOT_AXIS1:
			return 3;
		case SLOT_ROTATION:
			return 1;
		case SLOT_COLOR:
			return 4;
		case SLOT_TEXTURE_ID:
		case SLOT_TEXTURE_U:
		case SLOT_CURSOR:
			return 1;
		default:
			return 0;
		}
	}

	private static String findBySuffix(final LayerProgram lp, final String suffix) {
		for (final com.hiveworkshop.wc3.pkb.bind.ProgramDescriptor p : new com.hiveworkshop.wc3.pkb.bind.ProgramDescriptor[] {
				lp.initProgram, lp.physicsProgram, lp.timeFixedProgram, lp.timeVaryingProgram }) {
			for (final ExternalBinding ext : p.externals) {
				if (ext.name.endsWith(suffix)) {
					return ext.name;
				}
			}
		}
		return null;
	}

	private static String findByPrefix(final LayerProgram lp, final String prefix) {
		for (final com.hiveworkshop.wc3.pkb.bind.ProgramDescriptor p : new com.hiveworkshop.wc3.pkb.bind.ProgramDescriptor[] {
				lp.initProgram, lp.physicsProgram, lp.timeFixedProgram, lp.timeVaryingProgram }) {
			for (final ExternalBinding ext : p.externals) {
				if (ext.name.startsWith(prefix)) {
					return ext.name;
				}
			}
		}
		return null;
	}

	private static String findRenderInput(final LayerProgram lp, final String suffix, final String prefix) {
		final String n = findBySuffix(lp, suffix);
		return n != null ? n : findByPrefix(lp, prefix);
	}

	/** The reference's fallback: guess the render streams from external names. */
	public static InputMap inferInputMap(final LayerProgram lp) {
		final InputMap m = new InputMap();
		m.names[SLOT_POSITION] = findRenderInput(lp, "__Position", "Position_");
		m.names[SLOT_SIZE] = findRenderInput(lp, "__Size", "Size_");
		m.names[SLOT_ENABLED] = findRenderInput(lp, "__Enabled", "Enabled_");
		m.names[SLOT_ORIENTATION] = findRenderInput(lp, "__Orientation", "Orientation_");
		m.names[SLOT_AXIS0] = findRenderInput(lp, "__Axis", "Axis_");
		m.names[SLOT_AXIS1] = findRenderInput(lp, "__NormalAxis", "NormalAxis_");
		m.names[SLOT_ROTATION] = findRenderInput(lp, "__Rotation", "Rotation_");
		m.names[SLOT_COLOR] = findRenderInput(lp, "__Color", "Color_");
		m.names[SLOT_TEXTURE_ID] = findRenderInput(lp, "__TextureID", "TextureID_");
		return m;
	}

	/** Input map from the renderer's declared particle inputs (indexes into the layer's field names). */
	public static InputMap inputMapFromAsset(final LayerRenderer renderer, final LayerProgram layer) {
		final InputMap map = new InputMap();
		final String[] fields = layer.renderFieldNames;
		for (final LayerRenderer.ParticleInput in : renderer.particleInputs) {
			int slot = -1;
			if (!in.additionalFieldName.isEmpty()) {
				switch (in.additionalFieldName) {
				case "Color":
					slot = SLOT_COLOR;
					break;
				case "TextureID":
					slot = SLOT_TEXTURE_ID;
					break;
				case "TextureU":
					slot = SLOT_TEXTURE_U;
					break;
				case "Cursor":
					slot = SLOT_CURSOR;
					break;
				case "Orientation":
					slot = SLOT_ORIENTATION;
					break;
				case "Axis0":
				case "Axis":
					slot = SLOT_AXIS0;
					break;
				case "NormalAxis":
				case "Axis1":
					slot = SLOT_AXIS1;
					break;
				case "Rotation":
					slot = SLOT_ROTATION;
					break;
				default:
					break;
				}
			} else {
				switch (in.semantic) {
				case 0:
					slot = SLOT_POSITION;
					break;
				case 1:
					slot = SLOT_SIZE;
					break;
				case 2:
					slot = SLOT_ENABLED;
					break;
				case 4:
				case 8:
					slot = SLOT_AXIS0;
					break;
				case 5:
					slot = SLOT_AXIS1;
					break;
				case 6:
					slot = SLOT_ROTATION;
					break;
				case 12:
					slot = SLOT_ORIENTATION;
					break;
				default:
					break;
				}
			}
			if ((slot >= 0) && (in.indexInStorage >= 0) && (in.indexInStorage < fields.length)
					&& !fields[in.indexInStorage].isEmpty()) {
				map.names[slot] = fields[in.indexInStorage];
			}
		}
		return map;
	}

	private static ExternalBinding lookupBinding(final LayerProgram layer, final String name) {
		if ((name == null) || name.isEmpty()) {
			return null;
		}
		ExternalBinding b = ExternalBinding.findByName(layer.initProgram.externals, name);
		if (b == null) {
			b = ExternalBinding.findByName(layer.timeFixedProgram.externals, name);
		}
		if (b == null) {
			b = ExternalBinding.findByName(layer.timeVaryingProgram.externals, name);
		}
		if (b == null) {
			b = ExternalBinding.findByName(layer.physicsProgram.externals, name);
		}
		return b;
	}

	/** Fills this packet from the pool, reusing the arrays when the pool size is unchanged. */
	public void extract(final ParticlePool pool, final LayerProgram layer, final int layerIndex,
			final LayerRenderer renderer, final int rendererIndex, final InputMap mapping) {
		this.layerIndex = layerIndex;
		this.rendererIndex = rendererIndex;
		this.renderer = renderer;
		final int n = pool.size();
		particleCount = n;
		if (alive.length != n) {
			alive = new boolean[n];
		}
		for (int i = 0; i < n; i++) {
			alive[i] = !pool.particle(i).isDead();
		}
		final ExternalStore store = pool.externals();
		for (int s = 0; s < SLOT_COUNT; s++) {
			final int comps = componentsForSlot(s, renderer.rendererClass);
			final ExternalBinding b = lookupBinding(layer, mapping.names[s]);
			components[s] = comps;
			if ((b == null) || (comps == 0) || (n == 0)) {
				slots[s] = null;
				continue;
			}
			final int slotIdx = b.resolvedSlot();
			if (slotIdx >= store.slotCount()) {
				slots[s] = null;
				continue;
			}
			float[] buf = slots[s];
			if ((buf == null) || (buf.length != (n * comps))) {
				buf = new float[n * comps];
				slots[s] = buf;
			}
			for (int i = 0; i < n; i++) {
				if (!alive[i]) {
					for (int c = 0; c < comps; c++) {
						buf[(i * comps) + c] = 0;
					}
					continue;
				}
				for (int c = 0; c < comps; c++) {
					buf[(i * comps) + c] = store.loadLane(slotIdx, i, c);
				}
			}
		}
	}
}
