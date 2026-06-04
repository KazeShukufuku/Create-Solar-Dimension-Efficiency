# Create Solar Dimension Efficiency

1.20.1 Forge addon for Create: Solar Powered `1.2.6-1.20.1`.

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

The `convert_solar_output_unit` option is enabled by default. It changes the solar panel goggle tooltip output unit from the original symbol unit to `xxx FE/t`.

When Northstar Redux is installed, solar panel output is also multiplied by Northstar's own planet `sunMultiplier` automatically. Northstar planet values do not need to be added to `dimension_efficiencies`.

Custom dimensions can be added with the same format, for example:

```toml
"some_mod:custom_dimension=1.25"
```

Build:

```powershell
gradle build
```

The Create: Solar Powered jar is resolved from CurseMaven as a compile-only dependency.

Third-party attribution is documented in `THIRD_PARTY_NOTICES.md` and is packaged into the addon jar under `META-INF/`.

Output jar:

```text
build/libs/solardimensionaddon-1.0.0.jar
```
