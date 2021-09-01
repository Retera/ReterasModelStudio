package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;

public class TempStuffFromEditableModel {
	public static EditableModel deepClone(final EditableModel what, final String newName) {
		// Need to do a real save, because of strings being passed by reference.
		// Maybe other objects I didn't think about (or the code does by mistake).
		final EditableModel newModel = TempOpenModelStuff.createEditableModel(new MdlxModel(TempSaveModelStuff.toMdlx(what).saveMdx()));

		newModel.setName(newName);
		newModel.setFileRef(what.getFile());

		return newModel;
	}

	public static Object getAnimFlagSource(EditableModel model, final AnimFlag<?> animFlag) {
		// Probably will cause a bunch of lag, be wary
		for (final Material m : model.getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				final AnimFlag<?> timeline = lay.find(animFlag.getName());
				if (timeline != null) {
					return lay;
				}
			}
		}
		if (model.getTexAnims() != null) {
			for (final TextureAnim textureAnim : model.getTexAnims()) {
				final AnimFlag<?> timeline = textureAnim.find(animFlag.getName());
				if (timeline != null) {
					return textureAnim;
				}
			}
		}
		if (model.getGeosetAnims() != null) {
			for (final GeosetAnim geosetAnim : model.getGeosetAnims()) {
				final AnimFlag<?> timeline = geosetAnim.find(animFlag.getName());
				if (timeline != null) {
					return geosetAnim;
				}
			}
		}

//		for (final IdObject object : idObjects) {
		for (final IdObject object : model.getAllObjects()) {
			final AnimFlag<?> timeline = object.find(animFlag.getName());
			if (timeline != null) {
				return object;
			}
		}

		if (model.getCameras() != null) {
			for (final Camera x : model.getCameras()) {
				AnimFlag<?> timeline = x.getSourceNode().find(animFlag.getName());
				if (timeline != null) {
					return x;
				}

				timeline = x.getTargetNode().find(animFlag.getName());
				if (timeline != null) {
					return x;
				}
			}
		}

		return null;
	}

	/**
	 * Deletes all the animation in the model from the time track.
	 *
	 * Might leave behind nice things like global sequences if the code works out.
	 */
	public static void deleteAllAnimation(EditableModel model, final boolean clearUnusedNodes) {
		if (clearUnusedNodes) {
			// check the emitters
			final List<ParticleEmitter> particleEmitters = model.getParticleEmitters();
			final List<ParticleEmitter2> particleEmitters2 = model.getParticleEmitter2s();
			final List<RibbonEmitter> ribbonEmitters = model.getRibbonEmitters();
			final List<ParticleEmitterPopcorn> popcornEmitters = model.getPopcornEmitters();
			final List<IdObject> emitters = new ArrayList<>();
			emitters.addAll(particleEmitters2);
			emitters.addAll(particleEmitters);
			emitters.addAll(ribbonEmitters);
			emitters.addAll(popcornEmitters);

			for (final IdObject emitter : emitters) {
				int talliesFor = 0;
				int talliesAgainst = 0;
//				final AnimFlag<?> visibility = ((VisibilitySource) emitter).getVisibilityFlag();
				final AnimFlag<?> visibility = emitter.getVisibilityFlag();
				for (final Animation anim : model.getAnims()) {
					final Integer animStartTime = anim.getStart();
					final Number visible = (Number) visibility.valueAt(anim, animStartTime);
					if ((visible == null) || (visible.floatValue() > 0)) {
						talliesFor++;
					} else {
						talliesAgainst++;
					}
				}
				if (talliesAgainst > talliesFor) {
					model.remove(emitter);
				}
			}
		}
		final List<AnimFlag<?>> flags = model.getAllAnimFlags();
//		final List<EventObject> evts = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> evts = model.getEvents();
		for (final Animation anim : model.getAnims()) {
//			anim.clearData(flags, evts);
			for (AnimFlag<?> af : flags) {
				if (((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
					// !af.hasGlobalSeq && was above before
					af.deleteAnim(anim);
				}
			}
			for (EventObject e : evts) {
				e.deleteAnim(anim);
			}
		}
		if (clearUnusedNodes) {
			for (final EventObject e : evts) {
				if (e.size() <= 0) {
					model.remove(e);
//					idObjects.remove(e);
				}
			}
		}
		model.clearAnimations();
	}


	public static void setGlobalSequenceLength(EditableModel model, final int globalSequenceId, final Integer newLength) {
		if (globalSequenceId < model.globalSeqs.size()) {
			GlobalSeq globalSeq = model.globalSeqs.get(globalSequenceId);
			final Integer prevLength = globalSeq.getLength();
			final List<AnimFlag<?>> allAnimFlags = model.getAllAnimFlags();
			for (final AnimFlag<?> af : allAnimFlags) {
				if (af.hasGlobalSeq() && af.getGlobalSeq() == (globalSeq)) {// TODO eliminate redundant structure
					// todo maybe check if user wants to scale animation?
					if (af.getGlobalSeq().equals(prevLength)) {
//						af.setGlobalSeqLength(newLength);
					}
				}
			}
//			final List<EventObject> sortedEventObjects = (List<EventObject>) sortedIdObjects(EventObject.class);
			final List<EventObject> sortedEventObjects = model.getEvents();
			for (final EventObject eventObject : sortedEventObjects) {
				// TODO eliminate redundant structure
				if (eventObject.hasGlobalSeq() && (eventObject.getGlobalSeq() == globalSeq)) {
					if (eventObject.getGlobalSeq().equals(prevLength)) {
//						eventObject.setGlobalSeq(newLength);
					}
				}
			}
			globalSeq.setLength(newLength);
		}
	}


	public static void removeAllTimelinesForGlobalSeq(EditableModel model, final GlobalSeq selectedValue) {
		for (final Material m : model.getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				removeAllTimelinesForGlobalSeq(lay, selectedValue);
			}
		}
		if (model.getTexAnims() != null) {
			for (final TextureAnim texa : model.getTexAnims()) {
				if (texa != null) {
					removeAllTimelinesForGlobalSeq(texa, selectedValue);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (model.getGeosetAnims() != null) {
			for (final GeosetAnim ga : model.getGeosetAnims()) {
				if (ga != null) {
					removeAllTimelinesForGlobalSeq(ga, selectedValue);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}

		for (final IdObject object : model.getAllObjects()) {
			removeAllTimelinesForGlobalSeq(object, selectedValue);
		}

		if (model.getCameras() != null) {
			for (final Camera x : model.getCameras()) {
				removeAllTimelinesForGlobalSeq(x.getSourceNode(), selectedValue);
				removeAllTimelinesForGlobalSeq(x.getTargetNode(), selectedValue);
			}
		}
	}

	public static void removeAllTimelinesForGlobalSeq(TimelineContainer timelineContainer, GlobalSeq selectedValue) {
		Set<AnimFlag<?>> toRemove = new HashSet<>();
		for (AnimFlag<?> timeline : timelineContainer.animFlags.values()) {
			if (selectedValue.equals(timeline.getGlobalSeq())) {
				toRemove.add(timeline);
			}
		}
		for (AnimFlag<?> timeline : toRemove) {
			timelineContainer.remove(timeline);
		}
	}

	public static void exportBitmapTextureFile(Component component, EditableModel model, Bitmap selectedValue, File file) {
		if (file.exists()) {
			final int confirmOption = JOptionPane.showConfirmDialog(component,
					"File \"" + file.getPath() + "\" already exists. Continue?", "Confirm Export",
					JOptionPane.YES_NO_OPTION);
			if (confirmOption == JOptionPane.NO_OPTION) {
				return;
			}
		}
		final DataSource wrappedDataSource = model.getWrappedDataSource();
		final File workingDirectory = model.getWorkingDirectory();
		BufferedImage bufferedImage = BLPHandler.getImage(selectedValue, wrappedDataSource);
		String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toUpperCase();
		if (fileExtension.equals("BMP") || fileExtension.equals("JPG") || fileExtension.equals("JPEG")) {
			JOptionPane.showMessageDialog(component,
					"Warning: Alpha channel was converted to black. Some data will be lost" +
							"\nif you convert this texture back to Warcraft BLP.");
			bufferedImage = ImageUtils.removeAlphaChannel(bufferedImage);
		}
		if (fileExtension.equals("BLP")) {
			fileExtension = "blp";
		}
		boolean directExport = false;
		if (selectedValue.getPath().toLowerCase(Locale.US).endsWith(fileExtension)) {
			final CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
			if (gameDataFileSystem.has(selectedValue.getPath())) {
				final InputStream mpqFile = gameDataFileSystem.getResourceAsStream(selectedValue.getPath());
				try {
					Files.copy(mpqFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
					directExport = true;
				} catch (final IOException e) {
					e.printStackTrace();
					ExceptionPopup.display(e);
				}
			} else {
				if (workingDirectory != null) {
					final File wantedFile = new File(workingDirectory.getPath() + File.separatorChar + selectedValue.getPath());
					if (wantedFile.exists()) {
						try {
							Files.copy(wantedFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
							directExport = true;
						} catch (final IOException e) {
							e.printStackTrace();
							ExceptionPopup.display(e);
						}
					}
				}

			}
		}
		if (!directExport) {
			final boolean write;
			try {
				write = ImageIO.write(bufferedImage, fileExtension, file);
				if (!write) {
					JOptionPane.showMessageDialog(component, "File type unknown or unavailable");
				}
			} catch (final IOException e) {
				e.printStackTrace();
				ExceptionPopup.display(e);
			}
		}
	}
}
