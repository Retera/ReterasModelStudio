package net.wc3c.ode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.wc3c.slk.SLKFile;
import net.wc3c.util.CharInt;
import net.wc3c.util.Jass;
import net.wc3c.util.Jass.Parameter;
import net.wc3c.util.Jass.Type;
import net.wc3c.w3o.Property;
import net.wc3c.w3o.PropertyType;
import net.wc3c.w3o.W3UFile;
import net.wc3c.w3o.W3UFile.Unit;

class UnitDataExtractor extends Extractor<W3UFile, Unit> {
    /**
     * This is a field ID defined by Blizzard, but we need to load this property for every unit to restrict loading
     * default units to certain races.
     */
    private static final int                             FIELD_ID_RACE         = CharInt.toInt("urac");
    /**
     * This is a field ID thats not used by Blizzard for anything, so we are using it to mark units that are loaded
     * without having been modified.
     */
    private static final int                             FIELD_ID_DEFAULT_UNIT = CharInt.toInt("defu");
    private static final int                             COLUMN_UNIT_RACE      = 3;
    
    private final Hashtable<String, Metadata<UnitField>> metadata              = new Hashtable<>();
    
    @Override
    protected String getCommandString() {
        return "LoadUnitData";
    }
    
    @Override
    protected String getLibraryNameString() {
        return "ExportedUnitData";
    }
    
    @Override
    protected String getLibraryInitializerString() {
        return "InitExportedUnitData";
    }
    
    @Override
    protected Field createField(final Integer id, final String name) {
        return new UnitField(id, name);
    }
    
    @Override
    protected LoadRequest createLoadRequest(final String loadRequestLine) {
        return new UnitLoadRequest(loadRequestLine.split("\\s+"));
    }
    
    @Override
    protected FieldRequest createFieldRequest(final Field field, final LoadRequest request) {
        return new UnitFieldRequest((UnitField) field, (UnitLoadRequest) request);
    }
    
    private class UnitField extends Field {
        
        public UnitField(final Integer id, final String name) {
            super(id, name);
        }
        
        @Override
        public StringBuilder generateLoadFunction() {
            final StringBuilder code = new StringBuilder();
            
            code.append(Jass.function("GetUnitType" + getName(), getType().getJassType(), new Parameter(
                    Type.INTEGER,
                    "id")));
            code.append(Jass.ret(getType().loadFunctionName() + "(info,id," + getIndex() + ")"));
            code.append(Jass.endfunction());
            
            return code;
        }
        
        @Override
        public String generateSaveFunctionCall(final Unit object, final Property<Unit> property) {
            return generateSaveFunctionCall(
                    "'" + CharInt.toString(object.getId()) + "'",
                    Integer.toString(getIndex()),
                    property.getValue());
        }
        
    }
    
    private class UnitFieldRequest extends FieldRequest {
        private boolean      restrictToRaces = false;
        
        private Set<String>  racesToLoad     = new HashSet<String>();
        private Set<Integer> unitIdsToLoad   = new HashSet<Integer>();
        
        @Override
        public void absorb(final LoadRequest request) {
            super.absorb(request);
            
            final UnitLoadRequest req = (UnitLoadRequest) request;
            
            restrictToRaces = restrictToRaces && req.isRestrictedToRaces();
            racesToLoad.addAll(req.getRaces());
            unitIdsToLoad.addAll(req.getUnitIdsToLoad());
        }
        
        public UnitFieldRequest(final UnitField field, final UnitLoadRequest request) {
            super(field, request);
            
            restrictToRaces = request.isRestrictedToRaces();
            racesToLoad = request.getRaces();
            unitIdsToLoad = request.getUnitIdsToLoad();
        }
        
        @Override
        protected boolean canLoad(final Unit object) {
            if (object.getPropertyEx(FIELD_ID_DEFAULT_UNIT) == null) {
                return true;
            } else if (isLoadDefaults()) {
                if (restrictToRaces) {
                    final Property<Unit> property = object.getProperty(FIELD_ID_RACE);
                    return (property != null && racesToLoad.contains(property.getValue()))
                            || (unitIdsToLoad.contains(object.getId()));
                } else {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    private class UnitLoadRequest extends LoadRequest {
        private boolean            restrictToRaces = false;
        
        private final Set<String>  racesToLoad     = new HashSet<String>();
        private final Set<Integer> unitIdsToLoad   = new HashSet<Integer>();
        
        public UnitLoadRequest(final String[] words) {
            super(words);
            
            for (final String word : words) {
                if (word.toLowerCase().startsWith("races=")) {
                    restrictToRaces = true;
                    final String racestrs[] = word.substring(6).split(",");
                    for (final String s : racestrs) {
                        racesToLoad.add(s);
                    }
                } else if (word.toLowerCase().startsWith("rawcodes=")) {
                    final String idstrs[] = word.substring(9).split(",");
                    for (final String s : idstrs) {
                        unitIdsToLoad.add(CharInt.toInt(s));
                    }
                }
            }
        }
        
        public boolean isRestrictedToRaces() {
            return restrictToRaces;
        }
        
        public Set<String> getRaces() {
            return racesToLoad;
        }
        
        public Set<Integer> getUnitIdsToLoad() {
            return unitIdsToLoad;
        }
    }
    
    @Override
    protected void loadMetadata() throws IOException {
        final SLKFile slk = new SLKFile(new File(getMetaPath(), "UnitMetaData.slk"));
        for (int row = 1; row < slk.getHeight(); row += 1) {
            final String fieldID = slk.getCell(0, row).toString();
            final String name = slk.getCell(1, row).toString();
            final String slkName = slk.getCell(2, row).toString();
            final String index = slk.getCell(3, row).toString();
            final String type = slk.getCell(7, row).toString();
            
            final UnitField f = (UnitField) getField(CharInt.toInt(fieldID));
            
            if (f != null) {
                removeFromInvalidFields(f);
                
                Metadata<UnitField> m = metadata.get(slkName);
                if (m == null) {
                    m = new Metadata<UnitField>();
                    metadata.put(slkName, m);
                }
                m.insertIntoFieldMapping(name, Integer.parseInt(index), f);
                
                if (type.equals("int")) {
                    f.setType(FieldType.INTEGER);
                } else if (type.equals("bool")) {
                    f.setType(FieldType.BOOLEAN);
                } else if (type.endsWith("real")) {
                    f.setType(FieldType.REAL);
                } else {
                    f.setType(FieldType.STRING);
                }
            }
        }
    }
    
    @Override
    protected void loadSLK(final File file) throws IOException {
        final SLKFile slk = new SLKFile(file);
        
        // get the name of the SLK file without its extension
        final Metadata<UnitField> data = metadata.get(file.getName().substring(0, file.getName().lastIndexOf('.')));
        
        if (data == null) {
            return;
        }
        
        // Decide which values need to be loaded from the SLKs,
        // as we are only interested in the ones the map requests us to load.
        //
        final ArrayList<List<UnitField>> columnFields = new ArrayList<List<UnitField>>(slk.getWidth());
        for (int i = 0; i < slk.getWidth(); i += 1) {
            columnFields.add(null);
        }
        
        final Object[] firstRow = slk.getRow(0);
        for (int i = 1; i < firstRow.length; i += 1) { // skip the first column (i=0)
            final Object cell = firstRow[i];
            if (cell != null) {
                columnFields.set(i, data.getFields(cell.toString()));
            }
        }
        
        // Now load those that were requested
        //
        for (int i = 1; i < slk.getHeight(); i += 1) {
            final Object[] row = slk.getRow(i);
            
            final Unit unit = getObject(CharInt.toInt(row[0].toString()));
            if (unit == null) {
                continue;
            }
            
            for (int column = 1; column < slk.getWidth(); column += 1) {
                // Column headings might refer to multiple fields (identified uniquely by their field IDs)
                // Example: Buttonpos refers to ubpx and ubpy, in that order. Values are separated by commas.
                
                final List<UnitField> fields = columnFields.get(column);
                if (fields == null) {
                    continue;
                }
                
                final String columnHeading = slk.getCell(column, 0).toString();
                final Object value = row[column];
                
                for (final UnitField f : fields) {
                    if (value == null) {
                        unit.addProperty(new Property<Unit>(
                                f.getId(),
                                f.getType().getPropertyType(),
                                f.getType().getDefaultValue()));
                    } else {
                        unit.addProperty(new Property<Unit>(
                                f.getId(),
                                f.getType().getPropertyType(),
                                data.extractValue(columnHeading, f, value.toString())));
                    }
                }
            }
        }
    }
    
    @Override
    protected void loadProfile(final File profile) throws IOException {
        final Metadata<UnitField> data = metadata.get("Profile");
        
        final BufferedReader br = new BufferedReader(new FileReader(profile));
        boolean inBlock = false;
        Unit unit = null;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.trim();
            if (line.startsWith("//") || line.indexOf("=") == 0) {
                // ignore comments and invalid lines
                // invalid lines are lines where the first char is  '='
            } else if (line.startsWith("[")) {
                // new unit entry
                unit = getObject(CharInt.toInt(line.substring(1, 5)));
                inBlock = unit != null; // verify if its a valid entry
            } else if (inBlock) { // only proceed if the units valid, we dont want entries for invalid units
                final String[] lineParts = line.split("=", 2);
                
                // Column headings might refer to multiple fields (identified uniquely by their field IDs)
                // Example: Buttonpos refers to ubpx and ubpy, in that order. Values are separated by commas.
                final List<UnitField> fields = data.getFields(lineParts[0]);
                if (fields == null) {
                    continue;
                }
                
                for (final UnitField f : fields) {
                    unit.addProperty(new Property<Unit>(f.getId(), f.getType().getPropertyType(), data.extractValue(
                            lineParts[0],
                            f,
                            lineParts[1])));
                }
                
            }
        }
        br.close();
    }
    
    @Override
    protected void loadDefaultObjects() throws IOException {
        final File fi = new File(getDataPath(), "UnitData.slk");
        
        final SLKFile slk = new SLKFile(fi);
        
        // start at index one to skip column headings
        for (int i = 1; i < slk.getHeight(); i += 1) {
            final Object[] row = slk.getRow(i);
            
            final int id = CharInt.toInt(row[0].toString());
            final String race = row[COLUMN_UNIT_RACE].toString();
            
            if (getObject(id) == null) {
                final Unit unit = new Unit(id);
                unit.addProperty(new Property<Unit>(FIELD_ID_DEFAULT_UNIT, PropertyType.BOOLEAN, true));
                unit.addProperty(new Property<Unit>(FIELD_ID_RACE, PropertyType.STRING, race));
                addObject(unit);
            }
        }
    }
    
    UnitDataExtractor(final File odeFolder, final W3UFile w3uFile) throws IOException {
        super(odeFolder, w3uFile);
    }
    
    @Override
    protected void loadFields() {
        addRawcodeMapEntry("udaa", "DefaultActiveAbility");
        addRawcodeMapEntry("uhab", "HeroAbilities");
        addRawcodeMapEntry("uabi", "Abilities");
        addRawcodeMapEntry("utcc", "AllowCustomTeamColor");
        addRawcodeMapEntry("uble", "BlendTime");
        addRawcodeMapEntry("ucbs", "CastBackswing");
        addRawcodeMapEntry("ucpt", "CastPoint");
        addRawcodeMapEntry("urun", "RunSpeed");
        addRawcodeMapEntry("uwal", "WalkSpeed");
        addRawcodeMapEntry("ubpx", "ButtonPositionX");
        addRawcodeMapEntry("ubpy", "ButtonPositionY");
        addRawcodeMapEntry("udtm", "DeathTime");
        addRawcodeMapEntry("uept", "ElevationSamplePoints");
        addRawcodeMapEntry("uerd", "ElevationSampleRadius");
        addRawcodeMapEntry("ufrd", "FogOfWarSampleRadius");
        addRawcodeMapEntry("uubs", "GroundTexture");
        addRawcodeMapEntry("ushr", "HasWaterShadow");
        addRawcodeMapEntry("uico", "Icon");
        addRawcodeMapEntry("ussi", "ScoreScreenIcon");
        addRawcodeMapEntry("umxp", "MaxPitch");
        addRawcodeMapEntry("umxr", "MaxRoll");
        addRawcodeMapEntry("umdl", "Model");
        addRawcodeMapEntry("uver", "ModelExtraVersions");
        addRawcodeMapEntry("uocc", "OcculderHeight");
        addRawcodeMapEntry("uori", "OrientationInterpolation");
        addRawcodeMapEntry("uisz", "SwimProjectileImpactZ");
        addRawcodeMapEntry("uimz", "ProjectileImpactZ");
        addRawcodeMapEntry("ulpx", "ProjectileLaunchX");
        addRawcodeMapEntry("ulsz", "SwimProjectileLaunchZ");
        addRawcodeMapEntry("ulpz", "ProjectileLaunchZ");
        addRawcodeMapEntry("uprw", "PropulsionWindow");
        addRawcodeMapEntry("uani", "RequiredAnimationNames");
        addRawcodeMapEntry("uaap", "RequiredAnimationAttachments");
        addRawcodeMapEntry("ualp", "RequiredAnimationLinkNames");
        addRawcodeMapEntry("ubpr", "RequiredBoneNames");
        addRawcodeMapEntry("uscb", "ScaleProjectiles");
        addRawcodeMapEntry("usca", "Scale");
        addRawcodeMapEntry("uslz", "SelectionZ");
        addRawcodeMapEntry("usew", "SelectionOnWater");
        addRawcodeMapEntry("ussc", "SelectionScale");
        addRawcodeMapEntry("ushu", "ShadowImage");
        addRawcodeMapEntry("ushx", "ShadowImageCenterX");
        addRawcodeMapEntry("ushy", "ShadowImageCenterY");
        addRawcodeMapEntry("ushh", "ShadowImageHeight");
        addRawcodeMapEntry("ushw", "ShadowImageWidth");
        addRawcodeMapEntry("ushb", "ShadowTexture");
        addRawcodeMapEntry("uspa", "SpecialArt");
        addRawcodeMapEntry("utaa", "TargetArt");
        addRawcodeMapEntry("utco", "TeamColor");
        addRawcodeMapEntry("uclr", "RedTint");
        addRawcodeMapEntry("uclg", "GreenTint");
        addRawcodeMapEntry("uclb", "BlueTint");
        addRawcodeMapEntry("ulos", "UseExtendedLineOfSight");
        addRawcodeMapEntry("uacq", "AcquisitionRange");
        addRawcodeMapEntry("uarm", "ArmorType");
        addRawcodeMapEntry("ubs1", "BackswingPoint1");
        addRawcodeMapEntry("udp1", "DamagePoint1");
        addRawcodeMapEntry("ua1f", "AreaOfEffectFull1");
        addRawcodeMapEntry("ua1h", "AreaOfEffectMedium1");
        addRawcodeMapEntry("ua1q", "AreaOfEffectSmall1");
        addRawcodeMapEntry("ua1p", "AreaOfEffectTargets1");
        addRawcodeMapEntry("ua1t", "AttackType1");
        addRawcodeMapEntry("ua1c", "Cooldown1");
        addRawcodeMapEntry("ua1b", "DamageBase1");
        addRawcodeMapEntry("uhd1", "DamageFactorMedium1");
        addRawcodeMapEntry("uqd1", "DamageFactorSmall1");
        addRawcodeMapEntry("udl1", "DamageLossFactor1");
        addRawcodeMapEntry("ua1d", "DamageNumberOfDice1");
        addRawcodeMapEntry("ua1s", "DamageSidesPerDie1");
        addRawcodeMapEntry("usd1", "DamageSpillDistance1");
        addRawcodeMapEntry("usr1", "DamageSpillRadius1");
        addRawcodeMapEntry("udu1", "DamageUpgradeAmount1");
        addRawcodeMapEntry("utc1", "MaximumTargets1");
        addRawcodeMapEntry("uma1", "ProjectileArc1");
        addRawcodeMapEntry("ua1m", "ProjectileArt1");
        addRawcodeMapEntry("umh1", "ProjectileHoming1");
        addRawcodeMapEntry("ua1z", "ProjectileSpeed1");
        addRawcodeMapEntry("ua1r", "Range1");
        addRawcodeMapEntry("urb1", "RangeMotionBuffer1");
        addRawcodeMapEntry("uwu1", "ShowUI1");
        addRawcodeMapEntry("ua1g", "TargetsAllowed1");
        addRawcodeMapEntry("ucs1", "WeaponSound1");
        addRawcodeMapEntry("ua1w", "WeaponType1");
        addRawcodeMapEntry("ubs2", "BackswingPoint2");
        addRawcodeMapEntry("udp2", "DamagePoint2");
        addRawcodeMapEntry("ua2f", "AreaOfEffectFull2");
        addRawcodeMapEntry("ua2h", "AreaOfEffectMedium2");
        addRawcodeMapEntry("ua2q", "AreaOfEffectSmall2");
        addRawcodeMapEntry("ua2p", "AreaOfEffectTargets2");
        addRawcodeMapEntry("ua2t", "AttackType2");
        addRawcodeMapEntry("ua2c", "Cooldown2");
        addRawcodeMapEntry("ua2b", "DamageBase2");
        addRawcodeMapEntry("uhd2", "DamageFactorMedium2");
        addRawcodeMapEntry("uqd2", "DamageFactorSmall2");
        addRawcodeMapEntry("udl2", "DamageLossFactor2");
        addRawcodeMapEntry("ua2d", "DamageNumberOfDice2");
        addRawcodeMapEntry("ua2s", "DamageSidesPerDie2");
        addRawcodeMapEntry("usd2", "DamageSpillDistance2");
        addRawcodeMapEntry("usr2", "DamageSpillRadius2");
        addRawcodeMapEntry("udu2", "DamageUpgradeAmount2");
        addRawcodeMapEntry("utc2", "MaximumTargets2");
        addRawcodeMapEntry("uma2", "ProjectileArc2");
        addRawcodeMapEntry("ua2m", "ProjectileArt2");
        addRawcodeMapEntry("umh2", "ProjectileHoming2");
        addRawcodeMapEntry("ua2z", "ProjectileSpeed2");
        addRawcodeMapEntry("ua2r", "Range2");
        addRawcodeMapEntry("urb2", "RangeMotionBuffer2");
        addRawcodeMapEntry("uwu2", "ShowUI2");
        addRawcodeMapEntry("ua2g", "TargetsAllowed2");
        addRawcodeMapEntry("ucs2", "WeaponSound2");
        addRawcodeMapEntry("ua2w", "WeaponType2");
        addRawcodeMapEntry("uaen", "AttacksEnabled");
        addRawcodeMapEntry("udea", "DeathType");
        addRawcodeMapEntry("udef", "DefenseBase");
        addRawcodeMapEntry("udty", "DefenseType");
        addRawcodeMapEntry("udup", "DefenseUpgradeBonus");
        addRawcodeMapEntry("uamn", "MinimumAttackRange");
        addRawcodeMapEntry("utar", "TargetedAs");
        addRawcodeMapEntry("udro", "DropItemsOnDeath");
        addRawcodeMapEntry("ucam", "CategoryCampaign");
        addRawcodeMapEntry("uspe", "CategorySpecial");
        addRawcodeMapEntry("uhos", "DisplayAsNeutralHostile");
        addRawcodeMapEntry("utss", "HasTilesetSpecificData");
        addRawcodeMapEntry("uine", "PlaceableInEditor");
        addRawcodeMapEntry("util", "Tilesets");
        addRawcodeMapEntry("uuch", "UseClickHelper");
        addRawcodeMapEntry("urpo", "GroupSeparationEnabled");
        addRawcodeMapEntry("urpg", "GroupSeparationGroupNumber");
        addRawcodeMapEntry("urpp", "GroupSeparationParameter");
        addRawcodeMapEntry("urpr", "GroupSeparationPriority");
        addRawcodeMapEntry("umvh", "FlyHeight");
        addRawcodeMapEntry("umvf", "MinimumHeight");
        addRawcodeMapEntry("umvs", "SpeedBase");
        addRawcodeMapEntry("umas", "SpeedMaximum");
        addRawcodeMapEntry("umis", "SpeedMinimum");
        addRawcodeMapEntry("umvr", "TurnRate");
        addRawcodeMapEntry("umvt", "MoveType");
        addRawcodeMapEntry("uabr", "AIPlacementRadius");
        addRawcodeMapEntry("uabt", "AIPlacementType");
        addRawcodeMapEntry("ucol", "CollisionSize");
        addRawcodeMapEntry("upat", "PathingMap");
        addRawcodeMapEntry("upar", "PlacementPreventedBy");
        addRawcodeMapEntry("upap", "PlacementRequires");
        addRawcodeMapEntry("upaw", "PlacementRequiresWaterRadius");
        addRawcodeMapEntry("ubsl", "BuildSound");
        addRawcodeMapEntry("ulfi", "SoundLoopFadeInRate");
        addRawcodeMapEntry("ulfo", "SoundLoopFadeOutRate");
        addRawcodeMapEntry("umsl", "MoveSound");
        addRawcodeMapEntry("ursl", "RandomSound");
        addRawcodeMapEntry("usnd", "SoundSet");
        addRawcodeMapEntry("uagp", "AgilityPerLevel");
        addRawcodeMapEntry("ubld", "BuildTime");
        addRawcodeMapEntry("uibo", "CanBeBuiltOn");
        addRawcodeMapEntry("ucbo", "CanBuildOn");
        addRawcodeMapEntry("ufle", "CanFlee");
        addRawcodeMapEntry("ufoo", "FoodCost");
        addRawcodeMapEntry("ufma", "FoodProduced");
        addRawcodeMapEntry("ufor", "FormationRank");
        addRawcodeMapEntry("ubba", "GoldBountyBase");
        addRawcodeMapEntry("ubdi", "GoldBountyNumberOfDice");
        addRawcodeMapEntry("ubsi", "GoldBountySidesPerDie");
        addRawcodeMapEntry("ugol", "GoldCost");
        addRawcodeMapEntry("uhhd", "HideHeroDeathMessage");
        addRawcodeMapEntry("uhhb", "HideHeroInterfaceIcon");
        addRawcodeMapEntry("uhhm", "HideHeroMinimapDisplay");
        addRawcodeMapEntry("uhom", "HideMinimapDisplay");
        addRawcodeMapEntry("uhpm", "HitPointsMaximum");
        addRawcodeMapEntry("uhpr", "HitPointsRegeneration");
        addRawcodeMapEntry("uhrt", "HitPointsRegenerationType");
        addRawcodeMapEntry("uinp", "IntelligencePerLevel");
        addRawcodeMapEntry("ubdg", "IsABuilding");
        addRawcodeMapEntry("ulev", "Level");
        addRawcodeMapEntry("ulba", "LumberBountyBase");
        addRawcodeMapEntry("ulbd", "LumberBountyNumberOfDice");
        addRawcodeMapEntry("ulbs", "LumberBountySidesPerDie");
        addRawcodeMapEntry("ulum", "LumberCost");
        addRawcodeMapEntry("umpi", "ManaInitialAmount");
        addRawcodeMapEntry("umpm", "ManaMaximum");
        addRawcodeMapEntry("umpr", "ManaRegeneration");
        addRawcodeMapEntry("unbm", "ShowNeutralBuildingIcon");
        addRawcodeMapEntry("unbr", "ValidAsRandomNeutralBuilding");
        addRawcodeMapEntry("upoi", "PointValue");
        addRawcodeMapEntry("upra", "PrimaryAttribute");
        addRawcodeMapEntry("upri", "Priority");
        addRawcodeMapEntry("urac", "Race");
        addRawcodeMapEntry("ugor", "RepairGoldCost");
        addRawcodeMapEntry("ulur", "RepairLumberCost");
        addRawcodeMapEntry("urtm", "RepairTime");
        addRawcodeMapEntry("usid", "SightRadiusDay");
        addRawcodeMapEntry("usin", "SightRadiusNight");
        addRawcodeMapEntry("usle", "Sleeps");
        addRawcodeMapEntry("uagi", "StartingAgility");
        addRawcodeMapEntry("uint", "StartingIntelligence");
        addRawcodeMapEntry("ustr", "StartingStrength");
        addRawcodeMapEntry("usma", "StockMaximum");
        addRawcodeMapEntry("usrg", "StockReplenishInterval");
        addRawcodeMapEntry("usst", "StockStartDelay");
        addRawcodeMapEntry("ustp", "StrengthPerLevel");
        addRawcodeMapEntry("ucar", "TransportedSize");
        addRawcodeMapEntry("utyp", "UnitClassification");
        addRawcodeMapEntry("udep", "DependencyEquivalents");
        addRawcodeMapEntry("urva", "HeroRevivalLocations");
        addRawcodeMapEntry("umki", "ItemsMade");
        addRawcodeMapEntry("usei", "ItemsSold");
        addRawcodeMapEntry("ureq", "Requirements");
        addRawcodeMapEntry("urqa", "RequirementsLevels");
        addRawcodeMapEntry("urq1", "RequirementsTier2");
        addRawcodeMapEntry("urq2", "RequirementsTier3");
        addRawcodeMapEntry("urq3", "RequirementsTier4");
        addRawcodeMapEntry("urq4", "RequirementsTier5");
        addRawcodeMapEntry("urq5", "RequirementsTier6");
        addRawcodeMapEntry("urq6", "RequirementsTier7");
        addRawcodeMapEntry("urq7", "RequirementsTier8");
        addRawcodeMapEntry("urq8", "RequirementsTier9");
        addRawcodeMapEntry("urqc", "RequirementsTiersUsed");
        addRawcodeMapEntry("ubui", "StructuresBuilt");
        addRawcodeMapEntry("ures", "ResearchesAvailable");
        addRawcodeMapEntry("urev", "RevivesDeadHeroes");
        addRawcodeMapEntry("useu", "UnitsSold");
        addRawcodeMapEntry("utra", "UnitsTrained");
        addRawcodeMapEntry("uupt", "UpgradesTo");
        addRawcodeMapEntry("upgr", "UpgradesUsed");
        addRawcodeMapEntry("ides", "Description");
        addRawcodeMapEntry("uhot", "Hotkey");
        addRawcodeMapEntry("unam", "Name");
        addRawcodeMapEntry("unsf", "NameEditorSuffix");
        addRawcodeMapEntry("upro", "ProperNames");
        addRawcodeMapEntry("upru", "ProperNamesUsed");
        addRawcodeMapEntry("uawt", "AwakenTooltip");
        addRawcodeMapEntry("utip", "Tooltip");
        addRawcodeMapEntry("utub", "Ubertip");
        addRawcodeMapEntry("utpr", "ReviveTooltip");
    }
    
}
