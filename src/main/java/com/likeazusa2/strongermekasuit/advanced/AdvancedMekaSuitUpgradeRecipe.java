package com.likeazusa2.strongermekasuit.advanced;

import com.likeazusa2.strongermekasuit.StrongerMekaSuitRecipeSerializers;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.serializer.NucleosynthesizingRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AdvancedMekaSuitUpgradeRecipe extends NucleosynthesizingRecipe {

    private final ItemStack output;

    public AdvancedMekaSuitUpgradeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient chemicalInput, ItemStack output,
          int duration) {
        super(id, itemInput, chemicalInput, output, duration);
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, GasStack inputChemical) {
        ItemStack upgraded = output.copy();
        upgraded.setTag(inputItem.getTag() == null ? null : inputItem.getTag().copy());
        return upgraded;
    }

    @Override
    public List<@NotNull ItemStack> getOutputDefinition() {
        return List.of(output.copy());
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull net.minecraft.core.RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<AdvancedMekaSuitUpgradeRecipe> getSerializer() {
        return StrongerMekaSuitRecipeSerializers.ADVANCED_MEKASUIT_UPGRADE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MekanismRecipeType.NUCLEOSYNTHESIZING.getRecipeType();
    }

    public static final class Serializer extends NucleosynthesizingRecipeSerializer<AdvancedMekaSuitUpgradeRecipe> {

        public Serializer() {
            super(AdvancedMekaSuitUpgradeRecipe::new);
        }
    }
}

