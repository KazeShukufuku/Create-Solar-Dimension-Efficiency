# Create Solar Dimension Efficiency

1.21.1 NeoForge addon for Create: Solar Powered `1.2.7` and Create `6.0.9+`.

This mod keeps Create: Solar Powered's original `max_output`, `update_interval`, sunlight, weather, altitude, and temperature logic, then applies a dimension multiplier to the final solar panel output.

Config file: `config/solardimensionaddon-common.toml`

Default entries:

```toml
dimension_efficiencies = [
  "default=1.0",
  "minecraft:overworld=1.0",
  "minecraft:the_nether=0.0",
  "minecraft:the_end=0.0"
]
```

The `convert_solar_output_unit` option is enabled by default. It changes the solar panel goggle tooltip output unit to `xxx FE/t`.

When Create: Northstar - Redux Unhardcoded is installed, solar panel output is also multiplied by the planet `sunMultiplier` from Northstar's registry-backed `PlanetDefinition`. Legacy Northstar Redux `NorthstarPlanets.getSunMultiplier(...)` remains a fallback.

Custom dimensions can be added with the same format, for example:

```toml
"some_mod:custom_dimension=1.25"
```

Build:

```powershell
gradle build
```

The Create: Solar Powered jar is resolved as a compile-only dependency from CurseMaven when available. During local migration verification, `.codex-tmp/createsolar-7940354.jar` can be used as a compile-only fallback.

Third-party attribution is documented in `THIRD_PARTY_NOTICES.md` and is packaged into the addon jar under `META-INF/`.
