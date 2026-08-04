package com.likeazusa2.strongermekasuit;

import net.minecraftforge.common.ForgeConfigSpec;

public final class StrongerMekaSuitConfig {

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    private StrongerMekaSuitConfig() {
    }

    public static final class Server {

        private final ForgeConfigSpec.LongValue abnormalDamageClampEnergy;

        private Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server-side settings for Stronger Mekasuit.").push("damage");
            abnormalDamageClampEnergy = builder
                  .comment(
                        "Maximum FE that advanced MekaSuit damage absorption is allowed to consume for a single damage calculation.",
                        "If an incoming hit would cost more energy than this, the hit is clamped down before absorption is processed."
                  )
                  .defineInRange("abnormalDamageClampEnergy", 20_000_000_000L, 1L, Long.MAX_VALUE);
            builder.pop();
        }

        public long abnormalDamageClampEnergy() {
            return abnormalDamageClampEnergy.get();
        }
    }
}

