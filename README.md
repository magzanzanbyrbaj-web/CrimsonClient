# Crimson Client 1.21.11

Beautiful red glass-style PvP client with visuals, HUD and utilities. **No cheats.**

## Features (divided by sections)

### HUD
1. **Watermark** — Name + FPS + Ping
2. **Target HUD** — Beautiful display of selected target (name, HP, distance, health bar)
3. **Armor HUD** — Armor display with durability
4. **Totem Counter** — Totem of Undying counter
5. **Keystrokes** — Display of pressed keys (WASD + LMB/RMB)
6. **Module List** — List of enabled modules
7. **Inventory HUD** — Mini-inventory indicator
8. **Potion HUD** — Active potion effects with timers
9. **Durability HUD** — Item durability of held items

### Visuals / Effects
10. **HitParticles** — Particles on hit (toggle)
11. **Damage Numbers** — Damage numbers (toggle)
12. **Hitmarker** — Hit effect (X marker)
13. **Critical Effects** — Critical hit effects (toggle)
14. **Smooth Sword** — Smooth sword animation (toggle)
15. **Item Switch** — Item switch animation (toggle)
16. **Totem Animation** — Beautiful totem animation (toggle)
17. **Custom Crosshair** — Customizable crosshair
18. **Custom Sky** — Change sky appearance (toggle)
19. **Inventory Blur** — Inventory background blur (toggle)

### Utilities
20. **Fullbright** — Increased brightness
- Reduce Particles
- Optimization Mode
- Menu Animation

## How to build

```bash
./gradlew build
```

The ready JAR will be in `build/libs/crimsonclient-1.0.0.jar`.

Requires Java 21+, Fabric Loader 0.20+, Fabric API for 1.21.11.

## Controls
- **Right Shift** — Open menu

## Notes
Many combat visual effects (HitParticles, DamageNumbers, SmoothSword, TotemAnim, CustomSky, InventoryBlur) are present as toggles and partial implementations. Full advanced rendering for some of them requires additional mixins that can be extended later. Core HUD elements (Watermark, Target, Armor, Totem, Keystrokes, ModuleList, Potion, Durability, Crosshair, Hitmarker, Fullbright) are fully functional.

Config is saved to `config/crimsonclient.json`.
