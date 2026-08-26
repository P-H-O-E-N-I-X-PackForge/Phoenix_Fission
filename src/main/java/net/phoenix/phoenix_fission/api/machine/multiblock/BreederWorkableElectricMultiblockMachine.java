package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.iface.IFissionBlanketType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class BreederWorkableElectricMultiblockMachine extends DynamicFissionReactorMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            BreederWorkableElectricMultiblockMachine.class,
            DynamicFissionReactorMachine.MANAGED_FIELD_HOLDER);

    private transient List<IFissionBlanketType> activeBlankets = new ArrayList<>();

    @Nullable
    @Getter
    private transient IFissionBlanketType primaryBlanket = null;

    @Persisted
    private long blanketCycleCount = 0;

    @Persisted
    private int blanketCycleTicks = 0;

    public BreederWorkableElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        resolveBlanketsFromPersisted();
        selectPrimaryBlanket();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStructureFormed() {
        super.onStructureFormed();

        Object obj = getMultiblockState().getMatchContext().get("BlanketTypes");
        if (obj instanceof List<?> list) {
            this.activeBlankets = (List<IFissionBlanketType>) list;
        } else {
            this.activeBlankets = new ArrayList<>();
        }

        selectPrimaryBlanket();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.activeBlankets.clear();
        this.primaryBlanket = null;
    }

    @Override
    protected boolean shouldRunReactor() {
        if (!isFormed() || getComponentManager().getActiveFuelRods().isEmpty() || isScramActive()) return false;

        return getFuelManager().hasFuelAvailableForNextTick();
    }

    @Override
    public boolean onWorking() {
        boolean isWorking = super.onWorking();

        if (isWorking && isFormed() && primaryBlanket != null) {
            processBreeding(PhoenixFissionConfigs.INSTANCE.fission, Math.max(1, lastParallels));
        }

        return isWorking;
    }

    private void selectPrimaryBlanket() {
        this.primaryBlanket = activeBlankets.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void resolveBlanketsFromPersisted() {
        if (this.persistedBlanketIDs == null || this.persistedBlanketIDs.isEmpty()) return;

        this.activeBlankets = new ArrayList<>();
        for (String id : this.persistedBlanketIDs) {
            PhoenixAPI.FISSION_BLANKETS.keySet().stream()
                    .filter(b -> b.getName().equals(id))
                    .findFirst().ifPresent(t -> this.activeBlankets.add(t));
        }
    }

    private void processBreeding(PhoenixFissionConfigs.FissionConfigs cfg, int parallels) {
        if (activeBlankets == null || activeBlankets.isEmpty()) return;

        IFissionBlanketType primary = primaryBlanket != null ? primaryBlanket : activeBlankets.get(0);
        int duration = Math.max(1, primary.getDurationTicks());

        double burnMul = 1.0;

        if (!cfg.blanketUsageAdditive) {
            int amount = (int) Math.ceil(Math.max(0, primary.getAmountPerCycle()) * parallels * burnMul);

            if (amount > 0 && !canConsumeResource(primary.getInputKey(), amount)) {
                return;
            }
        }

        blanketCycleTicks++;
        if (blanketCycleTicks < duration) return;

        blanketCycleTicks = 0;
        blanketCycleCount++;

        int spectrumBias = getReactorSpectrumBias();
        Random rng = makeBlanketRng();

        if (!cfg.blanketUsageAdditive) {
            int amount = (int) Math.ceil(Math.max(0, primary.getAmountPerCycle()) * parallels * burnMul);
            if (amount <= 0) return;

            if (tryConsumeResource(primary.getInputKey(), amount)) return;

            var dist = buildAdjustedDistribution(primary, spectrumBias);
            var outputs = sampleOutputs(dist, amount, rng);
            outputBatch(outputs);
            return;
        }

        for (var blanket : activeBlankets) {
            int amount = (int) Math.ceil(Math.max(0, blanket.getAmountPerCycle()) * parallels * burnMul);
            if (amount <= 0) continue;

            if (tryConsumeResource(blanket.getInputKey(), amount)) continue;

            var dist = buildAdjustedDistribution(blanket, spectrumBias);
            var outputs = sampleOutputs(dist, amount, rng);
            outputBatch(outputs);
        }
    }

    private boolean canConsumeResource(String key, int amount) {
        if (amount <= 0) return true;

        ItemStack is = resolveKeyToItem(key, amount);
        assert is != null;
        if (!is.isEmpty()) return canConsumeItem(is);

        return false;
    }

    private boolean canConsumeItem(ItemStack stack) {
        if (stack.isEmpty()) return true;
        GTRecipe dummy = GTRecipeBuilder.ofRaw()
                .inputItems(stack)
                .buildRawRecipe();
        return RecipeHelper.matchRecipe(this, dummy).isSuccess();
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        return DynamicFissionReactorMachine.recipeModifier(machine, recipe);
    }

    @Nullable
    protected ItemStack resolveKeyToItem(String key, int amount) {
        if (amount <= 0) return ItemStack.EMPTY;

        ResourceLocation rl = ResourceLocation.tryParse(key);
        if (rl == null) return ItemStack.EMPTY;

        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, amount);
    }

    private void tryOutputResource(String key, int amount) {
        if (amount <= 0) return;

        ItemStack is = resolveKeyToItem(key, amount);
        assert is != null;
        if (!is.isEmpty()) {
            GTRecipe dummy = GTRecipeBuilder.ofRaw()
                    .outputItems(is)
                    .buildRawRecipe();

            var result = RecipeHelper.handleRecipeIO(this, dummy, IO.OUT, getRecipeLogic().getChanceCaches());

            if (!result.isSuccess() && getLevel() != null) {
                Containers.dropItemStack(
                        getLevel(),
                        getPos().getX(), getPos().getY(), getPos().getZ(),
                        is);
            }
        }
    }

    private void outputBatch(Map<String, Integer> outputs) {
        for (var e : outputs.entrySet()) {
            tryOutputResource(e.getKey(), e.getValue());
        }
    }

    private boolean tryConsumeResource(String key, int amount) {
        if (amount <= 0) return false;

        ItemStack is = resolveKeyToItem(key, amount);
        assert is != null;
        if (!is.isEmpty()) return !tryConsumeItem(is);

        return true;
    }

    private boolean tryConsumeItem(ItemStack stack) {
        if (stack.isEmpty()) return true;

        GTRecipe dummy = GTRecipeBuilder.ofRaw()
                .inputItems(stack)
                .buildRawRecipe();

        if (!RecipeHelper.matchRecipe(this, dummy).isSuccess()) return false;

        return RecipeHelper.handleRecipeIO(this, dummy, IO.IN, getRecipeLogic().getChanceCaches()).isSuccess();
    }

    private record WeightedKey(String key, double weight, int instability) {}

    private int getReactorSpectrumBias() {
        IFissionFuelRodType rod = getComponentManager().getPrimaryFuelRodType();
        if (rod == null) return 0;

        return Math.max(-100, Math.min(100, rod.getNeutronBias()));
    }

    private List<WeightedKey> buildAdjustedDistribution(IFissionBlanketType blanket, int spectrumBias) {
        double normalizedBias = spectrumBias / 100.0;
        List<WeightedKey> out = new ArrayList<>();

        for (var bo : blanket.getOutputs()) {
            if (bo == null) continue;
            int baseWeight = Math.max(0, bo.weight());
            if (baseWeight == 0) continue;

            double factor = 1.0 + (normalizedBias * bo.instability() * 0.5);
            double adjustedWeight = baseWeight * Math.max(0.1, factor);

            out.add(new WeightedKey(bo.key(), adjustedWeight, bo.instability()));
        }
        return out;
    }

    private Random makeBlanketRng() {
        long seed = 0x9E3779B97F4A7C15L;
        if (getLevel() instanceof ServerLevel serverLevel) {
            seed ^= serverLevel.getSeed();
        }

        seed ^= getPos().asLong() * 0xD1B54A32D192ED03L;
        seed ^= blanketCycleCount * 0x94D049BB133111EBL;
        return new Random(seed);
    }

    private Map<String, Integer> sampleOutputs(List<WeightedKey> dist, int amount, Random rng) {
        Map<String, Integer> result = new HashMap<>();
        if (amount <= 0 || dist.isEmpty()) return result;

        double totalWeight = dist.stream().mapToDouble(WeightedKey::weight).sum();
        if (totalWeight <= 0.0) return result;

        for (int i = 0; i < amount; i++) {
            double selector = rng.nextDouble() * totalWeight;
            double runningSum = 0.0;
            boolean picked = false;

            for (WeightedKey entry : dist) {
                runningSum += entry.weight();
                if (selector <= runningSum) {
                    result.merge(entry.key(), 1, Integer::sum);
                    picked = true;
                    break;
                }
            }

            if (!picked) {
                result.merge(dist.get(rng.nextInt(dist.size())).key(), 1, Integer::sum);
            }
        }
        return result;
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(0, 0, 224, 216);
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;

        if (activeBlankets.isEmpty()) {
            textList.add(Component.literal("Breeding Offline - No Blankets Present")
                    .withStyle(ChatFormatting.RED));
        } else {
            textList.add(Component.literal("Breeding Active")
                    .withStyle(ChatFormatting.GREEN));
        }
    }
}
