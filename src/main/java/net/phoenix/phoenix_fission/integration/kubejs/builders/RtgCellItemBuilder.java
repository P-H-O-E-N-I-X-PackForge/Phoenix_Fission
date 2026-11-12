package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.phoenix.phoenix_fission.api.item.RtgCellItem;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public class RtgCellItemBuilder extends ItemBuilder {

    @Setter
    public transient long euPerTick = 8;
    @Setter
    public transient int lifespanSeconds = 3600;

    public RtgCellItemBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public Item createObject() {
        return new RtgCellItem(createItemProperties(), Math.max(1, euPerTick), Math.max(1, lifespanSeconds));
    }
}
