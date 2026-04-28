package com.theoman02.freehands.common.action;

import com.theoman02.freehands.common.debug.FreeHandsDebug;
import com.theoman02.freehands.common.records.FreeHandsClientPreferences;
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

    // Public mutation entry point used by the server-side packet handler.
    public static void moveHandToFreeSlot(Player player, FreeHandsClientPreferences clientPreferences) {
        tryMoveHandToFreeSlot(player, clientPreferences, false);
    }

    // Client-side precheck used to avoid sending pointless payloads.
    public static boolean simulateMoveHandToFreeSlot(Player player, FreeHandsClientPreferences clientPreferences) {
        return tryMoveHandToFreeSlot(player, clientPreferences, true);
    }

    private static boolean tryMoveHandToFreeSlot(Player player,
                                                 FreeHandsClientPreferences clientPreferences,
                                                 boolean isSimulated) {
        Inventory inventory = player.getInventory();

        InteractionHand firstHand = clientPreferences.preferOffhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        InteractionHand secondHand = clientPreferences.preferOffhand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        InteractionHand[] handsToTry;
        if (clientPreferences.tryOtherHand() || player.getItemInHand(firstHand).isEmpty()) {
            handsToTry = new InteractionHand[] { firstHand, secondHand };
        } else {
            handsToTry = new InteractionHand[] { firstHand };
        }

        for (InteractionHand handToEmpty : handsToTry) {
            ItemStack stackToTransfer = player.getItemInHand(handToEmpty);

            if (stackToTransfer.isEmpty()) {
                FreeHandsDebug.logServer(isSimulated, "No item found in {}", handToEmpty);
                continue;
            }

            int sourceSlot = handToEmpty == InteractionHand.MAIN_HAND ? inventory.selected : -1;

            FreeHandsDebug.logServer(
                    isSimulated,
                    "Attempting move for {}. hand={}, stack={}x {}, sourceSlot={}, ignoreHotbar={}, tryOtherHand={}",
                    player.getName().getString(),
                    handToEmpty,
                    stackToTransfer.getCount(),
                    stackToTransfer.getItem(),
                    sourceSlot,
                    clientPreferences.ignoreHotbar(),
                    clientPreferences.tryOtherHand()
            );

            ItemStack returnedStack = insertToInventory(inventory, stackToTransfer, clientPreferences, sourceSlot, isSimulated);

            // No movement happened if the returned stack is the same item with the same count.
            if (ItemStack.isSameItemSameComponents(returnedStack, stackToTransfer)
                    && returnedStack.getCount() == stackToTransfer.getCount()) {
                FreeHandsDebug.logServer(isSimulated, "No valid inventory destination found for {}", handToEmpty);
                continue;
            }

            if (!isSimulated) {
                player.setItemInHand(handToEmpty, returnedStack);
                // Sync inventory changes to the client.
                player.inventoryMenu.broadcastChanges();

                FreeHandsDebug.logServer(
                        "Move complete. hand={}, remaining={}x {}",
                        handToEmpty,
                        returnedStack.getCount(),
                        returnedStack.isEmpty() ? "empty" : returnedStack.getItem()
                );
            }

            return true;
        }

        FreeHandsDebug.logServer(isSimulated, "No item found in either hand or no valid destination found");
        return false;
    }

    private static ItemStack insertToInventory(Inventory inventory, ItemStack stackArg,
                                               FreeHandsClientPreferences clientPreferences, int sourceSlot,
                                               boolean isSimulated) {
        // Copy the hand stack so we can safely calculate the remainder without changing the original.
        var stack = stackArg.copy();
        // Get the currently selected slot now, so later we ensure we
        // do not choose the selected hotbar slot as an empty destination.
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
        if (!clientPreferences.ignoreHotbar() && !stack.isEmpty()) {
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
            // Skip if stack is already at max size.
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