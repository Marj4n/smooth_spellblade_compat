package org.marj4n.smooth_spellblade_compat.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Tiny bytecode adapter for the handful of V1 references that cannot be expressed as
 * normal Java shims because the V2 class still owns the same member name with a new descriptor.
 */
public final class SmoothSpellbladeCompatMixinPlugin implements IMixinConfigPlugin {
    private static final String RELEASE = "net/spell_engine/api/spell/Spell$Release";
    private static final String PROJECTILE = "net/spell_engine/entity/SpellProjectile";
    private static final String SPELL_WEAPON = "net/spell_engine/api/item/weapon/SpellWeaponItem";
    private static final String CASTER = "net/spell_engine/internals/casting/SpellCasterEntity";
    private static final String SPELL_SCHOOL = "net/spell_power/api/SpellSchool";
    private static final String TARGET_HELPER = "net/spell_engine/utils/TargetHelper";
    private static final String SOUND_HELPER = "net/spell_engine/utils/SoundHelper";
    private static final String LEGACY_SOUND = "Lnet/spell_engine/api/spell/Sound;";
    private static final String LEGACY_TARGET_ACCESS = "org/marj4n/smooth_spellblade_compat/compat/LegacyTargetAccess";

    @Override public void onLoad(String mixinPackage) { }
    @Override public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FabricLoader.getInstance().isModLoaded("spellbladenext");
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    @Override public List<String> getMixins() { return null; }
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if ("net.spell_engine.utils.AnimationHelper".equals(targetClassName)
                && mixinClassName.endsWith("AnimationHelperLegacyMixin")) {
            injectLegacyAnimationOverload(targetClass);
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        patch(targetClass);
    }

    private static void patch(ClassNode clazz) {
        for (MethodNode method : clazz.methods) {
            patchMethod(method);
        }
    }

    private static void patchMethod(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();

            if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD) {
                if (RELEASE.equals(field.owner)) {
                    if ("animation".equals(field.name) && "Ljava/lang/String;".equals(field.desc)) {
                        method.instructions.set(field, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "org/marj4n/smooth_spellblade_compat/compat/LegacySpellAccess",
                                "releaseAnimation",
                                "(Lnet/spell_engine/api/spell/Spell$Release;)Ljava/lang/String;",
                                false));
                    } else if ("sound".equals(field.name) && LEGACY_SOUND.equals(field.desc)) {
                        method.instructions.set(field, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "org/marj4n/smooth_spellblade_compat/compat/LegacySpellAccess",
                                "releaseSound",
                                "(Lnet/spell_engine/api/spell/Spell$Release;)Lnet/spell_engine/api/spell/Sound;",
                                false));
                    }
                } else if (SPELL_SCHOOL.equals(field.owner) && "attribute".equals(field.name)) {
                    // Spell Power V1 exposed `SpellSchool.attribute` directly.
                    // 1.6.x stores an attribute RegistryEntry / owned attribute instead.
                    method.instructions.set(field, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/marj4n/smooth_spellblade_compat/compat/LegacySpellPowerAccess",
                            "attribute",
                            Type.getMethodDescriptor(Type.getType(field.desc), Type.getObjectType(SPELL_SCHOOL)),
                            false));
                }
            } else if (insn instanceof MethodInsnNode call) {
                if (isLegacyTargetHelperCall(call)) {
                    // Spellblades 2.4 calls TargetHelper methods removed in Spell Engine 1.10.x.
                    // Rewriting the call site is safer than trying to merge new public static
                    // methods into TargetHelper (Mixin 0.8.7 rejects that pattern).
                    call.owner = LEGACY_TARGET_ACCESS;
                    call.itf = false;
                } else if (isLegacySoundHelperCall(call)) {
                    call.owner = "org/marj4n/smooth_spellblade_compat/compat/LegacySoundAccess";
                    call.itf = false;
                } else if (call.getOpcode() == Opcodes.INVOKEINTERFACE && CASTER.equals(call.owner)
                        && "getCooldownManager".equals(call.name)
                        && "()Lnet/spell_engine/internals/SpellCooldownManager;".equals(call.desc)) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = "org/marj4n/smooth_spellblade_compat/compat/LegacyCastingAccess";
                    call.name = "cooldownManager";
                    call.desc = "(Lnet/spell_engine/internals/casting/SpellCasterEntity;)Lnet/spell_engine/internals/SpellCooldownManager;";
                    call.itf = false;
                } else if (call.getOpcode() == Opcodes.INVOKEINTERFACE && CASTER.equals(call.owner)
                        && "setSpellCastProcess".equals(call.name)) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = "org/marj4n/smooth_spellblade_compat/compat/LegacyCastingAccess";
                    call.name = "setSpellCastProcess";
                    call.desc = "(Lnet/spell_engine/internals/casting/SpellCasterEntity;Lnet/spell_engine/internals/casting/SpellCast$Process;)V";
                    call.itf = false;
                } else if (isLegacySpellSchoolConstructor(call)) {
                    // V1 SpellSchool ctor ended in EntityAttribute. Modern Spell Power
                    // uses RegistryEntry<EntityAttribute> for externally-owned attributes.
                    removeMatchingNewAndDup(method, call, SPELL_SCHOOL);
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = "org/marj4n/smooth_spellblade_compat/compat/LegacySpellPowerAccess";
                    call.name = "createSchool";
                    Type[] args = Type.getArgumentTypes(call.desc);
                    call.desc = Type.getMethodDescriptor(Type.getObjectType(SPELL_SCHOOL), args);
                    call.itf = false;
                } else if (isLegacyProjectileConstructor(call)) {
                    // V1: new SpellProjectile(world,caster,x,y,z,behaviour,spellId,target,context,perks)
                    removeMatchingNewAndDup(method, call, PROJECTILE);
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = "org/marj4n/smooth_spellblade_compat/compat/LegacyProjectileFactory";
                    call.name = "create";
                    Type[] args = Type.getArgumentTypes(call.desc);
                    call.desc = Type.getMethodDescriptor(Type.getObjectType(PROJECTILE), args);
                    call.itf = false;
                } else if (call.getOpcode() == Opcodes.INVOKESPECIAL && SPELL_WEAPON.equals(call.owner)
                        && "<init>".equals(call.name)) {
                    Type[] args = Type.getArgumentTypes(call.desc);
                    // V1 had (ToolMaterial, int, float, Settings); V2 kept (ToolMaterial, Settings).
                    if (args.length == 4 && args[1].getSort() == Type.INT && args[2].getSort() == Type.FLOAT) {
                        int local = method.maxLocals++;
                        InsnList adapt = new InsnList();
                        adapt.add(new VarInsnNode(Opcodes.ASTORE, local)); // Settings
                        adapt.add(new InsnNode(Opcodes.POP));              // float attack speed
                        adapt.add(new InsnNode(Opcodes.POP));              // int attack damage
                        adapt.add(new VarInsnNode(Opcodes.ALOAD, local));
                        method.instructions.insertBefore(call, adapt);
                        call.desc = Type.getMethodDescriptor(Type.VOID_TYPE, args[0], args[3]);
                    }
                }
            }

            insn = next;
        }
    }



    private static void injectLegacyAnimationOverload(ClassNode targetClass) {
        String playerDesc = FabricLoader.getInstance().isDevelopmentEnvironment()
                ? "Lnet/minecraft/entity/player/PlayerEntity;"
                : "Lnet/minecraft/class_1657;";
        String legacyDesc = "(" + playerDesc
                + "Ljava/util/Collection;"
                + "Lnet/spell_engine/internals/casting/SpellCast$Animation;"
                + "Ljava/lang/String;F)V";

        for (MethodNode method : targetClass.methods) {
            if ("sendAnimation".equals(method.name) && legacyDesc.equals(method.desc)) {
                return;
            }
        }

        MethodNode bridge = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "sendAnimation",
                legacyDesc,
                null,
                null);
        InsnList code = bridge.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new VarInsnNode(Opcodes.ALOAD, 2));
        code.add(new TypeInsnNode(Opcodes.NEW, "net/spell_engine/api/spell/fx/PlayerAnimation"));
        code.add(new InsnNode(Opcodes.DUP));
        code.add(new VarInsnNode(Opcodes.ALOAD, 3));
        code.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "net/spell_engine/api/spell/fx/PlayerAnimation",
                "<init>",
                "(Ljava/lang/String;)V",
                false));
        code.add(new VarInsnNode(Opcodes.FLOAD, 4));
        String modernDesc = "(" + playerDesc
                + "Ljava/util/Collection;"
                + "Lnet/spell_engine/internals/casting/SpellCast$Animation;"
                + "Lnet/spell_engine/api/spell/fx/PlayerAnimation;F)V";
        code.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/spell_engine/utils/AnimationHelper",
                "sendAnimation",
                modernDesc,
                false));
        code.add(new InsnNode(Opcodes.RETURN));
        bridge.maxStack = 7;
        bridge.maxLocals = 5;
        targetClass.methods.add(bridge);
    }

    private static boolean isLegacySoundHelperCall(MethodInsnNode call) {
        return call.getOpcode() == Opcodes.INVOKESTATIC
                && SOUND_HELPER.equals(call.owner)
                && "playSound".equals(call.name)
                && call.desc.endsWith(LEGACY_SOUND + ")V");
    }

    private static boolean isLegacyTargetHelperCall(MethodInsnNode call) {
        if (call.getOpcode() != Opcodes.INVOKESTATIC || !TARGET_HELPER.equals(call.owner)) {
            return false;
        }
        return switch (call.name) {
            case "getRelation", "actionAllowed", "allowedToHurt" -> true;
            case "targetsFromArea" -> call.desc.contains("Spell$Release$Target$Area");
            default -> false;
        };
    }

    private static boolean isLegacySpellSchoolConstructor(MethodInsnNode call) {
        if (call.getOpcode() != Opcodes.INVOKESPECIAL
                || !SPELL_SCHOOL.equals(call.owner)
                || !"<init>".equals(call.name)) {
            return false;
        }
        Type[] args = Type.getArgumentTypes(call.desc);
        if (args.length != 5) {
            return false;
        }
        // V1 final argument was EntityAttribute. In production mappings this is class_1320.
        String last = args[4].getInternalName();
        return "net/minecraft/entity/attribute/EntityAttribute".equals(last)
                || "net/minecraft/class_1320".equals(last);
    }

    private static boolean isLegacyProjectileConstructor(MethodInsnNode call) {
        if (call.getOpcode() != Opcodes.INVOKESPECIAL
                || !PROJECTILE.equals(call.owner)
                || !"<init>".equals(call.name)) {
            return false;
        }

        // Identifier is named in a dev environment and intermediary (class_2960) in production.
        // Accept both so this post-mixin bytecode pass works after remapping too.
        return call.desc.contains("Lnet/minecraft/util/Identifier;")
                || call.desc.contains("Lnet/minecraft/class_2960;");
    }

    private static void removeMatchingNewAndDup(MethodNode method, MethodInsnNode constructor, String owner) {
        for (AbstractInsnNode cursor = constructor.getPrevious(); cursor != null; cursor = cursor.getPrevious()) {
            if (cursor instanceof TypeInsnNode type && type.getOpcode() == Opcodes.NEW && owner.equals(type.desc)) {
                AbstractInsnNode maybeDup = nextOpcode(type);
                if (maybeDup != null && maybeDup.getOpcode() == Opcodes.DUP) method.instructions.remove(maybeDup);
                method.instructions.remove(type);
                return;
            }
        }
    }

    private static AbstractInsnNode nextOpcode(AbstractInsnNode node) {
        AbstractInsnNode cursor = node.getNext();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getNext();
        return cursor;
    }
}
