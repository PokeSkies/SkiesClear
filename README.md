# SkiesClear
<img width="100" height="50" src="https://i.imgur.com/c1DH9VL.png" alt="Requires Fabric Kotlin"/> <img src="https://img.shields.io/badge/Enviroment-Server-purple" alt="Enviroment Server"> <a href="https://discord.gg/invite/cgBww275Fg" rel="noopener nofollow ugc"><img src="https://img.shields.io/discord/1158447623989116980?color=blue&amp;logo=discord&amp;label=Discord" alt="Discord"></a>

A Fabric (1.20.1) server-sided entity clearing mod! Allows for timed automatic removal of entities, including mobs, items, and even Cobblemon!

More information on configuration can be found on the [Wiki](https://github.com/PokeSkies/SkiesClear/wiki)!

## Features
- Create practically infinite clear timers *(idk, haven't tested that)*
- Limit timers to certain dimensions
- Customizable clear messages and sound
- Customizable warning messages and sound (choose when your warnings appear)
- Customize which types of entities should be cleared, including:
  - Items (whitelists and blacklists)
  - Any Entities (whitelists and blacklists)
  - Cobblemon Pokemon (whitelists and blacklists, with optional flags like shiny, legendary, and aspects)

## Installation
1. Download the latest version of the mod from COMING SOON.
2. Download all required dependencies:
   - [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) 
   - [Fabric Permissions API](https://github.com/PokeSkies/fabric-permissions-api)
3. Download any optional dependencies:
   - [Cobblemon](https://modrinth.com/mod/cobblemon)
4. Install the mod and dependencies into your server's `mods` folder.
5. Configure your clear timers in the `./config/skiesclear/config.json` file.

## Commands/Permissions
| Command                                 | Description                                        | Permission                |
|-----------------------------------------|----------------------------------------------------|---------------------------|
| /skiesclear reload                      | Reload the Mod                                     | skiesclear.command.reload |
| /skiesclear info <timer_id>             | View info related to the specified timer           | skiesclear.command.info   |
| /skiesclear force <timer_id> [announce] | Force a clear timer to activate                    | skiesclear.command.force  |
| /skiesclear debug                       | Toggle the debug mode for more insight into errors | skiesclear.command.deug   |

## Planned Features
- Please submit your suggestions!

## Support
A community support Discord has been opened up for all Skies Development related projects! Feel free to join and ask questions or leave suggestions :)

<a class="discord-widget" href="https://discord.gg/cgBww275Fg" title="Join us on Discord"><img src="https://discordapp.com/api/guilds/1158447623989116980/embed.png?style=banner2"></a>
