package net.spell_engine.api.spell;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Spell Engine V1 container shape kept for Spellblades 2.4 binary compatibility.
 * Conversion deliberately preserves V1 proxy semantics on the V2 access model.
 */
public class SpellContainer {
    public enum ContentType { MAGIC, ARCHERY }

    public ContentType content = ContentType.MAGIC;
    public boolean is_proxy = false;
    public int max_spell_count = 0;
    public String pool;
    public List<String> spell_ids = List.of();

    public SpellContainer() { }

    public SpellContainer(boolean proxy, String pool, int max, List<String> ids) {
        this(null, proxy, pool, max, ids);
    }

    public SpellContainer(@Nullable ContentType content, boolean proxy, String pool, int max, List<String> ids) {
        if (content != null) this.content = content;
        this.is_proxy = proxy;
        this.pool = pool;
        this.max_spell_count = max;
        this.spell_ids = ids == null ? List.of() : ids;
    }

    public int cappedIndex(int selected) {
        if (spell_ids.isEmpty()) return 0;
        int remainder = selected % spell_ids.size();
        return remainder >= 0 ? remainder : remainder + spell_ids.size();
    }

    public String spellId(int selected) {
        return spell_ids == null || spell_ids.isEmpty() ? null : spell_ids.get(cappedIndex(selected));
    }

    public boolean isValid() {
        if (is_proxy) return true;
        if (max_spell_count < 0) return false;
        return !spell_ids.isEmpty() || (pool != null && !pool.isEmpty());
    }

    public boolean isUsable() {
        return isValid() && !spell_ids.isEmpty();
    }

    public SpellContainer copy() {
        return new SpellContainer(content, is_proxy, pool, max_spell_count, new ArrayList<>(spell_ids));
    }

    /**
     * V1 -> V2 mapping:
     * - a V1 proxy resolves compatible spell sources, so MAGIC/ARCHERY stay resolver access types;
     * - a V1 non-proxy casts only the spells it contains, so it becomes CONTAINED.
     */
    public net.spell_engine.api.spell.container.SpellContainer toModern() {
        net.spell_engine.api.spell.container.SpellContainer.ContentType access;
        if (!is_proxy) {
            access = net.spell_engine.api.spell.container.SpellContainer.ContentType.CONTAINED;
        } else {
            access = content == ContentType.ARCHERY
                    ? net.spell_engine.api.spell.container.SpellContainer.ContentType.ARCHERY
                    : net.spell_engine.api.spell.container.SpellContainer.ContentType.MAGIC;
        }
        return new net.spell_engine.api.spell.container.SpellContainer(
                access,
                "",
                pool == null ? "" : pool,
                "",
                max_spell_count,
                spell_ids,
                0
        );
    }

    public static SpellContainer fromModern(net.spell_engine.api.spell.container.SpellContainer container) {
        if (container == null) return null;

        var access = container.access();
        var legacyContent = access == net.spell_engine.api.spell.container.SpellContainer.ContentType.ARCHERY
                ? ContentType.ARCHERY
                : ContentType.MAGIC;

        // CONTAINED is V2's direct equivalent of V1 non-proxy ownership.
        // Other resolver access types behave as V1 proxies from Spellblades' perspective.
        boolean proxy = access != net.spell_engine.api.spell.container.SpellContainer.ContentType.CONTAINED
                && access != net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE;

        return new SpellContainer(
                legacyContent,
                proxy,
                container.pool(),
                container.max_spell_count(),
                new ArrayList<>(container.spell_ids())
        );
    }
}
