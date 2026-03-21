package com.likeazusa2.strongermekasuit;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class StrongerMekaSuitConfig {

    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    private StrongerMekaSuitConfig() {
    }

    public static final class Server {

        private final ModConfigSpec.LongValue abnormalDamageClampEnergy;

        private Server(ModConfigSpec.Builder builder) {
            builder.comment("Server-side settings for Stronger Mekasuit.")
                  .push("damage");

            abnormalDamageClampEnergy = builder
                  .comment(
                        "Maximum FE that advanced MekaSuit damage absorption is allowed to consume for a single damage calculation.",
                        "If an incoming hit would cost more energy than this, the hit is clamped down before absorption is processed.",
                        "This is mainly a safety cap for abnormal or overflow-level damage values.",
                        "Default: 20000000000 FE (20G FE)."
                  )
                  .defineInRange("abnormalDamageClampEnergy", 20_000_000_000L, 1L, Long.MAX_VALUE);

            builder.pop();
        }

        public long abnormalDamageClampEnergy() {
            return abnormalDamageClampEnergy.get();
        }
    }
}
