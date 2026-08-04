package com.likeazusa2.strongermekasuit;

import com.likeazusa2.strongermekasuit.advanced.AdvancedMekaSuitUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class StrongerMekaSuitRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
          DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, StrongerMekaSuit.MODID);

    public static final RegistryObject<RecipeSerializer<AdvancedMekaSuitUpgradeRecipe>> ADVANCED_MEKASUIT_UPGRADE =
          RECIPE_SERIALIZERS.register("advanced_mekasuit_upgrade", AdvancedMekaSuitUpgradeRecipe.Serializer::new);

    private StrongerMekaSuitRecipeSerializers() {
    }
}

