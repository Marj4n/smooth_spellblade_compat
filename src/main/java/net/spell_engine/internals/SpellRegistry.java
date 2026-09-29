package net.spell_engine.internals;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.*;
import org.marj4n.smooth_spellblade_compat.SmoothSpellbladeCompat;
import org.marj4n.smooth_spellblade_compat.compat.LegacyRuntimeContext;
import org.marj4n.smooth_spellblade_compat.compat.LegacySpellAccess;
import org.marj4n.smooth_spellblade_compat.compat.ModernSpellEntries;

import java.io.Reader;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compatibility implementation of Spell Engine V1's global registry.
 * V2 uses a world dynamic registry, so runtime calls are forwarded there. During
 * Spellblades' static bootstrap (before a world exists) a lightweight spell shell
 * is loaded from the original V1 JSON, enough for status-effect construction.
 */
public final class SpellRegistry {
    public static final Map<Identifier, SpellContainer> book_containers = new HashMap<>();

    private static final Gson GSON = new Gson();
    private static final Map<Identifier, Spell> BOOTSTRAP = new ConcurrentHashMap<>();
    private static final Map<Spell, Identifier> REVERSE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Identifier, SpellPool> POOLS = new ConcurrentHashMap<>();

    private SpellRegistry() { }

    public static Spell getSpell(Identifier id) {
        var world = LegacyRuntimeContext.world();
        if (world != null) {
            RegistryEntry<Spell> entry = ModernSpellEntries.find(world, id);
            if (entry != null) {
                var spell = entry.value();
                hydrateLegacyFields(id, spell);
                REVERSE.put(spell, id);
                return spell;
            }
        }
        return BOOTSTRAP.computeIfAbsent(id, SpellRegistry::bootstrapSpell);
    }

    public static Identifier idOf(Spell spell) {
        return REVERSE.get(spell);
    }

    public static SpellPool spellPool(Identifier id) {
        return POOLS.computeIfAbsent(id, SpellRegistry::loadLegacyPool);
    }

    private static Spell bootstrapSpell(Identifier id) {
        // During Spellblades' static initialization no World/dynamic spell registry exists yet.
        // Load the already-migrated V2 spell definition bundled by this compat mod so objects
        // captured by SpellStatusEffect constructors still have school/cost/impact data later.
        Spell spell = null;
        JsonObject modernRoot = readModernCompatSpellJson(id);
        if (modernRoot != null) {
            try {
                spell = GSON.fromJson(modernRoot, Spell.class);
            } catch (Throwable t) {
                SmoothSpellbladeCompat.LOGGER.warn("Could not bootstrap modern spell {}: {}", id, t.toString());
            }
        }
        if (spell == null) {
            spell = new Spell();
        }

        // Overlay the V1-only fields that Spellblades' bytecode still reads.
        JsonObject legacyRoot = readLegacySpellJson(id);
        if (legacyRoot != null) {
            if (legacyRoot.has("range")) spell.range = legacyRoot.get("range").getAsFloat();
            hydrateLegacyFields(id, spell, legacyRoot);
        }
        REVERSE.put(spell, id);
        return spell;
    }

    private static void hydrateLegacyFields(Identifier id, Spell spell) {
        JsonObject root = readLegacySpellJson(id);
        if (root != null) hydrateLegacyFields(id, spell, root);
    }

    private static void hydrateLegacyFields(Identifier id, Spell spell, JsonObject root) {
        try {
            if (root.has("cast")) {
                Object cast = GSON.fromJson(root.get("cast"), Class.forName("net.spell_engine.api.spell.Spell$Cast"));
                setField(spell, "cast", cast);
            }

            var impacts = spell.impacts != null ? spell.impacts : List.<Spell.Impact>of();
            setField(spell, "impact", impacts.toArray(new Spell.Impact[0]));
            JsonArray oldImpacts = root.has("impact") && root.get("impact").isJsonArray()
                    ? root.getAsJsonArray("impact") : new JsonArray();
            for (int i = 0; i < impacts.size() && i < oldImpacts.size(); i++) {
                var old = oldImpacts.get(i).getAsJsonObject();
                if (old.has("particles")) {
                    var particles = GSON.fromJson(old.get("particles"), ParticleBatch[].class);
                    setField(impacts.get(i), "particles", particles);
                }
            }

            if (root.has("release")) {
                var oldRelease = root.getAsJsonObject("release");
                if (oldRelease.has("target")) {
                    Object target = GSON.fromJson(oldRelease.get("target"), Class.forName("net.spell_engine.api.spell.Spell$Release$Target"));
                    setField(spell.release, "target", target);
                }
                if (oldRelease.has("particles")) {
                    setField(spell.release, "particles", GSON.fromJson(oldRelease.get("particles"), ParticleBatch[].class));
                }
                if (oldRelease.has("animation") && oldRelease.get("animation").isJsonPrimitive()) {
                    LegacySpellAccess.rememberReleaseAnimation(spell.release, oldRelease.get("animation").getAsString());
                }
            }

            if (spell.learn != null && root.has("learn")) {
                var learn = root.getAsJsonObject("learn");
                if (learn.has("tier")) setField(spell.learn, "tier", learn.get("tier").getAsInt());
            }
            REVERSE.put(spell, id);
        } catch (Throwable t) {
            SmoothSpellbladeCompat.LOGGER.warn("Could not hydrate V1 fields for {}: {}", id, t.toString());
        }
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        var field = target.getClass().getField(name);
        field.set(target, value);
    }


    private static JsonObject readModernCompatSpellJson(Identifier id) {
        String path = "data/" + id.getNamespace() + "/spell/" + id.getPath() + ".json";
        return readJsonFromMod(SmoothSpellbladeCompat.MOD_ID, path);
    }

    private static JsonObject readLegacySpellJson(Identifier id) {
        String path = "data/" + id.getNamespace() + "/spells/" + id.getPath() + ".json";
        return readJsonFromMod(modIdForNamespace(id.getNamespace()), path);
    }

    private static SpellPool loadLegacyPool(Identifier id) {
        String path = "data/" + id.getNamespace() + "/spell_pools/" + id.getPath() + ".json";
        JsonObject root = readJsonFromMod(modIdForNamespace(id.getNamespace()), path);
        if (root == null) return SpellPool.empty;
        try {
            List<Identifier> ids = new ArrayList<>();
            if (root.has("spell_ids")) {
                for (JsonElement e : root.getAsJsonArray("spell_ids")) ids.add(new Identifier(e.getAsString()));
            }
            boolean craftable;
            if (root.has("craftable")) {
                craftable = root.get("craftable").getAsBoolean();
            } else {
                craftable = !root.has("creatable_as_spellbook")
                        || root.get("creatable_as_spellbook").getAsBoolean();
            }
            return new SpellPool(ids, List.of(), craftable);
        } catch (Exception e) {
            return SpellPool.empty;
        }
    }

    private static String modIdForNamespace(String namespace) {
        return "spellbladenext".equals(namespace) ? "spellbladenext" : namespace;
    }

    private static JsonObject readJsonFromMod(String modId, String path) {
        try {
            var container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
            if (container == null) return null;
            var file = container.findPath(path).orElse(null);
            if (file == null) return null;
            try (Reader reader = Files.newBufferedReader(file)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
