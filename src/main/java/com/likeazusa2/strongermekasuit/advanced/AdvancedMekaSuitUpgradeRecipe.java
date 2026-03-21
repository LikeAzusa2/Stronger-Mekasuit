package com.likeazusa2.strongermekasuit.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitRecipeSerializers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AdvancedMekaSuitUpgradeRecipe extends NucleosynthesizingRecipe {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStack output;
    private final int duration;
    private final boolean perTickUsage;

    public AdvancedMekaSuitUpgradeRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, int duration,
          boolean perTickUsage) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.output = output.copy();
        this.duration = duration;
        this.perTickUsage = perTickUsage;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean perTickUsage() {
        return perTickUsage;
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, ChemicalStack inputChemical) {
        // Swap only the item identity so Mekanism energy, installed modules, and other components
        // from the source MekaSuit piece are preserved on the advanced output.
        return inputItem.transmuteCopy(output.getItem(), output.getCount());
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, ChemicalStack chemicalStack) {
        return itemInput.test(itemStack) && chemicalInput.test(chemicalStack);
    }

    @Override
    public List<@NotNull ItemStack> getOutputDefinition() {
        return List.of(output.copy());
    }

    public ItemStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<AdvancedMekaSuitUpgradeRecipe> getSerializer() {
        return StrongerMekaSuitRecipeSerializers.ADVANCED_MEKASUIT_UPGRADE.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdvancedMekaSuitUpgradeRecipe other = (AdvancedMekaSuitUpgradeRecipe) o;
        return duration == other.duration
              && perTickUsage == other.perTickUsage
              && itemInput.equals(other.itemInput)
              && chemicalInput.equals(other.chemicalInput)
              && ItemStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + duration;
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + ItemStack.hashItemAndComponents(output);
        result = 31 * result + output.getCount();
        return result;
    }

    public static final class Serializer implements RecipeSerializer<AdvancedMekaSuitUpgradeRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<AdvancedMekaSuitUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(AdvancedMekaSuitUpgradeRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT)
                    .forGetter(AdvancedMekaSuitUpgradeRecipe::getChemicalInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(AdvancedMekaSuitUpgradeRecipe::getOutputRaw),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(AdvancedMekaSuitUpgradeRecipe::getDuration),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(AdvancedMekaSuitUpgradeRecipe::perTickUsage)
        ).apply(instance, AdvancedMekaSuitUpgradeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AdvancedMekaSuitUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, AdvancedMekaSuitUpgradeRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), AdvancedMekaSuitUpgradeRecipe::getChemicalInput,
              ItemStack.STREAM_CODEC, AdvancedMekaSuitUpgradeRecipe::getOutputRaw,
              ByteBufCodecs.VAR_INT, AdvancedMekaSuitUpgradeRecipe::getDuration,
              ByteBufCodecs.BOOL, AdvancedMekaSuitUpgradeRecipe::perTickUsage,
              AdvancedMekaSuitUpgradeRecipe::new
        );

        private Serializer() {
        }

        @Override
        public MapCodec<AdvancedMekaSuitUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AdvancedMekaSuitUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
