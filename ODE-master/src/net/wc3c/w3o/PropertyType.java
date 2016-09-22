package net.wc3c.w3o;

public enum PropertyType {
    INTEGER(0),
    REAL(1),
    UNREAL(2),
    STRING(3),
    BOOLEAN(4),
    CHAR(5),
    UNIT_LIST(6),
    ITEM_LIST(7),
    REGENERATION_TYPE(8),
    ATTACK_TYPE(9),
    WEAPON_TYPE(10),
    TARGET_TYPE(11),
    MOVE_TYPE(12),
    DEFENSE_TYPE(13),
    PATHING_TEXTURE(14),
    UPGRADE_LIST(15),
    STRING_LIST(16),
    ABILITY_LIST(17),
    HERO_ABILITY_LIST(18),
    MISSILE_ART(19),
    ATTRIBUTE_TYPE(20),
    ATTACK_BITS(21);
    
    private int value;
    
    private PropertyType(final int value) {
        this.value = value;
    }
    
    static PropertyType fromInt(final int value) {
        for (final PropertyType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new RuntimeException("Unknown PropertyType value: " + value);
        //return null;
    }
}
