package com.hiveworkshop.wc3.pkb.bind;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.pkb.PkbEffect;
import com.hiveworkshop.wc3.pkb.PkbObject;
import com.hiveworkshop.wc3.pkb.PkbValue;
import com.hiveworkshop.wc3.pkb.vm.BytecodeDecoder;
import com.hiveworkshop.wc3.pkb.vm.VmException;

/**
 * Turns a parsed baked effect into an {@link EffectPlan}: one
 * {@link LayerProgram} per layer slot of the PC layer graph, with decoded
 * scope programs, externals, functions, samplers, renderers, attribute
 * defaults, event payload declarations and the kick routing table.
 */
public final class EffectBinder {
	private EffectBinder() {
	}

	public static EffectPlan bind(final PkbEffect effect) {
		final EffectPlan plan = new EffectPlan();
		plan.versionMajor = effect.versionMajor;
		plan.versionMinor = effect.versionMinor;
		final PkbObject root = effect.getRootEffect();
		if (root == null) {
			return plan;
		}
		PkbObject graph = effect.follow(root, "LayerGraphCompileCache");
		if (graph == null) {
			for (final PkbObject g : effect.followAll(root, "LayerGraphCompileCaches")) {
				if (!g.type.equals("CLayerGraphCompileCache")) {
					continue;
				}
				if (graph == null) {
					graph = g;
				}
				if (g.getString("BuildVersionName", "").equals("PC")) {
					graph = g;
					break;
				}
			}
		}
		if ((graph == null) || !graph.type.equals("CLayerGraphCompileCache")) {
			return plan;
		}
		final List<PkbObject> layerSlots = effect.followAll(graph, "LayerSlots");
		if (layerSlots.isEmpty()) {
			return plan;
		}
		// event slots: links (v2.5) or indices (v2.9)
		final List<PkbObject> eventSlotObjects = new ArrayList<>();
		final PkbValue eventSlotsValue = graph.get("EventSlots");
		if (eventSlotsValue != null) {
			final int[] indices = eventSlotsValue.baseType().equals("link") ? eventSlotsValue.asLinks()
					: eventSlotsValue.asIntArray();
			for (final int index : indices) {
				eventSlotObjects.add(effect.object(index));
			}
		}
		final String[] eventNames = new String[eventSlotObjects.size()];
		final int[] eventParentLayerSlots = new int[eventSlotObjects.size()];
		final int[][] eventLayerTargets = new int[eventSlotObjects.size()][];
		for (int i = 0; i < eventSlotObjects.size(); i++) {
			final PkbObject evt = eventSlotObjects.get(i);
			eventNames[i] = "";
			eventParentLayerSlots[i] = -1;
			eventLayerTargets[i] = new int[0];
			if ((evt != null) && evt.type.equals("CLayerGraphCompileCache_EventSlot")) {
				eventNames[i] = evt.getString("EventName", "");
				eventParentLayerSlots[i] = evt.getInt("ParentLayerSlot", -1);
				final PkbValue targets = evt.get("LayerTargets");
				eventLayerTargets[i] = targets == null ? new int[0] : targets.asIntArray();
			}
		}
		final List<LayerProgram> built = new ArrayList<>();
		int nextLayerId = 0;
		for (final PkbObject slot : layerSlots) {
			final PkbObject cache = effect.follow(slot, "LayerCache");
			if ((cache == null) || !cache.type.equals("CLayerCompileCache")) {
				continue;
			}
			final LayerProgram lp = new LayerProgram();
			lp.id = nextLayerId++;
			lp.name = cache.getString("LayerName", cache.getCustomName());
			loadScopePrograms(effect, cache, lp);
			loadRenderers(effect, cache, lp);
			loadSamplers(effect, cache, lp);
			loadSpatialLayers(effect, cache, lp);
			loadAttributeDefaults(effect, cache, lp);
			loadEventPayloadDecls(effect, cache, lp);
			canonicaliseLayerExternals(lp);
			final PkbValue owned = slot.get("EventSlots");
			if (owned != null) {
				final List<LayerProgram.EventExternal> bindings = new ArrayList<>();
				for (final int globalSlotId : owned.asIntArray()) {
					if ((globalSlotId < 0) || (globalSlotId >= eventNames.length)) {
						continue;
					}
					final LayerProgram.EventExternal b = new LayerProgram.EventExternal();
					b.externalName = eventNames[globalSlotId];
					b.globalEventSlotId = globalSlotId;
					bindings.add(b);
				}
				lp.eventExternals = bindings.toArray(new LayerProgram.EventExternal[0]);
			}
			built.add(lp);
		}
		plan.layers = built.toArray(new LayerProgram[0]);
		final List<EventRoute> routes = new ArrayList<>();
		for (int globalSlotId = 0; globalSlotId < eventNames.length; globalSlotId++) {
			for (final int targetSlotIdx : eventLayerTargets[globalSlotId]) {
				if ((targetSlotIdx < 0) || (targetSlotIdx >= plan.layers.length)) {
					continue;
				}
				final EventRoute r = new EventRoute();
				r.channel = eventNames[globalSlotId];
				r.targetLayer = plan.layers[targetSlotIdx].id;
				r.globalEventSlotId = globalSlotId;
				r.parentLayerSlot = eventParentLayerSlots[globalSlotId];
				routes.add(r);
			}
		}
		plan.routes = routes.toArray(new EventRoute[0]);
		return plan;
	}

	private static boolean isV29(final PkbEffect effect) {
		return (effect.versionMajor > 2) || ((effect.versionMajor == 2) && (effect.versionMinor >= 9));
	}

	// ------------------------------------------------------------------ scope programs

	private static ProgramDescriptor descriptorFromBlob(final PkbEffect effect, final PkbObject blob) {
		final ProgramDescriptor d = new ProgramDescriptor();
		final PkbValue blobValue = blob.get("Blob");
		if (blobValue == null) {
			return d;
		}
		final byte[] body = blobValue.asBlobBytes();
		if (body.length < 36) {
			return d;
		}
		final ByteBuffer buffer = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < 5; i++) {
			d.registerCounts[i] = buffer.getInt(16 + (i * 4));
		}
		final boolean v29 = isV29(effect);
		int bytecodeBytes;
		int constStorageBytes;
		int bcStart;
		int constStart;
		if (v29) {
			bytecodeBytes = buffer.getInt(4);
			constStorageBytes = buffer.getInt(12);
			bcStart = 36;
			constStart = bcStart + bytecodeBytes;
		} else {
			constStorageBytes = buffer.getInt(8);
			bytecodeBytes = buffer.getInt(12);
			constStart = 36;
			bcStart = constStart + constStorageBytes;
		}
		if ((bytecodeBytes < 0) || (constStorageBytes < 0) || ((bcStart + bytecodeBytes) > body.length)
				|| ((constStart + constStorageBytes) > body.length)) {
			return d;
		}
		if (bytecodeBytes == 0) {
			return d;
		}
		d.bytecode = new byte[bytecodeBytes];
		System.arraycopy(body, bcStart, d.bytecode, 0, bytecodeBytes);
		d.constantsPool = new byte[constStorageBytes];
		System.arraycopy(body, constStart, d.constantsPool, 0, constStorageBytes);
		try {
			d.instructions = BytecodeDecoder.decode(d.bytecode);
		} catch (final VmException e) {
			System.err.println("[pkb bind] " + e.getMessage());
			d.instructions = new com.hiveworkshop.wc3.pkb.vm.Instruction[0];
		}
		if (v29) {
			final PkbValue namesValue = blob.get("RuntimeExternalNames");
			final PkbValue metaValue = blob.get("RuntimeExternalsBlob");
			final List<String> names = namesValue == null ? new ArrayList<>() : namesValue.asStrings();
			final int[] meta = metaValue == null ? new int[0] : metaValue.asIntArray();
			final List<ExternalBinding> bindings = new ArrayList<>();
			for (int i = 0; i < names.size(); i++) {
				if (names.get(i).isEmpty()) {
					continue;
				}
				final ExternalBinding b = new ExternalBinding();
				b.slot = i;
				b.canonicalSlot = i;
				b.name = names.get(i);
				final int mo = i * 5;
				if ((mo + 5) <= meta.length) {
					b.nativeType = meta[mo + 1];
					b.storageSize = meta[mo + 2];
					b.accessMask = meta[mo + 4];
				}
				bindings.add(b);
			}
			d.externals = bindings.toArray(new ExternalBinding[0]);
			final PkbValue callsValue = blob.get("RuntimeExternalMangledCalls");
			if (callsValue != null) {
				final List<String> calls = callsValue.asStrings();
				final FunctionBinding[] fns = new FunctionBinding[calls.size()];
				for (int i = 0; i < calls.size(); i++) {
					final FunctionBinding f = new FunctionBinding();
					f.slot = i;
					f.symbolName = calls.get(i);
					f.canonicalName = FunctionBinding.canonicalizeSymbol(f.symbolName);
					fns[i] = f;
				}
				d.functions = fns;
			}
			return d;
		}
		final int[] extIndices = blob.getLinks("Externals");
		final List<ExternalBinding> bindings = new ArrayList<>();
		for (int i = 0; i < extIndices.length; i++) {
			final PkbObject ext = effect.object(extIndices[i]);
			if ((ext == null) || !ext.type.equals("CCompilerBlobCacheExternal")) {
				continue;
			}
			final ExternalBinding b = new ExternalBinding();
			b.slot = i;
			b.canonicalSlot = i;
			b.name = ext.getString("NameGUID", "");
			b.typeName = ext.getString("TypeName", "");
			b.nativeType = ext.getInt("NativeType", 0);
			b.storageSize = ext.getInt("StorageSize", 0);
			b.accessMask = ext.getInt("AccessMask", 0);
			bindings.add(b);
		}
		d.externals = bindings.toArray(new ExternalBinding[0]);
		final int[] callIndices = blob.getLinks("ExternalCalls");
		final List<FunctionBinding> fns = new ArrayList<>();
		for (int i = 0; i < callIndices.length; i++) {
			final PkbObject fn = effect.object(callIndices[i]);
			if ((fn == null) || !fn.type.equals("CCompilerBlobCacheFunctionDef")) {
				continue;
			}
			final FunctionBinding f = new FunctionBinding();
			f.slot = i;
			f.symbolName = fn.getString("SymbolName", "");
			f.canonicalName = FunctionBinding.canonicalizeSymbol(f.symbolName);
			f.symbolSlot = fn.getInt("SymbolSlot", 0);
			f.traits = fn.getInt("FunctionTraits", 0);
			fns.add(f);
		}
		d.functions = fns.toArray(new FunctionBinding[0]);
		return d;
	}

	private static void applyBlob(final PkbEffect effect, final PkbObject blob, final LayerProgram lp) {
		if ((blob == null) || !blob.type.equals("CCompilerBlobCache")) {
			return;
		}
		final int ident = blob.getInt("Identifier", 0);
		final ProgramDescriptor d = descriptorFromBlob(effect, blob);
		switch (ident) {
		case 0:
			if (lp.initProgram.isEmpty()) {
				lp.initProgram = d;
			}
			break;
		case 3:
			if (lp.physicsProgram.isEmpty()) {
				lp.physicsProgram = d;
			}
			break;
		case 4:
			if (lp.timeFixedProgram.isEmpty()) {
				lp.timeFixedProgram = d;
			}
			break;
		case 5:
			if (lp.timeVaryingProgram.isEmpty()) {
				lp.timeVaryingProgram = d;
			}
			break;
		default:
			break;
		}
	}

	private static void loadScopePrograms(final PkbEffect effect, final PkbObject cache, final LayerProgram lp) {
		if (isV29(effect)) {
			for (final PkbObject blob : effect.followAll(cache, "BlobCache_Backends")) {
				applyBlob(effect, blob, lp);
			}
		} else {
			for (final PkbObject blob : effect.followAll(cache, "BlobCache_IR_TimeFixed")) {
				applyBlob(effect, blob, lp);
			}
			final PkbObject tv = effect.follow(cache, "BlobCache_IR_TimeVarying");
			if ((tv != null) && tv.type.equals("CCompilerBlobCache")) {
				lp.timeVaryingProgram = descriptorFromBlob(effect, tv);
			}
		}
	}

	private static void canonicaliseLayerExternals(final LayerProgram lp) {
		final List<String> names = new ArrayList<>();
		for (final ProgramDescriptor p : lp.scopePrograms()) {
			for (final ExternalBinding b : p.externals) {
				int idx = names.indexOf(b.name);
				if (idx < 0) {
					names.add(b.name);
					idx = names.size() - 1;
				}
				b.canonicalSlot = idx + 1;
			}
		}
	}

	// ------------------------------------------------------------------ renderers

	private static void loadRenderers(final PkbEffect effect, final PkbObject cache, final LayerProgram lp) {
		final int[] fieldIndices = cache.getLinks("Fields");
		if (fieldIndices.length > 0) {
			lp.renderFieldNames = new String[fieldIndices.length];
			for (int i = 0; i < fieldIndices.length; i++) {
				final PkbObject f = effect.object(fieldIndices[i]);
				lp.renderFieldNames[i] = f == null ? "" : renderFieldName(f);
			}
		}
		final List<LayerRenderer> renderers = new ArrayList<>();
		for (final PkbObject r : effect.followAll(cache, "Renderers")) {
			if (r.type.equals("CLayerCompileCacheRenderer")) {
				renderers.add(buildRenderer(effect, r));
			}
		}
		lp.renderers = renderers.toArray(new LayerRenderer[0]);
	}

	private static String renderFieldName(final PkbObject f) {
		final String nm = f.getString("FieldName", "");
		if (!nm.isEmpty()) {
			return nm;
		}
		final PkbValue idValue = f.get("FieldNameID");
		if (idValue == null) {
			return "";
		}
		final int id = idValue.asInt();
		if ((id & 0x80000000) == 0) {
			return "";
		}
		return Integer.toHexString(id & 0x7FFFFFFF).toUpperCase();
	}

	private static LayerRenderer buildRenderer(final PkbEffect effect, final PkbObject rObj) {
		final LayerRenderer out = new LayerRenderer();
		final int cls = rObj.getInt("RendererClass", 0);
		out.rendererClass = ((cls >= 0) && (cls < 4)) ? cls : LayerRenderer.CLASS_BILLBOARD;
		boolean transparentEnabled = false;
		boolean opaqueEnabled = false;
		int transparentType = 0;
		int opaqueType = 0;
		boolean sawEnableRendering = false;
		for (final PkbObject p : effect.followAll(rObj, "Properties")) {
			if (!p.type.equals("CLayerCompileCacheRendererProperty")) {
				continue;
			}
			final String pname = p.getString("PropertyName", "");
			final PkbValue numeric = p.get("PropertyValueNumeric");
			final int word0 = numeric == null ? 0 : numeric.asInt();
			final boolean toggleOn = word0 != 0;
			final String str = p.getString("PropertyValueStr", "");
			switch (pname) {
			case "Lit":
				if (toggleOn) {
					out.isLit = true;
				}
				break;
			case "SoftParticles":
				if (toggleOn) {
					out.hasSoftParticles = true;
				}
				break;
			case "AlphaRemap":
				if (toggleOn) {
					out.hasAlphaLut = true;
				}
				break;
			case "Atlas":
				if (toggleOn) {
					out.isAtlas = true;
				}
				break;
			case "Distortion":
				if (toggleOn) {
					out.isDistortion = true;
				}
				break;
			case "FlipUVs":
				out.hasFlipUVs = toggleOn;
				break;
			case "CustomTextureU":
				out.hasCustomTextureU = toggleOn;
				break;
			case "EnableSize2D":
				out.hasEnableSize2D = toggleOn;
				break;
			case "EnableRendering":
				out.isRenderingEnabled = toggleOn;
				sawEnableRendering = true;
				break;
			case "Transparent":
				transparentEnabled = toggleOn;
				out.hasTransparent = toggleOn;
				break;
			case "Diffuse.DiffuseMap":
			case "Blend.TextureBase":
				if (!str.isEmpty()) {
					out.diffuseTexturePath = str;
				}
				break;
			case "Atlas.SubDiv":
				if (numeric != null) {
					final float[] words = numeric.asFloats();
					out.atlasSubDivX = (int) words[0];
					out.atlasSubDivY = words.length > 1 ? (int) words[1] : 0;
				}
				break;
			case "Atlas.Blending":
				out.atlasBlending = word0;
				break;
			case "AlphaRemap.AlphaMap":
				if (!str.isEmpty()) {
					out.alphaRemapMapPath = str;
				}
				break;
			case "TextureUVs.FlipU":
				out.textureFlipU = toggleOn;
				break;
			case "TextureUVs.FlipV":
				out.textureFlipV = toggleOn;
				break;
			case "TextureUVs.RotateTexture":
				out.textureRotateTexture = toggleOn;
				break;
			case "BillboardingMode":
				out.billboardingMode = word0;
				break;
			case "Transparent.SortMode":
				out.transparentSortMode = word0;
				break;
			case "Transparent.Type":
				transparentType = word0;
				break;
			case "Opaque":
				opaqueEnabled = toggleOn;
				break;
			case "Opaque.Type":
				opaqueType = word0;
				break;
			default:
				break;
			}
		}
		final List<LayerRenderer.ParticleInput> inputs = new ArrayList<>();
		for (final PkbObject s : effect.followAll(rObj, "Streams")) {
			if (!s.type.equals("CLayerCompileCacheRendererParticleInput")) {
				continue;
			}
			final LayerRenderer.ParticleInput in = new LayerRenderer.ParticleInput();
			in.semantic = s.getInt("Semantic", 0);
			in.indexInStorage = s.getInt("IndexInStorage", 0);
			in.additionalFieldName = s.getString("AdditionalFieldName", "");
			inputs.add(in);
		}
		out.particleInputs = inputs.toArray(new LayerRenderer.ParticleInput[0]);
		if (!sawEnableRendering) {
			out.isRenderingEnabled = true;
		}
		if (transparentEnabled) {
			out.blendMode = transparentType <= LayerRenderer.BLEND_BLEND_ADD ? transparentType
					: LayerRenderer.BLEND_OPAQUE;
		} else if (opaqueEnabled) {
			out.blendMode = opaqueType == 1 ? LayerRenderer.BLEND_ALPHA_KEY : LayerRenderer.BLEND_OPAQUE;
		} else {
			out.blendMode = LayerRenderer.BLEND_OPAQUE;
		}
		return out;
	}

	// ------------------------------------------------------------------ samplers

	private static void read3(final PkbObject obj, final String name, final float[] out) {
		final PkbValue v = obj.get(name);
		if (v == null) {
			return;
		}
		final float[] f = v.asFloats();
		for (int i = 0; (i < 3) && (i < f.length); i++) {
			out[i] = f[i];
		}
	}

	private static float[] floats(final PkbObject obj, final String name) {
		final PkbValue v = obj.get(name);
		return v == null ? new float[0] : v.asFloatArray();
	}

	private static void loadSamplers(final PkbEffect effect, final PkbObject cache, final LayerProgram lp) {
		final List<SamplerResource> out = new ArrayList<>();
		for (final PkbObject sObj : effect.followAll(cache, "Samplers")) {
			if (!sObj.type.equals("CLayerCompileCacheSampler")) {
				continue;
			}
			final SamplerResource res = new SamplerResource();
			res.name = sObj.getString("SamplerName", "");
			out.add(res);
			final PkbObject data = effect.follow(sObj, "Sampler");
			if (data == null) {
				continue;
			}
			switch (data.type) {
			case "CParticleNodeSamplerData_Curve":
			case "CSamplerCurve":
				buildCurve(res, data);
				break;
			case "CParticleNodeSamplerData_Shape":
				buildShape(res, data);
				break;
			case "CParticleNodeSamplerData_EventStream":
				res.kind = SamplerResource.KIND_EVENT_STREAM;
				res.eventStream.times = floats(data, "Times");
				break;
			case "CParticleNodeSamplerData_Turbulence":
				buildTurbulence(res, data);
				break;
			case "CParticleNodeSamplerData_Texture":
				res.kind = SamplerResource.KIND_TEXTURE;
				res.textureScriptOutputType = data.getInt("ScriptOutputType", 4);
				if ((res.textureScriptOutputType != 1) && (res.textureScriptOutputType != 4)) {
					res.textureScriptOutputType = 4;
				}
				break;
			default:
				break;
			}
		}
		lp.samplers = out.toArray(new SamplerResource[0]);
	}

	private static void buildCurve(final SamplerResource res, final PkbObject data) {
		res.kind = SamplerResource.KIND_CURVE;
		final SamplerResource.Curve c = res.curve;
		c.times = floats(data, "Times");
		c.values = floats(data, "FloatValues");
		c.tangents = floats(data, "FloatTangents");
		final int valueType = data.getInt("ValueType", 0);
		int components = 0;
		if ((valueType >= 1) && (valueType <= 4)) {
			components = valueType;
		} else if (c.times.length > 0) {
			final int derived = c.values.length / c.times.length;
			if ((derived >= 1) && (derived <= 4)) {
				components = derived;
			}
		}
		c.components = components > 0 ? components : 1;
		c.interpolator = data.getInt("Interpolator", 0);
		c.looped = data.getBool("IsLoopedCurve", false);
		c.isProbabilityCurve = data.getBool("IsProbabilityCurve", false);
		CurveCdf.build(c);
	}

	private static void buildShape(final SamplerResource res, final PkbObject data) {
		res.kind = SamplerResource.KIND_SHAPE;
		final SamplerResource.Shape sh = res.shape;
		sh.type = data.getInt("ShapeType", 0);
		sh.dimensionality = data.getInt("SampleDimensionality", 2);
		sh.radius = data.getFloat("Radius", 1.0f);
		sh.innerRadius = data.getFloat("InnerRadius", 0);
		sh.height = data.getFloat("Height", 0);
		sh.hemisphere = data.getBool("Hemisphere", false);
		sh.transformTranslate = data.getBool("TransformTranslate", true);
		sh.transformRotate = data.getBool("TransformRotate", true);
		read3(data, "BoxDimensions", sh.boxDimensions);
		read3(data, "Position", sh.position);
		read3(data, "EulerOrientation", sh.eulerOrientation);
		read3(data, "NonUniformScale", sh.nonUniformScale);
		sh.meshResource = data.getString("MeshResource", "");
		read3(data, "MeshScale", sh.meshScale);
		sh.meshSamplingMode = data.getInt("MeshSamplingMode", 1);
		final float degToRad = 0.01745329252f;
		sh.eulerOrientation[0] *= degToRad;
		sh.eulerOrientation[1] *= degToRad;
		sh.eulerOrientation[2] *= degToRad;
		final float posY = sh.position[1];
		sh.position[1] = -sh.position[2];
		sh.position[2] = posY;
		swap12(sh.nonUniformScale);
		swap12(sh.meshScale);
		swap12(sh.boxDimensions);
	}

	private static void swap12(final float[] v) {
		final float t = v[1];
		v[1] = v[2];
		v[2] = t;
	}

	private static void buildTurbulence(final SamplerResource res, final PkbObject data) {
		res.kind = SamplerResource.KIND_TURBULENCE;
		final SamplerResource.Turbulence tb = res.turbulence;
		tb.external = data.getInt("DataSource", 0) == 1;
		tb.strength = data.getFloat("Strength", 0.1f);
		tb.wavelength = data.getFloat("Wavelength", 0.5f);
		tb.globalScale = data.getFloat("GlobalScale", 1.0f);
		tb.lacunarity = data.getFloat("Lacunarity", 0.5f);
		tb.gain = data.getFloat("Gain", 0.5f);
		tb.gainMultiplier = data.getFloat("GainMultiplier", 1.0f);
		tb.octaves = data.getInt("Octaves", 2);
		tb.interpolator = data.getInt("Interpolator", 1);
		tb.seed = data.getInt("InitialSeed", 1114229502);
		tb.timeScale = data.getFloat("TimeScale", 0);
		tb.timeBase = data.getFloat("TimeBase", 0);
		tb.timeRandomVariation = data.getFloat("TimeRandomVariation", 0.5f);
		if (!tb.external) {
			res.turbulenceField = new TurbulenceField(tb);
		}
	}

	// ------------------------------------------------------------------ spatial layers, attributes, events

	private static void loadSpatialLayers(final PkbEffect effect, final PkbObject cache, final LayerProgram lp) {
		final List<SpatialLayerResource> out = new ArrayList<>();
		for (final PkbObject slObj : effect.followAll(cache, "SpatialLayers")) {
			if (!slObj.type.equals("CLayerCompileCacheSpatialLayer")) {
				continue;
			}
			final SpatialLayerResource res = new SpatialLayerResource();
			res.name = slObj.getString("SpatialLayerLocalName", "");
			res.fullName = slObj.getString("SpatialLayerName", "");
			res.cellSize = slObj.getFloat("SpatialLayerCellSize", 0.75f);
			if (res.cellSize <= 0) {
				res.cellSize = 0.75f;
			}
			res.flags = slObj.getInt("SpatialLayerFlags", 1);
			final List<SpatialLayerResource.Payload> payloads = new ArrayList<>();
			for (final PkbObject pObj : effect.followAll(slObj, "SpatialLayerPayload")) {
				if (!pObj.type.equals("CLayerCompileCacheSpatialLayerPayload")) {
					continue;
				}
				final SpatialLayerResource.Payload pl = new SpatialLayerResource.Payload();
				pl.name = pObj.getString("PayloadName", "");
				pl.payloadType = pObj.getInt("PayloadType", 0);
				pl.payloadFlags = pObj.getInt("PayloadFlags", 0);
				payloads.add(pl);
			}
			res.payloads = payloads.toArray(new SpatialLayerResource.Payload[0]);
			out.add(res);
		}
		lp.spatialLayers = out.toArray(new SpatialLayerResource[0]);
	}

	private static void loadAttributeDefaults(final PkbEffect effect, final PkbObject cache, final LayerProgram lp) {
		final List<LayerProgram.AttributeDefault> out = new ArrayList<>();
		for (final PkbObject aObj : effect.followAll(cache, "Attribs")) {
			if (!aObj.type.equals("CLayerCompileCacheAttrib")) {
				continue;
			}
			final LayerProgram.AttributeDefault a = new LayerProgram.AttributeDefault();
			a.name = aObj.getString("AttrName", "");
			final PkbValue v = aObj.get("AttrDefaultValueF4");
			if (v != null) {
				final float[] f = v.asFloats();
				for (int i = 0; (i < 4) && (i < f.length); i++) {
					a.defaultValue[i] = f[i];
				}
			}
			out.add(a);
		}
		lp.attributeDefaults = out.toArray(new LayerProgram.AttributeDefault[0]);
	}

	private static EventPayloadDecl.Element[] parseEventPayload(final PkbEffect effect, final PkbObject eventObj) {
		final int[] plIndices = eventObj.getLinks("EventPayload");
		final EventPayloadDecl.Element[] arr = new EventPayloadDecl.Element[plIndices.length];
		for (int k = 0; k < plIndices.length; k++) {
			final EventPayloadDecl.Element e = new EventPayloadDecl.Element();
			arr[k] = e;
			final PkbObject pl = effect.object(plIndices[k]);
			if ((pl == null) || !pl.type.equals("CLayerCompileCacheEventPayload")) {
				continue;
			}
			final int type = pl.getInt("PayloadType", 0);
			if ((type >= 31) && (type <= 34)) {
				e.width = type - 30;
			} else if (type == 36) {
				e.width = 4;
			}
			e.nameId = EventPayloadDecl.payloadNameId(pl.getString("PayloadName", ""));
			e.flags = pl.getInt("PayloadFlags", 0);
			e.kind = pl.getInt("PayloadKind", 0);
		}
		return arr;
	}

	private static void loadEventPayloadDecls(final PkbEffect effect, final PkbObject cache, final LayerProgram lp) {
		final List<EventPayloadDecl.Kicked> decls = new ArrayList<>();
		for (final PkbObject ev : effect.followAll(cache, "Events")) {
			if (!ev.type.equals("CLayerCompileCacheEvent")) {
				continue;
			}
			final EventPayloadDecl.Kicked d = new EventPayloadDecl.Kicked();
			d.channel = ev.getString("EventName", "");
			d.elements = parseEventPayload(effect, ev);
			decls.add(d);
		}
		lp.kickedEventDecls = decls.toArray(new EventPayloadDecl.Kicked[0]);
		final PkbObject root = effect.follow(cache, "RootEvent");
		if ((root != null) && root.type.equals("CLayerCompileCacheEvent")) {
			lp.rootEventDecl = parseEventPayload(effect, root);
		}
	}
}
