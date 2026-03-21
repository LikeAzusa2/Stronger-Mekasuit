package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitUpgradeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class StrongerMekaSuitRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
          DeferredRegister.create(Registries.RECIPE_SERIALIZER, StrongerMekaSuit.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AdvancedMekaSuitUpgradeRecipe>> ADVANCED_MEKASUIT_UPGRADE =
          RECIPE_SERIALIZERS.register("advanced_mekasuit_upgrade", () -> AdvancedMekaSuitUpgradeRecipe.Serializer.INSTANCE);

    private StrongerMekaSuitRecipeSerializers() {
    }
}
