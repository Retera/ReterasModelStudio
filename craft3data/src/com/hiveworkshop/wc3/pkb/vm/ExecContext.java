package com.hiveworkshop.wc3.pkb.vm;

import com.hiveworkshop.wc3.pkb.bind.EventPayloadDecl;
import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;
import com.hiveworkshop.wc3.pkb.bind.FunctionBinding;
import com.hiveworkshop.wc3.pkb.bind.SamplerResource;
import com.hiveworkshop.wc3.pkb.bind.SpatialLayerResource;
import com.hiveworkshop.wc3.pkb.sim.ExternalStore;
import com.hiveworkshop.wc3.pkb.sim.FastRand;
import com.hiveworkshop.wc3.pkb.sim.ProximityHash;
import com.hiveworkshop.wc3.pkb.sim.SpawnEvent;

/** Everything one scope run of one particle can see and mutate. */
public final class ExecContext {
	public static final int MAX_PENDING_POSITIONS = 64;
	public static final int MAX_EVENT_CACHE_ENTRIES = 16;

	/** 3x4 local-to-world matrix, row major: m[row][col], column 3 is the translation. */
	public static final class Mat4x3 {
		public final float[][] m = { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 } };

		public void setIdentity() {
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 4; c++) {
					m[r][c] = r == c ? 1 : 0;
				}
			}
		}

		public void set(final Mat4x3 o) {
			for (int r = 0; r < 3; r++) {
				System.arraycopy(o.m[r], 0, m[r], 0, 4);
			}
		}
	}

	public static final class SceneCamera {
		public final float[] position = new float[3];
		public final int[] resolution = { 1, 1 };
		public final float[][] basis = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
	}

	public static final class BuiltPayloadFloat {
		public boolean valid;
		public int elementId;
		public int width;
		public final float[] value = new float[4];
	}

	public static final class BuiltPayloadIndex {
		public boolean valid;
		public int elementId;
		public int base;
	}

	public static final class SpatialAppendSlot {
		public boolean valid;
		public int key;
		public int nameHash;
		public int components;
		public final float[] value = new float[4];
	}

	public static final class PendingKickPayload {
		public int eventId;
		public int count;
		public boolean valid;
	}

	public static final class PendingPayloadElement {
		public int eventId;
		public int positionPayloadId;
		public int orientationPayloadId;
		public int intPayloadId;
		public int boolPayloadId;
		public int positionCount;
		public final float[][] positions = new float[MAX_PENDING_POSITIONS][3];
		public boolean hasOrientation;
		public final float[] orientation = { 0, 0, 0, 1 };
		public boolean hasIntPayload;
		public int intPayloadWidth;
		public final int[] intPayload = new int[4];
		public boolean hasSpawnIndexPayload;
		public int spawnIndexPayloadId;
		public int spawnIndexBase;
		public boolean hasBoolPayload;
		public int boolPayloadWidth;
		public final int[] boolPayload = new int[4];
		public final SpawnEvent.PayloadFloatSlot[] floatSlots = SpawnEvent.newFloatSlots();
		public boolean valid;

		public void reset(final int newEventId) {
			eventId = newEventId;
			positionPayloadId = 0;
			orientationPayloadId = 0;
			intPayloadId = 0;
			boolPayloadId = 0;
			positionCount = 0;
			hasOrientation = false;
			orientation[0] = orientation[1] = orientation[2] = 0;
			orientation[3] = 1;
			hasIntPayload = false;
			intPayloadWidth = 0;
			intPayload[0] = intPayload[1] = intPayload[2] = intPayload[3] = 0;
			hasSpawnIndexPayload = false;
			spawnIndexPayloadId = 0;
			spawnIndexBase = 0;
			hasBoolPayload = false;
			boolPayloadWidth = 0;
			boolPayload[0] = boolPayload[1] = boolPayload[2] = boolPayload[3] = 0;
			for (final SpawnEvent.PayloadFloatSlot fs : floatSlots) {
				fs.clear();
			}
			valid = true;
		}
	}

	public static final class EventCacheEntry {
		public int key;
		public int count;
		public int currentElementIdx;
		public int countDup;
		public int forwardFlag;
		public final int[] particleIndices = new int[MAX_PENDING_POSITIONS];
		public final float[] tFractions = new float[MAX_PENDING_POSITIONS];
		public final float[] lerpedTimes = new float[MAX_PENDING_POSITIONS];
		public boolean valid;
	}

	public static final class HandleRegisterBinding {
		public int reg;
		public int slot;
	}

	// ---- bound per scope run
	public final RegisterValue[][] scopeRegisters = new RegisterValue[Bank.SCOPE_BUCKETS][];
	public final ExternalStore.View externals = new ExternalStore.View();
	public byte[] constantsPool = new byte[0];
	public FunctionBinding[] functions = new FunctionBinding[0];
	public ExternalBinding[] externalBindings = new ExternalBinding[0];
	public SamplerResource[] samplers = new SamplerResource[0];
	public SpatialLayerResource[] spatialLayers = new SpatialLayerResource[0];
	public EventPayloadDecl.Kicked[] kickedEventDecls = new EventPayloadDecl.Kicked[0];
	public EventPayloadDecl.Element[] rootEventDecl = new EventPayloadDecl.Element[0];
	public ProximityHash[] spatialHashes = new ProximityHash[0];
	public ProximityHash[] spatialHashesWrite = new ProximityHash[0];
	public SceneCamera[] cameras = new SceneCamera[0];
	public float simLod;
	public float simLodBias;
	public float simLodDistanceMin = 5;
	public float simLodDistanceMax = 200;
	public FastRand rng;
	public float effectAge;
	public boolean effectIsRunning = true;
	public float timeWindowEnd;
	public float timeWindowStart;
	public final Mat4x3 sceneL2W = new Mat4x3();
	public final float[] spawnTranslate = new float[3];
	public final float[] spawnQuat = { 0, 0, 0, 1 };
	public final float[] spawnScale = { 1, 1, 1 };
	public boolean hasSpawnIntPayload;
	public int spawnIntPayloadWidth;
	public final int[] spawnIntPayload = new int[4];
	public int spawnIntPayloadId;
	public boolean hasSpawnBoolPayload;
	public int spawnBoolPayloadWidth;
	public final int[] spawnBoolPayload = new int[4];
	public int spawnBoolPayloadId;
	public int spawnPositionPayloadId;
	public int spawnOrientationPayloadId;
	public final SpawnEvent.PayloadFloatSlot[] spawnFloatSlots = SpawnEvent.newFloatSlots();
	public SpawnEvent.Queue spawnQueue;
	public boolean inInitScope;
	public long currentSelfId;

	// ---- scratch reset each scope run
	public final BuiltPayloadFloat[] builtPayloadFloats = new BuiltPayloadFloat[8];
	public final BuiltPayloadIndex builtPayloadIndex = new BuiltPayloadIndex();
	public final SpatialAppendSlot[] spatialAppendStaged = new SpatialAppendSlot[16];
	public final HandleRegisterBinding[] handleRegisterSlots = new HandleRegisterBinding[16];
	public int handleRegisterCount;
	public int functionDepth;
	public final PendingKickPayload[] pendingKickPayloads = new PendingKickPayload[8];
	public final PendingPayloadElement[] pendingPayloadElements = new PendingPayloadElement[8];
	public int nextPayloadElementId = 1;
	public int lastGenerateCount;
	public boolean lastGenerateValid;
	public final float[] lastGenerateTs = new float[MAX_PENDING_POSITIONS];
	public final float[] lastGenerateLerpedTimes = new float[MAX_PENDING_POSITIONS];
	public boolean selfKillRequested;
	public final EventCacheEntry[] eventCaches = new EventCacheEntry[MAX_EVENT_CACHE_ENTRIES];
	public int simUnitScratchCounter;

	public ExecContext() {
		for (int i = 0; i < builtPayloadFloats.length; i++) {
			builtPayloadFloats[i] = new BuiltPayloadFloat();
		}
		for (int i = 0; i < spatialAppendStaged.length; i++) {
			spatialAppendStaged[i] = new SpatialAppendSlot();
		}
		for (int i = 0; i < handleRegisterSlots.length; i++) {
			handleRegisterSlots[i] = new HandleRegisterBinding();
		}
		for (int i = 0; i < pendingKickPayloads.length; i++) {
			pendingKickPayloads[i] = new PendingKickPayload();
		}
		for (int i = 0; i < pendingPayloadElements.length; i++) {
			pendingPayloadElements[i] = new PendingPayloadElement();
		}
		for (int i = 0; i < eventCaches.length; i++) {
			eventCaches[i] = new EventCacheEntry();
		}
	}

	public void resetPerScopeRun() {
		simUnitScratchCounter = 0;
		nextPayloadElementId = 1;
		selfKillRequested = false;
		functionDepth = 0;
		handleRegisterCount = 0;
		lastGenerateValid = false;
		lastGenerateCount = 0;
		builtPayloadIndex.valid = false;
		for (final EventCacheEntry e : eventCaches) {
			e.valid = false;
		}
		for (final PendingPayloadElement p : pendingPayloadElements) {
			p.valid = false;
		}
		for (final PendingKickPayload p : pendingKickPayloads) {
			p.valid = false;
		}
		for (final BuiltPayloadFloat b : builtPayloadFloats) {
			b.valid = false;
		}
		for (final SpatialAppendSlot s : spatialAppendStaged) {
			s.valid = false;
		}
	}

	public EventCacheEntry allocEventCacheEntry(final int key) {
		for (final EventCacheEntry e : eventCaches) {
			if (e.valid && (e.key == key)) {
				return e;
			}
		}
		for (final EventCacheEntry e : eventCaches) {
			if (!e.valid) {
				e.key = key;
				e.count = 0;
				e.currentElementIdx = 0;
				e.countDup = 0;
				e.forwardFlag = 0;
				e.valid = true;
				return e;
			}
		}
		return null;
	}

	public EventCacheEntry findEventCacheEntry(final int key) {
		for (final EventCacheEntry e : eventCaches) {
			if (e.valid && (e.key == key)) {
				return e;
			}
		}
		return null;
	}

	public void setPendingKickCount(final int eventId, final int count) {
		for (final PendingKickPayload p : pendingKickPayloads) {
			if (p.valid && (p.eventId == eventId)) {
				p.count = count;
				return;
			}
		}
		for (final PendingKickPayload p : pendingKickPayloads) {
			if (!p.valid) {
				p.eventId = eventId;
				p.count = count;
				p.valid = true;
				return;
			}
		}
	}

	public int takePendingKickCount(final int eventId) {
		for (final PendingKickPayload p : pendingKickPayloads) {
			if (p.valid && (p.eventId == eventId)) {
				p.valid = false;
				return p.count;
			}
		}
		return 0;
	}

	public PendingPayloadElement findOrCreatePendingPayload(final int eventId) {
		for (final PendingPayloadElement s : pendingPayloadElements) {
			if (s.valid && (s.eventId == eventId)) {
				return s;
			}
		}
		for (final PendingPayloadElement s : pendingPayloadElements) {
			if (!s.valid) {
				s.reset(eventId);
				return s;
			}
		}
		return null;
	}

	public void stashBuiltPayloadFloat(final int elementId, final int width, final float[] value) {
		for (final BuiltPayloadFloat b : builtPayloadFloats) {
			if (b.valid && (b.elementId == elementId)) {
				b.width = width;
				System.arraycopy(value, 0, b.value, 0, 4);
				return;
			}
		}
		for (final BuiltPayloadFloat b : builtPayloadFloats) {
			if (!b.valid) {
				b.valid = true;
				b.elementId = elementId;
				b.width = width;
				System.arraycopy(value, 0, b.value, 0, 4);
				return;
			}
		}
		final BuiltPayloadFloat b = builtPayloadFloats[0];
		b.valid = true;
		b.elementId = elementId;
		b.width = width;
		System.arraycopy(value, 0, b.value, 0, 4);
	}
}
