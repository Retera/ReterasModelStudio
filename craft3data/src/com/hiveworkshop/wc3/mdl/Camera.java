package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.IdObject.NodeFlags;
import com.hiveworkshop.wc3.mdx.CameraChunk;

/**
 * Camera class, these are the things most people would think of as a particle
 * emitter, I think. Blizzard favored use of these over ParticleEmitters and I
 * do too simply because I so often recycle data and there are more of these to
 * use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class Camera implements Named {
	String name;

	Vertex Position;

	double FieldOfView;
	double FarClip;
	double NearClip;

	ArrayList<AnimFlag> animFlags = new ArrayList<>();

	Vertex targetPosition;
	ArrayList<AnimFlag> targetAnimFlags = new ArrayList<>();
	private final SourceNode sourceNode = new SourceNode(this);
	private final TargetNode targetNode = new TargetNode(this);
	protected float[] bindPose;

	public SourceNode getSourceNode() {
		return sourceNode;
	}

	public TargetNode getTargetNode() {
		return targetNode;
	}

	private Camera() {

	}

	public Camera(final CameraChunk.Camera mdxSource) {
		this.name = mdxSource.name;
		Position = new Vertex(mdxSource.position);
		FieldOfView = mdxSource.fieldOfView;
		FarClip = mdxSource.farClippingPlane;
		NearClip = mdxSource.nearClippingPlane;
		targetPosition = new Vertex(mdxSource.targetPosition);
		if (mdxSource.cameraPositionTranslation != null) {
			add(new AnimFlag(mdxSource.cameraPositionTranslation));
		}
		if (mdxSource.cameraTargetTranslation != null) {
			targetAnimFlags.add(new AnimFlag(mdxSource.cameraTargetTranslation));
		}
		if (mdxSource.cameraRotation != null) {
			add(new AnimFlag(mdxSource.cameraRotation));
		}
	}

	public void setName(final String text) {
		name = text;
	}

	@Override
	public String getName() {
		return name;
	}

	public static Camera read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Camera")) {
			final Camera c = new Camera();
			c.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				if (line.contains("Position")) {
					c.Position = Vertex.parseText(line);
				} else if (line.contains("Rotation") || line.contains("Translation")) {
					MDLReader.reset(mdl);
					c.animFlags.add(AnimFlag.read(mdl));
				} else if (line.contains("FieldOfView")) {
					c.FieldOfView = MDLReader.readDouble(line);
				} else if (line.contains("FarClip")) {
					c.FarClip = MDLReader.readDouble(line);
				} else if (line.contains("NearClip")) {
					c.NearClip = MDLReader.readDouble(line);
				} else if (line.contains("Target")) {
					MDLReader.mark(mdl);
					line = MDLReader.nextLine(mdl);
					while (!line.startsWith("\t}")) {
						if (line.contains("Position")) {
							c.targetPosition = Vertex.parseText(line);
						} else if (line.contains("Translation")) {
							MDLReader.reset(mdl);
							c.targetAnimFlags.add(AnimFlag.read(mdl));
						} else {
							JOptionPane.showMessageDialog(null, "Camera target did not recognize data at: " + line
									+ "\nThis is probably not a major issue?");
						}
						MDLReader.mark(mdl);
						line = MDLReader.nextLine(mdl);
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"Camera did not recognize data at: " + line + "\nThis is probably not a major issue?");
				}

				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return c;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse Camera: Missing or unrecognized open statement.");
		}
		return null;
	}

	public void printTo(final PrintWriter writer) {
		// Remember to update the ids of things before using this
		// -- uses objectId value of idObject superclass
		// -- uses parentId value of idObject superclass
		// -- uses the parent (java Object reference) of idObject superclass
		writer.println(MDLReader.getClassName(this.getClass()) + " \"" + getName() + "\" {");
		writer.println("\tPosition " + Position.toString() + ",");
		for (int i = 0; i < animFlags.size(); i++) {
			if (animFlags.get(i).getName().equals("Translation")) {
				animFlags.get(i).printTo(writer, 1);
			}
		}
		for (int i = 0; i < animFlags.size(); i++) {
			if (animFlags.get(i).getName().equals("Rotation")) {
				animFlags.get(i).printTo(writer, 1);
			}
		}
		writer.println("\tFieldOfView " + MDLReader.doubleToString(FieldOfView) + ",");
		writer.println("\tFarClip " + MDLReader.doubleToString(FarClip) + ",");
		writer.println("\tNearClip " + MDLReader.doubleToString(NearClip) + ",");
		writer.println("\tTarget {");
		writer.println("\t\tPosition " + targetPosition.toString() + ",");
		for (int i = 0; i < targetAnimFlags.size(); i++) {
			targetAnimFlags.get(i).printTo(writer, 2);
		}
		writer.println("\t}");
		writer.println("}");
	}

	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	public Vertex getPosition() {
		return Position;
	}

	public void setPosition(final Vertex position) {
		Position = position;
	}

	public double getFieldOfView() {
		return FieldOfView;
	}

	public void setFieldOfView(final double fieldOfView) {
		FieldOfView = fieldOfView;
	}

	public double getFarClip() {
		return FarClip;
	}

	public void setFarClip(final double farClip) {
		FarClip = farClip;
	}

	public double getNearClip() {
		return NearClip;
	}

	public void setNearClip(final double nearClip) {
		NearClip = nearClip;
	}

	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	public void setAnimFlags(final ArrayList<AnimFlag> animFlags) {
		this.animFlags = animFlags;
	}

	public Vertex getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(final Vertex targetPosition) {
		this.targetPosition = targetPosition;
	}

	public ArrayList<AnimFlag> getTargetAnimFlags() {
		return targetAnimFlags;
	}

	public void setTargetAnimFlags(final ArrayList<AnimFlag> targetAnimFlags) {
		this.targetAnimFlags = targetAnimFlags;
	}

	public void remove(final AnimFlag timeline) {
		animFlags.remove(timeline);
	}

	public static final class SourceNode extends AbstractAnimatedNode {
		private final Camera parent;
		private static final QuaternionRotation rotationHeap = new QuaternionRotation(0, 0, 0, 1);
		private final Vertex axisHeap = new Vertex(0, 0, 0);

		private SourceNode(final Camera parent) {
			this.parent = parent;
		}

		@Override
		public void add(final AnimFlag timeline) {
			parent.animFlags.add(timeline);
		}

		@Override
		public void remove(final AnimFlag timeline) {
			parent.animFlags.remove(timeline);
		}

		@Override
		public List<AnimFlag> getAnimFlags() {
			return parent.animFlags;
		}

		@Override
		public boolean hasFlag(final NodeFlags flag) {
			return false;
		}

		@Override
		public AnimatedNode getParent() {
			return null;
		}

		@Override
		public Vertex getPivotPoint() {
			return parent.Position;
		}

		@Override
		public List<? extends AnimatedNode> getChildrenNodes() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public String getName() {
			return "Source of: " + parent.name;
		}

		@Override
		public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return 1;
		}

		@Override
		public Vertex getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation");
			if (translationFlag != null) {
				return (Vertex) translationFlag.interpolateAt(animatedRenderEnvironment);
			}
			return null;
		}

		@Override
		public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Rotation");
			if (translationFlag != null) {
				final Object interpolated = translationFlag.interpolateAt(animatedRenderEnvironment);
				if (interpolated instanceof Double) {
					final Double angle = (Double) interpolated;
					final Vertex targetTranslation = parent.targetNode.getRenderTranslation(animatedRenderEnvironment);
					final Vertex targetPosition = parent.targetPosition;
					final Vertex sourceTranslation = getRenderTranslation(animatedRenderEnvironment);
					final Vertex sourcePosition = parent.Position;
					axisHeap.x = (targetPosition.x + targetTranslation.x) - (sourcePosition.x + sourceTranslation.x);
					axisHeap.y = (targetPosition.y + targetTranslation.y) - (sourcePosition.y + sourceTranslation.y);
					axisHeap.z = (targetPosition.z + targetTranslation.z) - (sourcePosition.z + sourceTranslation.z);
					rotationHeap.set(axisHeap, angle);
					return rotationHeap;
				} else {
					return (QuaternionRotation) interpolated;
				}
			}
			return null;
		}

		public Double getRenderRotationScalar(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Rotation");
			if (translationFlag != null) {
				final Object interpolated = translationFlag.interpolateAt(animatedRenderEnvironment);
				if (interpolated instanceof Double) {
					final Double angle = (Double) interpolated;
					return angle;
				} else {
					return null;
				}
			}
			return null;
		}

		@Override
		public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}

	public static final class TargetNode extends AbstractAnimatedNode {
		private final Camera parent;

		private TargetNode(final Camera parent) {
			this.parent = parent;
		}

		@Override
		public void add(final AnimFlag timeline) {
			parent.targetAnimFlags.add(timeline);
		}

		@Override
		public void remove(final AnimFlag timeline) {
			parent.targetAnimFlags.remove(timeline);
		}

		@Override
		public List<AnimFlag> getAnimFlags() {
			return parent.animFlags;
		}

		@Override
		public boolean hasFlag(final NodeFlags flag) {
			return false;
		}

		@Override
		public AnimatedNode getParent() {
			return null;
		}

		@Override
		public Vertex getPivotPoint() {
			return parent.targetPosition;
		}

		@Override
		public List<? extends AnimatedNode> getChildrenNodes() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public String getName() {
			return "Target of: " + parent.name;
		}

		@Override
		public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return 1;
		}

		@Override
		public Vertex getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation");
			if (translationFlag != null) {
				return (Vertex) translationFlag.interpolateAt(animatedRenderEnvironment);
			}
			return null;
		}

		@Override
		public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.ROTATE_IDENTITY;
		}

		@Override
		public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}

	public void setBindPose(final float[] bindPose) {
		this.bindPose = bindPose;
	}

	public float[] getBindPose() {
		return bindPose;
	}
}
