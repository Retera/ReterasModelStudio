package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

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
}
