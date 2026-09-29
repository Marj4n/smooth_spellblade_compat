package net.spell_engine.api.item.trinket;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.SpellContainer;
import net.spell_engine.internals.SpellRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Spell Engine V1 spell-book facade, backed by the modern assignment fallback map. */
public final class SpellBooks {
    public static final ArrayList<SpellBookItem> all = new ArrayList<>();

    private SpellBooks() { }

    public static List<SpellBookItem> sorted() {
        return all.stream()
                .sorted(Comparator.comparing(item -> item.getPoolId().toString()))
                .filter(item -> SpellRegistry.spellPool(item.getPoolId()).craftable())
                .toList();
    }

    public static SpellBookItem create(Identifier id) {
        return create(id, SpellContainer.ContentType.MAGIC);
    }

    public static SpellBookItem create(Identifier id, SpellContainer.ContentType type) {
        var legacyContainer = new SpellContainer(type, false, id.toString(), 0, List.of());
        var itemId = itemIdFor(id);

        // Preserve the V1 public map for Spellblades' own bytecode.
        SpellRegistry.book_containers.put(itemId, legacyContainer);

        // Spell Engine 1.10 resolves spell-book fallback containers from this map.
        // Register it here, before server-start assignment loading copies the map.
        net.spell_engine.internals.container.SpellAssignments.book_containers
                .put(itemId, legacyContainer.toModern());

        SpellBookItem book = new SpellBookTrinketItem(id, new FabricItemSettings().maxCount(1));
        all.add(book);
        return book;
    }

    public static Identifier itemIdFor(Identifier id) {
        return new Identifier(id.getNamespace(), id.getPath() + "_spell_book");
    }

    public static void register(SpellBookItem book) {
        var id = itemIdFor(book.getPoolId());
        if (book instanceof Item item && !Registries.ITEM.containsId(id)) {
            Registry.register(Registries.ITEM, id, item);
        }
    }

    public static void createAndRegister(Identifier id, RegistryKey<ItemGroup> group) {
        createAndRegister(id, SpellContainer.ContentType.MAGIC, group);
    }

    public static void createAndRegister(Identifier id, SpellContainer.ContentType type, RegistryKey<ItemGroup> group) {
        var item = create(id, type);
        ItemGroupEvents.modifyEntriesEvent(group).register(content -> content.add(item));
        register(item);
    }
}
