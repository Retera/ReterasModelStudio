package com.hiveworkshop.wc3.mdl.render3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.gui.animedit.BasicTimeBoundProvider;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.gui.modelviewer.ViewerCamera;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.pkb.bind.EffectPlan;
import com.hiveworkshop.wc3.pkb.bind.LayerProgram;
import com.hiveworkshop.wc3.pkb.bind.LayerRenderer;
import com.hiveworkshop.wc3.pkb.sim.EffectRuntime;
import com.hiveworkshop.wc3.pkb.sim.RenderPacket;
import com.hiveworkshop.wc3.pkb.vm.ExecContext;

/**
 * Preview of a Reforged popcorn emitter: runs the baked PopcornFX effect in
 * the ported runtime and draws its billboard renderers with their textures
 * and blend modes. The emitter node's world transform, the MDX multiplier
 * tracks and the animation visibility guide feed the effect the way the game
 * feeds it.
 */
public class RenderPopcornEmitter {
	/** Game units per PopcornFX unit. */
	public static final float CORN_TO_GAME = 100f;
	public static final float GAME_TO_CORN = 0.01f;
	private static final float DEFAULT_DT = AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001f;
	private static final int[][] TEAM_COLORS = { { 255, 3, 3 }, { 0, 66, 255 }, { 28, 230, 185 }, { 84, 0, 129 },
			{ 255, 252, 1 }, { 254, 138, 14 }, { 32, 192, 0 }, { 229, 91, 176 }, { 149, 150, 151 }, { 126, 191, 241 },
			{ 16, 98, 70 }, { 78, 42, 4 }, { 155, 0, 0 }, { 0, 0, 195 }, { 0, 234, 255 }, { 190, 0, 254 },
			{ 235, 205, 144 }, { 248, 164, 139 }, { 191, 255, 128 }, { 220, 185, 235 }, { 40, 40, 40 },
			{ 235, 240, 255 }, { 0, 120, 30 }, { 164, 112, 54 } };

	private final ParticleEmitterPopcorn emitter;
	private final RenderModel instance;
	private final EffectPlan plan;
	private final Map<LayerRenderer, InternalResource> textures = new LinkedHashMap<>();
	private EffectRuntime runtime;
	private final EffectRuntime.FrameInputs inputs = new EffectRuntime.FrameInputs();
	private float effectAge;
	private boolean wasActive;
	private boolean nodeVisible;
	private int lastAnimationTime = -1;
	private BasicTimeBoundProvider lastAnimation;
	private final boolean cornEffectsScaling;

	// visibility guide
	private final Map<String, Boolean> visibilityGuide = new LinkedHashMap<>();
	private boolean guideDefault = true;
	private boolean isNonLoopingEffect;
	private String parsedGuide;

	// render scratch
	private float[] vertexBuffer = new float[6 * 5 * 256];
	private int[] sortPacket = new int[256];
	private int[] sortParticle = new int[256];
	private float[] sortDepth = new float[256];
	private Integer[] sortOrder = new Integer[0];
	private static final Vector3f v3Heap = new Vector3f();

	public RenderPopcornEmitter(final RenderModel instance, final ParticleEmitterPopcorn emitter,
			final EffectPlan plan, final RenderResourceAllocator allocator) {
		this.instance = instance;
		this.emitter = emitter;
		this.plan = plan;
		this.cornEffectsScaling = false; // the 0x40000 node flag is not carried by the editable model
		for (final LayerProgram layer : plan.layers) {
			for (final LayerRenderer renderer : layer.renderers) {
				if ((renderer.rendererClass == LayerRenderer.CLASS_BILLBOARD) && renderer.isRenderingEnabled
						&& !renderer.diffuseTexturePath.isEmpty() && !renderer.isDistortion) {
					textures.put(renderer, allocator.allocatePopcornTexture(new Bitmap(renderer.gameTexturePath()),
							emitter));
				}
			}
		}
		inputs.baseRngSeed = (int) ((System.identityHashCode(this) * 2654435761L) ^ 0xC0FFEE00L);
	}

	public ParticleEmitterPopcorn getEmitter() {
		return emitter;
	}

	public EffectPlan getPlan() {
		return plan;
	}

	public EffectRuntime getRuntime() {
		return runtime;
	}

	/** Called by the node update when the emitter node is shown; consumed by {@link #update()}. */
	public void fill() {
		nodeVisible = true;
	}

	private void resetRuntime() {
		if (runtime != null) {
			runtime.reset();
		}
		effectAge = 0;
	}

	// ------------------------------------------------------------------ per-frame update

	public void update() {
		final AnimatedRenderEnvironment environment = instance.getAnimatedRenderEnvironment();
		final boolean visibleNow = nodeVisible;
		nodeVisible = false;
		syncSequenceCycle(environment);
		final boolean guideEnabled = isEnabledForCurrentAnimation(environment);
		final float visibility = environment == null ? 1 : emitter.getRenderVisibility(environment);
		final boolean active = visibleNow && guideEnabled && instance.allowParticleSpawn() && (visibility > 0.5f);
		if (wasActive && !active) {
			resetRuntime();
		}
		wasActive = active;
		if ((runtime == null) && active) {
			runtime = new EffectRuntime(plan);
		}
		if (runtime == null) {
			return;
		}
		final float dt = DEFAULT_DT;
		inputs.dt = dt;
		inputs.effectAge = effectAge;
		inputs.effectIsRunning = active;
		fillEmitterTransform(inputs.emitterL2W);
		fillCamera(inputs.camera);
		if (active) {
			effectAge += dt;
		}
		runtime.setSpawnerEnabled(active);
		pushAttributes();
		runtime.tick(inputs);
	}

	/** Non-looping effects (attack/death guides) restart when the sequence loops, as in the game. */
	private void syncSequenceCycle(final AnimatedRenderEnvironment environment) {
		if (environment == null) {
			return;
		}
		final BasicTimeBoundProvider animation = environment.getCurrentAnimation();
		final int time = environment.getAnimationTime();
		if ((animation == lastAnimation) && (lastAnimationTime >= 0) && (time < lastAnimationTime)
				&& isNonLoopingEffect) {
			resetRuntime();
		}
		lastAnimation = animation;
		lastAnimationTime = time;
	}

	/**
	 * Node world transform, scale stripped, rotated +90 degrees about the local Z
	 * axis (the game's MDX to PopcornFX spawn frame), the node's world position
	 * (pivot through the world matrix) in corn units.
	 */
	private void fillEmitterTransform(final ExecContext.Mat4x3 out) {
		final RenderNode node = instance.getRenderNode(emitter);
		final Matrix4f m = node.getWorldMatrix();
		// column vectors of the 3x3 part (LWJGL mXY = column X, row Y)
		final float[] c0 = { m.m00, m.m01, m.m02 };
		final float[] c1 = { m.m10, m.m11, m.m12 };
		final float[] c2 = { m.m20, m.m21, m.m22 };
		final float s0 = length(c0);
		final float s1 = length(c1);
		final float s2 = length(c2);
		final float hostScale = (s0 + s1 + s2) / 3f;
		normalize(c0, s0);
		normalize(c1, s1);
		normalize(c2, s2);
		final float modelScale = cornEffectsScaling ? 1f : hostScale;
		// world = N * Rz(+90): new column 0 = N column 1, new column 1 = -N column 0
		for (int r = 0; r < 3; r++) {
			out.m[r][0] = c1[r] * modelScale;
			out.m[r][1] = -c0[r] * modelScale;
			out.m[r][2] = c2[r] * modelScale;
		}
		// RenderNode matrices map model-space points, so the node's position is the
		// matrix applied to its pivot (as the classic emitters do), not the last column.
		final Vector3f pivot = node.getPivot();
		out.m[0][3] = pivot.x * GAME_TO_CORN;
		out.m[1][3] = pivot.y * GAME_TO_CORN;
		out.m[2][3] = pivot.z * GAME_TO_CORN;
		lastHostScale = hostScale;
	}

	private float lastHostScale = 1;

	private static float length(final float[] v) {
		return (float) Math.sqrt((v[0] * v[0]) + (v[1] * v[1]) + (v[2] * v[2]));
	}

	private static void normalize(final float[] v, final float len) {
		if (len > 1.0e-6f) {
			v[0] /= len;
			v[1] /= len;
			v[2] /= len;
		}
	}

	private void fillCamera(final ExecContext.SceneCamera camera) {
		final ViewerCamera viewerCamera = instance.getCamera();
		if (viewerCamera == null) {
			return;
		}
		camera.position[0] = viewerCamera.location.x * GAME_TO_CORN;
		camera.position[1] = viewerCamera.location.y * GAME_TO_CORN;
		camera.position[2] = viewerCamera.location.z * GAME_TO_CORN;
		final Vector3f[] vectors = instance.getBillboardVectors();
		final Vector3f right = vectors[4];
		final Vector3f up = vectors[5];
		final Vector3f forward = viewerCamera.directionZ;
		camera.basis[0][0] = right.x;
		camera.basis[0][1] = right.y;
		camera.basis[0][2] = right.z;
		camera.basis[1][0] = -forward.x;
		camera.basis[1][1] = -forward.y;
		camera.basis[1][2] = -forward.z;
		camera.basis[2][0] = up.x;
		camera.basis[2][1] = up.y;
		camera.basis[2][2] = up.z;
	}

	private double track(final String name, final double fallback) {
		final AnimFlag flag = AnimFlag.find(emitter.getAnimFlags(), name);
		if ((flag != null) && (instance.getAnimatedRenderEnvironment() != null)) {
			final Object value = flag.interpolateAt(instance.getAnimatedRenderEnvironment());
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			}
		}
		return fallback;
	}

	private void pushAttributes() {
		final float lifeSpan = (float) track("LifeSpan", emitter.getLifeSpan());
		final float emissionRate = (float) track("EmissionRate", emitter.getEmissionRate());
		final float speed = (float) track("Speed", emitter.getSpeed());
		final float alpha = (float) track("Alpha", emitter.getAlpha());
		Vertex color = emitter.getColor();
		final AnimFlag colorFlag = AnimFlag.find(emitter.getAnimFlags(), "Color");
		if ((colorFlag != null) && (instance.getAnimatedRenderEnvironment() != null)) {
			final Object value = colorFlag.interpolateAt(instance.getAnimatedRenderEnvironment());
			if (value instanceof Vertex) {
				color = (Vertex) value;
			}
		}
		// model colours are stored BGR
		final float red = color == null ? 1 : (float) color.z;
		final float green = color == null ? 1 : (float) color.y;
		final float blue = color == null ? 1 : (float) color.x;
		runtime.setAttribute("__a_Game.LifespanMultiplier", lifeSpan, 0, 0, 0);
		runtime.setAttribute("__a_Game.EmissionRateMultiplier", emissionRate, 0, 0, 0);
		runtime.setAttribute("__a_Game.SpeedMultiplier", speed, 0, 0, 0);
		runtime.setAttribute("__a_Game.ColorMultiplier", red, green, blue, alpha);
		final int[] team = TEAM_COLORS[Math.max(0, Math.min(TEAM_COLORS.length - 1, Material.teamColor))];
		runtime.setAttribute("__a_Game.TeamColor", team[0] / 255f, team[1] / 255f, team[2] / 255f, 1);
		runtime.setAttribute("__a_Game.TargetPosition", inputs.emitterL2W.m[0][3], inputs.emitterL2W.m[1][3],
				inputs.emitterL2W.m[2][3], 0);
		runtime.setAttribute("__a_Game.Scale", cornEffectsScaling ? lastHostScale : 1f, 0, 0, 0);
	}

	// ------------------------------------------------------------------ visibility guide

	/**
	 * The guide names sequences that switch the effect on or off; a token
	 * "Always" sets the default. Matching is a case-insensitive substring test
	 * against the sequence name, like the game's.
	 */
	private boolean isEnabledForCurrentAnimation(final AnimatedRenderEnvironment environment) {
		final String guide = emitter.getAnimVisibilityGuide();
		if ((guide == null) || guide.trim().isEmpty()) {
			return true;
		}
		if (!guide.equals(parsedGuide)) {
			parseGuide(guide);
		}
		final BasicTimeBoundProvider current = environment == null ? null : environment.getCurrentAnimation();
		if (!(current instanceof Animation)) {
			return guideDefault;
		}
		final String name = ((Animation) current).getName().toLowerCase(Locale.US);
		boolean enabled = guideDefault;
		// the game consults only the list opposite to the default state
		for (final Map.Entry<String, Boolean> entry : visibilityGuide.entrySet()) {
			if ((entry.getValue() != guideDefault) && name.contains(entry.getKey())) {
				enabled = !guideDefault;
				break;
			}
		}
		return enabled;
	}

	private void parseGuide(final String guide) {
		parsedGuide = guide;
		visibilityGuide.clear();
		guideDefault = true;
		boolean sawAlways = false;
		boolean sawEnable = false;
		for (final String token : guide.split(",")) {
			final String trimmed = token.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			final int equals = trimmed.indexOf('=');
			final String key = (equals < 0 ? trimmed : trimmed.substring(0, equals)).trim().toLowerCase(Locale.US);
			final String value = equals < 0 ? "on" : trimmed.substring(equals + 1).trim().toLowerCase(Locale.US);
			final boolean on = value.equals("on");
			if (key.equals("always")) {
				guideDefault = on;
				sawAlways = true;
			} else {
				visibilityGuide.put(key, on);
				sawEnable |= on;
			}
		}
		if (!sawAlways && sawEnable) {
			guideDefault = false;
		}
		isNonLoopingEffect = false;
		if (!guideDefault) {
			for (final Map.Entry<String, Boolean> entry : visibilityGuide.entrySet()) {
				if (entry.getValue() && (entry.getKey().contains("attack") || entry.getKey().contains("death"))) {
					isNonLoopingEffect = true;
					break;
				}
			}
		}
	}

	// ------------------------------------------------------------------ rendering

	public static int blendSrc(final int blendMode) {
		switch (blendMode) {
		case LayerRenderer.BLEND_ADD:
		case LayerRenderer.BLEND_BLEND:
			return GL11.GL_SRC_ALPHA;
		default:
			return GL11.GL_ONE;
		}
	}

	public static int blendDst(final int blendMode) {
		switch (blendMode) {
		case LayerRenderer.BLEND_ADD:
		case LayerRenderer.BLEND_NO_ALPHA_ADD:
			return GL11.GL_ONE;
		case LayerRenderer.BLEND_BLEND:
		case LayerRenderer.BLEND_BLEND_ADD:
			return GL11.GL_ONE_MINUS_SRC_ALPHA;
		default:
			return GL11.GL_ZERO;
		}
	}

	private static float[] grow(final float[] array, final int needed) {
		if (array.length >= needed) {
			return array;
		}
		return Arrays.copyOf(array, Math.max(needed, array.length * 2));
	}

	public void render(final RenderModel modelView, final ParticleEmitterShader shader) {
		if (runtime == null) {
			return;
		}
		final List<RenderPacket> packets = runtime.lastPackets();
		final Vector3f[] vectors = instance.getBillboardVectors();
		final ViewerCamera viewerCamera = instance.getCamera();
		final Vector3f camRight = vectors[4];
		final Vector3f camUp = vectors[5];
		final Vector3f camForward = new Vector3f(-viewerCamera.directionZ.x, -viewerCamera.directionZ.y,
				-viewerCamera.directionZ.z);
		final Vector3f eye = viewerCamera.location;
		// ---- gather live billboards
		int live = 0;
		final List<RenderPacket> drawable = new ArrayList<>();
		for (final RenderPacket packet : packets) {
			if ((packet.renderer.rendererClass != LayerRenderer.CLASS_BILLBOARD)
					|| !textures.containsKey(packet.renderer) || (packet.slots[RenderPacket.SLOT_POSITION] == null)) {
				continue;
			}
			drawable.add(packet);
			live += packet.particleCount;
		}
		if (live == 0) {
			return;
		}
		if (sortPacket.length < live) {
			sortPacket = new int[live];
			sortParticle = new int[live];
			sortDepth = new float[live];
		}
		int count = 0;
		for (int pi = 0; pi < drawable.size(); pi++) {
			final RenderPacket packet = drawable.get(pi);
			final float[] pos = packet.slots[RenderPacket.SLOT_POSITION];
			final float[] size = packet.slots[RenderPacket.SLOT_SIZE];
			final float[] color = packet.slots[RenderPacket.SLOT_COLOR];
			final float[] enabled = packet.slots[RenderPacket.SLOT_ENABLED];
			for (int p = 0; p < packet.particleCount; p++) {
				if (!packet.alive[p]) {
					continue;
				}
				if ((enabled != null) && (enabled[p] == 0f)) {
					continue;
				}
				final float pSize = size != null ? size[p] : 1f;
				final float pAlpha = color != null ? color[(p * 4) + 3] : 1f;
				if ((pSize == 0f) && (pAlpha == 0f)) {
					continue;
				}
				final float wx = pos[p * 3] * CORN_TO_GAME;
				final float wy = pos[(p * 3) + 1] * CORN_TO_GAME;
				final float wz = pos[(p * 3) + 2] * CORN_TO_GAME;
				sortPacket[count] = pi;
				sortParticle[count] = p;
				sortDepth[count] = ((wx - eye.x) * camForward.x) + ((wy - eye.y) * camForward.y)
						+ ((wz - eye.z) * camForward.z);
				count++;
			}
		}
		if (count == 0) {
			return;
		}
		if (sortOrder.length < count) {
			sortOrder = new Integer[count];
		}
		for (int i = 0; i < count; i++) {
			sortOrder[i] = i;
		}
		final float[] depth = sortDepth;
		Arrays.sort(sortOrder, 0, count, (a, b) -> Float.compare(depth[b], depth[a]));
		// ---- emit runs back to front
		int runPacket = -1;
		int runVertices = 0;
		for (int k = 0; k < count; k++) {
			final int entry = sortOrder[k];
			final int pi = sortPacket[entry];
			if (pi != runPacket) {
				flushRun(shader, drawable, runPacket, runVertices);
				runPacket = pi;
				runVertices = 0;
			}
			vertexBuffer = grow(vertexBuffer, (runVertices + 6) * 5);
			runVertices += appendBillboard(drawable.get(pi), sortParticle[entry], camRight, camUp, camForward, eye,
					vertexBuffer, runVertices * 5);
		}
		flushRun(shader, drawable, runPacket, runVertices);
	}

	private void flushRun(final ParticleEmitterShader shader, final List<RenderPacket> drawable, final int packetIndex,
			final int vertices) {
		if ((packetIndex < 0) || (vertices == 0)) {
			return;
		}
		final LayerRenderer renderer = drawable.get(packetIndex).renderer;
		final InternalResource texture = textures.get(renderer);
		final boolean atlas = renderer.isAtlas && (renderer.atlasSubDivX > 0) && (renderer.atlasSubDivY > 0);
		shader.renderParticles(blendSrc(renderer.blendMode), blendDst(renderer.blendMode),
				atlas ? renderer.atlasSubDivY : 1, atlas ? renderer.atlasSubDivX : 1, texture, vertexBuffer, false,
				vertices);
	}

	private static final int[][] CORNER_UV = { { 0, 1 }, { 1, 1 }, { 1, 0 }, { 0, 0 } };
	private static final int[] QUAD_INDICES = { 0, 1, 2, 0, 2, 3 };
	private final float[][] corners = new float[4][3];
	private final int[] cornerUv = new int[4];

	/** Expands one particle into six vertices; returns the number of vertices written. */
	private int appendBillboard(final RenderPacket packet, final int p, final Vector3f camRight, final Vector3f camUp,
			final Vector3f camForward, final Vector3f eye, final float[] out, final int base) {
		final LayerRenderer renderer = packet.renderer;
		final float[] pos = packet.slots[RenderPacket.SLOT_POSITION];
		final float[] size = packet.slots[RenderPacket.SLOT_SIZE];
		final float[] color = packet.slots[RenderPacket.SLOT_COLOR];
		final float[] rotation = packet.slots[RenderPacket.SLOT_ROTATION];
		final float[] axis0 = packet.slots[RenderPacket.SLOT_AXIS0];
		final float[] axis1 = packet.slots[RenderPacket.SLOT_AXIS1];
		final float[] texIds = packet.slots[RenderPacket.SLOT_TEXTURE_ID];
		final float px = pos[p * 3] * CORN_TO_GAME;
		final float py = pos[(p * 3) + 1] * CORN_TO_GAME;
		final float pz = pos[(p * 3) + 2] * CORN_TO_GAME;
		float sx = 0;
		if (size != null) {
			final float raw = size[p];
			if (!Float.isNaN(raw) && !Float.isInfinite(raw) && (raw > 0)) {
				sx = raw * CORN_TO_GAME;
			}
		}
		final float sy = sx;
		int r = 255, g = 255, b = 255, a = 255;
		if (color != null) {
			r = toByte(color[p * 4]);
			g = toByte(color[(p * 4) + 1]);
			b = toByte(color[(p * 4) + 2]);
			a = toByte(color[(p * 4) + 3]);
		}
		// atlas cell
		final boolean atlas = renderer.isAtlas && (renderer.atlasSubDivX > 0) && (renderer.atlasSubDivY > 0);
		int cellU0 = 0;
		int cellV0 = 0;
		if (atlas) {
			final int maxFrame = renderer.atlasSubDivX * renderer.atlasSubDivY;
			final float rawId = texIds != null ? texIds[p] : 0;
			float cursor = (Float.isNaN(rawId) || Float.isInfinite(rawId)) ? 0 : Math.abs(rawId);
			final float maxCursor = maxFrame - 1;
			if (cursor > maxCursor) {
				cursor = maxCursor;
			}
			final int frameA = (int) cursor;
			cellU0 = frameA % renderer.atlasSubDivX;
			cellV0 = frameA / renderer.atlasSubDivX;
		}
		final int bbMode = renderer.billboardingMode;
		// ---- billboard axes r0 / u0 (unit, before size)
		float r0x, r0y, r0z, u0x, u0y, u0z;
		final boolean wantAxis0 = (bbMode == 2) || (bbMode == 3) || (bbMode == 4);
		final boolean wantBothAxes = (bbMode == 5) || ((bbMode == 0) && (axis0 != null) && (axis1 != null));
		if (wantBothAxes && (axis0 != null) && (axis1 != null)) {
			final float a0x = axis0[p * 3], a0y = axis0[(p * 3) + 1], a0z = axis0[(p * 3) + 2];
			final float a1x = axis1[p * 3], a1y = axis1[(p * 3) + 1], a1z = axis1[(p * 3) + 2];
			if (bbMode == 5) {
				float wx = (a0y * a1z) - (a0z * a1y);
				float wy = (a0z * a1x) - (a0x * a1z);
				float wz = (a0x * a1y) - (a0y * a1x);
				final float wl2 = (wx * wx) + (wy * wy) + (wz * wz);
				if (wl2 > 1e-20f) {
					final float inv = 1f / (float) Math.sqrt(wl2);
					wx *= inv;
					wy *= inv;
					wz *= inv;
				} else {
					final float a1l2 = (a1x * a1x) + (a1y * a1y) + (a1z * a1z);
					if (a1l2 > 1e-20f) {
						final boolean useX = Math.abs(a1z) > (0.9f * (float) Math.sqrt(a1l2));
						final float hx = useX ? 1f : 0f;
						final float hz = useX ? 0f : 1f;
						wx = a1y * hz;
						wy = (a1z * hx) - (a1x * hz);
						wz = -a1y * hx;
						final float nl2 = (wx * wx) + (wy * wy) + (wz * wz);
						if (nl2 > 1e-20f) {
							final float inv = 1f / (float) Math.sqrt(nl2);
							wx *= inv;
							wy *= inv;
							wz *= inv;
						}
					}
				}
				r0x = wx;
				r0y = wy;
				r0z = wz;
				u0x = (a1y * wz) - (a1z * wy);
				u0y = (a1z * wx) - (a1x * wz);
				u0z = (a1x * wy) - (a1y * wx);
			} else {
				r0x = a0x;
				r0y = a0z;
				r0z = a0y;
				u0x = a1x;
				u0y = a1z;
				u0z = a1y;
			}
		} else if ((bbMode == 3) && (axis0 != null)) {
			// spheroid: side = normalize(axis x camToParticle), up = c2p x side
			final float ax = axis0[p * 3] * CORN_TO_GAME, ay = axis0[(p * 3) + 1] * CORN_TO_GAME,
					az = axis0[(p * 3) + 2] * CORN_TO_GAME;
			float cx = px - eye.x, cy = py - eye.y, cz = pz - eye.z;
			final float cl2 = (cx * cx) + (cy * cy) + (cz * cz);
			if (cl2 > 1e-10f) {
				final float inv = 1f / (float) Math.sqrt(cl2);
				cx *= inv;
				cy *= inv;
				cz *= inv;
			} else {
				cx = -camForward.x;
				cy = -camForward.y;
				cz = -camForward.z;
			}
			float sxv = (ay * cz) - (az * cy), syv = (az * cx) - (ax * cz), szv = (ax * cy) - (ay * cx);
			final float sl2 = (sxv * sxv) + (syv * syv) + (szv * szv);
			if (sl2 > 1e-12f) {
				final float inv = 1f / (float) Math.sqrt(sl2);
				sxv *= inv;
				syv *= inv;
				szv *= inv;
			} else {
				sxv = camRight.x;
				syv = camRight.y;
				szv = camRight.z;
			}
			sxv *= sx;
			syv *= sx;
			szv *= sx;
			final float upx = (cy * szv) - (cz * syv), upy = (cz * sxv) - (cx * szv), upz = (cx * syv) - (cy * sxv);
			final float phdx = (ax * 0.5f) + upx, phdy = (ay * 0.5f) + upy, phdz = (az * 0.5f) + upz;
			final float invSz = sx != 0 ? 1f / sx : 0;
			r0x = sxv * -invSz;
			r0y = syv * -invSz;
			r0z = szv * -invSz;
			u0x = phdx * invSz;
			u0y = phdy * invSz;
			u0z = phdz * invSz;
		} else if (wantAxis0 && (axis0 != null)) {
			final float ax = axis0[p * 3] * CORN_TO_GAME, ay = axis0[(p * 3) + 1] * CORN_TO_GAME,
					az = axis0[(p * 3) + 2] * CORN_TO_GAME;
			final float invSz = sx != 0 ? 1f / sx : 0;
			u0x = ax * 0.5f * invSz;
			u0y = ay * 0.5f * invSz;
			u0z = az * 0.5f * invSz;
			final float alen2 = (ax * ax) + (ay * ay) + (az * az);
			r0x = camRight.x;
			r0y = camRight.y;
			r0z = camRight.z;
			if (alen2 > 1e-12f) {
				final float invLen = 1f / (float) Math.sqrt(alen2);
				final float nx = ax * invLen, ny = ay * invLen, nz = az * invLen;
				final float cx = px - eye.x, cy = py - eye.y, cz = pz - eye.z;
				float rx = (cy * nz) - (cz * ny), ry = (cz * nx) - (cx * nz), rz = (cx * ny) - (cy * nx);
				final float clen2 = (rx * rx) + (ry * ry) + (rz * rz);
				if (clen2 > 1e-12f) {
					final float inv = 1f / (float) Math.sqrt(clen2);
					r0x = rx * inv;
					r0y = ry * inv;
					r0z = rz * inv;
				}
			}
		} else {
			r0x = camRight.x;
			r0y = camRight.y;
			r0z = camRight.z;
			u0x = camUp.x;
			u0y = camUp.y;
			u0z = camUp.z;
		}
		final boolean bbModeRotates = (bbMode != 2) && (bbMode != 3) && (bbMode != 4);
		final float rot = rotation != null ? rotation[p] : 0;
		if ((rot != 0) && bbModeRotates) {
			final float ca = (float) Math.cos(rot);
			final float sa = (float) Math.sin(rot);
			final float sgn = bbMode == 5 ? -1f : 1f;
			final float rkx = r0x, rky = r0y, rkz = r0z;
			final float ukx = u0x, uky = u0y, ukz = u0z;
			r0x = (ca * rkx) + (sgn * sa * ukx);
			r0y = (ca * rky) + (sgn * sa * uky);
			r0z = (ca * rkz) + (sgn * sa * ukz);
			u0x = (-sgn * sa * rkx) + (ca * ukx);
			u0y = (-sgn * sa * rky) + (ca * uky);
			u0z = (-sgn * sa * rkz) + (ca * ukz);
		}
		final float rx = r0x * sx, ry = r0y * sx, rz = r0z * sx;
		final float ux = u0x * sy, uy = u0y * sy, uz = u0z * sy;
		corners[0][0] = px - rx - ux;
		corners[0][1] = py - ry - uy;
		corners[0][2] = pz - rz - uz;
		corners[1][0] = (px + rx) - ux;
		corners[1][1] = (py + ry) - uy;
		corners[1][2] = (pz + rz) - uz;
		corners[2][0] = px + rx + ux;
		corners[2][1] = py + ry + uy;
		corners[2][2] = pz + rz + uz;
		corners[3][0] = (px - rx) + ux;
		corners[3][1] = (py - ry) + uy;
		corners[3][2] = (pz - rz) + uz;
		final boolean flipU = renderer.hasFlipUVs || renderer.textureFlipU;
		final boolean flipV = renderer.hasFlipUVs || renderer.textureFlipV;
		for (int corner = 0; corner < 4; corner++) {
			int cu = CORNER_UV[corner][0];
			int cv = CORNER_UV[corner][1];
			if (renderer.textureRotateTexture) {
				final int t = cu;
				cu = cv;
				cv = 1 - t;
			}
			if (flipU) {
				cu = 1 - cu;
			}
			if (flipV) {
				cv = 1 - cv;
			}
			cornerUv[corner] = (((cellU0 + cu) & 0xFF) << 16) | (((cellV0 + cv) & 0xFF) << 8) | (a & 0xFF);
		}
		final int rgb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
		int o = base;
		for (final int index : QUAD_INDICES) {
			out[o++] = corners[index][0];
			out[o++] = corners[index][1];
			out[o++] = corners[index][2];
			out[o++] = cornerUv[index];
			out[o++] = rgb;
		}
		return 6;
	}

	private static int toByte(final float v) {
		if (Float.isNaN(v)) {
			return 0;
		}
		final float s = (v * 255f) + 0.5f;
		return (int) (s < 0 ? 0 : (s > 255 ? 255 : s));
	}
}
