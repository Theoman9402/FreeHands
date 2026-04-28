# Free Hands

This mod adds one simple keybind: press it to move the item in your main hand into your inventory. If your main hand is empty, it moves your offhand instead.

This is useful when you want to quickly empty your hands without opening your inventory first, mainly for interacting with blocks that need empty hands.

Due to the way this is implemented, it sadly is not clientside. It needs to be on both the server and the player.

I have not had a chance to test it on an external dedicated server yet, but it was made with multiplayer in mind.

This is my first published mod, so there may be a few rough edges. I have tested it in my modded ATM10 6.6 world and have not encountered any issues.

Please let me know if you run into any problems, and if it works well in multiplayer. Feedback on how to improve the current features or the implementation is encouraged!

## Features

- Move your held item or offhand item into your inventory with one key.
- Choose whether it checks your main hand or offhand first
- Optionally allow it to use hotbar slots as destinations (off by default)
- Shows a message if your inventory is full if it can't move anything

## How to Use

1. Bind a key to `Free Hands`
2. Press the key in-game to move the item out of your hand.

The key is unbound by default, so you need to choose one before using the mod.

By default, it checks your main hand first, then your offhand. You can change this order in the mod config.

Also by default, it does not place items into your hotbar. You can change this in the config. When hotbar destinations are enabled, slots are checked from left to right

## Notes

- This mod only moves items into the normal player's inventory.
- It does not move items into backpacks, curios slots, external storage, or other modded inventories.
- If there is no valid place for the item, nothing is moved.
- The mod does not intentionally delete or duplicate items.
- In multiplayer, the actual move is handled by the server.
- Not tested with mods that add more off hand slots or alter the inventory capacity in any way.

## Requirements

- Minecraft 1.21.1
- NeoForge for Minecraft 1.21.1

For multiplayer, this mod must be installed on both the client and the server.


## Implementation Details

This mod has a client-side keybind, but the actual inventory change is done on the server. This avoids the common multiplayer issue where the client appears to move an item locally, then the server corrects it back.

Before sending the request, the client runs the move logic in simulation mode. If the simulation says there is nowhere valid for the item to go, no packet is sent and the player gets an inventory-full message instead.

The packet only sends the two settings needed for the move:

- `preferOffhand`
- `ignoreHotbar`

Hand preference is handled as a simple two-step check: preferred hand first, then the other hand if the preferred hand is empty.