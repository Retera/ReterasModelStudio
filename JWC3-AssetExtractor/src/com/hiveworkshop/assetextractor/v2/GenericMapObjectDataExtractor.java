package com.hiveworkshop.assetextractor.v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.hiveworkshop.wc3.mdx.AttachmentChunk;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mdx.TextureChunk;
import com.hiveworkshop.wc3.mpq.Codebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public class GenericMapObjectDataExtractor {
	private static final War3ID BUFF_MISSILE_ART = War3ID.fromString("fmat");
	private static final War3ID BUFF_EFFECT_ART = War3ID.fromString("feat");
	private static final War3ID BUFF_SPECIAL_ART = War3ID.fromString("fsat");
	private static final War3ID BUFF_TARGET_ART = War3ID.fromString("ftat");
	private static final War3ID ABIL_MISSILE_ART = War3ID.fromString("amat");
	private static final War3ID ABIL_AOE_ART = War3ID.fromString("aaea");
	private static final War3ID ABIL_EFFECT_ART = War3ID.fromString("aeat");
	private static final War3ID ABIL_SPECIAL_ART = War3ID.fromString("asat");
	private static final War3ID ABIL_TARGET_ART = War3ID.fromString("atat");
	private static final War3ID ABIL_CASTER_ART = War3ID.fromString("acat");
	private static final War3ID ITEM_MODEL_FILE = War3ID.fromString("ifil");
	private static final War3ID UNIT_PROJECTILE_ART_2 = War3ID.fromString("ua2m");
	private static final War3ID UNIT_PROJECTILE_ART_1 = War3ID.fromString("ua1m");
	private static final War3ID UNIT_MODEL_FILE = War3ID.fromString("umdl");
	private final Warcraft3MapObjectData objectData;
	private final Path outputDirectory;
	private final Codebase codebase;

	private static final War3ID[] ITEM_META_KEYS = { ITEM_MODEL_FILE, War3ID.fromString("iico") };
	private static final War3ID[] UNIT_META_KEYS = { UNIT_MODEL_FILE, UNIT_PROJECTILE_ART_1, UNIT_PROJECTILE_ART_2,
			War3ID.fromString("ussi"), War3ID.fromString("ushb"), War3ID.fromString("upat"), War3ID.fromString("ucua"),
			War3ID.fromString("uico"), War3ID.fromString("ushu") };
	private static final War3ID[] ABIL_META_KEYS = { War3ID.fromString("aart"), War3ID.fromString("auar"),
			War3ID.fromString("arar"), ABIL_CASTER_ART, ABIL_TARGET_ART, ABIL_SPECIAL_ART, ABIL_EFFECT_ART,
			ABIL_AOE_ART, ABIL_MISSILE_ART };
	private static final War3ID[] BUFF_META_KEYS = { War3ID.fromString("fart"), BUFF_TARGET_ART, BUFF_SPECIAL_ART,
			BUFF_EFFECT_ART, BUFF_MISSILE_ART, };

	private static final List<War3ID> KEYS_WHO_REQUIRE_MDL_HACK = Arrays
			.asList(new War3ID[] { ITEM_MODEL_FILE, UNIT_PROJECTILE_ART_2, UNIT_PROJECTILE_ART_1, UNIT_MODEL_FILE,
					ABIL_AOE_ART, ABIL_CASTER_ART, ABIL_EFFECT_ART, ABIL_MISSILE_ART, ABIL_SPECIAL_ART, ABIL_TARGET_ART,
					BUFF_TARGET_ART, BUFF_SPECIAL_ART, BUFF_EFFECT_ART, BUFF_MISSILE_ART, });

	private static final War3ID UPGR_ICON_CODE = War3ID.fromString("gar1");
	private static final War3ID UPGR_LEVEL_CODE = War3ID.fromString("glvl");

	public GenericMapObjectDataExtractor(final Codebase codebase, final Warcraft3MapObjectData objectData,
			final Path outputDirectory) {
		this.codebase = codebase;
		this.objectData = objectData;
		this.outputDirectory = outputDirectory;
	}

	public void extract() {
		// final MutableObjectData units = objectData.getUnits();
		// for (final War3ID unitId : units.keySet()) {
		// extractUnit(units.get(unitId));
		// }
		// final MutableObjectData items = objectData.getItems();
		// for (final War3ID unitId : items.keySet()) {
		// extractItem(items.get(unitId));
		// }
		// final MutableObjectData abilities = objectData.getAbilities();
		// for (final War3ID unitId : abilities.keySet()) {
		// extractAbility(abilities.get(unitId));
		// }
		// final MutableObjectData buffs = objectData.getBuffs();
		// for (final War3ID unitId : buffs.keySet()) {
		// extractBuff(buffs.get(unitId));
		// }
		final MutableObjectData upgrades = objectData.getUpgrades();
		for (final War3ID unitId : upgrades.keySet()) {
			extractUpgrade(upgrades.get(unitId));
		}
	}

	public void extractFieldFromObject(final MutableGameObject object, final War3ID fieldMetaKey, final int levels,
			final Consumer<String> consumer) {
		if (levels > 1) {
			for (int level = 1; level <= levels; level++) {
				consumer.accept(object.getFieldAsString(fieldMetaKey, level));
			}
		} else {
			consumer.accept(object.getFieldAsString(fieldMetaKey, 0));
		}
	}

	public void extractFieldFromObjectUpg(final MutableGameObject object, final War3ID fieldMetaKey, final int levels,
			final Consumer<String> consumer) {
		if (levels > 1) {
			for (int level = 1; level <= levels; level++) {
				consumer.accept(object.getFieldAsString(fieldMetaKey, level));
			}
		} else {
			consumer.accept(object.getFieldAsString(fieldMetaKey, 1));
		}
	}

	public void extractUnit(final MutableGameObject unit) {
		for (final War3ID fieldMetaId : UNIT_META_KEYS) {
			final boolean isModelField = KEYS_WHO_REQUIRE_MDL_HACK.contains(fieldMetaId);
			if (isModelField) {
				extractFieldFromObject(unit, fieldMetaId, 0, new Consumer<String>() {
					@Override
					public void accept(final String path) {
						try {
							extractModel(codebase, outputDirectory, asMdxExtension(path));
							if (fieldMetaId == UNIT_MODEL_FILE) {
								extractModel(codebase, outputDirectory, ModelUtils.getPortrait(asMdxExtension(path)));
							}
						} catch (final IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			} else {
				extractFieldFromObject(unit, fieldMetaId, 0, new Consumer<String>() {

					@Override
					public void accept(final String path) {
						extract(codebase, outputDirectory, path, false);
					}

				});
			}
		}
	}

	public void extractItem(final MutableGameObject item) {
		for (final War3ID fieldMetaId : ITEM_META_KEYS) {
			final boolean isModelField = KEYS_WHO_REQUIRE_MDL_HACK.contains(fieldMetaId);
			if (isModelField) {
				extractFieldFromObject(item, fieldMetaId, 0, new Consumer<String>() {
					@Override
					public void accept(final String path) {
						try {
							extractModel(codebase, outputDirectory, asMdxExtension(path));
						} catch (final IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			} else {
				extractFieldFromObject(item, fieldMetaId, 0, new Consumer<String>() {

					@Override
					public void accept(final String path) {
						extract(codebase, outputDirectory, path, false);
					}

				});
			}
		}
	}

	public void extractAbility(final MutableGameObject abil) {
		for (final War3ID fieldMetaId : ABIL_META_KEYS) {
			final boolean isModelField = KEYS_WHO_REQUIRE_MDL_HACK.contains(fieldMetaId);
			if (isModelField) {
				extractFieldFromObject(abil, fieldMetaId, 0, new Consumer<String>() {
					@Override
					public void accept(final String path) {
						for (final String subPath : path.split(",")) {
							try {
								extractModel(codebase, outputDirectory, asMdxExtension(subPath));
							} catch (final IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				});
			} else {
				extractFieldFromObject(abil, fieldMetaId, 0, new Consumer<String>() {

					@Override
					public void accept(final String path) {
						for (final String subPath : path.split(",")) {
							extract(codebase, outputDirectory, subPath, false);
						}
					}

				});
			}
		}
	}

	public void extractBuff(final MutableGameObject abil) {
		for (final War3ID fieldMetaId : BUFF_META_KEYS) {
			final boolean isModelField = KEYS_WHO_REQUIRE_MDL_HACK.contains(fieldMetaId);
			if (isModelField) {
				extractFieldFromObject(abil, fieldMetaId, 0, new Consumer<String>() {
					@Override
					public void accept(final String path) {
						for (final String subPath : path.split(",")) {
							try {
								extractModel(codebase, outputDirectory, asMdxExtension(subPath));
							} catch (final IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				});
			} else {
				extractFieldFromObject(abil, fieldMetaId, 0, new Consumer<String>() {
					@Override
					public void accept(final String path) {
						for (final String subPath : path.split(",")) {
							extract(codebase, outputDirectory, subPath, false);
						}
					}
				});
			}
		}
	}

	public void extractUpgrade(final MutableGameObject upgrade) {
		System.out.println(upgrade.getName() + ", " + upgrade.getFieldAsString(War3ID.fromString("gnam"), 1) + ", "
				+ upgrade.getAlias());
		System.out.println(" - " + upgrade.getFieldAsString(War3ID.fromString("gar1"), 0));
		final int levels = upgrade.getFieldAsInteger(UPGR_LEVEL_CODE, 0);
		extractFieldFromObjectUpg(upgrade, UPGR_ICON_CODE, levels, new Consumer<String>() {
			@Override
			public void accept(final String path) {
				for (final String subPath : path.split(",")) {
					extract(codebase, outputDirectory, subPath, false);
				}
			}
		});
	}

	public static void extract(final Codebase codebase, final Path outputDirectory, final String path,
			final boolean includeInternal) {
		if (!codebase.has(path) || (!includeInternal && ((MpqCodebase) codebase).isBaseGameFile(path))) {
			return;
		}
		if (path.startsWith("BTN") || path.startsWith("ATC") || path.startsWith("PAS") || path.startsWith("UPG")
				|| path.startsWith("ATT")) {
			extract(codebase, outputDirectory, IconUtils.getDisabledIcon(path), false);
		}
		try {
			final Path destination = Paths.get(outputDirectory.toString(), path);
			final Path parentFolder = destination.getParent();
			if (!Files.exists(parentFolder)) {
				Files.createDirectories(parentFolder);
			}
			Files.copy(codebase.getResourceAsStream(path), destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("extracting: " + path);
	}

	public static void extractModel(final Codebase codebase, final Path outputDirectory, final String modelFile)
			throws IOException {
		extract(codebase, outputDirectory, modelFile, false);
		// Model Textures
		if (codebase.has(modelFile)) {
			final MdxModel mdxModel = MdxUtils
					.loadModel(new BlizzardDataInputStream(codebase.getResourceAsStream(modelFile)));
			// final MDL unitModel = new MDL(
			// mdxModel);
			if (mdxModel.textureChunk != null && mdxModel.textureChunk.texture != null) {
				for (final TextureChunk.Texture texture : mdxModel.textureChunk.texture) {
					if (texture.replaceableId > 0) {
						// it's a team color
					} else {
						final String path = texture.fileName;
						extract(codebase, outputDirectory, path, false);
					}
				}
			}
			// for (final Bitmap texture : unitModel.getTextures()) {
			// if (texture.getReplaceableId() > 0) {
			// // it's a team color
			// } else {
			// final String path = texture.getPath();
			// extract(codebase, outputDirectory, path, settings, true);
			// }
			// }
			if (mdxModel.attachmentChunk != null && mdxModel.attachmentChunk.attachment != null) {
				for (final AttachmentChunk.Attachment attachment : mdxModel.attachmentChunk.attachment) {
					if (attachment.unknownName_modelPath != null) {
						extractModel(codebase, outputDirectory, asMdxExtension(attachment.unknownName_modelPath));
					}
				}
				// for (final Attachment attachment :
				// unitModel.sortedIdObjects(Attachment.class)) {
				// if (attachment.getPath() != null) {
				// extractModel(codebase, outputDirectory,
				// asMdxExtension(attachment.getPath()), settings);
				// }
				// }
			}
		}
	}

	public static String asExtension(String modelFile, final String extension) {
		if (modelFile.contains(".")) {
			modelFile = modelFile.substring(0, modelFile.lastIndexOf('.')) + extension;
		}
		if (!modelFile.toLowerCase().endsWith(extension)) {
			modelFile += extension;
		}
		return modelFile;
	}

	public static String asMdxExtension(String modelFile) {
		if (modelFile.toLowerCase().endsWith(".mdl")) {
			modelFile = modelFile.substring(0, modelFile.lastIndexOf('.')) + ".mdx";
		}
		if (!modelFile.toLowerCase().endsWith(".mdx")) {
			modelFile += ".mdx";
		}
		return modelFile;
	}
}
