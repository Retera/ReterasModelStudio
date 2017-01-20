package com.hiveworkshop.wc3.jworldedit.wipdesign.units;

import java.lang.annotation.Target;
import java.util.List;

import javax.swing.Icon;

import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.AiBuffer;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.ArmorType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.AttackBits;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.AttackType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.CombatSound;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.DeathType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.DefenseType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.MoveType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.RegenType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.ShadowImage;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.TeamColor;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.UnitClass;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.UnitRace;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.VersionFlags;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums.WeaponType;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.CanBeEmpty;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.CaseSensitive;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.Category;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.DisplayName;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.EditorCategory;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.MaxValue;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.MinValue;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.Rawcode;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.SortLabel;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.SourceLocation;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.TTDesc;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.TTName;
import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.TTUber;

public class Unit {
	@Rawcode("uabr")
	@SourceLocation("UnitData")
	@EditorCategory(Category.PATHING)
	@DisplayName("AI Placement Radius")
	@SortLabel("d4b01")
	@MinValue(0f)
	@MaxValue(24f)
	float aIPlacementRadius;
	@Rawcode("uabt")
	@SourceLocation("UnitData")
	@EditorCategory(Category.PATHING)
	@DisplayName("AI Placement Type")
	@SortLabel("d4b00")
	AiBuffer aIPlacementType;
	@Rawcode("uacq")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Acquisition Range")
	@SortLabel("c8a00")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(20000f)
	float acquisitionRange;
	@Rawcode("utcc")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Allow Custom Team Color")
	@SortLabel("c1a06b")
	boolean allowCustomTeamColor;
	@Rawcode("uble")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Animation - Blend Time (seconds)")
	@SortLabel("c7a04c")
	@MinValue(0f)
	@MaxValue(1f)
	float animation_BlendTime_seconds;
	@Rawcode("ucbs")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Animation - Cast Backswing")
	@SortLabel("d2b01")
	@MinValue(0f)
	@MaxValue(10f)
	float animation_CastBackswing;
	@Rawcode("ucpt")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Animation - Cast Point")
	@SortLabel("d2b00")
	@MinValue(0f)
	@MaxValue(10f)
	float animation_CastPoint;
	@Rawcode("urun")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Animation - Run Speed")
	@SortLabel("d6c01")
	@MinValue(0f)
	@MaxValue(2000f)
	float animation_RunSpeed;
	@Rawcode("uwal")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Animation - Walk Speed")
	@SortLabel("d6c00")
	@MinValue(0f)
	@MaxValue(2000f)
	float animation_WalkSpeed;
	@Rawcode("uarm")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Armor Type")
	@SortLabel("c5b03")
	ArmorType armorType;
	@Rawcode("ubs1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Animation Backswing Point")
	@SortLabel("c6a08a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack1_AnimationBackswingPoint;
	@Rawcode("udp1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Animation Damage Point")
	@SortLabel("c6a08")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack1_AnimationDamagePoint;
	@Rawcode("ua1f")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Area of Effect (Full Damage)")
	@SortLabel("c6a09")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack1_AreaOfEffect_FullDamage;
	@Rawcode("ua1h")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Area of Effect (Medium Damage)")
	@SortLabel("c6a10")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack1_AreaOfEffect_MediumDamage;
	@Rawcode("ua1q")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Area of Effect (Small Damage)")
	@SortLabel("c6a11")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack1_AreaOfEffect_SmallDamage;
	@Rawcode("ua1p")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Area of Effect Targets")
	@SortLabel("c6a12")
	@CaseSensitive
	List<Target> attack1_AreaOfEffectTargets;
	@Rawcode("ua1t")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Attack Type")
	@SortLabel("c6a03")
	@CaseSensitive
	AttackType attack1_AttackType;
	@Rawcode("ua1c")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Cooldown Time")
	@SortLabel("c6a08")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(3600f)
	float attack1_CooldownTime;
	@Rawcode("ua1b")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Base")
	@SortLabel("c6a070")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(500000f)
	int attack1_DamageBase;
	@Rawcode("uhd1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Factor - Medium")
	@SortLabel("c6a10a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack1_DamageFactor_Medium;
	@Rawcode("uqd1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Factor - Small")
	@SortLabel("c6a11a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack1_DamageFactor_Small;
	@Rawcode("udl1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Loss Factor")
	@SortLabel("c6a12a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack1_DamageLossFactor;
	@Rawcode("ua1d")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Number of Dice")
	@SortLabel("c6a071")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int attack1_DamageNumberOfDice;
	@Rawcode("ua1s")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Sides per Die")
	@SortLabel("c6a072")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int attack1_DamageSidesPerDie;
	@Rawcode("usd1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Spill Distance")
	@SortLabel("c6a12b")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	float attack1_DamageSpillDistance;
	@Rawcode("usr1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Spill Radius")
	@SortLabel("c6a12c")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	float attack1_DamageSpillRadius;
	@Rawcode("udu1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Damage Upgrade Amount")
	@SortLabel("c6a073")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack1_DamageUpgradeAmount;
	@Rawcode("utc1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Maximum Number of Targets")
	@SortLabel("c6a7a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int attack1_MaximumNumberOfTargets;
	@Rawcode("uma1")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Projectile Arc")
	@SortLabel("c6a06a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1f)
	float attack1_ProjectileArc;
	@Rawcode("ua1m")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Projectile Art")
	@SortLabel("c6a05")
	@CanBeEmpty
	Model attack1_ProjectileArt;
	@Rawcode("umh1")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Projectile Homing Enabled")
	@SortLabel("c6a06b")
	@CaseSensitive
	boolean attack1_ProjectileHomingEnabled;
	@Rawcode("ua1z")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Projectile Speed")
	@SortLabel("c6a06")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	int attack1_ProjectileSpeed;
	@Rawcode("ua1r")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Range")
	@SortLabel("c6a02")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(20000f)
	int attack1_Range;
	@Rawcode("urb1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Range Motion Buffer")
	@SortLabel("c6a02a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(2000f)
	float attack1_RangeMotionBuffer;
	@Rawcode("uwu1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Show UI")
	@SortLabel("c6a01a")
	boolean attack1_ShowUI;
	@Rawcode("ua1g")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Targets Allowed")
	@SortLabel("c6a07")
	@CaseSensitive
	List<Target> attack1_TargetsAllowed;
	@Rawcode("ucs1")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Weapon Sound")
	@SortLabel("c6a04a")
	@CaseSensitive
	@CanBeEmpty
	CombatSound attack1_WeaponSound;
	@Rawcode("ua1w")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 1 - Weapon Type")
	@SortLabel("c6a04")
	@CaseSensitive
	@CanBeEmpty
	WeaponType attack1_WeaponType;
	@Rawcode("ubs2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Animation Backswing Point")
	@SortLabel("c6b08a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack2_AnimationBackswingPoint;
	@Rawcode("udp2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Animation Damage Point")
	@SortLabel("c6b08")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack2_AnimationDamagePoint;
	@Rawcode("ua2f")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Area of Effect (Full Damage)")
	@SortLabel("c6b09")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack2_AreaOfEffect_FullDamage;
	@Rawcode("ua2h")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Area of Effect (Medium Damage)")
	@SortLabel("c6b10")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack2_AreaOfEffect_MediumDamage;
	@Rawcode("ua2q")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Area of Effect (Small Damage)")
	@SortLabel("c6b11")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack2_AreaOfEffect_SmallDamage;
	@Rawcode("ua2p")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Area of Effect Targets")
	@SortLabel("c6b12")
	@CaseSensitive
	List<Target> attack2_AreaOfEffectTargets;
	@Rawcode("ua2t")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Attack Type")
	@SortLabel("c6b03")
	@CaseSensitive
	AttackType attack2_AttackType;
	@Rawcode("ua2c")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Cooldown Time")
	@SortLabel("c6b08")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(3600f)
	float attack2_CooldownTime;
	@Rawcode("ua2b")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Base")
	@SortLabel("c6b070")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(500000f)
	int attack2_DamageBase;
	@Rawcode("uhd2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Factor - Medium")
	@SortLabel("c6b10a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack2_DamageFactor_Medium;
	@Rawcode("uqd2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Factor - Small")
	@SortLabel("c6b11a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack2_DamageFactor_Small;
	@Rawcode("udl2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Loss Factor")
	@SortLabel("c6b12a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10f)
	float attack2_DamageLossFactor;
	@Rawcode("ua2d")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Number of Dice")
	@SortLabel("c6b071")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int attack2_DamageNumberOfDice;
	@Rawcode("ua2s")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Sides per Die")
	@SortLabel("c6b072")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int attack2_DamageSidesPerDie;
	@Rawcode("usd2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Spill Distance")
	@SortLabel("c6b12b")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	float attack2_DamageSpillDistance;
	@Rawcode("usr2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Spill Radius")
	@SortLabel("c6b12c")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	float attack2_DamageSpillRadius;
	@Rawcode("udu2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Damage Upgrade Amount")
	@SortLabel("c6b073")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int attack2_DamageUpgradeAmount;
	@Rawcode("utc2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Maximum Number of Targets")
	@SortLabel("c6b7a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int attack2_MaximumNumberOfTargets;
	@Rawcode("uma2")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Projectile Arc")
	@SortLabel("c6b06a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1f)
	float attack2_ProjectileArc;
	@Rawcode("ua2m")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Projectile Art")
	@SortLabel("c6b05")
	@CanBeEmpty
	Model attack2_ProjectileArt;
	@Rawcode("umh2")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Projectile Homing Enabled")
	@SortLabel("c6b06b")
	@CaseSensitive
	boolean attack2_ProjectileHomingEnabled;
	@Rawcode("ua2z")
	@SourceLocation("Profile")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Projectile Speed")
	@SortLabel("c6b06")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	int attack2_ProjectileSpeed;
	@Rawcode("ua2r")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Range")
	@SortLabel("c6b02")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(20000f)
	int attack2_Range;
	@Rawcode("urb2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Range Motion Buffer")
	@SortLabel("c6b02a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(2000f)
	float attack2_RangeMotionBuffer;
	@Rawcode("uwu2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Show UI")
	@SortLabel("c6b01a")
	boolean attack2_ShowUI;
	@Rawcode("ua2g")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Targets Allowed")
	@SortLabel("c6b07")
	@CaseSensitive
	List<Target> attack2_TargetsAllowed;
	@Rawcode("ucs2")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Weapon Sound")
	@SortLabel("c6b04a")
	@CaseSensitive
	@CanBeEmpty
	CombatSound attack2_WeaponSound;
	@Rawcode("ua2w")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attack 2 - Weapon Type")
	@SortLabel("c6b04")
	@CaseSensitive
	@CanBeEmpty
	WeaponType attack2_WeaponType;
	@Rawcode("uaen")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Attacks Enabled")
	@SortLabel("c6a01")
	@CaseSensitive
	AttackBits attacksEnabled;
	@Rawcode("ubld")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Build Time")
	@SortLabel("c2a06")
	@CaseSensitive
	@MinValue(1f)
	@MaxValue(298f)
	int buildTime;
	@Rawcode("ubpx")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Button Position (X)")
	@SortLabel("c2a03x")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(3f)
	int buttonPosition_X;
	@Rawcode("ubpy")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Button Position (Y)")
	@SortLabel("c2a03y")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(2f)
	int buttonPosition_Y;
	@Rawcode("udro")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Can Drop Items On Death")
	@SortLabel("c1a06g")
	boolean canDropItemsOnDeath;
	@Rawcode("ufle")
	@SourceLocation("UnitData")
	@EditorCategory(Category.STATS)
	@DisplayName("Can Flee")
	@SortLabel("c8a031")
	@CaseSensitive
	boolean canFlee;
	@Rawcode("ucam")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Categorization - Campaign")
	@SortLabel("c1a06f")
	boolean categorization_Campaign;
	@Rawcode("uspe")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Categorization - Special")
	@SortLabel("c1a06e")
	boolean categorization_Special;
	@Rawcode("ucol")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.PATHING)
	@DisplayName("Collision Size")
	@SortLabel("c1a07")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1024f)
	float collisionSize;
	@Rawcode("udtm")
	@SourceLocation("UnitData")
	@EditorCategory(Category.ART)
	@DisplayName("Death Time (seconds)")
	@SortLabel("c1b10")
	@MinValue(0.1f)
	@MaxValue(20f)
	float deathTime_seconds;
	@Rawcode("udea")
	@SourceLocation("UnitData")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Death Type")
	@SortLabel("c1b09")
	DeathType deathType;
	@Rawcode("udaa")
	@SourceLocation("UnitAbilities")
	@EditorCategory(Category.ABILITY)
	@DisplayName("Default Active Ability")
	@SortLabel("c6c01")
	@CaseSensitive
	@CanBeEmpty
	Ability defaultActiveAbility;
	@Rawcode("udef")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Defense Base")
	@SortLabel("c5b00")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int defenseBase;
	@Rawcode("udty")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Defense Type")
	@SortLabel("c5b02")
	@CaseSensitive
	DefenseType defenseType;
	@Rawcode("udup")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Defense Upgrade Bonus")
	@SortLabel("c5b01")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	int defenseUpgradeBonus;
	@Rawcode("udep")
	@SourceLocation("Profile")
	@EditorCategory(Category.TECHTREE)
	@DisplayName("Dependency Equivalents")
	@SortLabel("c5a00a")
	@CaseSensitive
	@CanBeEmpty
	List<Unit> dependencyEquivalents;
	@Rawcode("ides")
	@SourceLocation("Profile")
	@EditorCategory(Category.TEXT)
	@DisplayName("Description")
	@SortLabel("d0a05")
	@CaseSensitive
	@CanBeEmpty
	@TTDesc
	String description;
	@Rawcode("uhos")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Display as Neutral Hostile")
	@SortLabel("c8a10")
	@CaseSensitive
	boolean displayAsNeutralHostile;
	@Rawcode("uept")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Elevation - Sample Points")
	@SortLabel("d5c00")
	@MinValue(0f)
	@MaxValue(4f)
	int elevation_SamplePoints;
	@Rawcode("uerd")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Elevation - Sample Radius")
	@SortLabel("d5c01")
	@MinValue(0f)
	@MaxValue(2048f)
	float elevation_SampleRadius;
	@Rawcode("ufrd")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Fog of War - Sample Radius")
	@SortLabel("d5c02")
	@MinValue(0f)
	@MaxValue(2048f)
	float fogOfWar_SampleRadius;
	@Rawcode("ufoo")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Food Cost")
	@SortLabel("c2a03")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(300f)
	int foodCost;
	@Rawcode("ufma")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Food Produced")
	@SortLabel("c2a030")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(300f)
	int foodProduced;
	@Rawcode("ubba")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Gold Bounty Awarded - Base")
	@SortLabel("c8a07")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	int goldBountyAwarded_Base;
	@Rawcode("ubdi")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Gold Bounty Awarded - Number of Dice")
	@SortLabel("c8a08")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int goldBountyAwarded_NumberOfDice;
	@Rawcode("ubsi")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Gold Bounty Awarded - Sides per Die")
	@SortLabel("c8a09")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int goldBountyAwarded_SidesPerDie;
	@Rawcode("ugol")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Gold Cost")
	@SortLabel("c2a00")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int goldCost;
	@Rawcode("utss")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Has Tileset Specific Data")
	@SortLabel("c1a06d")
	boolean hasTilesetSpecificData;
	@Rawcode("ushr")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Has Water Shadow")
	@SortLabel("c1a11e")
	boolean hasWaterShadow;
	@Rawcode("umvh")
	@SourceLocation("UnitData")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Height")
	@SortLabel("c7a02")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float height;
	@Rawcode("umvf")
	@SourceLocation("UnitData")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Height Minimum")
	@SortLabel("c7a02a")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float heightMinimum;
	@Rawcode("uhom")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.STATS)
	@DisplayName("Hide Minimap Display")
	@SortLabel("c1c01")
	boolean hideMinimapDisplay;
	@Rawcode("uhpm")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Hit Points Maximum (Base)")
	@SortLabel("c4a00")
	@CaseSensitive
	@MinValue(1f)
	@MaxValue(500000f)
	int hitPointsMaximum_Base;
	@Rawcode("uhpr")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Hit Points Regeneration Rate")
	@SortLabel("c4a01")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	float hitPointsRegenerationRate;
	@Rawcode("uhrt")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Hit Points Regeneration Type")
	@SortLabel("c4a02")
	@CaseSensitive
	RegenType hitPointsRegenerationType;
	@Rawcode("uhot")
	@SourceLocation("Profile")
	@EditorCategory(Category.TEXT)
	@DisplayName("Hotkey")
	@SortLabel("d0a01")
	@CaseSensitive
	@CanBeEmpty
	char hotkey;
	@Rawcode("uico")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Icon - Game Interface")
	@SortLabel("c1a02")
	Icon icon_GameInterface;
	@Rawcode("ussi")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Icon - Score Screen")
	@SortLabel("c1a02a")
	@CaseSensitive
	@CanBeEmpty
	Icon icon_ScoreScreen;
	@Rawcode("ubdg")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Is a Building")
	@SortLabel("c1a001a")
	boolean isABuilding;
	@Rawcode("usei")
	@SourceLocation("Profile")
	@EditorCategory(Category.TECHTREE)
	@DisplayName("Items Sold")
	@SortLabel("c5a05")
	@CaseSensitive
	@MaxValue(12f)
	List<Item> itemsSold;
	@Rawcode("ulfi")
	@SourceLocation("Profile")
	@EditorCategory(Category.SOUND)
	@DisplayName("Looping Fade In Rate")
	@SortLabel("d8b03")
	@CanBeEmpty
	@MinValue(0f)
	@MaxValue(12700f)
	int loopingFadeInRate;
	@Rawcode("ulfo")
	@SourceLocation("Profile")
	@EditorCategory(Category.SOUND)
	@DisplayName("Looping Fade Out Rate")
	@SortLabel("d8b04")
	@CanBeEmpty
	@MinValue(0f)
	@MaxValue(12700f)
	int loopingFadeOutRate;
	@Rawcode("ulba")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Lumber Bounty Awarded - Base")
	@SortLabel("c8a11")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(10000f)
	int lumberBountyAwarded_Base;
	@Rawcode("ulbd")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Lumber Bounty Awarded - Number of Dice")
	@SortLabel("c8a10")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int lumberBountyAwarded_NumberOfDice;
	@Rawcode("ulbs")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Lumber Bounty Awarded - Sides per Die")
	@SortLabel("c8a12")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100f)
	int lumberBountyAwarded_SidesPerDie;
	@Rawcode("ulum")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Lumber Cost")
	@SortLabel("c2a01")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int lumberCost;
	@Rawcode("umpi")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Mana Initial Amount")
	@SortLabel("c4a05")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int manaInitialAmount;
	@Rawcode("umpm")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Mana Maximum")
	@SortLabel("c4a03")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int manaMaximum;
	@Rawcode("umpr")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Mana Regeneration")
	@SortLabel("c4a04")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1000f)
	float manaRegeneration;
	@Rawcode("umxp")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Maximum Pitch Angle (degrees)")
	@SortLabel("d5b00")
	@MinValue(0f)
	@MaxValue(180f)
	float maximumPitchAngle_degrees;
	@Rawcode("umxr")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Maximum Roll Angle (degrees)")
	@SortLabel("d5b01")
	@MinValue(0f)
	@MaxValue(180f)
	float maximumRollAngle_degrees;
	@Rawcode("uamn")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Minimum Attack Range")
	@SortLabel("c6a00")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(20000f)
	int minimumAttackRange;
	@Rawcode("umdl")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Model File")
	@SortLabel("c1a01")
	Model modelFile;
	@Rawcode("uver")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Model File - Extra Versions")
	@SortLabel("c1a01a")
	@MinValue(0f)
	@MaxValue(3f)
	VersionFlags modelFile_ExtraVersions;
	@Rawcode("umsl")
	@SourceLocation("Profile")
	@EditorCategory(Category.SOUND)
	@DisplayName("Movement")
	@SortLabel("d8b01")
	@CaseSensitive
	@CanBeEmpty
	SoundLabel movement;
	@Rawcode("unam")
	@SourceLocation("Profile")
	@EditorCategory(Category.TEXT)
	@DisplayName("Name")
	@SortLabel("c1a00")
	@CaseSensitive
	@TTName
	String name;
	@Rawcode("unsf")
	@SourceLocation("Profile")
	@EditorCategory(Category.TEXT)
	@DisplayName("Name - Editor Suffix")
	@SortLabel("c1a000")
	@CaseSensitive
	@CanBeEmpty
	@MaxValue(50f)
	String name_EditorSuffix;
	@Rawcode("uabi")
	@SourceLocation("UnitAbilities")
	@EditorCategory(Category.ABILITY)
	@DisplayName("Normal")
	@SortLabel("c6c00")
	@CaseSensitive
	List<Ability> normal;
	@Rawcode("uocc")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Occluder Height")
	@SortLabel("d6c03")
	@MinValue(0f)
	@MaxValue(2048f)
	float occluderHeight;
	@Rawcode("uori")
	@SourceLocation("UnitData")
	@EditorCategory(Category.ART)
	@DisplayName("Orientation Interpolation")
	@SortLabel("c7a04b")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(8f)
	int orientationInterpolation;
	@Rawcode("uine")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Placeable In Editor")
	@SortLabel("c1a06d0")
	boolean placeableInEditor;
	@Rawcode("upoi")
	@SourceLocation("UnitData")
	@EditorCategory(Category.STATS)
	@DisplayName("Point Value")
	@SortLabel("c8a05")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int pointValue;
	@Rawcode("upri")
	@SourceLocation("UnitData")
	@EditorCategory(Category.STATS)
	@DisplayName("Priority")
	@SortLabel("c8a06")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(20f)
	int priority;
	@Rawcode("uimz")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Projectile Impact - Z")
	@SortLabel("c5a04")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float projectileImpact_Z;
	@Rawcode("uisz")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Projectile Impact - Z (Swimming)")
	@SortLabel("c5a04a")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float projectileImpact_Z_Swimming;
	@Rawcode("ulpx")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Projectile Launch - X")
	@SortLabel("c5a03a")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float projectileLaunch_X;
	@Rawcode("ulpy")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Projectile Launch - Y")
	@SortLabel("c5a03b")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float projectileLaunch_Y;
	@Rawcode("ulpz")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Projectile Launch - Z")
	@SortLabel("c5a03c")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float projectileLaunch_Z;
	@Rawcode("ulsz")
	@SourceLocation("UnitWeapons")
	@EditorCategory(Category.ART)
	@DisplayName("Projectile Launch - Z (Swimming)")
	@SortLabel("c5a03d")
	@CaseSensitive
	@MinValue(-1000f)
	@MaxValue(1000f)
	float projectileLaunch_Z_Swimming;
	@Rawcode("uprw")
	@SourceLocation("UnitData")
	@EditorCategory(Category.ART)
	@DisplayName("Propulsion Window (degrees)")
	@SortLabel("c7a04a")
	@CaseSensitive
	@MinValue(1f)
	@MaxValue(180f)
	float propulsionWindow_degrees;
	@Rawcode("urac")
	@SourceLocation("UnitData")
	@EditorCategory(Category.STATS)
	@DisplayName("Race")
	@SortLabel("c1a001")
	UnitRace race;
	@Rawcode("ursl")
	@SourceLocation("Profile")
	@EditorCategory(Category.SOUND)
	@DisplayName("Random")
	@SortLabel("d8b02")
	@CaseSensitive
	@CanBeEmpty
	SoundLabel random;
	@Rawcode("ugor")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Repair Gold Cost")
	@SortLabel("c2a02")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int repairGoldCost;
	@Rawcode("ulur")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Repair Lumber Cost")
	@SortLabel("c2a02")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(100000f)
	int repairLumberCost;
	@Rawcode("urtm")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Repair Time")
	@SortLabel("c2a07")
	@CaseSensitive
	@MinValue(1f)
	@MaxValue(10000f)
	int repairTime;
	@Rawcode("uani")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Required Animation Names")
	@SortLabel("c1a010")
	@MaxValue(20f)
	List<String> requiredAnimationNames;
	@Rawcode("uaap")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Required Animation Names - Attachments")
	@SortLabel("c1a010a")
	@MaxValue(20f)
	List<String> requiredAnimationNames_Attachments;
	@Rawcode("ualp")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Required Attachment Link Names")
	@SortLabel("c1a010b")
	@MaxValue(20f)
	List<String> requiredAttachmentLinkNames;
	@Rawcode("ubpr")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Required Bone Names")
	@SortLabel("c1a010c")
	@MaxValue(20f)
	List<String> requiredBoneNames;
	@Rawcode("ureq")
	@SourceLocation("Profile")
	@EditorCategory(Category.TECHTREE)
	@DisplayName("Requirements")
	@SortLabel("c5a00")
	@CaseSensitive
	@CanBeEmpty
	List<Tech> requirements;
	@Rawcode("urqa")
	@SourceLocation("Profile")
	@EditorCategory(Category.TECHTREE)
	@DisplayName("Requirements - Levels")
	@SortLabel("c5a00a")
	@CanBeEmpty
	List<int> requirements_Levels;
	@Rawcode("uscb")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Scale Projectiles")
	@SortLabel("c1a08a")
	boolean scaleProjectiles;
	@Rawcode("usca")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Scaling Value")
	@SortLabel("c1a03")
	@CaseSensitive
	@MinValue(0.1f)
	@MaxValue(10f)
	float scalingValue;
	@Rawcode("uslz")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Selection Circle - Height")
	@SortLabel("d6c02")
	@MinValue(0f)
	@MaxValue(2048f)
	float selectionCircle_Height;
	@Rawcode("usew")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Selection Circle On Water")
	@SortLabel("c1a12")
	boolean selectionCircleOnWater;
	@Rawcode("ussc")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Selection Scale")
	@SortLabel("c1a08")
	@CaseSensitive
	@MinValue(0.1f)
	@MaxValue(20f)
	float selectionScale;
	@Rawcode("ushu")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Shadow Image (Unit)")
	@SortLabel("c1a11")
	@CanBeEmpty
	ShadowImage shadowImage_Unit;
	@Rawcode("ushx")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Shadow Image - Center X")
	@SortLabel("c1a11c")
	@MinValue(0f)
	@MaxValue(2048f)
	float shadowImage_CenterX;
	@Rawcode("ushy")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Shadow Image - Center Y")
	@SortLabel("c1a11d")
	@MinValue(0f)
	@MaxValue(2048f)
	float shadowImage_CenterY;
	@Rawcode("ushh")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Shadow Image - Height")
	@SortLabel("c1a11b")
	@MinValue(0f)
	@MaxValue(2048f)
	float shadowImage_Height;
	@Rawcode("ushw")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Shadow Image - Width")
	@SortLabel("c1a11a")
	@MinValue(0f)
	@MaxValue(2048f)
	float shadowImage_Width;
	@Rawcode("ushb")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Shadow Texture (Building)")
	@SortLabel("c1a10a")
	@CanBeEmpty
	ShadowTexture shadowTexture_Building;
	@Rawcode("usid")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Sight Radius (Day)")
	@SortLabel("c8a01")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1800f)
	int sightRadius_Day;
	@Rawcode("usin")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Sight Radius (Night)")
	@SortLabel("c8a02")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(1800f)
	int sightRadius_Night;
	@Rawcode("usle")
	@SourceLocation("UnitData")
	@EditorCategory(Category.STATS)
	@DisplayName("Sleeps")
	@SortLabel("c8a030")
	@CaseSensitive
	boolean sleeps;
	@Rawcode("uspa")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Special")
	@SortLabel("d9b00")
	@CanBeEmpty
	List<Model> special;
	@Rawcode("umvs")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Speed Base")
	@SortLabel("c7a03")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(522f)
	int speedBase;
	@Rawcode("umas")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Speed Maximum")
	@SortLabel("c7a03b")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(522f)
	int speedMaximum;
	@Rawcode("umis")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Speed Minimum")
	@SortLabel("c7a03a")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(522f)
	int speedMinimum;
	@Rawcode("usma")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Stock Maximum")
	@SortLabel("c9a00")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(32f)
	int stockMaximum;
	@Rawcode("usrg")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Stock Replenish Interval")
	@SortLabel("c9a01")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(3600f)
	int stockReplenishInterval;
	@Rawcode("usst")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Stock Start Delay")
	@SortLabel("c9a000")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(3600f)
	int stockStartDelay;
	@Rawcode("utaa")
	@SourceLocation("Profile")
	@EditorCategory(Category.ART)
	@DisplayName("Target")
	@SortLabel("d9b01")
	@CanBeEmpty
	List<Model> target;
	@Rawcode("utar")
	@SourceLocation("UnitData")
	@EditorCategory(Category.COMBAT)
	@DisplayName("Targeted as")
	@SortLabel("c7a00")
	@CaseSensitive
	List<Target> targetedAs;
	@Rawcode("utco")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Team Color")
	@SortLabel("c1a06a")
	TeamColor teamColor;
	@Rawcode("util")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Tilesets")
	@SortLabel("c1a06c")
	@CaseSensitive
	List<Tileset> tilesets;
	@Rawcode("uclr")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Tinting Color 1 (Red)")
	@SortLabel("c1a04")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(255f)
	int tintingColor1_Red;
	@Rawcode("uclg")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Tinting Color 2 (Green)")
	@SortLabel("c1a05")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(255f)
	int tintingColor2_Green;
	@Rawcode("uclb")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.ART)
	@DisplayName("Tinting Color 3 (Blue)")
	@SortLabel("c1a06")
	@CaseSensitive
	@MinValue(0f)
	@MaxValue(255f)
	int tintingColor3_Blue;
	@Rawcode("utip")
	@SourceLocation("Profile")
	@EditorCategory(Category.TEXT)
	@DisplayName("Tooltip - Basic")
	@SortLabel("d0a00")
	@CaseSensitive
	@CanBeEmpty
	@TTDesc
	String tooltip_Basic;
	@Rawcode("utub")
	@SourceLocation("Profile")
	@EditorCategory(Category.TEXT)
	@DisplayName("Tooltip - Extended")
	@SortLabel("d0a03")
	@CaseSensitive
	@CanBeEmpty
	@TTUber
	String tooltip_Extended;
	@Rawcode("umvr")
	@SourceLocation("UnitData")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Turn Rate")
	@SortLabel("c7a04")
	@CaseSensitive
	@MinValue(0.1f)
	@MaxValue(3f)
	float turnRate;
	@Rawcode("umvt")
	@SourceLocation("UnitData")
	@EditorCategory(Category.MOVEMENT)
	@DisplayName("Type")
	@SortLabel("c7a01")
	@CaseSensitive
	@CanBeEmpty
	MoveType type;
	@Rawcode("utyp")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.STATS)
	@DisplayName("Unit Classification")
	@SortLabel("c2a04")
	UnitClass unitClassification;
	@Rawcode("usnd")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.SOUND)
	@DisplayName("Unit Sound Set")
	@SortLabel("c1a10")
	@CanBeEmpty
	UnitSound unitSoundSet;
	@Rawcode("useu")
	@SourceLocation("Profile")
	@EditorCategory(Category.TECHTREE)
	@DisplayName("Units Sold")
	@SortLabel("c5a04")
	@CaseSensitive
	@MaxValue(12f)
	List<Unit> unitsSold;
	@Rawcode("upgr")
	@SourceLocation("UnitBalance")
	@EditorCategory(Category.TECHTREE)
	@DisplayName("Upgrades Used")
	@SortLabel("c5a02")
	@CaseSensitive
	List<Upgrade> upgradesUsed;
	@Rawcode("uuch")
	@SourceLocation("UnitUI")
	@EditorCategory(Category.EDITOR)
	@DisplayName("Use Click Helper")
	@SortLabel("c1c02")
	boolean useClickHelper;
	@Rawcode("ulos")
	@SourceLocation("UnitData")
	@EditorCategory(Category.ART)
	@DisplayName("Use Extended Line of Sight")
	@SortLabel("d3b00")
	boolean useExtendedLineOfSight;
	public float getAIPlacementRadius() {
		return aIPlacementRadius;
	}
	public void setAIPlacementRadius(final float aIPlacementRadius) {
		this.aIPlacementRadius = aIPlacementRadius;
	}
	public AiBuffer getAIPlacementType() {
		return aIPlacementType;
	}
	public void setAIPlacementType(final AiBuffer aIPlacementType) {
		this.aIPlacementType = aIPlacementType;
	}
	public float getAcquisitionRange() {
		return acquisitionRange;
	}
	public void setAcquisitionRange(final float acquisitionRange) {
		this.acquisitionRange = acquisitionRange;
	}
	public boolean getAllowCustomTeamColor() {
		return allowCustomTeamColor;
	}
	public void setAllowCustomTeamColor(final boolean allowCustomTeamColor) {
		this.allowCustomTeamColor = allowCustomTeamColor;
	}
	public float getAnimation_BlendTime_seconds() {
		return animation_BlendTime_seconds;
	}
	public void setAnimation_BlendTime_seconds(final float animation_BlendTime_seconds) {
		this.animation_BlendTime_seconds = animation_BlendTime_seconds;
	}
	public float getAnimation_CastBackswing() {
		return animation_CastBackswing;
	}
	public void setAnimation_CastBackswing(final float animation_CastBackswing) {
		this.animation_CastBackswing = animation_CastBackswing;
	}
	public float getAnimation_CastPoint() {
		return animation_CastPoint;
	}
	public void setAnimation_CastPoint(final float animation_CastPoint) {
		this.animation_CastPoint = animation_CastPoint;
	}
	public float getAnimation_RunSpeed() {
		return animation_RunSpeed;
	}
	public void setAnimation_RunSpeed(final float animation_RunSpeed) {
		this.animation_RunSpeed = animation_RunSpeed;
	}
	public float getAnimation_WalkSpeed() {
		return animation_WalkSpeed;
	}
	public void setAnimation_WalkSpeed(final float animation_WalkSpeed) {
		this.animation_WalkSpeed = animation_WalkSpeed;
	}
	public ArmorType getArmorType() {
		return armorType;
	}
	public void setArmorType(final ArmorType armorType) {
		this.armorType = armorType;
	}
	public float getAttack1_AnimationBackswingPoint() {
		return attack1_AnimationBackswingPoint;
	}
	public void setAttack1_AnimationBackswingPoint(final float attack1_AnimationBackswingPoint) {
		this.attack1_AnimationBackswingPoint = attack1_AnimationBackswingPoint;
	}
	public float getAttack1_AnimationDamagePoint() {
		return attack1_AnimationDamagePoint;
	}
	public void setAttack1_AnimationDamagePoint(final float attack1_AnimationDamagePoint) {
		this.attack1_AnimationDamagePoint = attack1_AnimationDamagePoint;
	}
	public int getAttack1_AreaOfEffect_FullDamage() {
		return attack1_AreaOfEffect_FullDamage;
	}
	public void setAttack1_AreaOfEffect_FullDamage(final int attack1_AreaOfEffect_FullDamage) {
		this.attack1_AreaOfEffect_FullDamage = attack1_AreaOfEffect_FullDamage;
	}
	public int getAttack1_AreaOfEffect_MediumDamage() {
		return attack1_AreaOfEffect_MediumDamage;
	}
	public void setAttack1_AreaOfEffect_MediumDamage(final int attack1_AreaOfEffect_MediumDamage) {
		this.attack1_AreaOfEffect_MediumDamage = attack1_AreaOfEffect_MediumDamage;
	}
	public int getAttack1_AreaOfEffect_SmallDamage() {
		return attack1_AreaOfEffect_SmallDamage;
	}
	public void setAttack1_AreaOfEffect_SmallDamage(final int attack1_AreaOfEffect_SmallDamage) {
		this.attack1_AreaOfEffect_SmallDamage = attack1_AreaOfEffect_SmallDamage;
	}
	public List<Target> getAttack1_AreaOfEffectTargets() {
		return attack1_AreaOfEffectTargets;
	}
	public void setAttack1_AreaOfEffectTargets(final List<Target> attack1_AreaOfEffectTargets) {
		this.attack1_AreaOfEffectTargets = attack1_AreaOfEffectTargets;
	}
	public AttackType getAttack1_AttackType() {
		return attack1_AttackType;
	}
	public void setAttack1_AttackType(final AttackType attack1_AttackType) {
		this.attack1_AttackType = attack1_AttackType;
	}
	public float getAttack1_CooldownTime() {
		return attack1_CooldownTime;
	}
	public void setAttack1_CooldownTime(final float attack1_CooldownTime) {
		this.attack1_CooldownTime = attack1_CooldownTime;
	}
	public int getAttack1_DamageBase() {
		return attack1_DamageBase;
	}
	public void setAttack1_DamageBase(final int attack1_DamageBase) {
		this.attack1_DamageBase = attack1_DamageBase;
	}
	public float getAttack1_DamageFactor_Medium() {
		return attack1_DamageFactor_Medium;
	}
	public void setAttack1_DamageFactor_Medium(final float attack1_DamageFactor_Medium) {
		this.attack1_DamageFactor_Medium = attack1_DamageFactor_Medium;
	}
	public float getAttack1_DamageFactor_Small() {
		return attack1_DamageFactor_Small;
	}
	public void setAttack1_DamageFactor_Small(final float attack1_DamageFactor_Small) {
		this.attack1_DamageFactor_Small = attack1_DamageFactor_Small;
	}
	public float getAttack1_DamageLossFactor() {
		return attack1_DamageLossFactor;
	}
	public void setAttack1_DamageLossFactor(final float attack1_DamageLossFactor) {
		this.attack1_DamageLossFactor = attack1_DamageLossFactor;
	}
	public int getAttack1_DamageNumberOfDice() {
		return attack1_DamageNumberOfDice;
	}
	public void setAttack1_DamageNumberOfDice(final int attack1_DamageNumberOfDice) {
		this.attack1_DamageNumberOfDice = attack1_DamageNumberOfDice;
	}
	public int getAttack1_DamageSidesPerDie() {
		return attack1_DamageSidesPerDie;
	}
	public void setAttack1_DamageSidesPerDie(final int attack1_DamageSidesPerDie) {
		this.attack1_DamageSidesPerDie = attack1_DamageSidesPerDie;
	}
	public float getAttack1_DamageSpillDistance() {
		return attack1_DamageSpillDistance;
	}
	public void setAttack1_DamageSpillDistance(final float attack1_DamageSpillDistance) {
		this.attack1_DamageSpillDistance = attack1_DamageSpillDistance;
	}
	public float getAttack1_DamageSpillRadius() {
		return attack1_DamageSpillRadius;
	}
	public void setAttack1_DamageSpillRadius(final float attack1_DamageSpillRadius) {
		this.attack1_DamageSpillRadius = attack1_DamageSpillRadius;
	}
	public int getAttack1_DamageUpgradeAmount() {
		return attack1_DamageUpgradeAmount;
	}
	public void setAttack1_DamageUpgradeAmount(final int attack1_DamageUpgradeAmount) {
		this.attack1_DamageUpgradeAmount = attack1_DamageUpgradeAmount;
	}
	public int getAttack1_MaximumNumberOfTargets() {
		return attack1_MaximumNumberOfTargets;
	}
	public void setAttack1_MaximumNumberOfTargets(final int attack1_MaximumNumberOfTargets) {
		this.attack1_MaximumNumberOfTargets = attack1_MaximumNumberOfTargets;
	}
	public float getAttack1_ProjectileArc() {
		return attack1_ProjectileArc;
	}
	public void setAttack1_ProjectileArc(final float attack1_ProjectileArc) {
		this.attack1_ProjectileArc = attack1_ProjectileArc;
	}
	public Model getAttack1_ProjectileArt() {
		return attack1_ProjectileArt;
	}
	public void setAttack1_ProjectileArt(final Model attack1_ProjectileArt) {
		this.attack1_ProjectileArt = attack1_ProjectileArt;
	}
	public boolean getAttack1_ProjectileHomingEnabled() {
		return attack1_ProjectileHomingEnabled;
	}
	public void setAttack1_ProjectileHomingEnabled(final boolean attack1_ProjectileHomingEnabled) {
		this.attack1_ProjectileHomingEnabled = attack1_ProjectileHomingEnabled;
	}
	public int getAttack1_ProjectileSpeed() {
		return attack1_ProjectileSpeed;
	}
	public void setAttack1_ProjectileSpeed(final int attack1_ProjectileSpeed) {
		this.attack1_ProjectileSpeed = attack1_ProjectileSpeed;
	}
	public int getAttack1_Range() {
		return attack1_Range;
	}
	public void setAttack1_Range(final int attack1_Range) {
		this.attack1_Range = attack1_Range;
	}
	public float getAttack1_RangeMotionBuffer() {
		return attack1_RangeMotionBuffer;
	}
	public void setAttack1_RangeMotionBuffer(final float attack1_RangeMotionBuffer) {
		this.attack1_RangeMotionBuffer = attack1_RangeMotionBuffer;
	}
	public boolean getAttack1_ShowUI() {
		return attack1_ShowUI;
	}
	public void setAttack1_ShowUI(final boolean attack1_ShowUI) {
		this.attack1_ShowUI = attack1_ShowUI;
	}
	public List<Target> getAttack1_TargetsAllowed() {
		return attack1_TargetsAllowed;
	}
	public void setAttack1_TargetsAllowed(final List<Target> attack1_TargetsAllowed) {
		this.attack1_TargetsAllowed = attack1_TargetsAllowed;
	}
	public CombatSound getAttack1_WeaponSound() {
		return attack1_WeaponSound;
	}
	public void setAttack1_WeaponSound(final CombatSound attack1_WeaponSound) {
		this.attack1_WeaponSound = attack1_WeaponSound;
	}
	public WeaponType getAttack1_WeaponType() {
		return attack1_WeaponType;
	}
	public void setAttack1_WeaponType(final WeaponType attack1_WeaponType) {
		this.attack1_WeaponType = attack1_WeaponType;
	}
	public float getAttack2_AnimationBackswingPoint() {
		return attack2_AnimationBackswingPoint;
	}
	public void setAttack2_AnimationBackswingPoint(final float attack2_AnimationBackswingPoint) {
		this.attack2_AnimationBackswingPoint = attack2_AnimationBackswingPoint;
	}
	public float getAttack2_AnimationDamagePoint() {
		return attack2_AnimationDamagePoint;
	}
	public void setAttack2_AnimationDamagePoint(final float attack2_AnimationDamagePoint) {
		this.attack2_AnimationDamagePoint = attack2_AnimationDamagePoint;
	}
	public int getAttack2_AreaOfEffect_FullDamage() {
		return attack2_AreaOfEffect_FullDamage;
	}
	public void setAttack2_AreaOfEffect_FullDamage(final int attack2_AreaOfEffect_FullDamage) {
		this.attack2_AreaOfEffect_FullDamage = attack2_AreaOfEffect_FullDamage;
	}
	public int getAttack2_AreaOfEffect_MediumDamage() {
		return attack2_AreaOfEffect_MediumDamage;
	}
	public void setAttack2_AreaOfEffect_MediumDamage(final int attack2_AreaOfEffect_MediumDamage) {
		this.attack2_AreaOfEffect_MediumDamage = attack2_AreaOfEffect_MediumDamage;
	}
	public int getAttack2_AreaOfEffect_SmallDamage() {
		return attack2_AreaOfEffect_SmallDamage;
	}
	public void setAttack2_AreaOfEffect_SmallDamage(final int attack2_AreaOfEffect_SmallDamage) {
		this.attack2_AreaOfEffect_SmallDamage = attack2_AreaOfEffect_SmallDamage;
	}
	public List<Target> getAttack2_AreaOfEffectTargets() {
		return attack2_AreaOfEffectTargets;
	}
	public void setAttack2_AreaOfEffectTargets(final List<Target> attack2_AreaOfEffectTargets) {
		this.attack2_AreaOfEffectTargets = attack2_AreaOfEffectTargets;
	}
	public AttackType getAttack2_AttackType() {
		return attack2_AttackType;
	}
	public void setAttack2_AttackType(final AttackType attack2_AttackType) {
		this.attack2_AttackType = attack2_AttackType;
	}
	public float getAttack2_CooldownTime() {
		return attack2_CooldownTime;
	}
	public void setAttack2_CooldownTime(final float attack2_CooldownTime) {
		this.attack2_CooldownTime = attack2_CooldownTime;
	}
	public int getAttack2_DamageBase() {
		return attack2_DamageBase;
	}
	public void setAttack2_DamageBase(final int attack2_DamageBase) {
		this.attack2_DamageBase = attack2_DamageBase;
	}
	public float getAttack2_DamageFactor_Medium() {
		return attack2_DamageFactor_Medium;
	}
	public void setAttack2_DamageFactor_Medium(final float attack2_DamageFactor_Medium) {
		this.attack2_DamageFactor_Medium = attack2_DamageFactor_Medium;
	}
	public float getAttack2_DamageFactor_Small() {
		return attack2_DamageFactor_Small;
	}
	public void setAttack2_DamageFactor_Small(final float attack2_DamageFactor_Small) {
		this.attack2_DamageFactor_Small = attack2_DamageFactor_Small;
	}
	public float getAttack2_DamageLossFactor() {
		return attack2_DamageLossFactor;
	}
	public void setAttack2_DamageLossFactor(final float attack2_DamageLossFactor) {
		this.attack2_DamageLossFactor = attack2_DamageLossFactor;
	}
	public int getAttack2_DamageNumberOfDice() {
		return attack2_DamageNumberOfDice;
	}
	public void setAttack2_DamageNumberOfDice(final int attack2_DamageNumberOfDice) {
		this.attack2_DamageNumberOfDice = attack2_DamageNumberOfDice;
	}
	public int getAttack2_DamageSidesPerDie() {
		return attack2_DamageSidesPerDie;
	}
	public void setAttack2_DamageSidesPerDie(final int attack2_DamageSidesPerDie) {
		this.attack2_DamageSidesPerDie = attack2_DamageSidesPerDie;
	}
	public float getAttack2_DamageSpillDistance() {
		return attack2_DamageSpillDistance;
	}
	public void setAttack2_DamageSpillDistance(final float attack2_DamageSpillDistance) {
		this.attack2_DamageSpillDistance = attack2_DamageSpillDistance;
	}
	public float getAttack2_DamageSpillRadius() {
		return attack2_DamageSpillRadius;
	}
	public void setAttack2_DamageSpillRadius(final float attack2_DamageSpillRadius) {
		this.attack2_DamageSpillRadius = attack2_DamageSpillRadius;
	}
	public int getAttack2_DamageUpgradeAmount() {
		return attack2_DamageUpgradeAmount;
	}
	public void setAttack2_DamageUpgradeAmount(final int attack2_DamageUpgradeAmount) {
		this.attack2_DamageUpgradeAmount = attack2_DamageUpgradeAmount;
	}
	public int getAttack2_MaximumNumberOfTargets() {
		return attack2_MaximumNumberOfTargets;
	}
	public void setAttack2_MaximumNumberOfTargets(final int attack2_MaximumNumberOfTargets) {
		this.attack2_MaximumNumberOfTargets = attack2_MaximumNumberOfTargets;
	}
	public float getAttack2_ProjectileArc() {
		return attack2_ProjectileArc;
	}
	public void setAttack2_ProjectileArc(final float attack2_ProjectileArc) {
		this.attack2_ProjectileArc = attack2_ProjectileArc;
	}
	public Model getAttack2_ProjectileArt() {
		return attack2_ProjectileArt;
	}
	public void setAttack2_ProjectileArt(final Model attack2_ProjectileArt) {
		this.attack2_ProjectileArt = attack2_ProjectileArt;
	}
	public boolean getAttack2_ProjectileHomingEnabled() {
		return attack2_ProjectileHomingEnabled;
	}
	public void setAttack2_ProjectileHomingEnabled(final boolean attack2_ProjectileHomingEnabled) {
		this.attack2_ProjectileHomingEnabled = attack2_ProjectileHomingEnabled;
	}
	public int getAttack2_ProjectileSpeed() {
		return attack2_ProjectileSpeed;
	}
	public void setAttack2_ProjectileSpeed(final int attack2_ProjectileSpeed) {
		this.attack2_ProjectileSpeed = attack2_ProjectileSpeed;
	}
	public int getAttack2_Range() {
		return attack2_Range;
	}
	public void setAttack2_Range(final int attack2_Range) {
		this.attack2_Range = attack2_Range;
	}
	public float getAttack2_RangeMotionBuffer() {
		return attack2_RangeMotionBuffer;
	}
	public void setAttack2_RangeMotionBuffer(final float attack2_RangeMotionBuffer) {
		this.attack2_RangeMotionBuffer = attack2_RangeMotionBuffer;
	}
	public boolean getAttack2_ShowUI() {
		return attack2_ShowUI;
	}
	public void setAttack2_ShowUI(final boolean attack2_ShowUI) {
		this.attack2_ShowUI = attack2_ShowUI;
	}
	public List<Target> getAttack2_TargetsAllowed() {
		return attack2_TargetsAllowed;
	}
	public void setAttack2_TargetsAllowed(final List<Target> attack2_TargetsAllowed) {
		this.attack2_TargetsAllowed = attack2_TargetsAllowed;
	}
	public CombatSound getAttack2_WeaponSound() {
		return attack2_WeaponSound;
	}
	public void setAttack2_WeaponSound(final CombatSound attack2_WeaponSound) {
		this.attack2_WeaponSound = attack2_WeaponSound;
	}
	public WeaponType getAttack2_WeaponType() {
		return attack2_WeaponType;
	}
	public void setAttack2_WeaponType(final WeaponType attack2_WeaponType) {
		this.attack2_WeaponType = attack2_WeaponType;
	}
	public AttackBits getAttacksEnabled() {
		return attacksEnabled;
	}
	public void setAttacksEnabled(final AttackBits attacksEnabled) {
		this.attacksEnabled = attacksEnabled;
	}
	public int getBuildTime() {
		return buildTime;
	}
	public void setBuildTime(final int buildTime) {
		this.buildTime = buildTime;
	}
	public int getButtonPosition_X() {
		return buttonPosition_X;
	}
	public void setButtonPosition_X(final int buttonPosition_X) {
		this.buttonPosition_X = buttonPosition_X;
	}
	public int getButtonPosition_Y() {
		return buttonPosition_Y;
	}
	public void setButtonPosition_Y(final int buttonPosition_Y) {
		this.buttonPosition_Y = buttonPosition_Y;
	}
	public boolean getCanDropItemsOnDeath() {
		return canDropItemsOnDeath;
	}
	public void setCanDropItemsOnDeath(final boolean canDropItemsOnDeath) {
		this.canDropItemsOnDeath = canDropItemsOnDeath;
	}
	public boolean getCanFlee() {
		return canFlee;
	}
	public void setCanFlee(final boolean canFlee) {
		this.canFlee = canFlee;
	}
	public boolean getCategorization_Campaign() {
		return categorization_Campaign;
	}
	public void setCategorization_Campaign(final boolean categorization_Campaign) {
		this.categorization_Campaign = categorization_Campaign;
	}
	public boolean getCategorization_Special() {
		return categorization_Special;
	}
	public void setCategorization_Special(final boolean categorization_Special) {
		this.categorization_Special = categorization_Special;
	}
	public float getCollisionSize() {
		return collisionSize;
	}
	public void setCollisionSize(final float collisionSize) {
		this.collisionSize = collisionSize;
	}
	public float getDeathTime_seconds() {
		return deathTime_seconds;
	}
	public void setDeathTime_seconds(final float deathTime_seconds) {
		this.deathTime_seconds = deathTime_seconds;
	}
	public DeathType getDeathType() {
		return deathType;
	}
	public void setDeathType(final DeathType deathType) {
		this.deathType = deathType;
	}
	public Ability getDefaultActiveAbility() {
		return defaultActiveAbility;
	}
	public void setDefaultActiveAbility(final Ability defaultActiveAbility) {
		this.defaultActiveAbility = defaultActiveAbility;
	}
	public int getDefenseBase() {
		return defenseBase;
	}
	public void setDefenseBase(final int defenseBase) {
		this.defenseBase = defenseBase;
	}
	public DefenseType getDefenseType() {
		return defenseType;
	}
	public void setDefenseType(final DefenseType defenseType) {
		this.defenseType = defenseType;
	}
	public int getDefenseUpgradeBonus() {
		return defenseUpgradeBonus;
	}
	public void setDefenseUpgradeBonus(final int defenseUpgradeBonus) {
		this.defenseUpgradeBonus = defenseUpgradeBonus;
	}
	public List<Unit> getDependencyEquivalents() {
		return dependencyEquivalents;
	}
	public void setDependencyEquivalents(final List<Unit> dependencyEquivalents) {
		this.dependencyEquivalents = dependencyEquivalents;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(final String description) {
		this.description = description;
	}
	public boolean getDisplayAsNeutralHostile() {
		return displayAsNeutralHostile;
	}
	public void setDisplayAsNeutralHostile(final boolean displayAsNeutralHostile) {
		this.displayAsNeutralHostile = displayAsNeutralHostile;
	}
	public int getElevation_SamplePoints() {
		return elevation_SamplePoints;
	}
	public void setElevation_SamplePoints(final int elevation_SamplePoints) {
		this.elevation_SamplePoints = elevation_SamplePoints;
	}
	public float getElevation_SampleRadius() {
		return elevation_SampleRadius;
	}
	public void setElevation_SampleRadius(final float elevation_SampleRadius) {
		this.elevation_SampleRadius = elevation_SampleRadius;
	}
	public float getFogOfWar_SampleRadius() {
		return fogOfWar_SampleRadius;
	}
	public void setFogOfWar_SampleRadius(final float fogOfWar_SampleRadius) {
		this.fogOfWar_SampleRadius = fogOfWar_SampleRadius;
	}
	public int getFoodCost() {
		return foodCost;
	}
	public void setFoodCost(final int foodCost) {
		this.foodCost = foodCost;
	}
	public int getFoodProduced() {
		return foodProduced;
	}
	public void setFoodProduced(final int foodProduced) {
		this.foodProduced = foodProduced;
	}
	public int getGoldBountyAwarded_Base() {
		return goldBountyAwarded_Base;
	}
	public void setGoldBountyAwarded_Base(final int goldBountyAwarded_Base) {
		this.goldBountyAwarded_Base = goldBountyAwarded_Base;
	}
	public int getGoldBountyAwarded_NumberOfDice() {
		return goldBountyAwarded_NumberOfDice;
	}
	public void setGoldBountyAwarded_NumberOfDice(final int goldBountyAwarded_NumberOfDice) {
		this.goldBountyAwarded_NumberOfDice = goldBountyAwarded_NumberOfDice;
	}
	public int getGoldBountyAwarded_SidesPerDie() {
		return goldBountyAwarded_SidesPerDie;
	}
	public void setGoldBountyAwarded_SidesPerDie(final int goldBountyAwarded_SidesPerDie) {
		this.goldBountyAwarded_SidesPerDie = goldBountyAwarded_SidesPerDie;
	}
	public int getGoldCost() {
		return goldCost;
	}
	public void setGoldCost(final int goldCost) {
		this.goldCost = goldCost;
	}
	public boolean getHasTilesetSpecificData() {
		return hasTilesetSpecificData;
	}
	public void setHasTilesetSpecificData(final boolean hasTilesetSpecificData) {
		this.hasTilesetSpecificData = hasTilesetSpecificData;
	}
	public boolean getHasWaterShadow() {
		return hasWaterShadow;
	}
	public void setHasWaterShadow(final boolean hasWaterShadow) {
		this.hasWaterShadow = hasWaterShadow;
	}
	public float getHeight() {
		return height;
	}
	public void setHeight(final float height) {
		this.height = height;
	}
	public float getHeightMinimum() {
		return heightMinimum;
	}
	public void setHeightMinimum(final float heightMinimum) {
		this.heightMinimum = heightMinimum;
	}
	public boolean getHideMinimapDisplay() {
		return hideMinimapDisplay;
	}
	public void setHideMinimapDisplay(final boolean hideMinimapDisplay) {
		this.hideMinimapDisplay = hideMinimapDisplay;
	}
	public int getHitPointsMaximum_Base() {
		return hitPointsMaximum_Base;
	}
	public void setHitPointsMaximum_Base(final int hitPointsMaximum_Base) {
		this.hitPointsMaximum_Base = hitPointsMaximum_Base;
	}
	public float getHitPointsRegenerationRate() {
		return hitPointsRegenerationRate;
	}
	public void setHitPointsRegenerationRate(final float hitPointsRegenerationRate) {
		this.hitPointsRegenerationRate = hitPointsRegenerationRate;
	}
	public RegenType getHitPointsRegenerationType() {
		return hitPointsRegenerationType;
	}
	public void setHitPointsRegenerationType(final RegenType hitPointsRegenerationType) {
		this.hitPointsRegenerationType = hitPointsRegenerationType;
	}
	public char getHotkey() {
		return hotkey;
	}
	public void setHotkey(final char hotkey) {
		this.hotkey = hotkey;
	}
	public Icon getIcon_GameInterface() {
		return icon_GameInterface;
	}
	public void setIcon_GameInterface(final Icon icon_GameInterface) {
		this.icon_GameInterface = icon_GameInterface;
	}
	public Icon getIcon_ScoreScreen() {
		return icon_ScoreScreen;
	}
	public void setIcon_ScoreScreen(final Icon icon_ScoreScreen) {
		this.icon_ScoreScreen = icon_ScoreScreen;
	}
	public boolean getIsABuilding() {
		return isABuilding;
	}
	public void setIsABuilding(final boolean isABuilding) {
		this.isABuilding = isABuilding;
	}
	public List<Item> getItemsSold() {
		return itemsSold;
	}
	public void setItemsSold(final List<Item> itemsSold) {
		this.itemsSold = itemsSold;
	}
	public int getLoopingFadeInRate() {
		return loopingFadeInRate;
	}
	public void setLoopingFadeInRate(final int loopingFadeInRate) {
		this.loopingFadeInRate = loopingFadeInRate;
	}
	public int getLoopingFadeOutRate() {
		return loopingFadeOutRate;
	}
	public void setLoopingFadeOutRate(final int loopingFadeOutRate) {
		this.loopingFadeOutRate = loopingFadeOutRate;
	}
	public int getLumberBountyAwarded_Base() {
		return lumberBountyAwarded_Base;
	}
	public void setLumberBountyAwarded_Base(final int lumberBountyAwarded_Base) {
		this.lumberBountyAwarded_Base = lumberBountyAwarded_Base;
	}
	public int getLumberBountyAwarded_NumberOfDice() {
		return lumberBountyAwarded_NumberOfDice;
	}
	public void setLumberBountyAwarded_NumberOfDice(final int lumberBountyAwarded_NumberOfDice) {
		this.lumberBountyAwarded_NumberOfDice = lumberBountyAwarded_NumberOfDice;
	}
	public int getLumberBountyAwarded_SidesPerDie() {
		return lumberBountyAwarded_SidesPerDie;
	}
	public void setLumberBountyAwarded_SidesPerDie(final int lumberBountyAwarded_SidesPerDie) {
		this.lumberBountyAwarded_SidesPerDie = lumberBountyAwarded_SidesPerDie;
	}
	public int getLumberCost() {
		return lumberCost;
	}
	public void setLumberCost(final int lumberCost) {
		this.lumberCost = lumberCost;
	}
	public int getManaInitialAmount() {
		return manaInitialAmount;
	}
	public void setManaInitialAmount(final int manaInitialAmount) {
		this.manaInitialAmount = manaInitialAmount;
	}
	public int getManaMaximum() {
		return manaMaximum;
	}
	public void setManaMaximum(final int manaMaximum) {
		this.manaMaximum = manaMaximum;
	}
	public float getManaRegeneration() {
		return manaRegeneration;
	}
	public void setManaRegeneration(final float manaRegeneration) {
		this.manaRegeneration = manaRegeneration;
	}
	public float getMaximumPitchAngle_degrees() {
		return maximumPitchAngle_degrees;
	}
	public void setMaximumPitchAngle_degrees(final float maximumPitchAngle_degrees) {
		this.maximumPitchAngle_degrees = maximumPitchAngle_degrees;
	}
	public float getMaximumRollAngle_degrees() {
		return maximumRollAngle_degrees;
	}
	public void setMaximumRollAngle_degrees(final float maximumRollAngle_degrees) {
		this.maximumRollAngle_degrees = maximumRollAngle_degrees;
	}
	public int getMinimumAttackRange() {
		return minimumAttackRange;
	}
	public void setMinimumAttackRange(final int minimumAttackRange) {
		this.minimumAttackRange = minimumAttackRange;
	}
	public Model getModelFile() {
		return modelFile;
	}
	public void setModelFile(final Model modelFile) {
		this.modelFile = modelFile;
	}
	public VersionFlags getModelFile_ExtraVersions() {
		return modelFile_ExtraVersions;
	}
	public void setModelFile_ExtraVersions(final VersionFlags modelFile_ExtraVersions) {
		this.modelFile_ExtraVersions = modelFile_ExtraVersions;
	}
	public SoundLabel getMovement() {
		return movement;
	}
	public void setMovement(final SoundLabel movement) {
		this.movement = movement;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public String getName_EditorSuffix() {
		return name_EditorSuffix;
	}
	public void setName_EditorSuffix(final String name_EditorSuffix) {
		this.name_EditorSuffix = name_EditorSuffix;
	}
	public List<Ability> getNormal() {
		return normal;
	}
	public void setNormal(final List<Ability> normal) {
		this.normal = normal;
	}
	public float getOccluderHeight() {
		return occluderHeight;
	}
	public void setOccluderHeight(final float occluderHeight) {
		this.occluderHeight = occluderHeight;
	}
	public int getOrientationInterpolation() {
		return orientationInterpolation;
	}
	public void setOrientationInterpolation(final int orientationInterpolation) {
		this.orientationInterpolation = orientationInterpolation;
	}
	public boolean getPlaceableInEditor() {
		return placeableInEditor;
	}
	public void setPlaceableInEditor(final boolean placeableInEditor) {
		this.placeableInEditor = placeableInEditor;
	}
	public int getPointValue() {
		return pointValue;
	}
	public void setPointValue(final int pointValue) {
		this.pointValue = pointValue;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(final int priority) {
		this.priority = priority;
	}
	public float getProjectileImpact_Z() {
		return projectileImpact_Z;
	}
	public void setProjectileImpact_Z(final float projectileImpact_Z) {
		this.projectileImpact_Z = projectileImpact_Z;
	}
	public float getProjectileImpact_Z_Swimming() {
		return projectileImpact_Z_Swimming;
	}
	public void setProjectileImpact_Z_Swimming(final float projectileImpact_Z_Swimming) {
		this.projectileImpact_Z_Swimming = projectileImpact_Z_Swimming;
	}
	public float getProjectileLaunch_X() {
		return projectileLaunch_X;
	}
	public void setProjectileLaunch_X(final float projectileLaunch_X) {
		this.projectileLaunch_X = projectileLaunch_X;
	}
	public float getProjectileLaunch_Y() {
		return projectileLaunch_Y;
	}
	public void setProjectileLaunch_Y(final float projectileLaunch_Y) {
		this.projectileLaunch_Y = projectileLaunch_Y;
	}
	public float getProjectileLaunch_Z() {
		return projectileLaunch_Z;
	}
	public void setProjectileLaunch_Z(final float projectileLaunch_Z) {
		this.projectileLaunch_Z = projectileLaunch_Z;
	}
	public float getProjectileLaunch_Z_Swimming() {
		return projectileLaunch_Z_Swimming;
	}
	public void setProjectileLaunch_Z_Swimming(final float projectileLaunch_Z_Swimming) {
		this.projectileLaunch_Z_Swimming = projectileLaunch_Z_Swimming;
	}
	public float getPropulsionWindow_degrees() {
		return propulsionWindow_degrees;
	}
	public void setPropulsionWindow_degrees(final float propulsionWindow_degrees) {
		this.propulsionWindow_degrees = propulsionWindow_degrees;
	}
	public UnitRace getRace() {
		return race;
	}
	public void setRace(final UnitRace race) {
		this.race = race;
	}
	public SoundLabel getRandom() {
		return random;
	}
	public void setRandom(final SoundLabel random) {
		this.random = random;
	}
	public int getRepairGoldCost() {
		return repairGoldCost;
	}
	public void setRepairGoldCost(final int repairGoldCost) {
		this.repairGoldCost = repairGoldCost;
	}
	public int getRepairLumberCost() {
		return repairLumberCost;
	}
	public void setRepairLumberCost(final int repairLumberCost) {
		this.repairLumberCost = repairLumberCost;
	}
	public int getRepairTime() {
		return repairTime;
	}
	public void setRepairTime(final int repairTime) {
		this.repairTime = repairTime;
	}
	public List<String> getRequiredAnimationNames() {
		return requiredAnimationNames;
	}
	public void setRequiredAnimationNames(final List<String> requiredAnimationNames) {
		this.requiredAnimationNames = requiredAnimationNames;
	}
	public List<String> getRequiredAnimationNames_Attachments() {
		return requiredAnimationNames_Attachments;
	}
	public void setRequiredAnimationNames_Attachments(final List<String> requiredAnimationNames_Attachments) {
		this.requiredAnimationNames_Attachments = requiredAnimationNames_Attachments;
	}
	public List<String> getRequiredAttachmentLinkNames() {
		return requiredAttachmentLinkNames;
	}
	public void setRequiredAttachmentLinkNames(final List<String> requiredAttachmentLinkNames) {
		this.requiredAttachmentLinkNames = requiredAttachmentLinkNames;
	}
	public List<String> getRequiredBoneNames() {
		return requiredBoneNames;
	}
	public void setRequiredBoneNames(final List<String> requiredBoneNames) {
		this.requiredBoneNames = requiredBoneNames;
	}
	public List<Tech> getRequirements() {
		return requirements;
	}
	public void setRequirements(final List<Tech> requirements) {
		this.requirements = requirements;
	}
	public List<int> getRequirements_Levels() {
		return requirements_Levels;
	}
	public void setRequirements_Levels(List<int> requirements_Levels) {
		this.requirements_Levels = requirements_Levels;
	}
	public boolean getScaleProjectiles() {
		return scaleProjectiles;
	}
	public void setScaleProjectiles(final boolean scaleProjectiles) {
		this.scaleProjectiles = scaleProjectiles;
	}
	public float getScalingValue() {
		return scalingValue;
	}
	public void setScalingValue(final float scalingValue) {
		this.scalingValue = scalingValue;
	}
	public float getSelectionCircle_Height() {
		return selectionCircle_Height;
	}
	public void setSelectionCircle_Height(final float selectionCircle_Height) {
		this.selectionCircle_Height = selectionCircle_Height;
	}
	public boolean getSelectionCircleOnWater() {
		return selectionCircleOnWater;
	}
	public void setSelectionCircleOnWater(final boolean selectionCircleOnWater) {
		this.selectionCircleOnWater = selectionCircleOnWater;
	}
	public float getSelectionScale() {
		return selectionScale;
	}
	public void setSelectionScale(final float selectionScale) {
		this.selectionScale = selectionScale;
	}
	public ShadowImage getShadowImage_Unit() {
		return shadowImage_Unit;
	}
	public void setShadowImage_Unit(final ShadowImage shadowImage_Unit) {
		this.shadowImage_Unit = shadowImage_Unit;
	}
	public float getShadowImage_CenterX() {
		return shadowImage_CenterX;
	}
	public void setShadowImage_CenterX(final float shadowImage_CenterX) {
		this.shadowImage_CenterX = shadowImage_CenterX;
	}
	public float getShadowImage_CenterY() {
		return shadowImage_CenterY;
	}
	public void setShadowImage_CenterY(final float shadowImage_CenterY) {
		this.shadowImage_CenterY = shadowImage_CenterY;
	}
	public float getShadowImage_Height() {
		return shadowImage_Height;
	}
	public void setShadowImage_Height(final float shadowImage_Height) {
		this.shadowImage_Height = shadowImage_Height;
	}
	public float getShadowImage_Width() {
		return shadowImage_Width;
	}
	public void setShadowImage_Width(final float shadowImage_Width) {
		this.shadowImage_Width = shadowImage_Width;
	}
	public ShadowTexture getShadowTexture_Building() {
		return shadowTexture_Building;
	}
	public void setShadowTexture_Building(final ShadowTexture shadowTexture_Building) {
		this.shadowTexture_Building = shadowTexture_Building;
	}
	public int getSightRadius_Day() {
		return sightRadius_Day;
	}
	public void setSightRadius_Day(final int sightRadius_Day) {
		this.sightRadius_Day = sightRadius_Day;
	}
	public int getSightRadius_Night() {
		return sightRadius_Night;
	}
	public void setSightRadius_Night(final int sightRadius_Night) {
		this.sightRadius_Night = sightRadius_Night;
	}
	public boolean getSleeps() {
		return sleeps;
	}
	public void setSleeps(final boolean sleeps) {
		this.sleeps = sleeps;
	}
	public List<Model> getSpecial() {
		return special;
	}
	public void setSpecial(final List<Model> special) {
		this.special = special;
	}
	public int getSpeedBase() {
		return speedBase;
	}
	public void setSpeedBase(final int speedBase) {
		this.speedBase = speedBase;
	}
	public int getSpeedMaximum() {
		return speedMaximum;
	}
	public void setSpeedMaximum(final int speedMaximum) {
		this.speedMaximum = speedMaximum;
	}
	public int getSpeedMinimum() {
		return speedMinimum;
	}
	public void setSpeedMinimum(final int speedMinimum) {
		this.speedMinimum = speedMinimum;
	}
	public int getStockMaximum() {
		return stockMaximum;
	}
	public void setStockMaximum(final int stockMaximum) {
		this.stockMaximum = stockMaximum;
	}
	public int getStockReplenishInterval() {
		return stockReplenishInterval;
	}
	public void setStockReplenishInterval(final int stockReplenishInterval) {
		this.stockReplenishInterval = stockReplenishInterval;
	}
	public int getStockStartDelay() {
		return stockStartDelay;
	}
	public void setStockStartDelay(final int stockStartDelay) {
		this.stockStartDelay = stockStartDelay;
	}
	public List<Model> getTarget() {
		return target;
	}
	public void setTarget(final List<Model> target) {
		this.target = target;
	}
	public List<Target> getTargetedAs() {
		return targetedAs;
	}
	public void setTargetedAs(final List<Target> targetedAs) {
		this.targetedAs = targetedAs;
	}
	public TeamColor getTeamColor() {
		return teamColor;
	}
	public void setTeamColor(final TeamColor teamColor) {
		this.teamColor = teamColor;
	}
	public List<Tileset> getTilesets() {
		return tilesets;
	}
	public void setTilesets(final List<Tileset> tilesets) {
		this.tilesets = tilesets;
	}
	public int getTintingColor1_Red() {
		return tintingColor1_Red;
	}
	public void setTintingColor1_Red(final int tintingColor1_Red) {
		this.tintingColor1_Red = tintingColor1_Red;
	}
	public int getTintingColor2_Green() {
		return tintingColor2_Green;
	}
	public void setTintingColor2_Green(final int tintingColor2_Green) {
		this.tintingColor2_Green = tintingColor2_Green;
	}
	public int getTintingColor3_Blue() {
		return tintingColor3_Blue;
	}
	public void setTintingColor3_Blue(final int tintingColor3_Blue) {
		this.tintingColor3_Blue = tintingColor3_Blue;
	}
	public String getTooltip_Basic() {
		return tooltip_Basic;
	}
	public void setTooltip_Basic(final String tooltip_Basic) {
		this.tooltip_Basic = tooltip_Basic;
	}
	public String getTooltip_Extended() {
		return tooltip_Extended;
	}
	public void setTooltip_Extended(final String tooltip_Extended) {
		this.tooltip_Extended = tooltip_Extended;
	}
	public float getTurnRate() {
		return turnRate;
	}
	public void setTurnRate(final float turnRate) {
		this.turnRate = turnRate;
	}
	public MoveType getType() {
		return type;
	}
	public void setType(final MoveType type) {
		this.type = type;
	}
	public UnitClass getUnitClassification() {
		return unitClassification;
	}
	public void setUnitClassification(final UnitClass unitClassification) {
		this.unitClassification = unitClassification;
	}
	public UnitSound getUnitSoundSet() {
		return unitSoundSet;
	}
	public void setUnitSoundSet(final UnitSound unitSoundSet) {
		this.unitSoundSet = unitSoundSet;
	}
	public List<Unit> getUnitsSold() {
		return unitsSold;
	}
	public void setUnitsSold(final List<Unit> unitsSold) {
		this.unitsSold = unitsSold;
	}
	public List<Upgrade> getUpgradesUsed() {
		return upgradesUsed;
	}
	public void setUpgradesUsed(final List<Upgrade> upgradesUsed) {
		this.upgradesUsed = upgradesUsed;
	}
	public boolean getUseClickHelper() {
		return useClickHelper;
	}
	public void setUseClickHelper(final boolean useClickHelper) {
		this.useClickHelper = useClickHelper;
	}
	public boolean getUseExtendedLineOfSight() {
		return useExtendedLineOfSight;
	}
	public void setUseExtendedLineOfSight(final boolean useExtendedLineOfSight) {
		this.useExtendedLineOfSight = useExtendedLineOfSight;
	}

	public List<Unit> getStructuresBuilt() {
		return null;
	}
}