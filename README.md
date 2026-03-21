# Stronger Mekasuit

`Stronger Mekasuit` is a NeoForge mod for Minecraft `1.21.1` that adds an advanced MekaSuit set on top of Mekanism.

The mod keeps Mekanism's module workflow and rendering style, while providing stronger armor values, expanded energy-based damage handling, and a custom upgrade path from the vanilla MekaSuit set.

## Features

- Adds an advanced helmet, bodyarmor, pants, and boots based on Mekanism's MekaSuit.
- Keeps Mekanism module compatibility for the advanced armor pieces.
- Increases armor, toughness, knockback resistance, and related protection behavior.
- Applies server-side abnormal damage clamping before energy absorption logic.
- Supports a fallback damage clamp path for cases that bypass the normal damage entry.
- Whitelists `/kill` so command kills do not drain suit energy.
- Adds Antiprotonic Nucleosynthesizer upgrade recipes that convert vanilla MekaSuit armor into advanced armor while preserving item data such as stored energy and installed modules.
- Adds advanced-exclusive visual tweaks, including purple-themed advanced armor lighting and advanced elytra unit visuals.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.206` or compatible `21.1.x`
- Mekanism `10.7.18+`

## Development

### Build

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

### Run the client

```powershell
.\gradlew.bat runClient
```

## Project Notes

- The mod id is `strongermekasuit`.
- The published archive base name is also `strongermekasuit`.
- Client rendering changes are designed to affect the advanced armor only, while leaving vanilla Mekanism MekaSuit visuals intact.

## Credits

- Mekanism for the original MekaSuit system, module framework, and the upstream assets that this mod integrates with.
- NeoForge for the modding platform and runtime.
- See [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for modified third-party asset notices.

## License

This project is licensed under the MIT License. See [LICENSE.md](./LICENSE.md).

This repository also includes modified third-party assets derived from Mekanism. See [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md).
