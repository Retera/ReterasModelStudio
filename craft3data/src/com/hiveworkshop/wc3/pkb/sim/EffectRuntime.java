package com.hiveworkshop.wc3.pkb.sim;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.pkb.bind.EffectPlan;
import com.hiveworkshop.wc3.pkb.bind.EventRoute;
import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;
import com.hiveworkshop.wc3.pkb.bind.LayerProgram;
import com.hiveworkshop.wc3.pkb.bind.LayerRenderer;
import com.hiveworkshop.wc3.pkb.bind.SpatialLayerResource;
import com.hiveworkshop.wc3.pkb.vm.ExecContext;
import com.hiveworkshop.wc3.pkb.vm.Interpreter;
import com.hiveworkshop.wc3.pkb.vm.RegisterValue;

/**
 * Ticks a bound effect: per layer, drains pending births, advances life,
 * injects scene inputs and attributes, runs the evolve program, routes the
 * kicked events, and finally extracts render packets.
 */
public final class EffectRuntime {
	public static final int RAND_STATE_SPAWN_ADDEND = 111;
	private static final int FIBONACCI_HASH_STRIDE = 0x9E3779B1;
	private static final int MAX_POOL_PER_LAYER = 1 << 17;
	private static final int SLOT_UNBOUND = -1;

	/** Per-tick inputs. */
	public static final class FrameInputs {
		public float dt = 1.0f / 60.0f;
		public float effectAge;
		public final ExecContext.Mat4x3 emitterL2W = new ExecContext.Mat4x3();
		public int baseRngSeed = 0xC0FFEE00;
		public boolean effectIsRunning = true;
		public final ExecContext.SceneCamera camera = new ExecContext.SceneCamera();
	}

	private final EffectPlan plan;
	private final Interpreter vm = new Interpreter();
	private final ExecContext ctx = new ExecContext();
	private final SimIssues issues = new SimIssues();
	private final ParticlePool[] pools;
	private final RenderPacket.InputMap[] inputMaps;
	private final RenderPacket.InputMap[][] perRendererInputMaps;
	private final SpawnEvent.Queue[] spawnQueues;
	private final int[] spawnHeads;
	private final long[] spawnedTotals;
	private final int[] invLifeSlots;
	private final int[] lifeRatioSlots;
	private final List<ProximityHash> spatialHashesOwned = new ArrayList<>();
	private final List<ProximityHash> spatialHashesOwnedAlt = new ArrayList<>();
	private final List<String> spatialHashNames = new ArrayList<>();
	private final ProximityHash[][] spatialHashesPerLayer;
	private final ProximityHash[][] spatialHashesAltPerLayer;
	private boolean spatialReadIsAlt;
	private long nextSelfId = 1;
	private final List<String> attributeNames = new ArrayList<>();
	private final List<float[]> attributeValues = new ArrayList<>();
	private boolean initialized;
	private boolean spawnerEnabled = true;
	private float sceneTime;
	private final ExecContext.SceneCamera[] cameras = { new ExecContext.SceneCamera() };
	private final List<RenderPacket> packets = new ArrayList<>();
	private int defaultRenderPoolSize = 256;

	public EffectRuntime(final EffectPlan plan) {
		this.plan = plan;
		final int layerCount = plan.layers.length;
		pools = new ParticlePool[layerCount];
		inputMaps = new RenderPacket.InputMap[layerCount];
		perRendererInputMaps = new RenderPacket.InputMap[layerCount][];
		spawnQueues = new SpawnEvent.Queue[layerCount];
		spawnHeads = new int[layerCount];
		spawnedTotals = new long[layerCount];
		invLifeSlots = new int[layerCount];
		lifeRatioSlots = new int[layerCount];
		spatialHashesPerLayer = new ProximityHash[layerCount][];
		spatialHashesAltPerLayer = new ProximityHash[layerCount][];
		for (int i = 0; i < layerCount; i++) {
			pools[i] = new ParticlePool();
			spawnQueues[i] = new SpawnEvent.Queue();
			pools[i].resizeForLayer(plan.layers[i]);
			pools[i].resize(1);
			setupSelfLifeSlots(i);
			setupSpatialHashes(i);
			setupRenderInputMaps(i);
		}
	}

	public EffectPlan getPlan() {
		return plan;
	}

	public SimIssues getIssues() {
		return issues;
	}

	public int layerCount() {
		return plan.layers.length;
	}

	public ParticlePool pool(final int layerIdx) {
		return pools[layerIdx];
	}

	public int aliveCount(final int layerIdx) {
		return pools[layerIdx].aliveCount();
	}

	public long spawnedTotal(final int layerIdx) {
		return spawnedTotals[layerIdx];
	}

	public List<RenderPacket> lastPackets() {
		return packets;
	}

	public void setDefaultRenderPoolSize(final int size) {
		defaultRenderPoolSize = size;
	}

	public void setSpawnerEnabled(final boolean enabled) {
		spawnerEnabled = enabled;
	}

	public boolean isSpawnerEnabled() {
		return spawnerEnabled;
	}

	public void setAttribute(final String name, final float x, final float y, final float z, final float w) {
		final int idx = attributeNames.indexOf(name);
		if (idx >= 0) {
			final float[] v = attributeValues.get(idx);
			v[0] = x;
			v[1] = y;
			v[2] = z;
			v[3] = w;
			return;
		}
		attributeNames.add(name);
		attributeValues.add(new float[] { x, y, z, w });
	}

	private void setupSelfLifeSlots(final int layerIdx) {
		final LayerProgram lp = plan.layers[layerIdx];
		invLifeSlots[layerIdx] = SLOT_UNBOUND;
		lifeRatioSlots[layerIdx] = SLOT_UNBOUND;
		ExternalBinding b = ExternalBinding.findByName(lp.initProgram.externals, "self.invLife");
		if (b == null) {
			b = ExternalBinding.findByName(lp.physicsProgram.externals, "self.invLife");
		}
		if (b != null) {
			invLifeSlots[layerIdx] = b.canonicalSlot;
		}
		b = ExternalBinding.findByName(lp.physicsProgram.externals, "self.lifeRatio");
		if (b == null) {
			b = ExternalBinding.findByName(lp.timeFixedProgram.externals, "self.lifeRatio");
		}
		if (b == null) {
			b = ExternalBinding.findByName(lp.timeVaryingProgram.externals, "self.lifeRatio");
		}
		if (b == null) {
			b = ExternalBinding.findByName(lp.initProgram.externals, "self.lifeRatio");
		}
		if (b != null) {
			lifeRatioSlots[layerIdx] = b.canonicalSlot;
		}
	}

	private void setupSpatialHashes(final int layerIdx) {
		final LayerProgram lp = plan.layers[layerIdx];
		final ProximityHash[] main = new ProximityHash[lp.spatialLayers.length];
		final ProximityHash[] alt = new ProximityHash[lp.spatialLayers.length];
		for (int i = 0; i < lp.spatialLayers.length; i++) {
			final SpatialLayerResource sl = lp.spatialLayers[i];
			final String identity = sl.identity();
			int slot = spatialHashNames.indexOf(identity);
			if (slot < 0) {
				spatialHashNames.add(identity);
				spatialHashesOwned.add(new ProximityHash(sl.cellSize));
				spatialHashesOwnedAlt.add(new ProximityHash(sl.cellSize));
				slot = spatialHashNames.size() - 1;
			} else {
				spatialHashesOwned.get(slot).setCellSize(sl.cellSize);
				spatialHashesOwnedAlt.get(slot).setCellSize(sl.cellSize);
			}
			main[i] = spatialHashesOwned.get(slot);
			alt[i] = spatialHashesOwnedAlt.get(slot);
		}
		spatialHashesPerLayer[layerIdx] = main;
		spatialHashesAltPerLayer[layerIdx] = alt;
	}

	private void setupRenderInputMaps(final int layerIdx) {
		final LayerProgram lp = plan.layers[layerIdx];
		final RenderPacket.InputMap inferred = RenderPacket.inferInputMap(lp);
		inputMaps[layerIdx] = inferred;
		perRendererInputMaps[layerIdx] = new RenderPacket.InputMap[lp.renderers.length];
		boolean anyAssetBindings = false;
		for (final LayerRenderer r : lp.renderers) {
			anyAssetBindings |= r.hasAssetInputBindings();
		}
		for (int r = 0; r < lp.renderers.length; r++) {
			final LayerRenderer renderer = lp.renderers[r];
			perRendererInputMaps[layerIdx][r] = (anyAssetBindings && renderer.hasAssetInputBindings())
					? RenderPacket.inputMapFromAsset(renderer, lp)
					: inferred;
		}
	}

	private ProximityHash[] spatialReadHashes(final int layerIdx) {
		return spatialReadIsAlt ? spatialHashesAltPerLayer[layerIdx] : spatialHashesPerLayer[layerIdx];
	}

	private ProximityHash[] spatialWriteHashes(final int layerIdx) {
		return spatialReadIsAlt ? spatialHashesPerLayer[layerIdx] : spatialHashesAltPerLayer[layerIdx];
	}

	private static int layerSeedFor(final int baseRngSeed, final int layerIdx) {
		return baseRngSeed + (layerIdx * FIBONACCI_HASH_STRIDE);
	}

	public void reset() {
		for (final ParticlePool pool : pools) {
			for (int p = 0; p < pool.size(); p++) {
				pool.particle(p).markDead();
			}
		}
		for (final SpawnEvent.Queue q : spawnQueues) {
			q.clear();
		}
		java.util.Arrays.fill(spawnHeads, 0);
		nextSelfId = 1;
		sceneTime = 0;
		initialized = false;
	}

	private void applyFrameState(final LayerTickHarness particle, final int layerIdx, final FrameInputs inputs) {
		particle.setSceneL2W(inputs.emitterL2W);
		particle.setCameras(cameras);
		particle.setSimLod(0);
		particle.setSimLodDistances(5, 200);
		particle.setEffectAge(inputs.effectAge);
		particle.setEffectIsRunning(inputs.effectIsRunning);
	}

	private void initializeOnFirstTick(final FrameInputs inputs) {
		for (int i = 0; i < plan.layers.length; i++) {
			final LayerProgram layer = plan.layers[i];
			final boolean isSpawner = layer.isSpawner();
			final boolean isRoot = !plan.isKickTarget(layer.id);
			if (!(isSpawner && isRoot)) {
				if (!isSpawner && (pools[i].size() < defaultRenderPoolSize)) {
					pools[i].resize(defaultRenderPoolSize);
					pools[i].resizeForLayer(layer);
				}
				for (int p = 0; p < pools[i].size(); p++) {
					pools[i].particle(p).markDead();
				}
				continue;
			}
			if (pools[i].size() != 1) {
				pools[i].resize(1);
				pools[i].resizeForLayer(layer);
			}
			final int seed = layerSeedFor(inputs.baseRngSeed, i) + RAND_STATE_SPAWN_ADDEND;
			for (int p = 0; p < pools[i].size(); p++) {
				pools[i].particle(p).setSpatialHashes(spatialReadHashes(i), spatialWriteHashes(i));
				pools[i].particle(p).setInitSceneTime(sceneTime);
			}
			pools[i].initRange(layer, seed, 0, pools[i].size(), vm, ctx, issues);
			for (int p = 0; p < pools[i].size(); p++) {
				applyFrameState(pools[i].particle(p), i, inputs);
			}
		}
	}

	private void drainPendingSpawns(final int i, final FrameInputs inputs) {
		final SpawnEvent.Queue q = spawnQueues[i];
		if (q.events.isEmpty()) {
			return;
		}
		int cap = pools[i].size();
		if (cap == 0) {
			q.dropped += q.events.size();
			q.events.clear();
			return;
		}
		final List<SpawnEvent> incoming = new ArrayList<>(q.events);
		q.events.clear();
		final LayerProgram layer = plan.layers[i];
		final int layerRSM = layerSeedFor(inputs.baseRngSeed, i);
		for (final SpawnEvent ev : incoming) {
			int slot = spawnHeads[i] % cap;
			int probed = 0;
			while ((probed < cap) && !pools[i].particle(slot).isDead()) {
				slot = (slot + 1) % cap;
				probed++;
			}
			if (probed >= cap) {
				final int oldCap = pools[i].size();
				if (oldCap >= MAX_POOL_PER_LAYER) {
					q.dropped++;
					continue;
				}
				final int newCap = Math.min(MAX_POOL_PER_LAYER, Math.max(oldCap * 2, oldCap + 1));
				pools[i].resize(newCap);
				pools[i].resizeForLayer(layer);
				for (int np = oldCap; np < newCap; np++) {
					pools[i].particle(np).markDead();
				}
				cap = newCap;
				slot = oldCap;
			}
			spawnHeads[i] = (slot + 1) % cap;
			final LayerTickHarness particle = pools[i].particle(slot);
			particle.setSelfId(nextSelfId++);
			particle.setParentIdentity(ev.parentSelfId, ev.parentRngState);
			if (ev.hasIntPayload) {
				particle.setSpawnIntPayload(ev.intPayloadWidth, ev.intPayload, ev.intPayloadId);
			} else {
				particle.clearSpawnIntPayload();
			}
			if (ev.hasBoolPayload) {
				particle.setSpawnBoolPayload(ev.boolPayloadWidth, ev.boolPayload, ev.boolPayloadId);
			} else {
				particle.clearSpawnBoolPayload();
			}
			particle.setSpawnPositionPayloadId(ev.hasSpawnPosition ? ev.spawnPositionPayloadId : 0);
			particle.setSpawnOrientationPayloadId(ev.hasSpawnOrientation ? ev.spawnOrientationPayloadId : 0);
			particle.setSpawnFloatSlots(ev.floatSlots);
			applyFrameState(particle, i, inputs);
			particle.setTimeWindowEnd(ev.lerpedTime);
			if (ev.hasSpawnPosition) {
				final float[] quat = ev.hasSpawnOrientation ? ev.spawnOrientation : new float[] { 0, 0, 0, 1 };
				particle.setSpawnTRS(ev.spawnPosition, quat, new float[] { 1, 1, 1 });
			} else if (ev.hasSpawnOrientation) {
				particle.setSpawnTRS(new float[3], ev.spawnOrientation, new float[] { 1, 1, 1 });
			}
			particle.setSpatialHashes(spatialReadHashes(i), spatialWriteHashes(i));
			particle.setSpawnQueue(spawnQueues[i]);
			particle.setInitSceneTime(sceneTime);
			final int seed = ev.parentRngState + layerRSM + RAND_STATE_SPAWN_ADDEND;
			pools[i].initRange(layer, seed, slot, 1, vm, ctx, issues);
			spawnedTotals[i]++;
		}
	}

	private void prepareParticlesForTick(final int i, final FrameInputs inputs) {
		final int invLifeSlot = invLifeSlots[i];
		final int lifeRatioSlot = lifeRatioSlots[i];
		final ParticlePool pool = pools[i];
		for (int p = 0; p < pool.size(); p++) {
			final LayerTickHarness particle = pool.particle(p);
			applyFrameState(particle, i, inputs);
			particle.setSpawnQueue(spawnQueues[i]);
			particle.setSpatialHashes(spatialReadHashes(i), spatialWriteHashes(i));
			particle.noteFrameStartDeadState();
			if ((invLifeSlot != SLOT_UNBOUND) && (invLifeSlot < particle.externalCount())) {
				final float invLife = particle.externalLane(invLifeSlot, 0);
				particle.advanceLifeRatio(inputs.dt * invLife);
			}
			if ((lifeRatioSlot != SLOT_UNBOUND) && (lifeRatioSlot < particle.externalCount())) {
				particle.setExternalScalar(lifeRatioSlot, particle.lifeRatio());
			}
		}
	}

	private void injectSceneScalar(final int i, final String name, final float value) {
		final LayerProgram layer = plan.layers[i];
		final int[] slots = new int[3];
		int slotCount = 0;
		for (final ExternalBinding[] scope : new ExternalBinding[][] { layer.physicsProgram.externals,
				layer.timeFixedProgram.externals, layer.timeVaryingProgram.externals }) {
			final ExternalBinding hit = ExternalBinding.findByName(scope, name);
			if (hit == null) {
				continue;
			}
			boolean seen = false;
			for (int k = 0; k < slotCount; k++) {
				if (slots[k] == hit.canonicalSlot) {
					seen = true;
					break;
				}
			}
			if (!seen) {
				slots[slotCount++] = hit.canonicalSlot;
			}
		}
		if (slotCount == 0) {
			return;
		}
		final ParticlePool pool = pools[i];
		for (int p = 0; p < pool.size(); p++) {
			final LayerTickHarness particle = pool.particle(p);
			for (int k = 0; k < slotCount; k++) {
				if (slots[k] < particle.externalCount()) {
					particle.setExternalScalar(slots[k], value);
				}
			}
		}
	}

	private void applyAttributeOverrides(final int i) {
		if (attributeNames.isEmpty()) {
			return;
		}
		final LayerProgram layer = plan.layers[i];
		final ParticlePool pool = pools[i];
		for (int a = 0; a < attributeNames.size(); a++) {
			final ExternalBinding hit = layer.findBindingAcrossScopes(attributeNames.get(a));
			if (hit == null) {
				continue;
			}
			final int slot = hit.resolvedSlot();
			final float[] value = attributeValues.get(a);
			for (int p = 0; p < pool.size(); p++) {
				final LayerTickHarness particle = pool.particle(p);
				if (slot < particle.externalCount()) {
					particle.setExternalFloat4(slot, value);
				}
			}
		}
	}

	private void routeEventsForLayer(final int i) {
		final SpawnEvent.Queue srcQ = spawnQueues[i];
		if (srcQ.events.isEmpty()) {
			return;
		}
		for (final SpawnEvent ev : srcQ.events) {
			for (final EventRoute route : plan.routes) {
				if (route.globalEventSlotId != ev.eventId) {
					continue;
				}
				final int tgtIdx = route.targetLayer;
				if ((tgtIdx < 0) || (tgtIdx >= spawnQueues.length)) {
					continue;
				}
				final SpawnEvent.Queue dstQ = spawnQueues[tgtIdx];
				if ((dstQ.capacity != 0) && (dstQ.events.size() >= dstQ.capacity)) {
					dstQ.dropped++;
					continue;
				}
				final SpawnEvent routed = ev.copy();
				routed.sequenceIndex = dstQ.events.size();
				dstQ.events.add(routed);
			}
		}
		srcQ.events.clear();
	}

	private void updateCamera(final FrameInputs inputs) {
		final ExecContext.SceneCamera cam = cameras[0];
		System.arraycopy(inputs.camera.position, 0, cam.position, 0, 3);
		cam.resolution[0] = inputs.camera.resolution[0];
		cam.resolution[1] = inputs.camera.resolution[1];
		for (int r = 0; r < 3; r++) {
			System.arraycopy(inputs.camera.basis[r], 0, cam.basis[r], 0, 3);
		}
	}

	public void tick(final FrameInputs inputs) {
		spatialReadIsAlt = !spatialReadIsAlt;
		for (final ProximityHash h : (spatialReadIsAlt ? spatialHashesOwned : spatialHashesOwnedAlt)) {
			h.clear();
		}
		updateCamera(inputs);
		if (!initialized) {
			initializeOnFirstTick(inputs);
			initialized = true;
		}
		sceneTime += inputs.dt;
		for (int i = 0; i < plan.layers.length; i++) {
			final LayerProgram layer = plan.layers[i];
			final boolean isSpawner = layer.isSpawner();
			final boolean kickTarget = plan.isKickTarget(layer.id);
			if (kickTarget) {
				drainPendingSpawns(i, inputs);
			} else {
				spawnQueues[i].clear();
			}
			prepareParticlesForTick(i, inputs);
			injectSceneScalar(i, "scene.dt", inputs.dt);
			injectSceneScalar(i, "scene.time", sceneTime);
			applyAttributeOverrides(i);
			final boolean skipTick = isSpawner && !spawnerEnabled;
			if (!skipTick) {
				pools[i].tickBatch(layer, vm, ctx, issues);
			}
			routeEventsForLayer(i);
		}
		buildPackets();
	}

	private void buildPackets() {
		int packetIndex = 0;
		for (int i = 0; i < plan.layers.length; i++) {
			final LayerProgram layer = plan.layers[i];
			if (layer.renderers.length == 0) {
				continue;
			}
			for (int r = 0; r < layer.renderers.length; r++) {
				final LayerRenderer renderer = layer.renderers[r];
				if (!renderer.isRenderingEnabled) {
					continue;
				}
				RenderPacket packet;
				if (packetIndex < packets.size()) {
					packet = packets.get(packetIndex);
				} else {
					packet = new RenderPacket();
					packets.add(packet);
				}
				packetIndex++;
				final RenderPacket.InputMap mapping = perRendererInputMaps[i][r] != null ? perRendererInputMaps[i][r]
						: inputMaps[i];
				packet.extract(pools[i], layer, i, renderer, r, mapping);
			}
		}
		while (packets.size() > packetIndex) {
			packets.remove(packets.size() - 1);
		}
	}

	/** Debug helper: the value of an external of one particle. */
	public RegisterValue readExternal(final int layerIdx, final int particle, final String name) {
		final ExternalBinding b = plan.layers[layerIdx].findBindingAcrossScopes(name);
		if (b == null) {
			return null;
		}
		final LayerTickHarness p = pools[layerIdx].particle(particle);
		return p.externals().get(b.resolvedSlot());
	}
}
