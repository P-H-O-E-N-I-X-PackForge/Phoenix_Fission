package net.phoenix.phoenix_fission.gtm8.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public final class HatchIO {

    private HatchIO() {}

    public static boolean fluid(IRecipeCapabilityHolder machine, FluidStack stack, IO type,
                                boolean simulate) {
        if (stack.isEmpty()) return false;

        var handlers = new java.util.ArrayList<IFluidHandler>();
        for (var handler : machine.getCapabilitiesFlat(type, FluidRecipeCapability.CAP)) {
            if (handler instanceof IFluidHandler tank) handlers.add(tank);
        }

        if (type == IO.IN) {
            int available = 0;
            for (var tank : handlers) available += drain(tank, stack, IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (available < stack.getAmount()) return false;
            if (simulate) return true;
            int remaining = stack.getAmount();
            for (var tank : handlers) {
                if (remaining <= 0) break;
                remaining -= drain(tank, new FluidStack(stack, remaining), IFluidHandler.FluidAction.EXECUTE)
                        .getAmount();
            }
            return remaining <= 0;
        }

        int room = 0;
        for (var tank : handlers) room += fill(tank, stack, IFluidHandler.FluidAction.SIMULATE);
        if (room < stack.getAmount()) return false;
        if (simulate) return true;
        int remaining = stack.getAmount();
        for (var tank : handlers) {
            if (remaining <= 0) break;
            remaining -= fill(tank, new FluidStack(stack, remaining), IFluidHandler.FluidAction.EXECUTE);
        }
        return remaining <= 0;
    }

    public static boolean item(IRecipeCapabilityHolder machine, ItemStack stack, IO type,
                               boolean simulate) {
        if (stack.isEmpty()) return false;

        var handlers = new java.util.ArrayList<IItemHandler>();
        for (var handler : machine.getCapabilitiesFlat(type, ItemRecipeCapability.CAP)) {
            if (handler instanceof IItemHandler items) handlers.add(items);
        }

        if (type == IO.IN) {
            int available = 0;
            for (var items : handlers) {
                for (int slot = 0; slot < items.getSlots(); slot++) {
                    ItemStack inSlot = items.getStackInSlot(slot);
                    if (ItemStack.isSameItem(inSlot, stack)) available += inSlot.getCount();
                }
            }
            if (available < stack.getCount()) return false;
            if (simulate) return true;
            int remaining = stack.getCount();
            for (var items : handlers) {
                for (int slot = 0; slot < items.getSlots() && remaining > 0; slot++) {
                    if (!ItemStack.isSameItem(items.getStackInSlot(slot), stack)) continue;
                    remaining -= extract(items, slot, remaining).getCount();
                }
            }
            return remaining <= 0;
        }

        ItemStack left = stack.copy();
        for (var items : handlers) {
            for (int slot = 0; slot < items.getSlots() && !left.isEmpty(); slot++) {
                left = insert(items, slot, left, true);
            }
        }
        if (!left.isEmpty()) return false;
        if (simulate) return true;
        left = stack.copy();
        for (var items : handlers) {
            for (int slot = 0; slot < items.getSlots() && !left.isEmpty(); slot++) {
                left = insert(items, slot, left, false);
            }
        }
        return left.isEmpty();
    }

    private static ItemStack extract(IItemHandler handler, int slot, int amount) {
        return handler instanceof NotifiableItemStackHandler h ? h.extractItemInternal(slot, amount, false) :
                handler.extractItem(slot, amount, false);
    }

    private static ItemStack insert(IItemHandler handler, int slot, ItemStack stack, boolean simulate) {
        return handler instanceof NotifiableItemStackHandler h ? h.insertItemInternal(slot, stack, simulate) :
                handler.insertItem(slot, stack, simulate);
    }

    private static FluidStack drain(IFluidHandler tank, FluidStack stack, IFluidHandler.FluidAction action) {
        return tank instanceof NotifiableFluidTank t ? t.drainInternal(stack, action) : tank.drain(stack, action);
    }

    private static int fill(IFluidHandler tank, FluidStack stack, IFluidHandler.FluidAction action) {
        return tank instanceof NotifiableFluidTank t ? t.fillInternal(stack, action) : tank.fill(stack, action);
    }
}
