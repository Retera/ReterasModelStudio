package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxEventObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for EventObjects, which include such things as craters, footprints,
 * splashes, blood spurts, and sounds
 * <p>
 * Eric Theller 3/10/2012 3:52 PM
 */
public class EventObject extends IdObject {
	List<Integer> eventTrack = new ArrayList<>();
	Integer globalSeq;
	int globalSeqId = -1;
	boolean hasGlobalSeq = false;

	public EventObject() {

	}

	public EventObject(final String name) {
		this.name = name;
	}

	public EventObject(final EventObject object) {
		copyObject(object);

		eventTrack = new ArrayList<>(object.eventTrack);
		globalSeq = object.globalSeq;
		globalSeqId = object.globalSeqId;
		hasGlobalSeq = object.hasGlobalSeq;
	}

	public EventObject(final MdlxEventObject object) {
		if ((object.flags & 1024) != 1024) {
			System.err.println("MDX -> MDL error: An eventobject '" + object.name
					+ "' not flagged as eventobject in MDX!");
		}

		loadObject((object));

		final int globalSequenceId = object.globalSequenceId;

		if (globalSequenceId >= 0) {
			globalSeqId = globalSequenceId;
			hasGlobalSeq = true;
		}

		for (final long val : object.keyFrames) {
			eventTrack.add((int) val);
		}
	}

	public MdlxEventObject toMdlx(EditableModel model) {
		final MdlxEventObject object = new MdlxEventObject();

		objectToMdlx(object, model);

		if (isHasGlobalSeq()) {
			object.globalSequenceId = getGlobalSeqId();
		}

		final List<Integer> keyframes = getEventTrack();

		object.keyFrames = new long[keyframes.size()];

		for (int i = 0, l = keyframes.size(); i < l; i++) {
			object.keyFrames[i] = keyframes.get(i).longValue();
		}

		return object;
	}

	@Override
	public EventObject copy() {
		return new EventObject(this);
	}

	public int size() {
		return eventTrack.size();
	}

	public static EventObject buildEmptyFrom(final EventObject source) {
		return new EventObject(source);

	}

	public void setValuesTo(final EventObject source) {
		eventTrack = source.eventTrack;
	}

	public void deleteAnim(final Animation anim) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (int index = eventTrack.size() - 1; index >= 0; index--) {
			final int i = eventTrack.get(index);
			if ((i >= anim.getStart()) && (i <= anim.getEnd())) {
				// If this "i" is a part of the anim being removed
				eventTrack.remove(index);
			}
		}

		// BOOM magic happens
	}

	public void timeScale(final int start, final int end, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (final Integer integer : eventTrack) {
			final int i = integer;
			if ((i >= start) && (i <= end)) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.set(eventTrack.indexOf(integer), (int) (newStart + (ratio * (newEnd - newStart))));
			}
		}

		sort();

		// BOOM magic happens
	}

	public void copyFrom(final EventObject source, final int start, final int end, final int newStart,
						 final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (final Integer integer : source.eventTrack) {
			final int i = integer;
			if ((i >= start) && (i <= end)) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.add((int) (newStart + (ratio * (newEnd - newStart))));
			}
		}

		sort();

		// BOOM magic happens
	}

	public void sort() {
		final int low = 0;
		final int high = eventTrack.size() - 1;

		if (eventTrack.size() > 0) {
			quicksort(low, high);
		}
	}

	private void quicksort(final int low, final int high) {
		// Thanks to Lars Vogel for the quicksort concept code (something to
		// look at), found on google
		// (re-written by Eric "Retera" for use in AnimFlags)
		int i = low, j = high;
		final Integer pivot = eventTrack.get(low + ((high - low) / 2));

		while (i <= j) {
			while (eventTrack.get(i) < pivot) {
				i++;
			}
			while (eventTrack.get(j) > pivot) {
				j--;
			}
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}

		if (low < j) {
			quicksort(low, j);
		}
		if (i < high) {
			quicksort(i, high);
		}
	}

	private void exchange(final int i, final int j) {
		final Integer iTime = eventTrack.get(i);

		eventTrack.set(i, eventTrack.get(j));

		eventTrack.set(j, iTime);
	}

	public void updateGlobalSeqRef(final EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeq = mdlr.getGlobalSeq(globalSeqId);
		}
	}

	public void updateGlobalSeqId(final EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeqId = mdlr.getGlobalSeqId(globalSeq);
		}
	}

	/**
	 * @deprecated Use getGlobalSeq
	 */
	@Deprecated
	public int getGlobalSeqId() {
		return globalSeqId;
	}

	/**
	 * @deprecated Use setGlobalSeq
	 */
	@Deprecated
	public void setGlobalSeqId(final int globalSeqId) {
		this.globalSeqId = globalSeqId;
	}

	public boolean isHasGlobalSeq() {
		return hasGlobalSeq;
	}

	public void setHasGlobalSeq(final boolean hasGlobalSeq) {
		this.hasGlobalSeq = hasGlobalSeq;
	}

	public Integer getGlobalSeq() {
		return globalSeq;
	}

	public void setGlobalSeq(final Integer globalSeq) {
		this.globalSeq = globalSeq;
	}

	public List<Integer> getEventTrack() {
		return eventTrack;
	}

	public void setEventTrack(final List<Integer> eventTrack) {
		this.eventTrack = eventTrack;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.eventObject(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return 1;
	}

	public static String getEventName(String code) {
		String typeCode = code.substring(0, code.length() - 4);
		String eventCode = code.substring(code.length() - 4);
		return switch (typeCode.toUpperCase()) {
			case "SNDX" -> "Sound - " + getSoundNameString(eventCode);
			case "SPNX" -> {
				String objName = getSpawnObjectName(eventCode).replace(".*\\", "");
				yield "Spawned Object - " + objName;
			}
			case "UBRX" -> "UberSplat - " + getUberSplatName(eventCode);
			case "SPLX" -> "Splat - " + getSplatName(eventCode);
			case "FTPX" -> "Footprint - " + getSplatName(eventCode);
//			case "SND" -> "Sound";
//			case "FTP" -> "Footprint";
//			case "SPN" -> "Spawned Object";
//			case "SPL" -> "Splat";
//			case "UBR" -> "UberSplat";
			default -> "unknown";
		};
	}

	// copied from khalvs mdl-exporter for blender, https://github.com/tw1lac/mdl-exporter/blob/2.8/export_mdl/sound_types.txt
	public static String getSoundNameString(String code) {
		return switch (code) {
			case "FBCL" -> "TestFootstep";
			case "FBCR" -> "TestFootstep";
			case "AAMS" -> "AntiMagicshell";
			case "AAST" -> "AncestralSpirit";
			case "AAVE" -> "ObsidianStatueMorph";
			case "ABLO" -> "Bloodlust";
			case "ABPD" -> "BothGlueScreenPopDown";
			case "ABPU" -> "BothGlueScreenPopUp";
			case "ABRW" -> "Burrow";
			case "ABSK" -> "BerserkerRage";
			case "ABTR" -> "BattleRoar";
			case "ACAN" -> "Cannibalize";
			case "ACBC" -> "BreathOfFire";
			case "ACBF" -> "BreathOfFrost";
			case "ACCV" -> "CrushingWave";
			case "ACWD" -> "CrushingWaveDamage";
			case "ACLB" -> "CinematicLightningBolt";
			case "ACRH" -> "CorrosiveBreathMissileHit";
			case "ACRI" -> "Cripple";
			case "ACRL" -> "CorrosiveBreathMissileLaunch";
			case "ACRS" -> "Curse";
			case "ACSI" -> "Silence";
			case "ACSL" -> "CreepSleep";
			case "ACYB" -> "CycloneBirth";
			case "ACYD" -> "CycloneDeath";
			case "ADEF" -> "Defend";
			case "FDFL" -> "DeepFootstep";
			case "FDFR" -> "DeepFootstep";
			case "FDSL" -> "FiendStep";
			case "FDSR" -> "FiendStep";
			case "FHCL" -> "HeroCinematicStep";
			case "FHCR" -> "HeroCinematicStep";
			case "ADCM" -> "DruidOfTheClawMorph";
			case "ADCA" -> "DruidOfTheClawMorphAlternate";
			case "ADEV" -> "Devour";
			case "ADHM" -> "DemonHunterMorph";
			case "ADIS" -> "DispelMagic";
			case "ADTM" -> "DruidOfTheTalonMorph";
			case "ADTA" -> "DruidOfTheTalonMorphAlternate";
			case "ADVP" -> "DevourPuke";
			case "AEAT" -> "EatTreeMunch";
			case "AEBA" -> "Barkskin";
			case "AEBD" -> "Earthbind";
			case "AEBL" -> "BlinkCaster";
			case "AEBT" -> "BlinkTarget";
			case "AHDR" -> "SiphonManaCaster";
			case "AHEA" -> "Heal";
			case "AHER" -> "LevelUp";
			case "AHFS" -> "FlameStrike";
			case "AHFT" -> "FlameStrikeTarget";
			case "AHHB" -> "HolyBolt";
			case "AHMC" -> "MarkOfChaos";
			case "AHMF" -> "ManaFlareMissile";
			case "AHMT" -> "MassTeleport";
			case "AHRE" -> "Resurrect";
			case "AHRV" -> "ReviveHuman";
			case "AHTB" -> "StormBolt";
			case "AHSL" -> "StormBoltLaunch";
			case "AHTC" -> "ThunderClap";
			case "AHWD" -> "HealingWardBirth";
			case "AICB" -> "OrbOfCorruptionLaunch";
			case "AICH" -> "OrbOfCorruptionHit";
			case "AIDC" -> "NeutralizationWandHit";
			case "AILL" -> "ItemIllusion";
			case "AIMA" -> "ManaPotion";
			case "AINB" -> "InfernalBirth";
			case "AINF" -> "InnerFire";
			case "AIRE" -> "RestorationPotion";
			case "AISO" -> "SoulGem";
			case "AITM" -> "Tome";
			case "AIVS" -> "Invisibility";
			case "AKDL" -> "KodoDrumLeft";
			case "AKDR" -> "KodoDrumRight";
			case "ALPD" -> "LeftGlueScreenPopDown";
			case "ALPU" -> "LeftGlueScreenPopUp";
			case "ALSD" -> "LightningShield";
			case "ANBA" -> "BlackArrowHit";
			case "ANDO" -> "DoomTarget";
			case "ANDT" -> "RevealMap";
			case "ANEU" -> "NeutralBuildingActivate";
			case "ANHT" -> "HowlOfTerror";
			case "ANMO" -> "MonsoonBolt";
			case "ANPA" -> "Parasite";
			case "ANSA" -> "SacrificeUnit";
			case "ANSD" -> "StrongDrink";
			case "ANSM" -> "StrongDrinkMissile";
			case "ANSS" -> "SpellShieldAmulet";
			case "AOAG" -> "WardBirth";
			case "AOCR" -> "CriticalStrike";
			case "AOHW" -> "HealingWaveTarget";
			case "AOLB" -> "LightningBolt";
			case "AOMI" -> "MirrorImageDeath";
			case "AOMC" -> "MirrorImage";
			case "AORE" -> "Reincarnation";
			case "AORV" -> "ReviveOrc";
			case "AOSD" -> "FeralSpiritDone";
			case "AOSF" -> "FeralSpiritTarget";
			case "AOSH" -> "ShockWave";
			case "AOVD" -> "VoodooBirth";
			case "AOWS" -> "Warstomp";
			case "AOWW" -> "Whirlwind";
			case "APHS" -> "PhaseShift";
			case "APHX" -> "PhoenixBirth";
			case "APLA" -> "PolymorphAir";
			case "APLD" -> "PolymorphDone";
			case "APOL" -> "Polymorph";
			case "APRG" -> "Purge";
			case "APSH" -> "PossessionMissileHit";
			case "APSL" -> "PossessionMissileLaunch";
			case "APXB" -> "PhoenixEggBirth";
			case "AREJ" -> "Rejuvenation";
			case "AREP" -> "Repair";
			case "AROO" -> "Root";
			case "ARPD" -> "RightGlueScreenPopDown";
			case "ARPU" -> "RightGlueScreenPopUp";
			case "ASHP" -> "ShadowPact";
			case "ASKA" -> "RaiseSkeletonArcher";
			case "ASKW" -> "RaiseSkeletonWarrior";
			case "ASLC" -> "SlowCaster";
			case "ASLO" -> "Slow";
			case "ASPL" -> "SpiritLink";
			case "ASPM" -> "SpellStealMissileLaunch";
			case "ASPS" -> "SpellStealTarget";
			case "ASTO" -> "StoneFormMorph1";
			case "AST2" -> "StoneFormMorph2";
			case "AST3" -> "StoneFormMorph3";
			case "ASTA" -> "StoneFormMorphAlternate";
			case "ASTB" -> "StasisTotemBirth";
			case "ASTH" -> "StampedeHit";
			case "ASTS" -> "StasisTotemDeath";
			case "ASWB" -> "SpiritWolfBirth";
			case "ASWE" -> "WaterElementalBirth";
			case "ATAU" -> "Taunt";
			case "ATRB" -> "TreeWallBirth";
			case "AUB1" -> "UndeadBuildingBirth1";
			case "AUB2" -> "UndeadBuildingBirth2";
			case "AUB3" -> "UndeadBuildingBirth3";
			case "AUB4" -> "UndeadBuildingBirth4";
			case "AUCB" -> "ScarabBirth";
			case "AUCD" -> "CarrionSwarmDamage";
			case "AUCH" -> "Charm";
			case "AUCO" -> "UnstableConcoction";
			case "AUCS" -> "CarrionSwarmLaunch";
			case "AUDA" -> "DarkRitual";
			case "AUDC" -> "DeathCoil";
			case "AUDM" -> "DarkSummoningMissileLaunch";
			case "AUDP" -> "DeathPactTarget";
			case "AUDS" -> "DarkSummoningTarget";
			case "AUDT" -> "DeathAndDecayTarget";
			case "AUFA" -> "FrostArmor";
			case "AUFN" -> "FrostNova";
			case "AUGS" -> "GatherShadowsMorph";
			case "AUGA" -> "GatherShadowsMorphAlternate";
			case "AUHF" -> "UnholyFrenzy";
			case "AUIH" -> "ImpaleLand";
			case "AUIM" -> "Impale";
			case "AUIT" -> "ImpaleHit";
			case "AUPR" -> "Uproot";
			case "AURV" -> "ReviveUndead";
			case "AWBS" -> "BigWaterStep";
			case "AWEB" -> "Web";
			case "AWRS" -> "Pulverize";
			case "AWST" -> "WaterStep";
			case "DABA" -> "AbominationAlternateDeath";
			case "DABO" -> "AbominationDeath";
			case "DACO" -> "AcolyteDeath";
			case "DADR" -> "DruidOfTheTalonDeath";
			case "DALB" -> "AlbatrossDeath";
			case "DAMG" -> "HeroArchMageDeath";
			case "DANG" -> "WardDeath";
			case "DANP" -> "TreantDeath";
			case "DARC" -> "ArachnathidDeath";
			case "DARG" -> "ArmorGolemDeath";
			case "DART" -> "ArtilleryExplodeDeath";
			case "DASS" -> "AssassinDeath";
			case "DWTC" -> "WatcherDeath";
			case "DBAL" -> "BallistaDeath";
			case "DBAN" -> "BansheeDeath";
			case "DBAT" -> "BatRiderDeath";
			case "DBES" -> "HeroBloodElfDeath";
			case "DBLA" -> "HeroBladeMasterDeath";
			case "DBNT" -> "BanditDeath";
			case "DBRG" -> "DeathBridge";
			case "DBRI" -> "BristleBackDeath";
			case "DBSF" -> "BlackStagFemaleDeath";
			case "DBSM" -> "BlackStagMaleDeath";
			case "DBSP" -> "BattleShipDeath";
			case "DBSX" -> "ObsidianAvengerDeath";
			case "DBTM" -> "BeastmasterDeath";
			case "DCAT" -> "CatapultDeath";
			case "DCBL" -> "DeathCityBuilding";
			case "DCEN" -> "CentaurDeath";
			case "DCNA" -> "CentaurArcherDeath";
			case "DCRL" -> "HeroCryptLordDeath";
			case "DDEM" -> "HeroDemonHunterDeath";
			case "DDHK" -> "DragonHawkDeath";
			case "DDKN" -> "HeroDeathKnightDeath";
			case "DDKR" -> "DarkRangerDeath";
			case "DDMA" -> "HeroDemonHunterDeathAlternate";
			case "DDMG" -> "DoomGuardDeath";
			case "DDOC" -> "DruidOfTheClawDeath";
			case "DDCA" -> "DruidOfTheClawDeathAlternate";
			case "DDMN" -> "DemonessDeath";
			case "DDNW" -> "DuneWormDeath";
			case "DDTA" -> "DruidOfTheTalonDeathAlternate";
			case "DDRA" -> "DragonDeath";
			case "DDRL" -> "HeroDreadLordDeath";
			case "DDRN" -> "DraeneiDeath";
			case "DDRS" -> "DragonspawnDeath";
			case "DDRY" -> "DryadDeath";
			case "DDSH" -> "HumanDissipate";
			case "DDSN" -> "NightElfDissipate";
			case "DDSO" -> "OrcDissipate";
			case "DDWF" -> "DireWolfDeath";
			case "DEBA" -> "DeathWalkingNightElfBuilding";
			case "DEBC" -> "DeathNightElfBuildingCancel";
			case "DEGS" -> "EggSackDeath";
			case "DELB" -> "DeathNightElfLargeBuilding";
			case "DELS" -> "DeathNightElfSmallBuilding";
			case "DENT" -> "EntDeath";
			case "DFAR" -> "HeroFarSeerDeath";
			case "DFCO" -> "FacelessOneDeath";
			case "DFDR" -> "FaerieDragonDeath";
			case "DFLG" -> "FelguardDeath";
			case "DFEL" -> "FelhoundDeath";
			case "DFOO" -> "FootmanDeath";
			case "DFOR" -> "ForgottenOneDeath";
			case "DFRG" -> "FrogDeath";
			case "DFRM" -> "FrostmourneDeath";
			case "DFRT" -> "ForestTrollDeath";
			case "DFRW" -> "FrostWyrmDeath";
			case "DFSP" -> "ForestTrollShadowPriestDeath";
			case "DFTN" -> "ForgottenOneTentacleDeath";
			case "DFUR" -> "FurbolgDeath";
			case "DGAR" -> "GargoyleDeath";
			case "DGAS" -> "GargoyleStoneDeath";
			case "DGAT" -> "GateDeath";
			case "DGHO" -> "GhoulDeath";
			case "DGLD" -> "GoldMineDeath";
			case "DGLM" -> "GoblinLandMineDeath";
			case "DGNA" -> "GnollArcherDeath";
			case "DGNL" -> "GnollDeath";
			case "DGOB" -> "GemstoneObeliskDeath";
			case "DGRU" -> "GruntDeath";
			case "DGRY" -> "GryphonRiderDeath";
			case "DGRZ" -> "GrizzlyBearDeath";
			case "DGRS" -> "BearSwimDeath";
			case "DGSD" -> "GoblinSapperDeath";
			case "DGSP" -> "GoblinSapperExplode";
			case "DGST" -> "GiantSeaTurtleDeath";
			case "DTDS" -> "GiantSeaTurtleDeathSwim";
			case "DGYR" -> "GyrocopterDeath";
			case "DGZP" -> "GoblinZeppelinDeath";
			case "DHBC" -> "DeathHumanBuildingCancel";
			case "DHIP" -> "HippogryphDeath";
			case "DHLB" -> "DeathHumanLargeBuilding";
			case "DHLS" -> "DeathHumanSmallBuilding";
			case "DHMC" -> "HermitCrabDeath";
			case "DHOR" -> "HorseDeath";
			case "DHRP" -> "HarpyDeath";
			case "DHUN" -> "HeadHunterDeath";
			case "DHWD" -> "HealingWardDeath";
			case "DHDA" -> "HydraDeath";
			case "DHDS" -> "HydraDeathSwim";
			case "DHYD" -> "HydraliskDeath";
			case "DICT" -> "IceTrollDeath";
			case "DINF" -> "InfernalDeath";
			case "DINM" -> "InfernalMachineDeath";
			case "DIPW" -> "PrisonWagonDeath";
			case "DIRG" -> "IronGolemDeath";
			case "DJAN" -> "JainaDeath";
			case "DKBS" -> "KoboldShovelerDeath";
			case "DKEE" -> "HeroKeeperOfTheGroveDeath";
			case "DKNI" -> "KnightDeath";
			case "DKOB" -> "KoboldDeath";
			case "DKOD" -> "KodoBeastDeath";
			case "DLIC" -> "HeroLichDeath";
			case "DLOC" -> "LocustDeath";
			case "DLVR" -> "LeverDeath";
			case "DMAG" -> "MagnataurDeath";
			case "DMAK" -> "MakruraDeath";
			case "DMAM" -> "MammothDeath";
			case "DMGS" -> "MurgulDeathSwim";
			case "DMGT" -> "MountainGiantDeath";
			case "DMKG" -> "HeroMountainKingDeath";
			case "DMLF" -> "MalfurionDeath";
			case "DMOO" -> "HeroMoonPriestessDeath";
			case "DMOR" -> "MortarTeamDeath";
			case "DMTW" -> "MeatWagonDeath";
			case "DMUR" -> "MurlocDeath";
			case "DNBL" -> "NagaBuildingDeath";
			case "DNDR" -> "NetherDragonDeath";
			case "DNDS" -> "HumanDissipate";
			case "DNEC" -> "NecromancerDeath";
			case "DMYR" -> "NagaMyrmidonDeath";
			case "DMYS" -> "NagaMyrmidonDeathSwim";
			case "DNSR" -> "NagaSirenDeath";
			case "DNSS" -> "NagaSirenDeathSwim";
			case "DNSW" -> "NagaSeaWitchDeath";
			case "DNWS" -> "NagaSeaWitchDeathSwim";
			case "DOBS" -> "ObsidianStatueDeath";
			case "DOGR" -> "OgreDeath";
			case "DOLB" -> "DeathOrcLargeBuilding";
			case "DOLS" -> "DeathOrcSmallBuilding";
			case "DORW" -> "OrcWarlockDeath";
			case "DOWB" -> "OwlbearDeath";
			case "DOWL" -> "SnowOwlDeath";
			case "DPAL" -> "HeroPaladinDeath";
			case "DPMB" -> "PandarenBrewmasterDeath";
			case "DPEN" -> "PenguinDeath";
			case "DPEO" -> "PeonDeath";
			case "DPES" -> "PeasantDeath";
			case "DPHX" -> "PhoenixDeath";
			case "DPIG" -> "PigDeath";
			case "DPIT" -> "CryptFiendDeath";
			case "DPLD" -> "PitlordDeath";
			case "DPRS" -> "PriestDeath";
			case "DQBS" -> "QuillBeastDeath";
			case "DRAI" -> "RaiderDeath";
			case "DRAN" -> "ArcherDeath";
			case "DRAT" -> "RatDeath";
			case "DREV" -> "RevenantDeath";
			case "DRKG" -> "RockGolemDeath";
			case "DRKW" -> "RockWallDeath";
			case "DRHG" -> "RiddenHippogryphDeath";
			case "DRIF" -> "RiflemanDeath";
			case "DSAT" -> "SatyrDeath";
			case "DSCB" -> "ScarabDeath";
			case "DSEL" -> "SealDeath";
			case "DSEN" -> "SentinelDeath";
			case "DSGT" -> "SeaGiantDeath";
			case "DSGW" -> "SeaGiantSwimDeath";
			case "DSHD" -> "ShadeDeath";
			case "DSHH" -> "HeroShadowHunterDeath";
			case "DSHM" -> "ShamanDeath";
			case "DSHP" -> "SheepDeath";
			case "DSHW" -> "SheepDeathSwim";
			case "DSKE" -> "SkeletonDeath";
			case "DSKK" -> "SkinkDeath";
			case "DSLG" -> "SludgeMonsterDeath";
			case "DSND" -> "SnapDragonDeath";
			case "DSNS" -> "SnapDragonDeathSwim";
			case "DSOR" -> "SorceressDeath";
			case "DSPB" -> "SpellBreakerDeath";
			case "DSPC" -> "SpiderCrabDeath";
			case "DSPD" -> "SpiderDeath";
			case "DSPL" -> "SplatDeath";
			case "DSPV" -> "SpiritOfVengeanceDeath";
			case "DSTT" -> "SteamTankDeath";
			case "DTAU" -> "TaurenDeath";
			case "DTCH" -> "HeroTaurenChieftainDeath";
			case "DTRW" -> "TreeWallDeath";
			case "DTUS" -> "TuskarrDeath";
			case "DUAB" -> "UndeadAirBargeDeath";
			case "DUBC" -> "DeathUndeadBuildingCancel";
			case "DUDS" -> "UndeadDissipate";
			case "DULB" -> "DeathUndeadLargeBuilding";
			case "DULS" -> "DeathUndeadSmallBuilding";
			case "DUNB" -> "UnbrokenDeath";
			case "DVLC" -> "VillagerChildDeath";
			case "DVLM" -> "VillagerManDeath";
			case "DVLW" -> "VillagerWomanDeath";
			case "DVNG" -> "VengeanceDeath";
			case "DVUL" -> "VultureDeath";
			case "DWAR" -> "WarlockDeath";
			case "DWAT" -> "WaterElementalDeath";
			case "DWCD" -> "WyvernCageDeath";
			case "DWDS" -> "WingedSerpentDeath";
			case "DWEN" -> "WendigoDeath";
			case "DWIT" -> "WitchDoctorDeath";
			case "DWLD" -> "WarlordDeath";
			case "DWLF" -> "WolfDeath";
			case "DWRD" -> "HeroWardenDeath";
			case "DWRE" -> "WarEagleDeath";
			case "DWSP" -> "WispDeath";
			case "DWYV" -> "WyvernRiderDeath";
			case "DZOM" -> "ZombieDeath";
			case "KANG" -> "AncestralGuardianAttack1";
			case "KAOE" -> "AncientOfTheEarthAttack1";
			case "KAOM" -> "AncientOfTheMoonAttack1";
			case "KAW1" -> "AncientOfTheWildAttack1";
			case "KAW2" -> "AncientOfTheWildAttack2";
			case "KANP" -> "AncientProtectorMissileAttack";
			case "KAP1" -> "AncientProtectorMeleeAttack1";
			case "KAP2" -> "AncientProtectorMeleeAttack2";
			case "KBAL" -> "BalrogAttack1";
			case "KAZB" -> "AzureDragonAttack1";
			case "KBLL" -> "BallistaAttack";
			case "KBLB" -> "BlackDragonAttack1";
			case "KBRB" -> "BronzeDragonAttack1";
			case "KBST" -> "BeastmasterAttack";
			case "KGRB" -> "GreenDragonAttack1";
			case "KRDB" -> "RedDragonAttack1";
			case "KBM1" -> "HeroBladeMasterAttack1";
			case "KBM2" -> "HeroBladeMasterAttack2";
			case "KCAN" -> "CannonTowerAttack";
			case "KCAT" -> "CatapultAttack1";
			case "KCL1" -> "CryptLordAttack1";
			case "KCL2" -> "CryptLordAttack2";
			case "KDH1" -> "HeroDemonHunterAttack1";
			case "KDH2" -> "HeroDemonHunterAttack2";
			case "KDK1" -> "HeroDeathKnightAttack1";
			case "KFAR" -> "HeroFarSeerAttack1";
			case "KFRB" -> "FrostWyrmAttack1";
			case "KGUA" -> "GuardTowerAttack";
			case "KGYR" -> "GyrocopterAttack";
			case "KIN1" -> "InfernalAttack1";
			case "KIN2" -> "InfernalAttack2";
			case "KINJ" -> "InfernalJuggernaughtAttack";
			case "KINM" -> "InfernalMachineAttack";
			case "KIRG" -> "IronGolemAttack1";
			case "KLIC" -> "HeroLichAttack1";
			case "KMKG" -> "HeroMountainKingAttack1";
			case "KMT1" -> "MortarTeamAttack1";
			case "KMT2" -> "MortarTeamAttack2";
			case "KMTW" -> "MeatWagonAttack1";
			case "KPB1" -> "BrewmasterAttack1";
			case "KPB2" -> "BrewmasterAttack2";
			case "KPL1" -> "HeroPaladinAttack1";
			case "KPL2" -> "HeroPaladinAttack2";
			case "KPD1" -> "PitLordAttack1";
			case "KPD2" -> "PitLordAttack2";
			case "KPD3" -> "PitLordAttack3";
			case "KPS1" -> "PitLordAttackSlam1";
			case "KPS2" -> "PitLordAttackSlam2";
			case "KRG1" -> "RockGolemAttack1";
			case "KRG2" -> "RockGolemAttack2";
			case "KRIF" -> "RiflemanAttack1";
			case "KRN1" -> "HeroRangerAttack1";
			case "KRN2" -> "HeroRangerAttack2";
			case "KSTT" -> "SteamTankAttack";
			case "KTC1" -> "HeroTaurenChieftainAttack1";
			case "KTC2" -> "HeroTaurenChieftainAttack2";
			case "KTOL" -> "TreeOfLifeAttack1";
			case "KWAR" -> "WardenAttack";
			case "MABS" -> "AbsorbManaLaunch";
			case "MANG" -> "AncestralGuardianMissileHit";
			case "MANL" -> "AncestralGuardianMissileLaunch";
			case "MANP" -> "AncientProtectorMissileHit";
			case "MAPL" -> "AncientProtectorMissileLaunch";
			case "MARL" -> "ArrowLaunch";
			case "MARR" -> "ArrowHit";
			case "MAXE" -> "AxeMissileHit";
			case "MAXL" -> "AxeMissileLaunch";
			case "MBAL" -> "BallistaMissileHit";
			case "MBAN" -> "BansheeMissileHit";
			case "MBHT" -> "BoatMissileHit";
			case "MBHL" -> "BoatMissileLaunch";
			case "MBML" -> "BloodMageMissileLaunch";
			case "MBNL" -> "BansheeMissileLaunch";
			case "MBLT" -> "Bolt";
			case "MBRH" -> "BristleBackMissileHit";
			case "MBRL" -> "BristleBackMissileLaunch";
			case "MBSL" -> "PriestMissileLaunch";
			case "MBSH" -> "PriestMissileHit";
			case "MCAH" -> "ChimaeraAcidHit";
			case "MCAL" -> "ChimaeraAcidLaunch";
			case "MCAN" -> "CannonTowerMissile";
			case "MCAT" -> "Catapult";
			case "MCDA" -> "ColdArrow";
			case "MCRH" -> "CryptFiendMissileHit";
			case "MCRL" -> "CryptFiendMissileLaunch";
			case "MDCL" -> "DeathCoilMissile";
			case "MDEM" -> "DemonHunterMissileHit";
			case "MDLL" -> "DestroyerMissileLaunch";
			case "MDML" -> "DemonHunterMissileLaunch";
			case "MDOC" -> "WitchDoctorMissileLaunch";
			case "MDOH" -> "WitchDoctorMissileHit";
			case "MDRY" -> "DryadMissile";
			case "MDTL" -> "DruidOfTheTalonMissileLaunch";
			case "MDTH" -> "DruidOfTheTalonMissileHit";
			case "MDVM" -> "DevourMagicLaunch";
			case "MFAH" -> "FrostArrowHit";
			case "MFAL" -> "FrostArrowLaunch";
			case "MFAR" -> "FarseerMissile";
			case "MFBL" -> "FrostBoltLaunch";
			case "MFBH" -> "FrostBoltHit";
			case "MFDL" -> "FaerieDragonLaunch";
			case "MFKH" -> "FanOfKnivesHit";
			case "MFLA" -> "SearingArrowHit";
			case "MFLL" -> "SearingArrowLaunch";
			case "MFRB" -> "Fireball";
			case "MFRL" -> "FireballLaunch";
			case "MGML" -> "GryphonRiderMissileLaunch";
			case "MGRH" -> "GargoyleMissileHit";
			case "MGRL" -> "GargoyleMissileLaunch";
			case "MGUA" -> "GuardTowerMissileHit";
			case "MHAR" -> "HarpyMissileHit";
			case "MHRL" -> "HarpyMissileLaunch";
			case "MHNL" -> "HunterMissileLaunch";
			case "MHUN" -> "HunterMissileHit";
			case "MKGL" -> "KeeperOfTheGroveMissileLaunch";
			case "MKGH" -> "KeeperOfTheGroveMissileHit";
			case "MKML" -> "NecromancerMissileLaunch";
			case "MKMH" -> "NecromancerMissileHit";
			case "MLIC" -> "LichMissile";
			case "MLSL" -> "BansheeMissileLaunch";
			case "MLSH" -> "BansheeMissileHit";
			case "MMEA" -> "MeatWagonMissileHit";
			case "MMTI" -> "Mortar";
			case "MNCH" -> "NecromancerMissileHit";
			case "MNCL" -> "NecromancerMissileLaunch";
			case "MPAH" -> "PoisonArrowHit";
			case "MPML" -> "PriestMissileLaunch";
			case "MPMH" -> "PriestMissileHit";
			case "MPXL" -> "PhoenixMissileLaunch";
			case "MRAN" -> "RangerMissile";
			case "MRIF" -> "Rifle";
			case "MSBL" -> "PriestMissileLaunch";
			case "MSBH" -> "PriestMissileHit";
			case "MSEH" -> "SentinelMissileHit";
			case "MSEL" -> "SentinelMissileLaunch";
			case "MSHD" -> "ShadowHunterMissileLaunch";
			case "MSHH" -> "ShadowHunterMissileHit";
			case "MSMH" -> "SorceressMissileHit";
			case "MSML" -> "SorceressMissileLaunch";
			case "MSNL" -> "SnapDragonMissileLaunch";
			case "MSPR" -> "Spear";
			case "MSVL" -> "GargoyleMissileLaunch";
			case "MSVH" -> "GargoyleMissileHit";
			case "MTBL" -> "TrollBatriderMissileLaunch";
			case "MWAT" -> "WaterElementalMissile";
			case "MWEB" -> "WebMissileLaunch";
			case "MWIN" -> "DragonHawkMissileHit";
			case "MWNL" -> "DragonHawkMissileLaunch";
			case "MWYV" -> "WyvernSpearMissile";
			case "MZIG" -> "ZigguratMissileLaunch";
			case "MZGH" -> "ZigguratMissileHit";
			case "MZFL" -> "ZigguratFrostMissileLaunch";
			case "MZFH" -> "ZigguratFrostMissileHit";
			case "GSMN" -> "ExpansionGlueMonster";
			case "AIFT" -> "FinalCinematic";
			default -> "Unknown";
		};
	}

	public static String getSplatName(String code) {
		return switch (code) {
			case "DBL0" -> "DemonBloodLarge0";
			case "DBL1" -> "DemonBloodLarge1";
			case "DBL2" -> "DemonBloodLarge2";
			case "DBL3" -> "DemonBloodLarge3";
			case "DBS0" -> "DemonBloodSmall0";
			case "DBS1" -> "DemonBloodSmall1";
			case "DBS2" -> "DemonBloodSmall2";
			case "DBS3" -> "DemonBloodSmall3";
			case "EBL0" -> "NightElfBloodLarge0";
			case "EBL1" -> "NightElfBloodLarge1";
			case "EBL2" -> "NightElfBloodLarge2";
			case "EBL3" -> "NightElfBloodLarge3";
			case "EBS0" -> "NightElfBloodSmall0";
			case "EBS1" -> "NightElfBloodSmall1";
			case "EBS2" -> "NightElfBloodSmall2";
			case "EBS3" -> "NightElfBloodSmall3";
			case "FAL0" -> "FootprintBareSmallLeft";
			case "FAL1" -> "FootprintBareLargeLeft1";
			case "FAL2" -> "FootprintBareGiantLeft1";
			case "FAL3" -> "FootprintBareHugeLeft1";
			case "FAR0" -> "FootprintBareSmallRight1";
			case "FAR1" -> "FootprintBareLargeRight1";
			case "FAR2" -> "FootprintBareGiantRight1";
			case "FAR3" -> "FootprintBareHugeRight1";
			case "FBL0" -> "FootprintBootSmallLeft0";
			case "FBL1" -> "FootprintBootLargeLeft0";
			case "FBL2" -> "FootprintBootSmallLeft1";
			case "FBL3" -> "FootprintBootLargeLeft1";
			case "FBL4" -> "FootprintBootGiantLeft1";
			case "FBR0" -> "FootprintBootSmallRight0";
			case "FBR1" -> "FootprintBootLargeRight0";
			case "FBR2" -> "FootprintBootSmallRight1";
			case "FBR3" -> "FootprintBootLargeRight1";
			case "FBR4" -> "FootprintBootGiantRight1";
			case "FCR0" -> "FootprintClovenSmallRight";
			case "FCL0" -> "FootprintClovenSmallLeft";
			case "FCL1" -> "FootprintClovenLargeLeft";
			case "FCR1" -> "FootprintClovenLargeRight";
			case "FCL3" -> "FootprintClovenXtraLargeLeft";
			case "FCR3" -> "FootprintClovenXtraLargeRight";
			case "FCL2" -> "FootprintClovenReallySmallLeft";
			case "FCR2" -> "FootprintClovenReallySmallRight";
			case "FHL0" -> "FootprintHorseSmallLeft";
			case "FHL1" -> "FootprintHorseLargeLeft";
			case "FHR0" -> "FootprintHorseSmallRight";
			case "FHR1" -> "FootprintHorseLargeRight";
			case "FPL0" -> "FootprintPawLeft0";
			case "FPL1" -> "FootprintPawLargeLeft";
			case "FPR0" -> "FootprintPawRight0";
			case "FPR1" -> "FootprintPawLargeRight";
			case "FRL0" -> "FootprintRootLeft";
			case "FRR0" -> "FootprintRootRight";
			case "FRL1" -> "FootprintRootLeftSmall";
			case "FRR1" -> "FootprintRootRightSmall";
			case "FTL0" -> "FootPrintTrollLeft";
			case "FTR0" -> "FootPrintTrollRight";
			case "FWL0" -> "FootPrintWheelLeft0";
			case "FWR0" -> "FootPrintWheelRight0";
			case "FWL1" -> "FootPrintWheelLeft1";
			case "FWR1" -> "FootPrintWheelRight1";
			case "FML0" -> "FootPrintMurlocLeft";
			case "FMR0" -> "FootPrintMurlocRight";
			case "FPL2" -> "FootPrintPawBearLeft";
			case "FPR2" -> "FootPrintPawBearRight";
			case "FSL0" -> "FooprintSkeletonRight";
			case "FSR0" -> "FooprintSkeletonLeft";
			case "FSL1" -> "FooprintSkeletonGiantRight";
			case "FSR1" -> "FooprintSkeletonGiantLeft";
			case "FFL0" -> "FootprintFlameLeft";
			case "FFR0" -> "FootprintFlameRight";
			case "FFL1" -> "FootprintFlameGiantLeft";
			case "FFR1" -> "FootprintFlameGiantRight";
			case "FKL0" -> "FootprintSpikeLeft";
			case "FKR0" -> "FootprintSpikeRight";
			case "FKL1" -> "FootprintSpikeLargeLeft";
			case "FKR1" -> "FootprintSpikeLargeRight";
			case "FKL2" -> "FootprintSpikeGiantLeft";
			case "FKR2" -> "FootprintSpikeGiantRight";
			case "FLSL" -> "FootprintLizzardSmallLeft";
			case "FLSR" -> "FootprintLizzardSmallRight";
			case "FLLL" -> "FootprintLizzardLargeLeft";
			case "FLLR" -> "FootprintLizzardLargeRight";
			case "FSSL" -> "FootprintSnakeSmallLeft";
			case "FSSR" -> "FootprintSnakeSmallRight";
			case "FSLL" -> "FootprintSnakeLargeLeft";
			case "FSLR" -> "FootprintSnakeLargeRight";
			case "FDSL" -> "FootprintDragSmallLeft";
			case "FDSR" -> "FootprintDragSmallRight";
			case "FDLL" -> "FootprintDragLargeLeft";
			case "FDLR" -> "FootprintDragLargeRight";
			case "HBL0" -> "HumanBloodLarge0";
			case "HBL1" -> "HumanBloodLarge1";
			case "HBL2" -> "HumanBloodLarge2";
			case "HBS0" -> "HumanBloodSmall0";
			case "HBS1" -> "HumanBloodSmall1";
			case "HBS2" -> "HumanBloodSmall2";
			case "HBS3" -> "HumanBloodSmall3";
			case "HBL3" -> "HumanBloodLarge3";
			case "OBL0" -> "OrcBloodLarge0";
			case "OBL1" -> "OrcBloodLarge1";
			case "OBL2" -> "OrcBloodLarge2";
			case "OBL3" -> "OrcBloodLarge3";
			case "OBS0" -> "OrcBloodSmall0";
			case "OBS1" -> "OrcBloodSmall1";
			case "OBS2" -> "OrcBloodSmall2";
			case "OBS3" -> "OrcBloodSmall3";
			case "UBL0" -> "UndeadBloodLarge0";
			case "UBL1" -> "UndeadBloodLarge1";
			case "UBL2" -> "UndeadBloodLarge2";
			case "UBL3" -> "UndeadBloodLarge3";
			case "UBS0" -> "UndeadBloodSmall0";
			case "UBS1" -> "UndeadBloodSmall1";
			case "UBS2" -> "UndeadBloodSmall2";
			case "UBS3" -> "UndeadBloodSmall3";
			case "WSL0" -> "WaterSplashLarge0";
			case "WSL1" -> "WaterSplashLarge1";
			case "WSS0" -> "WaterSplashSmall0";
			case "WSS1" -> "WaterSplashSmall1";
			case "WHL0" -> "WaterHumanBloodLarge0";
			case "WHL1" -> "WaterHumanBloodLarge1";
			case "WHS0" -> "WaterHumanBloodSmall0";
			case "WHS1" -> "WaterHumanBloodSmall1";
			case "WOL0" -> "WaterOrcBloodLarge0";
			case "WOL1" -> "WaterOrcBloodLarge1";
			case "WOS0" -> "WaterOrcBloodSmall0";
			case "WOS1" -> "WaterOrcBloodSmall1";
			case "WEL0" -> "WaterNightElfBloodLarge0";
			case "WEL1" -> "WaterNightElfBloodLarge1";
			case "WES0" -> "WaterNightElfBloodSmall0";
			case "WES1" -> "WaterNightElfBloodSmall1";
			case "WUL0" -> "WaterUndeadBloodLarge0";
			case "WUL1" -> "WaterUndeadBloodLarge1";
			case "WUS0" -> "WaterUndeadBloodSmall0";
			case "WUS1" -> "WaterUndeadBloodSmall1";
			case "WDL0" -> "WaterDemonBloodLarge0";
			case "WDL1" -> "WaterDemonBloodLarge1";
			case "WDS0" -> "WaterDemonBloodSmall0";
			case "WDS1" -> "WaterDemonBloodSmall1";
			case "WSX0" -> "WaterSplashOnly0";
			case "WSX1" -> "WaterSplashOnly1";
			default -> "Unknown";
		};
	}

	public static String getUberSplatName(String code) {
		return switch (code) {
			case "LSDS" -> "LordSummerPlainDirtSmall";
			case "LSDM" -> "LordSummerPlainDirtMedium";
			case "LSDL" -> "LordSummerPlainDirtLarge";
			case "HCRT" -> "HumanCrater";
			case "UDSU" -> "UndeadUberSplat";
			case "DNCS" -> "DeathNeutralCityBuildingSmall";
			case "HMTP" -> "HumanMassTeleport";
			case "SCTP" -> "ScrollOfTownPortal";
			case "AMRC" -> "AmuletOfRecall";
			case "DRKC" -> "DarkConversion";
			case "DOSB" -> "DeathOrcSmallBuilding";
			case "DOMB" -> "DeathOrcMediumBuilding";
			case "DOLB" -> "DeathOrcLargeBuilding";
			case "DHSB" -> "DeathHumanSmallBuilding";
			case "DHMB" -> "DeathHumanMediumBuilding";
			case "DHLB" -> "DeathHumanLargeBuilding";
			case "DUSB" -> "DeathUndeadSmallBuilding";
			case "DUMB" -> "DeathUndeadMediumBuilding";
			case "DULB" -> "DeathUndeadLargeBuilding";
			case "DNSB" -> "DeathNightElfSmallBuilding";
			case "DNMB" -> "DeathNightElfMediumBuilding";
			case "DNSA" -> "DeathNightElfSmallAncient";
			case "DNMA" -> "DeathNightElfMediumAncient";
			case "HSMA" -> "HumanUberSplatSmall";
			case "HMED" -> "HumanUberSplatMedium";
			case "HLAR" -> "HumanUberSplatLarge";
			case "OSMA" -> "OrcUberSplatSmall";
			case "OMED" -> "OrcUberSplatMedium";
			case "OLAR" -> "OrcUberSplatLarge";
			case "USMA" -> "UndeadUberSplatSmall";
			case "UMED" -> "UndeadUberSplatMedium";
			case "ULAR" -> "UndeadUberSplatLarge";
			case "ESMA" -> "AncientUberSplatSmall";
			case "EMDA" -> "AncientUberSplatMedium";
			case "ESMB" -> "NightElfUberSplatSmall";
			case "EMDB" -> "NightElfUberSplatMedium";
			case "HTOW" -> "TownHallUberSplat";
			case "HCAS" -> "CastleUberSplat";
			case "NGOL" -> "GoldmineUberSplat";
			case "THND" -> "ThunderClap";
			case "NDGS" -> "DemonGateSplat";
			case "CLTS" -> "ThornyShieldSplat";
			case "HFS1" -> "HumanFlameStrike1";
			case "HFS2" -> "HumanFlameStrike2";
			case "USBR" -> "Burrow";
			case "NLAR" -> "NagaUberSplatLarge";
			case "NMED" -> "NagaUberSplatMedium";
			case "DPSW" -> "DarkPortalSWsplat";
			case "DPSE" -> "DarkPortalSEsplat";
			default -> "Unknown";
		};
	}

	public static String getSpawnObjectName(String code) {
		return switch (code) {
			case "UEGG" -> "Objects\\Spawnmodels\\Undead\\CryptFiendEggsack\\CryptFiendEggsack.mdl";
			case "GCBL" -> "Objects\\Spawnmodels\\Undead\\GargoyleCrumble\\GargoyleCrumble.mdl";
			case "UDIS" -> "Objects\\Spawnmodels\\Undead\\UndeadDissipate\\UndeadDissipate.mdl";
			case "EDIS" -> "Objects\\Spawnmodels\\NightElf\\NightelfDissipate\\NightElfDissipate.mdl";
			case "DDIS" -> "Objects\\Spawnmodels\\Demon\\DemonDissipate\\DemonDissipate.mdl";
			case "ODIS" -> "Objects\\Spawnmodels\\Orc\\OrcDissipate\\OrcDissipate.mdl";
			case "HDIS" -> "Objects\\Spawnmodels\\Human\\HumanDissipate\\HumanDissipate.mdl";
			case "HBS0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodSmall0.mdl";
			case "HBS1" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodSmall1.mdl";
			case "HBL0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodLarge0.mdl";
			case "HBL1" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodLarge1.mdl";
			case "EENT" -> "Objects\\Spawnmodels\\NightElf\\EntBirthTarget\\EntBirthTarget.mdl";
			case "DNAM" -> "Objects\\Spawnmodels\\NightElf\\NEDeathMedium\\NEDeath.mdl";
			case "DNAS" -> "Objects\\Spawnmodels\\NightElf\\NEDeathSmall\\NEDeathSmall.mdl";
			case "DUME" -> "Objects\\Spawnmodels\\Undead\\UDeathMedium\\UDeath.mdl";
			case "DUSM" -> "Objects\\Spawnmodels\\Undead\\UDeathSmall\\UDeathSmall.mdl";
			case "INFR" -> "Objects\\Spawnmodels\\Demon\\InfernalMeteor\\InfernalMeteor.mdl";
			case "INFL" -> "Objects\\Spawnmodels\\Demon\\InfernalMeteor\\InfernalMeteor2.mdl";
			case "INFU" -> "Objects\\Spawnmodels\\Demon\\InfernalMeteor\\InfernalMeteor3.mdl";
			case "HBF0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodFootman.mdl";
			case "HBK0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodKnight.mdl";
			case "HBM0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodMortarTeam.mdl";
			case "HBP0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodPeasant.mdl";
			case "HBPR" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodPriest.mdl";
			case "HBR0" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodRifleman.mdl";
			case "HBSR" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HumanBloodSorceress.mdl";
			case "HBNE" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\UndeadBloodNecromancer.mdl";
			case "NBVW" -> "Objects\\Spawnmodels\\Other\\NPCBlood\\NpcBloodVillagerWoman.mdl";
			case "OBHE" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodHeadhunter.mdl";
			case "OBHS" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodHellScream.mdl";
			case "OBFS" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodHeroFarSeer.mdl";
			case "OBTC" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodHeroTaurenChieftain.mdl";
			case "OBKB" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodKotoBeast.mdl";
			case "OBWD" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodWitchDoctor.mdl";
			case "OBWR" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodWolfrider.mdl";
			case "OBWY" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrdBloodWyvernRider.mdl";
			case "OBWV" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodRiderlessWyvernRider.mdl";
			case "OBT0" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodTauren.mdl";
			case "OBG0" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodGrunt.mdl";
			case "OBP0" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\OrcBloodPeon.mdl";
			case "OKBP" -> "Objects\\Spawnmodels\\Orc\\KodoBeastPuke\\KodoBeastPuke.mdl";
			case "UBGA" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\UndeadBloodGargoyle.mdl";
			case "UBGH" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\UndeadBloodGhoul.mdl";
			case "UBAB" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\UndeadBloodAbomination.mdl";
			case "UBAC" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\UndeadBloodAcolyte.mdl";
			case "DBCR" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\UndeadBloodCryptFiend.mdl";
			case "NBAR" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodArcher.mdl";
			case "NBDC" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodDruidoftheClaw.mdl";
			case "NBDT" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodDruidoftheTalon.mdl";
			case "NBDR" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodDryad.mdl";
			case "NBHU" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodHuntress.mdl";
			case "NBDB" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodDruidBear.mdl";
			case "NBDA" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodDruidRaven.mdl";
			case "NBDH" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodHeroDemonHunter.mdl";
			case "NBKG" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodHeroKeeperoftheGrove.mdl";
			case "NBMP" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodHeroMoonPriestess.mdl";
			case "NBCH" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodChimaera.mdl";
			case "NBHG" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\NightElfBloodHippogryph.mdl";
			case "DBPT" -> "Objects\\Spawnmodels\\Demon\\DemonBlood\\DemonBloodPitlord.mdl";
			case "DNBL" -> "Objects\\Spawnmodels\\Other\\NeutralBuildingExplosion\\NeutralBuildingExplosion.mdl";
			case "CLID" -> "Objects\\Spawnmodels\\Undead\\ImpaleTargetDust\\ImpaleTargetDust.mdl";
			case "HFSS" -> "Objects\\Spawnmodels\\Human\\SmallFlameSpawn\\SmallFlameSpawn.mdl";
			case "UBSC" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\ObsidianStatueCrumble.mdl";
			case "UBCC" -> "Objects\\Spawnmodels\\Undead\\UndeadBlood\\ObsidianStatueCrumble2.mdl";
			case "HBBM" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\HeroBloodElfBlood.mdl";
			case "HBSB" -> "Objects\\Spawnmodels\\Human\\HumanBlood\\BloodElfSpellThiefBlood.mdl";
			case "NBMF" -> "Objects\\Spawnmodels\\NightElf\\NightElfBlood\\MALFurion_Blood.mdl";
			case "OBBT" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\BattrollBlood.mdl";
			case "OBSH" -> "Objects\\Spawnmodels\\Orc\\Orcblood\\HeroShadowHunterBlood.mdl";
			case "DBPB" -> "Objects\\Spawnmodels\\Other\\PandarenBrewmasterBlood\\PandarenBrewmasterBlood.mdl";
			case "DBBM" -> "Objects\\Spawnmodels\\Other\\BeastmasterBlood\\BeastmasterBlood.mdl";
			case "PEFI" -> "Abilities\\Spells\\Other\\ImmolationRed\\ImmolationREDTarget.mdl";
			case "DNBD" -> "Objects\\Spawnmodels\\Naga\\NagaDeath\\NagaDeath.mdl";
			case "FTSO" -> "Objects\\Spawnmodels\\Other\\FlameThrower\\FlameThrowerSpawnObj.mdl";
			case "TOBO" -> "Objects\\Spawnmodels\\Other\\ToonBoom\\ToonBoom.mdl";
			case "CBAL" -> "Objects\\Spawnmodels\\Critters\\Albatross\\CritterBloodAlbatross.mdl";
			case "IFP0" -> "Objects\\Spawnmodels\\Other\\IllidanFootprint\\IllidanSpawnFootPrint0.mdl";
			case "IFP1" -> "Objects\\Spawnmodels\\Other\\IllidanFootprint\\IllidanSpawnFootPrint1.mdl";
			case "IFPW" -> "Objects\\Spawnmodels\\Other\\IllidanFootprint\\IllidanWaterSpawnFootPrint.mdl";
			case "HBCE" -> "Objects\\Spawnmodels\\Other\\HumanBloodCinematicEffect\\HumanBloodCinematicEffect.mdl";
			case "OBCE" -> "Objects\\Spawnmodels\\Other\\OrcBloodCinematicEffect\\OrcBloodCinematicEffect.mdl";
			case "FRBS" -> "Objects\\Spawnmodels\\Human\\FragmentationShards\\FragBoomSpawn.mdl";
			case "PBSX" -> "Objects\\Spawnmodels\\Other\\PandarenBrewmasterExplosionUltimate\\PandarenBrewmasterExplosionUltimate.mdl";
			case "GDCR" -> "UI\\Feedback\\GoldCredit\\GoldCredit.mdl";
			case "NBWS" -> "Objects\\Spawnmodels\\Naga\\NagaBlood\\NagaBloodWindserpent.mdl";
			default -> "Unknown";
		};
	}

	public String getDispString() {
		String soundCode = name.substring(name.length() - 4);
//		System.out.println("EventObject name: " + name);
		return name + " - " + getEventName(name);
	}
}
