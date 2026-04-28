package com.theoman02.freehands.common.action;

import com.theoman02.freehands.FreeHands;
import com.theoman02.freehands.config.FreeHandsServerConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

public final class FreeHandsActions {
    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_END = 8;
    private static final int INVENTORY_START = 9;
    private static final int INVENTORY_END = 35;

    private FreeHandsActions() {
    }

    private static InteractionHand findHandToEmpty(Player player, boolean preferOffhand) {
        InteractionHand firstHand = preferOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        InteractionHand secondHand = preferOffhand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        if (!player.getItemInHand(firstHand).isEmpty()) {
            return firstHand;
        }

        if (!player.getItemInHand(secondHand).isEmpty()) {
            return secondHand;
        }

        return null;
    }

    // Public mutation entry point used by the server-side packet handler.
    public static void moveHandToFreeSlot(Player player, boolean preferOffhand, boolean isHotbarIgnored) {
        tryMoveHandToFreeSlot(player, preferOffhand, isHotbarIgnored, false);
    }

    // Client-side precheck used to avoid sending pointless payloads.
    public static boolean simulateMoveHandToFreeSlot(Player player, boolean preferOffhand, boolean isHotbarIgnored) {
        return tryMoveHandToFreeSlot(player, preferOffhand, isHotbarIgnored, true);
    }

    private static boolean tryMoveHandToFreeSlot(Player player, boolean preferOffhand,
                                                 boolean isHotbarIgnored, boolean isSimulated) {
        //Cache for now.
        boolean serverOnlyDebug = !isSimulated && FreeHandsServerConfig.DEBUG.get();

        Inventory inventory = player.getInventory();
        InteractionHand handToEmpty = findHandToEmpty(player, preferOffhand);

        if (handToEmpty == null) {
            if (serverOnlyDebug) {
                FreeHands.LOGGER.info("[FREEHANDS][SERVER]: No item found in either hand for {}", player.getName().getString());
            }
            return false;
        }
        ItemStack stackToTransfer = player.getItemInHand(handToEmpty);
        // Main-hand source can overlap with the selected hotbar slot.
        int sourceSlot = handToEmpty == InteractionHand.MAIN_HAND ? inventory.selected : -1;

        if (serverOnlyDebug) {
            FreeHands.LOGGER.info(
                    "[FREEHANDS][SERVER]: Attempting move for {}. hand={}, stack={}x {}, sourceSlot={}, ignoreHotbar={}",
                    player.getName().getString(),
                    handToEmpty,
                    stackToTransfer.getCount(),
                    stackToTransfer.getItem(),
                    sourceSlot,
                    isHotbarIgnored
            );
        }
        ItemStack returnedStack = insertToInventory(inventory, stackToTransfer, isHotbarIgnored, sourceSlot, isSimulated);

        // No movement happened if the returned stack is the same item with the same count.
        if (ItemStack.isSameItemSameComponents(returnedStack, stackToTransfer)
                && returnedStack.getCount() == stackToTransfer.getCount()) {
            if (serverOnlyDebug) {
                FreeHands.LOGGER.info("[FREEHANDS][SERVER]: No valid inventory destination found");
            }
            return false;
        }
        if (!isSimulated) {
            player.setItemInHand(handToEmpty, returnedStack);
            // Sync inventory changes to the client.
            player.inventoryMenu.broadcastChanges();

            if (serverOnlyDebug) {
                FreeHands.LOGGER.info(
                        "[FREEHANDS][SERVER]: Move complete. remaining={}x {}",
                        returnedStack.getCount(),
                        returnedStack.isEmpty() ? "empty" : returnedStack.getItem()
                );
            }
        }
        return true;
    }

    private static ItemStack insertToInventory(Inventory inventory, ItemStack stackArg,
                                               boolean isHotbarIgnored, int sourceSlot,
                                               boolean isSimulated) {
        // Copy the hand stack so we can safely calculate the remainder without changing the original.
        var stack = stackArg.copy();
        //Get the currently selected slot now, so later we ensure we
        //do not choose the selected hotbar slot as an empty destination.
        var selectedSlot = inventory.selected;
        var inventoryHandler = new PlayerMainInvWrapper(inventory);

        // Always try main inventory first.
        stack = insertIntoInventoryRange(stack,
                inventoryHandler,
                sourceSlot,
                selectedSlot,
                INVENTORY_START,
                INVENTORY_END,
                isSimulated);

        // Only try hotbar afterwards if hotbar is allowed and there is still a remainder.
        if (!isHotbarIgnored && !stack.isEmpty()) {
            stack = insertIntoInventoryRange(stack,
                    inventoryHandler,
                    sourceSlot,
                    selectedSlot,
                    HOTBAR_START,
                    HOTBAR_END,
                    isSimulated);
        }
        return stack;
    }

    private static ItemStack insertIntoInventoryRange(ItemStack stack,
                                                      PlayerMainInvWrapper inventoryHandler,
                                                      int sourceSlot,
                                                      int selectedSlot,
                                                      int start,
                                                      int end,
                                                      boolean isSimulated) {
        boolean isUnstackable = stack.getMaxStackSize() == 1;

        // Cache first valid empty slot so stacking checks and final insert can be done in one loop.
        int freeSlot = -1;
        for (int slot = start; slot <= end; slot++) {
            if (slot == sourceSlot) {
                continue;
            }
            var slotStack = inventoryHandler.getStackInSlot(slot);

            if (slotStack.isEmpty()) {
                if (slot != selectedSlot) {
                    // Remember only the first valid empty slot.
                    freeSlot = freeSlot == -1 ? slot : freeSlot;
                    // Unstackable items cannot merge, so the first valid empty slot is enough.
                    if (isUnstackable) {
                        break;
                    }
                }
                continue;
            }
            //Skip if stack is already at max size.
            if (slotStack.getCount() >= slotStack.getMaxStackSize()) {
                continue;
            }
            // Skip if it's not the same type of item.
            if (!ItemStack.isSameItemSameComponents(slotStack, stack)) {
                continue;
            }
            stack = inventoryHandler.insertItem(slot, stack, isSimulated);

            // Empty remainder means transfer completed.
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        if (freeSlot != -1 && !stack.isEmpty()) {
            stack = inventoryHandler.insertItem(freeSlot, stack, isSimulated);
        }

        return stack;
    }
}
