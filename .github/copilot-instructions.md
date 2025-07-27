<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

This is a Minecraft Spigot plugin project for Paper 1.21.5 using Java 21 and Maven.

## Project Overview
- **Plugin Name**: SwapBall 2.0
- **Purpose**: Enhanced swap ball with heat-seeking capabilities
- **Dependencies**: Paper API, WorldGuard API

## Key Features
- Heat-seeking snowball projectiles that lock onto the closest player
- WorldGuard region restrictions
- Configurable heat-seeking parameters
- Particle and sound effects
- Cooldown system
- Permission-based access control

## Code Guidelines
- Use Paper API instead of Bukkit/Spigot API when possible
- Follow Java naming conventions (PascalCase for classes, camelCase for methods/variables)
- Use proper error handling and null checks
- Always check player permissions before allowing actions
- Respect WorldGuard region restrictions
- Use configuration values instead of hardcoded constants

## Important Classes
- `SwapBallPlugin`: Main plugin class
- `HeatSeekingManager`: Manages heat-seeking projectile behavior
- `ProjectileListener`: Handles projectile launch and hit events
- `ItemBuilder`: Creates and identifies SwapBall items
- `HeatSeekingProjectile`: Model for tracking projectiles

## Configuration Structure
The plugin uses a config.yml with sections for:
- `allowed-regions`: WorldGuard regions where SwapBalls can be used
- `heat-seeking`: Parameters for projectile behavior
- `swap-ball`: General settings for particles, sounds, and cooldowns
