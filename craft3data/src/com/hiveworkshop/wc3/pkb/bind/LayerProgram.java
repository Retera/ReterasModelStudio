package com.hiveworkshop.wc3.pkb.bind;

/** Everything the runtime needs for one layer: scope programs, renderers, samplers, events. */
public final class LayerProgram {
	public static final class AttributeDefault {
		public String name = "";
		public final float[] defaultValue = new float[4];
	}

	public static final class EventExternal {
		public String externalName = "";
		public int globalEventSlotId;
	}

	public int id;
	public String name = "";
	public ProgramDescriptor initProgram = new ProgramDescriptor();
	public ProgramDescriptor physicsProgram = new ProgramDescriptor();
	public ProgramDescriptor timeFixedProgram = new ProgramDescriptor();
	public ProgramDescriptor timeVaryingProgram = new ProgramDescriptor();
	public LayerRenderer[] renderers = new LayerRenderer[0];
	public String[] renderFieldNames = new String[0];
	public SamplerResource[] samplers = new SamplerResource[0];
	public SpatialLayerResource[] spatialLayers = new SpatialLayerResource[0];
	public AttributeDefault[] attributeDefaults = new AttributeDefault[0];
	public EventExternal[] eventExternals = new EventExternal[0];
	public EventPayloadDecl.Kicked[] kickedEventDecls = new EventPayloadDecl.Kicked[0];
	public EventPayloadDecl.Element[] rootEventDecl = new EventPayloadDecl.Element[0];

	public ProgramDescriptor evolveProgram() {
		if (!physicsProgram.isEmpty()) {
			return physicsProgram;
		}
		if (!timeFixedProgram.isEmpty()) {
			return timeFixedProgram;
		}
		return timeVaryingProgram;
	}

	public ProgramDescriptor[] scopePrograms() {
		return new ProgramDescriptor[] { initProgram, physicsProgram, timeFixedProgram, timeVaryingProgram };
	}

	public ExternalBinding findBindingAcrossScopes(final String name) {
		for (final ProgramDescriptor p : scopePrograms()) {
			final ExternalBinding hit = ExternalBinding.findByName(p.externals, name);
			if (hit != null) {
				return hit;
			}
		}
		return null;
	}

	/** The externals of init, physics, timeFixed, timeVarying in that order (the reference's lookup order). */
	public ExternalBinding findBindingInitPhysicsFixedVarying(final String name) {
		return findBindingAcrossScopes(name);
	}

	private int worldSpaceCache = -1;

	/**
	 * Whether this layer's programs ever convert between local and world space
	 * (xform_l2w / xform_w2l). A layer that never does simulates in the emitter's
	 * local frame: its positions, and the spawn payloads it hands to child layers,
	 * are relative to the emitter. The compiler emits the conversion for
	 * world-space layers and leaves it out for local-space ones, so the presence
	 * of the calls is the layer's space.
	 */
	public boolean simulatesInWorldSpace() {
		if (worldSpaceCache < 0) {
			boolean world = false;
			for (final ProgramDescriptor scope : scopePrograms()) {
				for (final FunctionBinding f : scope.functions) {
					if (f.canonicalName.contains("xform_l2w") || f.canonicalName.contains("xform_w2l")
							|| f.symbolName.contains("xform_l2w") || f.symbolName.contains("xform_w2l")) {
						world = true;
					}
				}
			}
			worldSpaceCache = world ? 1 : 0;
		}
		return worldSpaceCache == 1;
	}

	public boolean isSpawner() {
		return renderers.length == 0;
	}
}
