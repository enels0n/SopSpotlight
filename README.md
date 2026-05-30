# SopSpotlight

`SopSpotlight` is a cross-server spotlight plugin for Bukkit/Paper + Velocity networks.

It lets a backend server announce that a player is currently playing on that server, while keeping the final message rendering on each Bukkit server. That means local placeholders and local chat/locale plugins can still shape the final output per recipient.

## Modules

- `SopSpotlight-Bukkit` - command handling, local formatting template, PlaceholderAPI support, local delivery to players
- `SopSpotlight-Velocity` - proxy relay between backend servers

## How it works

1. A player or console runs `/spotlight send [player] [server]` on a Bukkit/Paper server.
2. The Bukkit plugin builds a small payload:
   - player name
   - origin server id
   - sender text
   - send-avatar flag
3. The payload is sent to Velocity.
4. Velocity forwards that payload to backend servers across the network.
5. Each Bukkit server renders its own `lines` template locally and applies PlaceholderAPI for each recipient.

This is important because placeholders like locale strings should be resolved on the receiving server, not on the sender.

## Bukkit config

`SopSpotlight-Bukkit` uses `config.yml`.

Example:

```yml
messages:
  no-permission: "&cYou do not have permission to do this."
  usage: "&e/spotlight send [player] [server]"
  console-player-required: "&cConsole must specify an online player carrier."
  player-offline: "&cPlayer is offline."
  sent: "&aSpotlight request sent."
  reloaded: "&aSopSpotlight-Bukkit reloaded."

spotlight:
  server-id: "pillars"
  send-avatar: true
  sender-text: "%player_name%"
  lines:
    - " "
    - ""
    - ""
    - "   {sender-text} <#FFAA00>%soplocales_sopspotlight_message-playing-on%"
    - "  <#F097F7>%soplocales_sopspotlight_server-{server}%</#F097F7>"
    - ""
    - "          <hover:show_text:'<yellow>Click'><click:run_command:/server {server}><#B0DDBD>[<#FFAA00>%soplocales_sopspotlight_message-click-to-join%<#B0DDBD>]"
    - ""
    - ""
    - " "
```

### Supported tokens in `lines`

- `{player}` - spotlighted player name
- `{server}` - origin server id
- `{server-name}` - currently the same as `{server}`
- `{sender-text}` - custom sender text prepared on the sending server

### PlaceholderAPI

`lines` are rendered on each Bukkit server for each recipient, so PlaceholderAPI works there in the final display stage.

That allows patterns like:

- locale placeholders
- per-recipient placeholders
- group / prefix placeholders
- world-specific placeholders

`sender-text` is also built on the sending server, so it can contain backend-specific PlaceholderAPI formatting such as a colored nickname or prefix.

Example:

```yml
sender-text: "%vault_prefix%%player_name%"
```

## Velocity config

`SopSpotlight-Velocity` uses `config.yml`.

```yml
debug: false
```

- `debug: false` - normal quiet behavior
- `debug: true` - log spotlight forwarding info in Velocity console

## Commands

### Bukkit

- `/spotlight send [player] [server]`
- `/spotlight reload`

### Velocity

- `/spotlight send <player> [server]`
- `/spotlight reload`

## Permission

- `sopspotlight.admin`

## Notes

- To correctly avoid showing the spotlight to other players on the origin server, set `spotlight.server-id` on each Bukkit server.
- Avatar rendering is controlled by the Bukkit-side `send-avatar` flag and is rendered locally on backend servers.
- Velocity no longer formats spotlight messages; it only forwards payloads.
