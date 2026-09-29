package net.spell_engine.utils;
public enum TargetHelper$Relation {
    FRIENDLY, SEMI_FRIENDLY, NEUTRAL, HOSTILE, MIXED;
    public static TargetHelper$Relation coalesce(TargetHelper$Relation value, TargetHelper$Relation fallback) {
        return value != null ? value : fallback;
    }
}
