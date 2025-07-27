# SwapBall 2.0

A Minecraft Spigot plugin for Paper 1.21.5 that adds enhanced swap balls with heat-seeking capabilities.

**Author:** SansNom  
**Organization:** SansCraft  
**Website:** https://sanscraft.top

## Features

- **Heat-Seeking Projectiles**: SwapBalls lock onto the closest player and track them
- **WorldGuard Integration**: Respect region boundaries and restrictions
- **Configurable Settings**: Customize heat-seeking behavior, particles, sounds, and more
- **Permission System**: Control who can give and use SwapBalls
- **Cooldown System**: Prevent spam with configurable cooldowns
- **Visual & Audio Effects**: Particle trails and sound effects

## Item Details

**SwapBall 2.0**
- Name: `§f§lSwap Ball §42.0`
- Material: Snowball
- Lore:
  - `§fJust your average`
  - `§feveryday swap ball`
  - `§bEnhanced by the power of`
  - `§4§lDetermination`

## Commands

- `/swapball` - Give yourself a SwapBall 2.0 item
  - Permission: `swapball.give`

## Permissions

- `swapball.give` - Allow giving SwapBall items (default: op)
- `swapball.use` - Allow using SwapBall items (default: true)

## Configuration

### WorldGuard Regions
Configure which regions allow SwapBall usage in `config.yml`:

```yaml
config:
  allowed-regions:
    - "spawn"
    - "pvp-arena"
    - "battlefield"
```

Leave empty to allow usage everywhere.

### Heat-Seeking Settings
```yaml
heat-seeking:
  max-target-distance: 50.0    # Maximum distance to search for targets
  update-frequency: 2          # How often to update direction (ticks)
  speed-multiplier: 1.5        # Speed boost for projectiles
  max-turn-angle: 15.0         # Maximum turn angle per update (degrees)
```

### General Settings
```yaml
swap-ball:
  cooldown: 5                  # Cooldown between uses (seconds)
  particles:
    enabled: true
    type: "FLAME"
    amount: 10
  sounds:
    launch: "ENTITY_SNOWBALL_THROW"
    hit: "ENTITY_ENDERMAN_TELEPORT"
```

## How It Works

1. **Launch**: When a player throws a SwapBall, it scans for the closest player within range
2. **Lock-On**: The projectile locks onto the target and begins heat-seeking
3. **Tracking**: The projectile continuously adjusts its trajectory to follow the target
4. **Impact**: When the projectile hits the target, both players swap positions
5. **Effects**: Particles and sounds play at both the launch and impact locations

## Dependencies

- **Paper 1.21.5** (or compatible version)
- **WorldGuard 7.0.12+** (optional, for region restrictions)

## Building

1. Ensure you have Java 21 and Maven installed
2. Clone the repository
3. Run `mvn clean package`
4. The compiled plugin will be in the `target/` directory

## Installation

1. Place the compiled JAR file in your server's `plugins/` directory
2. Install WorldGuard if you want region restrictions
3. Start your server
4. Configure the plugin in `plugins/SwapBall/config.yml`
5. Restart or reload the server

## License

This project is provided as-is for educational and entertainment purposes.
