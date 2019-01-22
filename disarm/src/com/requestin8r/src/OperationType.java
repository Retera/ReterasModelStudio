package com.requestin8r.src;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.mdl.Bone;

public class OperationType extends OperationNode {

	Map<String, Field<?>> fields;

	static class Field<E> {
		String name;
		String description;
		Class<E> fieldClass;

		public Field(final String name, final String description, final Class<E> fieldClass) {
			this.name = name;
			this.description = description;
			this.fieldClass = fieldClass;
		}
	}

	public OperationType(final String name, final String desc, final Image icon) {
		super(name, desc, icon);
		fields = new HashMap<String, Field<?>>();
	}

	public OperationType addField(final Field<?> what) {
		fields.put(what.name, what);
		return this;
	}

	public Field<?> getField(final String name) {
		return fields.get(name);
	}

	static class OperationGroup extends OperationNode {
		List<OperationNode> nodes = new ArrayList<OperationNode>();
		public OperationGroup(final String name, final String desc, final Image icon) {
			super(name, desc, icon);
		}

		public OperationGroup add(final OperationNode node) {
			nodes.add(node);
			if( path == null ) {
				path = name;
			}
			if( node.path == null ) {
				node.path = node.name;
			}
			node.path = path + " > " +node.path;
			if( node instanceof OperationGroup) {
				for( final OperationNode n: ((OperationGroup)node).nodes ) {
					n.path = path + " > " + n.path;
				}
			}
			return this;
		}
	}

	static OperationGroup add, remove, change;// = new OperationGroup("Operations", "Operations that you can perform on the current model.", IconGet.get("Skillz", 64));
	static {
		add = new OperationGroup("Add", "Add something to the model.", IconGet.get("Replay-SpeedUp", 64))
			.add(new OperationType("Shape", "Add a 3D shape from something else to the model.", IconGet.get("HumanArmorUpOne", 64))
				.addField(new Field<String>("Source Model", "The model from which to get the shape to add to this model.", String.class))
				.addField(new Field<String>("Which Part", "The part of the source model to add.", String.class))
				.addField(new Field<Bone>("Attach to", "The part of the this model to attach the shape onto.", Bone.class))
				.addField(new Field<Float>("Scale", "The size of the part once placed into this model.", Float.class))
				)
			.add(new OperationGroup("Player Color", "Add more to the model that changes color with ownership.", IconGet.get("OrcCaptureFlag", 64))
					.add(new OperationType("Shape", "Add a solid player color 3D shape from something else to the model.", IconGet.get("HumanArmorUpOne", 64))
						.addField(new Field<String>("Source Model", "The model from which to get the shape to add to this model.", String.class))
						.addField(new Field<String>("Which Part", "The part of the source model to add.", String.class))
						.addField(new Field<Bone>("Attach to", "The part of the this model to attach the shape onto.", Bone.class))
						.addField(new Field<Float>("Scale", "The size of the part once placed into this model.", Float.class))
						)
					.add(new OperationType("Custom Texture", "Add player color marks on the texture of this model, thereby creating a custom texture.", IconGet.get("Spy", 64))
						.addField(new Field<String>("Where?", "What part of the texture should gain player color markings?", String.class))
						.addField(new Field<Image>("New Texture", "Make a rough sketch of what the new texture should look like.", Image.class))
						)
				)
			.add(new OperationType("Glow", "Add a glow with a constant shape to the model.", new ImageIcon(RemovePanel.class.getResource("BTNTeamGlow00.png")).getImage())
				.addField(new Field<String>("Glow Texture", "The texture to use for the glow.", String.class))
				.addField(new Field<Bone>("Attach to", "The part of the this model to attach the glow onto.", Bone.class))
			)
			.add(new OperationType("Attachment", "Add an attachment point to the model.", IconGet.get("Glove", 64))
				.addField(new Field<String>("Name", "The name, or type, of attachment to add.", String.class))
				.addField(new Field<Bone>("Attach to", "What to attach the new attachment point onto within the model.", Bone.class))
			)
			.add(new OperationGroup("Special Effect", "A special effect in the model, such as a particle emitter or glow.", IconGet.get("ScatterRockets", 64))
				.add(new OperationType("Particle Emitter", "Add a particle emitter.", IconGet.get("Fire", 64))
					.addField(new Field<String>("Style", "The style of particle -- fire, magic, clouds, or custom", String.class))
					.addField(new Field<Color>("Color", "The emitter's color.", Color.class))
					.addField(new Field<Bone>("Attach to", "What to attach the new particle emitter onto within the model.", Bone.class))
				)
				.add(new OperationType("Ribbon Emitter", "Add a ribbon emitter.", IconGet.get("AntiMagicShell", 64))
					.addField(new Field<String>("Texture", "The texture to use for creating a ribbon", String.class))
					.addField(new Field<Color>("Color", "The emitter's color.", Color.class))
					.addField(new Field<Bone>("Attach to", "What to attach the new ribbon emitter onto within the model.", Bone.class))
				)
				.add(new OperationType("Light", "Add a light emitter.", IconGet.get("HolyBolt", 64))
					.addField(new Field<Color>("Color", "The emitter's color.", Color.class))
					.addField(new Field<Integer>("Intensity", "The emitter's brightness level.", Integer.class))
					.addField(new Field<Bone>("Attach to", "What to attach the new light emitter onto within the model.", Bone.class))
				)
				.add(new OperationType("Sound Effect", "Add a sound effect.", IconGet.get("CallToArms", 64))
					.addField(new Field<String>("Type", "What type of sound emitter?", String.class))
					.addField(new Field<Bone>("Attach to", "What to attach the new sound emitter onto within the model.", Bone.class))
				)
			)
			;
	}
}

class OperationNode {
	String name, path;
	Image icon;
	String description;
	public OperationNode(final String name, final String desc, final Image icon) {
		this.name = name;
		this.description = desc;
		this.icon = icon;
	}
}
