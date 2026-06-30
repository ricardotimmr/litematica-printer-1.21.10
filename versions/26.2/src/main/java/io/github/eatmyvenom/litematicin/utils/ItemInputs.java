package io.github.eatmyvenom.litematicin.utils;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;

@SuppressWarnings({"ConstantConditions", "unused"})
public
class ItemInputs {

	private static long handling = System.currentTimeMillis();
	private static final HashSet<Long> handledPos = new HashSet<>();
	private static Map.Entry<Long, Long> entry;
	public static BlockPos clickedPos = null;

	public static void clear() {
		handledPos.clear();
	}

	public static boolean canHandle() {
		return System.currentTimeMillis() > handling + LitematicaMixinMod.PRINTER_INVENTORY_SCREEN_WAIT.getIntegerValue();
	}

	private static void handle() {
		handling = System.currentTimeMillis();
	}

	/***
	 @param player : player Entity
	 @param required : List of stacks, might be empty to return false
	 @return boolean : should process or not
	 ***/
	public static boolean matchStacks(List<ItemStack> required, List<Slot> current, LocalPlayer player, boolean allowNamed) {
		if (player == null || required.isEmpty() || current.size() < required.size()) {
			return false;
		}
		//#if MC>=12105
		List<ItemStack> playerStacks = player.getInventory().getNonEquipmentItems();
		//#else
		//$$ List<ItemStack> playerStacks = player.getInventory().main;
		//#endif
		List<ItemStack> availableStacks = new ArrayList<>(playerStacks);
		for (int i = 0; i < current.size(); i++) {
			ItemStack currentStack = current.get(i).getItem();
			if (currentStack.isEmpty()) {
				continue;
			}
			ItemStack requiredStack = i < required.size() ? required.get(i) : ItemStack.EMPTY;
			if (requiredStack.isEmpty()
				|| !InventoryUtils.areItemsExact(currentStack, requiredStack, allowNamed)
				|| currentStack.getCount() > requiredStack.getCount()) {
				availableStacks.add(currentStack);
			}
		}
		int[] countArray = new int[availableStacks.size()];
		for (int i = 0; i < availableStacks.size(); i++) {
			countArray[i] = availableStacks.get(i).getCount();
		}
		for (int slotIndex = 0; slotIndex < required.size(); slotIndex++) {
			ItemStack itemStack = required.get(slotIndex);
			if (itemStack.isEmpty()) {
				continue;
			}
			int requiredAmount = itemStack.getCount();
			ItemStack currentStack = current.get(slotIndex).getItem();
			if (InventoryUtils.areItemsExact(itemStack, currentStack, allowNamed) && currentStack.getCount() <= itemStack.getCount()) {
				requiredAmount -= currentStack.getCount();
			}
			if (requiredAmount <= 0) {
				continue;
			}
			for (int i = 0; i < availableStacks.size(); i++) {
				if (countArray[i] <= 0) {
					continue;
				}
				ItemStack availableStack = availableStacks.get(i);
				if (InventoryUtils.areItemsExact(itemStack, availableStack, allowNamed)) {
					if (countArray[i] >= requiredAmount) {
						countArray[i] -= requiredAmount;
						requiredAmount = 0;
					} else {
						requiredAmount -= countArray[i];
						countArray[i] = 0;
					}
				}
				if (requiredAmount <= 0) {
					break;
				}
			}
			if (requiredAmount > 0) {
				return false;
			}
		}
		return true;
	}

	private static int getPreference(AbstractContainerMenu screen, ItemStack itemStack, boolean allowNamed, int requiredCount) {
		if (screen == null || itemStack.isEmpty()) {
			return -1;
		}
		int bestPartialSlot = -1;
		int bestPartialCount = 0;
		for (int i = 0; i < screen.slots.size(); i++) {
			if (!(screen.getSlot(i).container instanceof Inventory)) {
				continue;
			}
			ItemStack playerStacks = screen.getSlot(i).getItem();
			if (InventoryUtils.areItemsExact(itemStack, playerStacks, allowNamed)) {
				if (playerStacks.getCount() >= requiredCount) {
					return i;
				}
				if (playerStacks.getCount() > bestPartialCount) {
					bestPartialSlot = i;
					bestPartialCount = playerStacks.getCount();
				}
			}
		}
		return bestPartialSlot;
	}

	private static List<Slot> getNonPlayerSlots(AbstractContainerMenu screen) {
		List<Slot> retVal = new ArrayList<>();
		if (screen == null) {
			return retVal;
		}
		for (int i = 0; i < screen.slots.size(); i++) {
			if (isPlayerInventorySlot(screen.getSlot(i))) {
				continue;
			}
			retVal.add(screen.getSlot(i));
		}
		return retVal;
	}

	private static boolean isPlayerInventorySlot(Slot slot) {
		return slot.container instanceof Inventory;
	}

	private static boolean clearCursor(Minecraft client) {
		if (client == null) {
			return true;
		}
		final LocalPlayer player = client.player;
		if (player == null || player.containerMenu == null || player.containerMenu.getCarried().isEmpty()) {
			return true;
		}
		AbstractContainerMenu handler = player.containerMenu;
		if (moveCursorToPlayerSlots(client, handler, true)) {
			return true;
		}
		if (moveCursorToPlayerSlots(client, handler, false)) {
			return true;
		}
		MessageHolder.sendMessageUncheckedUnique(player, "Could not clear carried stack safely; inventory operation paused");
		return false;
	}

	private static boolean moveCursorToPlayerSlots(Minecraft client, AbstractContainerMenu handler, boolean matchingOnly) {
		LocalPlayer player = client.player;
		if (player == null || handler == null || handler.getCarried().isEmpty()) {
			return true;
		}
		ItemStack cursorStack = handler.getCarried();
		for (int i = 0; i < handler.slots.size(); i++) {
			Slot slot = handler.getSlot(i);
			if (!isPlayerInventorySlot(slot) || !slot.mayPlace(cursorStack)) {
				continue;
			}
			if (matchingOnly) {
				//#if MC >= 12006
				if (!ItemStack.isSameItemSameComponents(cursorStack, slot.getItem())) {
				//#else
				//$$ if (!ItemStack.canCombine(cursorStack, slot.getStack())) {
				//#endif
					continue;
				}
				if (slot.getItem().getCount() >= slot.getMaxStackSize(cursorStack)) {
					continue;
				}
			} else if (!slot.getItem().isEmpty()) {
				continue;
			}
			if (!clickSlot(handler, i, 0, ContainerInput.PICKUP)) {
				return false;
			}
			if (handler.getCarried().isEmpty()) {
				return true;
			}
			cursorStack = handler.getCarried();
		}
		return handler.getCarried().isEmpty();
	}

	public static void execute(Minecraft client) {
		LocalPlayer player = client == null ? null : client.player;
		if (player == null || player.containerMenu == null) {
			return;
		}
		if (canHandle()) {
			handle();
		} else {
			MessageHolder.sendUniqueMessageActionBar(player, "Cooldown....");
			return;
		}
		boolean allowNamed = LitematicaMixinMod.PRINTER_INVENTORY_OPERATION_ALLOW_ALL_NAMED.getBooleanValue();
		BlockPos where = rayCast(client);
		if (where == null) {
			MessageHolder.sendUniqueMessageActionBar(player, "Failed to raycast");
			return;
		}
		if (handledPos.contains(where.asLong())) {
			MessageHolder.sendUniqueMessageActionBar(player, "Position is already handled");
			clickedPos = null;
			player.closeContainer();
			return;
		}
		if (!hasOpenExtraMenu(client)) {
			MessageHolder.sendUniqueMessageActionBar(player, "Screen is not extra screen");
			return;
		}
		AbstractContainerMenu menu = player.containerMenu;
		menu.resumeRemoteUpdates();
		menu.sendAllDataToRemote();
		if (!clearCursor(client)) {
			return;
		}
		if (!isActiveMenu(client, menu)) {
			return;
		}
		List<Slot> nonPlayerSlot = getNonPlayerSlots(menu);
		List<ItemStack> requiredStacks = getRaycastRequiredItemStacks(client, nonPlayerSlot.size());
		if (requiredStacks.isEmpty()) {
			MessageHolder.sendUniqueDebugMessage("required stacks were empty for " + where.toShortString());
			player.closeContainer();
			return;
		}
		MessageHolder.sendUniqueDebugMessage("Handled pos " + where.toShortString());
		if (requiredStacks.size() != nonPlayerSlot.size()) {
			MessageHolder.sendMessageUncheckedUnique(player, "Sizes differ as " + requiredStacks.size() + " but non-player slot size : " + nonPlayerSlot.size());
			return;
		}
		if (matchStacks(requiredStacks, nonPlayerSlot, player, allowNamed)) {
			//MessageHolder.sendUniqueDebugMessage("Required pos " + where.toShortString());

			//MessageHolder.sendUniqueDebugMessage(nonPlayerSlot.toString());
			long now = System.currentTimeMillis();
			if (entry == null || entry.getKey() != where.asLong()) {
				entry = Map.entry(where.asLong(), now + LitematicaMixinMod.PRINTER_INVENTORY_SCREEN_WAIT.getIntegerValue());
				return;
			} else if (entry.getValue() > now) {
				return;
			} else {
				entry = null;
			}
			boolean allCorrect = false;
			int retryCount = Math.max(1, LitematicaMixinMod.PRINTER_INVENTORY_OPERATIONS_RETRY.getIntegerValue());
			for (int j = 0; j < retryCount; j++) {
				allCorrect = true;
				for (int i = 0; i < requiredStacks.size(); i++) {
					if (isSlotExact(nonPlayerSlot.get(i).getItem(), requiredStacks.get(i), allowNamed)) {
						continue;
					}
					boolean sent = sendItem(client, nonPlayerSlot.get(i), requiredStacks.get(i), allowNamed);
					if (!isSlotExact(nonPlayerSlot.get(i).getItem(), requiredStacks.get(i), allowNamed)) {
						allCorrect = false;
					}
					if (!sent) {
						allCorrect = false;
						break;
					}
				}
				if (allCorrect) {
					break;
				}
				if (!isActiveMenu(client, menu)) {
					return;
				}
			}
			if (allCorrect) {
				handledPos.add(where.asLong());
				MessageHolder.sendUniqueDebugMessage("Successfully done operation at " + where.toShortString());
				if (LitematicaMixinMod.PRINTER_INVENTORY_OPERATIONS_CLOSE_SCREEN.getBooleanValue()) {
					player.closeContainer();
				}
			} else {
				MessageHolder.sendUniqueDebugMessage("Partially failed to send all items at " + where.toShortString() + ", will retry");
			}
		} else {
			MessageHolder.sendUniqueDebugMessage("Does not have enough item for " + where.toShortString() + "!");
			if (LitematicaMixinMod.PRINTER_INVENTORY_OPERATIONS_CLOSE_SCREEN.getBooleanValue()) {
				player.closeContainer();
			}
		}
	}

	private static boolean hasOpenExtraMenu(Minecraft client) {
		LocalPlayer player = client == null ? null : client.player;
		if (player == null || player.containerMenu == null || player.containerMenu == player.inventoryMenu) {
			return false;
		}
		Screen screen = client.gui == null ? null : client.gui.screen();
		return screen != null && !(screen instanceof InventoryScreen);
	}

	private static boolean isActiveMenu(Minecraft client, AbstractContainerMenu menu) {
		LocalPlayer player = client == null ? null : client.player;
		return player != null && menu != null && player.containerMenu == menu;
	}

	/***
	 * Part of the execution
	 * @param client : MinecraftClient
	 * @param targetSlot : Integer of slot defined as slot.id
	 * @param stack : Wanted slot to send
	 * @param allowNamed : allows Named item to go instead
	 */
	private static boolean sendItem(Minecraft client, int targetSlot, ItemStack stack, boolean allowNamed) {
		AbstractContainerMenu screenHandler = client == null || client.player == null ? null : client.player.containerMenu;
		if (!isActiveMenu(client, screenHandler) || !hasSlot(screenHandler, targetSlot)) {
			if (client != null && client.player != null) {
				MessageHolder.sendMessageUncheckedUnique(client.player, "Target slot was invalid: " + targetSlot);
			}
			return false;
		}
		if (!clearCursor(client) || !clearUnmatchTargetSlot(client, targetSlot, stack, allowNamed)) {
			return false;
		}
		if (stack.isEmpty()) {
			return true;
		}
		if (!isActiveMenu(client, screenHandler)) {
			return false;
		}
		int amountToSend = getMissingCount(screenHandler, targetSlot, stack, allowNamed);
		if (amountToSend < 0) {
			return false;
		}
		if (amountToSend <= 0) {
			return true;
		}
		int lastHolding = -1;
		while (amountToSend > 0) {
			if (!isActiveMenu(client, screenHandler)) {
				return false;
			}
			int holding = getPreference(screenHandler, stack, allowNamed, amountToSend);
			if (holding == -1) {
				return false;
			} //actually we can do this
			lastHolding = holding;
			int previousTargetCount = screenHandler.getSlot(targetSlot).getItem().getCount();
			if (!leftClickSlot(screenHandler, holding)) {
				return false;
			}
			for (int i = 0; i < amountToSend && !screenHandler.getCarried().isEmpty(); i++) {
				if (!rightClickSlot(screenHandler, targetSlot)) {
					clearCursor(client);
					return false;
				}
				if (getMissingCount(screenHandler, targetSlot, stack, allowNamed) <= 0) {
					break;
				}
			}
			if (!leftClickSlot(screenHandler, holding)) {
				clearCursor(client);
				return false;
			}
			if (!clearCursor(client)) {
				return false;
			}
			int currentTargetCount = screenHandler.getSlot(targetSlot).getItem().getCount();
			if (currentTargetCount <= previousTargetCount) {
				return false;
			}
			amountToSend = getMissingCount(screenHandler, targetSlot, stack, allowNamed);
			if (amountToSend < 0) {
				return false;
			}
		}
		if (!clearCursor(client)) {
			return false;
		}
		MessageHolder.sendUniqueDebugMessage("Sent item from " + lastHolding + " to " + targetSlot);
		//client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, targetSlot, holding, SlotActionType.SWAP, client.player);
		return isSlotExact(screenHandler.getSlot(targetSlot).getItem(), stack, allowNamed);
	}

	private static int getMissingCount(AbstractContainerMenu screenHandler, int targetSlot, ItemStack stack, boolean allowNamed) {
		if (!hasSlot(screenHandler, targetSlot)) {
			return -1;
		}
		ItemStack currentStack = screenHandler.getSlot(targetSlot).getItem();
		if (!isTargetSlotReady(currentStack, stack, allowNamed)) {
			return -1;
		}
		return stack.getCount() - currentStack.getCount();
	}

	private static boolean sendItem(Minecraft client, Slot targetSlot, ItemStack stack, boolean allowNamed) {
		AbstractContainerMenu menu = client == null || client.player == null ? null : client.player.containerMenu;
		return sendItem(client, getMenuSlotIndex(menu, targetSlot), stack, allowNamed);
	}

	private static boolean clearUnmatchTargetSlot(Minecraft client, int targetSlot, ItemStack wantedItem, boolean allowNamed) {
		AbstractContainerMenu gui = client == null || client.player == null ? null : client.player.containerMenu;
		if (!hasSlot(gui, targetSlot)) {
			return false;
		}
		ItemStack slotStack = gui.getSlot(targetSlot).getItem();
		if (isTargetSlotReady(slotStack, wantedItem, allowNamed)) {
			return true;
		}
		if (!leftClickSlot(gui, targetSlot)) {
			return false;
		}
		if (clearCursor(client) && isActiveMenu(client, gui)) {
			return isTargetSlotReady(gui.getSlot(targetSlot).getItem(), wantedItem, allowNamed);
		}
		if (isActiveMenu(client, gui) && !gui.getCarried().isEmpty() && gui.getSlot(targetSlot).getItem().isEmpty()) {
			leftClickSlot(gui, targetSlot);
		}
		clearCursor(client);
		return false;
	}

	private static boolean isTargetSlotReady(ItemStack currentStack, ItemStack wantedItem, boolean allowNamed) {
		if (wantedItem.isEmpty()) {
			return currentStack.isEmpty();
		}
		return currentStack.isEmpty()
			|| InventoryUtils.areItemsExact(currentStack, wantedItem, allowNamed) && currentStack.getCount() <= wantedItem.getCount();
	}

	private static boolean leftClickSlot(AbstractContainerMenu gui, int slotNum) {
		return clickSlot(gui, slotNum, 0, ContainerInput.PICKUP);
	}

	private static boolean rightClickSlot(AbstractContainerMenu gui, int slotNum) {
		return clickSlot(gui, slotNum, 1, ContainerInput.PICKUP);
	}

	private static boolean isSlotExact(ItemStack current, ItemStack required, boolean allowNamed) {
		if (required.isEmpty()) {
			return current.isEmpty();
		}
		return InventoryUtils.areItemsExactCount(current, required, allowNamed);
	}

	private static boolean shiftClickSlot(AbstractContainerMenu gui, int slotNum) {
		return clickSlot(gui, slotNum, 0, ContainerInput.QUICK_MOVE);
	}

	public static boolean clickSlot(AbstractContainerMenu gui, int slotNum, int button, ContainerInput action) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.gameMode == null || gui == null || minecraft.player.containerMenu != gui || slotNum < 0 || slotNum >= gui.slots.size()) {
			return false;
		}
		try {
			minecraft.gameMode.handleContainerInput(gui.containerId, slotNum, button, action, minecraft.player);
			return true;
		} catch (Exception e) {
			MessageHolder.sendMessageUncheckedUnique(minecraft.player, "Clicking slot failed ");
			MessageHolder.sendMessageUncheckedUnique(minecraft.player, e.getMessage());
			return false;
		}
	}

	public static boolean clickSlot(AbstractContainerMenu gui, Slot slot, int button, ContainerInput action) {
		return clickSlot(gui, getMenuSlotIndex(gui, slot), button, action);
	}

	private static int getMenuSlotIndex(AbstractContainerMenu gui, Slot slot) {
		if (gui == null || slot == null) {
			return -1;
		}
		return gui.slots.indexOf(slot);
	}

	private static boolean hasSlot(AbstractContainerMenu gui, int slotNum) {
		return gui != null && slotNum >= 0 && slotNum < gui.slots.size();
	}

	public static List<ItemStack> getRaycastRequiredItemStacks(Minecraft minecraftClient) {
		return getRaycastRequiredItemStacks(minecraftClient, -1);
	}

	public static List<ItemStack> getRaycastRequiredItemStacks(Minecraft minecraftClient, int expectedSlots) {
		List<ItemStack> retVal = new ArrayList<>();
		if (minecraftClient == null || minecraftClient.player == null) {
			return retVal;
		}
		Screen screen = minecraftClient.gui.screen();
		if (screen == null) {
			return retVal;
		}
		if (screen instanceof InventoryScreen) {
			MessageHolder.sendUniqueDebugMessage(minecraftClient.player, "Screen was InventoryScreen");
			return retVal;
		}
		BlockPos context = rayCast(minecraftClient);
		if (context == null) {
			return retVal;
		}
		if (handledPos.contains(context.asLong())) {
			MessageHolder.sendUniqueDebugMessage(minecraftClient.player, "Screen was already registered");
			return retVal;
		}
		return InventoryUtils.getRequiredStackInSchematic(SchematicWorldHandler.getSchematicWorld(), minecraftClient, context, expectedSlots);
	}

	private static BlockPos rayCast(Minecraft minecraftClient) {
		LocalPlayer player = minecraftClient == null ? null : minecraftClient.player;
		if (player == null) {
			return null;
		}
		if (clickedPos == null) {
			MessageHolder.sendUniqueMessageActionBar(player, "Current raycast is set to null");
			return null;
		}
		MessageHolder.sendUniqueMessageActionBar(player, "Current raycast is set to " + clickedPos.toShortString());
		BlockPos castedPos = clickedPos;
		Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
		if (schematicWorld == null || minecraftClient.level == null) {
			return null;
		}
		BlockEntity schematicEntity = schematicWorld.getBlockEntity(castedPos);
		BlockEntity clientEntity = minecraftClient.level.getBlockEntity(castedPos);
		if (clientEntity instanceof Container
			&& (schematicEntity instanceof Container
			|| Printer.sameBlockState(schematicWorld.getBlockState(castedPos), minecraftClient.level.getBlockState(castedPos)))) {
			return castedPos;
		}
		return null;
	}


}
