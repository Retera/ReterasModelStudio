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

	public boolean isSpawner() {
		return renderers.length == 0;
	}
}
