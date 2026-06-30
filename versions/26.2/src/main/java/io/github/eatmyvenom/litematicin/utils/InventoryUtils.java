package io.github.eatmyvenom.litematicin.utils;

import java.util.*;
import java.util.function.Predicate;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import fi.dy.masa.malilib.util.EquipmentUtils;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.materials.MaterialCache;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.*;
import static io.github.eatmyvenom.litematicin.utils.Printer.*;

public class InventoryUtils
{
	private static int ptr = -1;

	public final static HashSet<Item> ITEMS = new HashSet<>();
	public static int lastCount = 0;
	public static int itemChangeCount = 0;
	public static Item handlingItem = null;
	public static Item previousItem = null; //only used for checks
	public static int trackedSelectedSlot = -1;

	private static long tickCount = 0;
	private static long lastWorkedTick = 0;

	private static String cachedPickBlockableSlots = "";

	private static final Set<Integer> pickBlockableSlots = new LinkedHashSet<>();
	private static final List<Integer> pickBlockableSlotOrder = new ArrayList<>();
	private static final Map<Item, Boolean> toolLikeItemCache = new IdentityHashMap<>();
	public static Map<Integer, Item> usedSlots = new LinkedHashMap<>();
	public static Map<Integer, Integer> slotCounts = new LinkedHashMap<>();

	public static void tick() {
		tickCount++;
		if (RENDER_ONLY_HOLDING_ITEMS.getBooleanValue() && tickCount % 20 == 0) {
			calculateCache();
		}
		if (INVENTORY_CACHE_TICKS.getIntegerValue() != 0 && tickCount - lastWorkedTick > INVENTORY_CACHE_TICKS.getIntegerValue()){
			clearCache();
		}
		if (!isSleeping && Configs.Generic.EASY_PLACE_MODE.getBooleanValue() && Configs.Generic.EASY_PLACE_HOLD_ENABLED.getBooleanValue() && Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld()) {
			Minecraft client = Minecraft.getInstance();
			LocalPlayer player = client == null ? null : client.player;
			if (player != null) {
				pruneCachedSlots(getInventory(player));
			} else {
				clearCache();
			}
		} else {
			clearCache();
		}
	}

	public static void clearCache(){
		if (!usedSlots.isEmpty()) MessageHolder.sendOrderMessage("Clearing cache");
		trackedSelectedSlot = -1;
		previousItem = null;
		handlingItem = null;
		usedSlots.clear();
		slotCounts.clear();
		lastWorkedTick = tickCount;
	}


	@SuppressWarnings("PatternVariableCanBeUsed") // because for compatibility with 1.16.5
	private static void calculateCache() {
		ITEMS.clear();
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null || client.level == null) {
			return;
		}
		//#if MC>=12105
		for (ItemStack stack : client.player.getInventory().getNonEquipmentItems()) {
		//#elseif MC>=11700
		//$$ for (ItemStack stack : client.player.getInventory().main) {
		//#else
		//$$ for (ItemStack stack : client.player.inventory.main) {
		//#endif
			if (stack.isEmpty() || stack.is(Items.AIR)) {
				continue;
			}
			Item item = stack.getItem();
			if (item != null) {
				ITEMS.add(item);
			}
			if (item instanceof BlockItem) {
				BlockItem blockItem = (BlockItem) item;
				if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
					int invSize = 27;
					//#if MC >= 12006
					NonNullList<ItemStack> returnStacks = NonNullList.withSize(invSize, ItemStack.EMPTY);
					ItemContainerContents container = stack.getComponents().get(DataComponents.CONTAINER);
					if (container != null) {
						container.copyInto(returnStacks);
						for (ItemStack returnStack : returnStacks) {
							if (returnStack.isEmpty() || returnStack.is(Items.AIR)) {
								continue;
							}
							Item returnItem = returnStack.getItem();
							if (returnItem != null) {
								ITEMS.add(returnItem);
							}
						}
					}
					//#else
					//$$ NbtCompound compound = stack.getSubNbt("BlockEntityTag");
					//$$ if (compound == null) {
					//$$ 	continue;
					//$$ }
					//$$ DefaultedList<ItemStack> returnStacks = DefaultedList.ofSize(invSize, ItemStack.EMPTY);
					//$$ if (compound.contains("Items")) {
					//$$ 	Inventories.readNbt(compound, returnStacks);
					//$$ }
					//$$ for (ItemStack returnStack : returnStacks) {
					//$$ 	Item returnItem = returnStack.getItem();
					//$$ 	if (returnItem != null) {
					//$$ 		ITEMS.add(returnItem);
					//$$ 	}
					//$$ }
					//#endif
				}
			}
		}
	}

	public static Inventory getInventory(LocalPlayer player) {
		//#if MC>=11700
		return player.getInventory();
		//#else
		//$$ return player.inventory;
		//#endif
	}

	public static boolean isCreative(LocalPlayer player) {
		//#if MC>=11700
		return player.getAbilities().instabuild;
		//#else
		//$$ return player.abilities.creativeMode;
		//#endif
	}

	public static boolean isToolLikeItem(Item item) {
		if (item == null || item == Items.AIR) {
			return false;
		}
		Boolean cached = toolLikeItemCache.get(item);
		if (cached != null) {
			return cached;
		}
		// ToolItem or FlintAndSteelItem or ShearsItem
		//#if MC>=12105
		boolean toolLike = item instanceof FlintAndSteelItem || item instanceof ShearsItem || EquipmentUtils.isRegularTool(item.getDefaultInstance());
		//#elseif MC>=12102
		//$$ boolean toolLike = item instanceof MiningToolItem || item instanceof FlintAndSteelItem || item instanceof ShearsItem;
		//#else
		//$$ boolean toolLike = item instanceof ToolItem || item instanceof FlintAndSteelItem || item instanceof ShearsItem;
		//#endif
		toolLikeItemCache.put(item, toolLike);
		return toolLike;
	}

	static boolean isEmptyOrAir(ItemStack stack) {
		return stack == null || stack.isEmpty() || stack.is(Items.AIR);
	}

	public static void decrementCount(boolean isCreative) {
		if (isCreative) {
			lastCount = 65536;
			slotCounts.computeIfPresent(trackedSelectedSlot, (key, value) -> 65536);
			return;
		}
		if (lastCount > 0 && usedSlots.get(trackedSelectedSlot) != null && !isToolLikeItem(usedSlots.get(trackedSelectedSlot))) {
			lastCount--;
			slotCounts.computeIfPresent(trackedSelectedSlot, (key, value) -> value - 1);
		}
	}
	private static int getPtr() {
		parsePickblockableSlots();
		if (pickBlockableSlotOrder.isEmpty()) {
			return -1;
		}
		ptr++;
		ptr = ptr % pickBlockableSlotOrder.size();
		return pickBlockableSlotOrder.get(ptr);
	}

	private static void parsePickblockableSlots() {
		String pickBlockableSlot = Configs.Generic.PICK_BLOCKABLE_SLOTS.getStringValue();
		if (!pickBlockableSlot.equals(cachedPickBlockableSlots)) {
			cachedPickBlockableSlots = pickBlockableSlot;
			pickBlockableSlots.clear();
			pickBlockableSlotOrder.clear();
			ptr = -1;
			for (String s : pickBlockableSlot.split(",")) {
				try {
					int i = Integer.parseInt(s);
					if (i>0 && i<10) {
						int slot = i - 1;
						if (pickBlockableSlots.add(slot)) {
							pickBlockableSlotOrder.add(slot);
						}
					}
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
	}

	// getAvailableSlot() is used to get the slot that the item is in, or the next available slot if it's not in the hotbar
	public static int getAvailableSlot(Item item) {
		if (item == null || item == Items.AIR) {
			return -1;
		}
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client == null ? null : client.player;
		Inventory inv = player == null ? null : getInventory(player);
		if (inv != null) {
			pruneCachedSlots(inv);
		}
		for (Map.Entry<Integer, Item> entry : usedSlots.entrySet()) {
			if (entry.getValue() == item) {
				return entry.getKey();
			}
		}
		parsePickblockableSlots();
		if (inv != null) {
			for (Integer slot : pickBlockableSlotOrder) {
				if (!usedSlots.containsKey(slot) && isValidHotbarSlot(inv, slot) && isEmptyOrAir(inv.getItem(slot))) {
					return slot;
				}
			}
		}
		for (Integer slot : pickBlockableSlotOrder) {
			if (!usedSlots.containsKey(slot) && (inv == null || isValidHotbarSlot(inv, slot))) {
				return slot;
			}
		}
		int reusableSlot = getPtr();
		if (reusableSlot == -1 || inv != null && !isValidHotbarSlot(inv, reusableSlot)) {
			return -1;
		}
		usedSlots.remove(reusableSlot);
		slotCounts.remove(reusableSlot);
		return reusableSlot;
	}

	private static void pruneCachedSlots(Inventory inv) {
		Iterator<Map.Entry<Integer, Item>> iterator = usedSlots.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Item> entry = iterator.next();
			int slot = entry.getKey();
			boolean validHotbarSlot = isValidHotbarSlot(inv, slot);
			ItemStack stack = validHotbarSlot ? inv.getItem(slot) : ItemStack.EMPTY;
			if (!validHotbarSlot
				|| slotCounts.getOrDefault(slot, 0) <= 0
				|| entry.getValue() == null
				|| entry.getValue() == Items.AIR
				|| isEmptyOrAir(stack)
				|| stack.getItem() != entry.getValue()) {
				iterator.remove();
				slotCounts.remove(slot);
			}
		}
	}

	private static boolean isValidHotbarSlot(Inventory inv, int slot) {
		return inv != null && slot >= 0 && Inventory.isHotbarSlot(slot) && slot < inv.getNonEquipmentItems().size();
	}

	private static int searchSlot(LocalPlayer player, ItemStack stack) {
		Inventory inv = getInventory(player);
		pruneCachedSlots(inv);
		for (Map.Entry<Integer, Item> entry : usedSlots.entrySet()) {
			int slot = entry.getKey();
			if (slotCounts.getOrDefault(slot, 0) > 0
				&& slot >= 0
				&& slot < inv.getNonEquipmentItems().size()
				&& areItemsExact(inv.getItem(slot), stack)) {
				return slot;
			}
		}
		return -1;
	}

	public static ItemStack getMainHandStack(LocalPlayer player) {
		return player.getMainHandItem();
	}

	public static boolean areItemsExact(ItemStack a, ItemStack b) {
		// ToolItem or FlintAndSteelItem
		return exceptToolItems(a, b);
	}

	public static boolean areItemsExact(ItemStack a, ItemStack b, boolean allowNamed) {
		if (allowNamed) {
			return areItemsExactAllowNamed(a, b);
		}
		return exceptToolItems(a, b);
	}

	private static boolean exceptToolItems(ItemStack a, ItemStack b) {
		if (isEmptyOrAir(a) || isEmptyOrAir(b)) {
			return isEmptyOrAir(a) && isEmptyOrAir(b);
		}
		if (isToolLikeItem(a.getItem()) || isToolLikeItem(b.getItem())) {
			return a.getItem() == b.getItem();
		}
		boolean isItemEqual = ItemStack.isSameItem(a, b);
		//#if MC >= 12006
		boolean nbtCondition = LitematicaMixinMod.PRINTER_IGNORE_NBT.getBooleanValue() || ItemStack.isSameItemSameComponents(a, b);
		//#else
		//$$ boolean nbtCondition = PRINTER_IGNORE_NBT.getBooleanValue() || ItemStack.canCombine(a, b);
		//#endif
		return isItemEqual && nbtCondition;
	}

	public static boolean areItemsExactCount(ItemStack a, ItemStack b, boolean allowNamed) {
		if (isEmptyOrAir(a) || isEmptyOrAir(b)) {
			return isEmptyOrAir(a) && isEmptyOrAir(b);
		}
		if (a.getCount() != b.getCount()) {
			return false;
		}
		if (allowNamed) {
			return areItemsExactAllowNamed(a, b);
		}
		//#if MC >= 12006
		boolean nbtCondition = LitematicaMixinMod.PRINTER_IGNORE_NBT.getBooleanValue() || ItemStack.isSameItemSameComponents(a, b);
		//#else
		//$$ boolean nbtCondition = PRINTER_IGNORE_NBT.getBooleanValue() || ItemStack.canCombine(a, b);
		//#endif
		return ItemStack.isSameItem(a, b) && nbtCondition;
	}

	public static ItemStack getStackForState(Minecraft client, BlockState state, Level world, BlockPos pos) {
		// if state is nether portal block, return FLINT_AND_STEEL
		if (state.is(Blocks.NETHER_PORTAL)) {
			if (!PRINTER_LIT_PORTAL_USE_FIRECHARGE.getBooleanValue()) return Items.FLINT_AND_STEEL.getDefaultInstance();
			else {
				return Items.FIRE_CHARGE.getDefaultInstance();
			}
		}
		ItemStack stack = isReplaceableWaterFluidSource(state) && PRINTER_PLACE_ICE.getBooleanValue() ? Items.ICE.getDefaultInstance() : MaterialCache.getInstance().getRequiredBuildItemForState(state, world, pos);
		if (PRINTER_PRINT_DIRT_VARIANTS.getBooleanValue() && !canPickItem(client, stack)) {
			if (state.is(Blocks.FARMLAND)) stack = Items.DIRT.getDefaultInstance();
			else if (state.is(Blocks.DIRT_PATH)) stack = Items.DIRT.getDefaultInstance();
		}
		return stack;
	}

	public static boolean areItemsExactAllowNamed(ItemStack a, ItemStack b) {
		if (isEmptyOrAir(a) || isEmptyOrAir(b)) {
			return isEmptyOrAir(a) && isEmptyOrAir(b);
		}
		// ToolItem or FlintAndSteelItem
		if (isToolLikeItem(a.getItem()) || isToolLikeItem(b.getItem())) {
			return a.getItem() == b.getItem();
		}
		//#if MC>=12102
		else if (EquipmentUtils.isRegularTool(a) || EquipmentUtils.isRegularTool(b)) {
		//#else
		//$$ else if (a.getItem() instanceof ToolItem || b.getItem() instanceof ToolItem) {
		//#endif
			return false; // safety
		}
		//#if MC >= 12006
		return ItemStack.isSameItem(a, b) || a.getMaxStackSize() == b.getMaxStackSize() && a.has(DataComponents.CUSTOM_NAME) && b.has(DataComponents.CUSTOM_NAME);
		//#else
		//$$ return ItemStack.areItemsEqual(a, b) || a.getMaxCount() == b.getMaxCount() && a.hasCustomName() && b.hasCustomName();
		//#endif
	}

	public static boolean requiresSwap(LocalPlayer player, ItemStack stack) {
		int selectedSlot = getInventory(player).getSelectedSlot();
		if (usedSlots.get(selectedSlot) != null) {
			return slotCounts.getOrDefault(selectedSlot, 0) <= 0 || !areItemsExact(getMainHandStack(player), stack);
		}
		return !areItemsExact(getMainHandStack(player), stack) || previousItem != null && lastCount <= 0;
	}

	public static boolean canSwap(LocalPlayer player, ItemStack stack) {
		if (player == null) {
			return false;
		}
		if (isEmptyOrAir(stack)) {
			return false;
		}
		if (isCreative(player)) {
			return true;
		}
		if (areItemsExact(player.getOffhandItem(), stack)) {
			return true;
		}
		int slotNum = getSlotWithStack(player, stack);
		return slotNum != -1;
	}

	public static int getSlotWithItem(Inventory inv, ItemStack stack) {
		if (inv == null || isEmptyOrAir(stack)) {
			return -1;
		}
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			ItemStack candidate = inv.getItem(i);
			if (!isEmptyOrAir(candidate) && ItemStack.isSameItem(candidate, stack)) {
				return i;
			}
		}
		return -1;
	}

	public static boolean canSwap(LocalPlayer player, Predicate<ItemStack> predicate) {
		if (player == null || predicate == null) {
			return false;
		}
		Inventory inv = getInventory(player);
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			if (isPredicateSwapCandidate(inv.getItem(i), predicate)) {
				return true;
			}
		}
		return isPredicateSwapCandidate(player.getOffhandItem(), predicate);
	}

	synchronized public static boolean swapToItem(Minecraft client, ItemStack stack) {
		if (client == null) {
			MessageHolder.sendOrderMessage("Minecraft client was null");
			return false;
		}
		if (isEmptyOrAir(stack)) {
			MessageHolder.sendOrderMessage("Cannot swap to an empty stack");
			return false;
		}
		MessageHolder.sendOrderMessage("Trying to swap item into " + stack.getItem());
		LocalPlayer player = client.player;
		int maxChange = LitematicaMixinMod.PRINTER_MAX_ITEM_CHANGES.getIntegerValue();
		if (player == null || client.gameMode == null || client.getConnection() == null) {
			MessageHolder.sendOrderMessage("Player, interaction manager, or connection was null");
			return false;
		}
		if (player.containerMenu == null || player.inventoryMenu == null || player.containerMenu != player.inventoryMenu) {
			MessageHolder.sendOrderMessage("Cannot swap printer item while another container is open");
			return false;
		}
		//getInventory(player).updateItems();
		if (stack.getItem() != handlingItem) {
			if (maxChange != 0 && itemChangeCount >= maxChange) {
				MessageHolder.sendOrderMessage("Exceeded item change count");
				return false;
			}
		}
		if (!requiresSwap(player, stack)) {
			assert trackedSelectedSlot == -1 || trackedSelectedSlot == getInventory(player).getSelectedSlot() :
				"Selected slot changed for external reason! : expected " + trackedSelectedSlot + ", current " + getInventory(player).getSelectedSlot();
			assert previousItem == null || previousItem == stack.getItem() : "Handling item :  " + handlingItem + " was not equal to " + stack.getItem();
			MessageHolder.sendOrderMessage("Didn't require swap for item " + stack.getItem() + " previous handling item : " + previousItem);
			lastCount = isCreative(player) ? 65536 : getMainHandStack(player).getCount();
			int selectedSlot = getInventory(player).getSelectedSlot();
			int cachedSlot = searchSlot(player, stack);
			if (cachedSlot != -1 && cachedSlot != selectedSlot) {
				MessageHolder.sendMessageUncheckedUnique("Hotbar has duplicate item references, which should not happen!");
			}
			trackedSelectedSlot = selectedSlot;
			usedSlots.put(selectedSlot, getMainHandStack(player).getItem());
			slotCounts.put(selectedSlot, lastCount);
			previousItem = stack.getItem();
			lastWorkedTick = tickCount;
			return true;
		}
		int slot = searchSlot(player, stack);
		if (slot != -1) {
			getInventory(player).setSelectedSlot(slot);
			trackedSelectedSlot = getInventory(player).getSelectedSlot();
			usedSlots.put(trackedSelectedSlot, stack.getItem());
			lastCount = isCreative(player) ? 65536 : getInventory(player).getItem(trackedSelectedSlot).getCount();
			slotCounts.put(trackedSelectedSlot, lastCount);
			previousItem = stack.getItem();
			handlingItem = previousItem;
			lastWorkedTick = tickCount;
			MessageHolder.sendOrderMessage("Selected slot " + getInventory(player).getSelectedSlot() + " based on cache for " + stack.getItem());
			client.getConnection().send(new ServerboundSetCarriedItemPacket(getInventory(player).getSelectedSlot()));
			return !getInventory(player).getSelectedItem().isEmpty();
		}
		else if (usedSlots.containsValue(stack.getItem())) {
			MessageHolder.sendOrderMessage("Used slot contains item but cannot find exact stack " + stack.getItem());
		}
		MessageHolder.sendOrderMessage("Trying survival Swap");
		if (survivalSwap(client, player, stack)) {
			usedSlots.put(getInventory(player).getSelectedSlot(), stack.getItem());
			slotCounts.put(trackedSelectedSlot, lastCount);
			MessageHolder.sendOrderMessage("Swapped to item " + stack.getItem());
			handlingItem = stack.getItem();
			previousItem = handlingItem;
			itemChangeCount++;
			lastWorkedTick = tickCount;
			return true;
		}
		MessageHolder.sendOrderMessage("Survival swap failed, trying creative swap");
		return creativeSwap(client, player, stack);
	}

	synchronized public static ItemStack findItem(Minecraft client, Predicate<ItemStack> predicate) {
		LocalPlayer player = client == null ? null : client.player;
		if (player == null || predicate == null) {
			return ItemStack.EMPTY;
		}
		Inventory inv = getInventory(player);
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (isPredicateSwapCandidate(stack, predicate)) {
				return stack;
			}
		}
		ItemStack offhandStack = player.getOffhandItem();
		if (isPredicateSwapCandidate(offhandStack, predicate)) {
			return offhandStack;
		}
		return ItemStack.EMPTY;
	}

	private static boolean isPredicateSwapCandidate(ItemStack stack, Predicate<ItemStack> predicate) {
		if (predicate == null || isEmptyOrAir(stack)) {
			return false;
		}
		//#if MC>=12102
		return EquipmentUtils.isRegularTool(stack) && predicate.test(stack);
		//#else
		//$$ return stack.getItem() instanceof ToolItem && predicate.test(stack);
		//#endif
	}

	synchronized public static boolean swapToItem(Minecraft client, Predicate<ItemStack> predicate) {
		ItemStack stack = findItem(client, predicate);
		if (isEmptyOrAir(stack)) {
			return false;
		}
		return swapToItem(client, stack);
	}

	public static int getSlotWithStack(LocalPlayer player, ItemStack stack) {
		if (player == null || isEmptyOrAir(stack)) {
			return -1;
		}
		Inventory inv = getInventory(player);
		//#if MC>=12102
		return EquipmentUtils.isRegularTool(stack) || isToolLikeItem(stack.getItem()) ? getSlotWithItem(inv, stack) :getSlotWIthStackIgnoreNbt(inv, stack);
		//#else
		//$$ return stack.getItem() instanceof ToolItem || isToolLikeItem(stack.getItem()) ? getSlotWithItem(inv, stack) :getSlotWIthStackIgnoreNbt(getInventory(player), stack);
		//#endif
	}

	public static void printAllItems(Inventory inv, ItemStack stack) {
		// Debug
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			//#if MC >= 12006
			boolean areNbtsEqual = ItemStack.isSameItemSameComponents(inv.getItem(i), stack);
			//#else
			//$$ boolean areNbtsEqual = ItemStack.canCombine(inv.getStack(i), stack);
			//#endif
			boolean areItemsEqual = ItemStack.isSameItem(inv.getItem(i), stack);
			MessageHolder.sendUniqueDebugMessage("Slot " + i + ", " + inv.getItem(i).getItem() + " : " + areItemsEqual + " : " + areNbtsEqual);
		}
	}

	public static int getSlotWIthStackIgnoreNbt(Inventory inv, ItemStack stack) {
		// Debug
		//printAllItems(inv, stack);
		if (inv == null || isEmptyOrAir(stack)) {
			return -1;
		}
		int defaultSlot = inv.findSlotMatchingItem(stack);
		if (defaultSlot != -1) {
			return defaultSlot;
		}
		if (!PRINTER_IGNORE_NBT.getBooleanValue()) {
			return defaultSlot;
		}
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			ItemStack candidate = inv.getItem(i);
			if (isEmptyOrAir(candidate)) {
				continue;
			}
			boolean areItemsEqual = ItemStack.isSameItem(candidate, stack);
			if (areItemsEqual) {
				return i;
			}
		}
		return -1;
	}

	public static int getSlotWithStack(Inventory inv, ItemStack stack) {
		if (inv == null || isEmptyOrAir(stack)) {
			return -1;
		}
		int findingStack = getSlotWIthStackIgnoreNbt(inv, stack);
		//#if MC>=12102
		return EquipmentUtils.isRegularTool(stack) || isToolLikeItem(stack.getItem()) ? getSlotWithItem(inv, stack) :findingStack;
		//#else
		//$$ return stack.getItem() instanceof ToolItem || isToolLikeItem(stack.getItem()) ? getSlotWithItem(inv, stack) :findingStack;
		//#endif
	}

	@SuppressWarnings("ConstantConditions")
	private static boolean creativeSwap(Minecraft client, LocalPlayer player, ItemStack stack) {
		if (!isCreative(player)) {
			MessageHolder.sendOrderMessage("Player is not in creative mode");
			return false;
		}
		int selectedSlot = getAvailableSlot(stack.getItem());
		if (selectedSlot == -1) {
			MessageHolder.sendOrderMessage("No available slot for " + stack.getItem());
			return false;
		}
		MessageHolder.sendOrderMessage("Clicked creative stack " + stack.getItem() + " for slot " + selectedSlot);
		//getInventory(player).addPickBlock(stack);
		ItemStack pickedStack = stack.copy();
		getInventory(player).setSelectedSlot(selectedSlot);
		client.gameMode.handleCreativeModeItemAdd(pickedStack, 36 + selectedSlot);
		client.getConnection().send(new ServerboundSetCarriedItemPacket(getInventory(player).getSelectedSlot()));
		trackedSelectedSlot = selectedSlot;
		getInventory(player).getNonEquipmentItems().set(selectedSlot, pickedStack.copy());
		usedSlots.put(getInventory(player).getSelectedSlot(), stack.getItem());
		slotCounts.put(trackedSelectedSlot, 65536);
		lastCount = 65536;
		handlingItem = stack.getItem();
		previousItem = handlingItem;
		itemChangeCount++;
		lastWorkedTick = tickCount;
		return true;
	}

	@SuppressWarnings("ConstantConditions")
	private static boolean survivalSwap(Minecraft client, LocalPlayer player, ItemStack stack) {
		if (!canSwap(player, stack)) {
			return false;
		}
		if (client.getConnection() == null || client.gameMode == null || player.containerMenu != player.inventoryMenu) {
			return false;
		}
		if (areItemsExact(player.getOffhandItem(), stack) && !areItemsExact(getMainHandStack(player), stack)) {
			Inventory inv = getInventory(player);
			trackedSelectedSlot = inv.getSelectedSlot();
			lastCount = isCreative(player) ? 65536 : player.getOffhandItem().getCount();
			client.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
			ItemStack selectedStack = inv.getSelectedItem().copy();
			inv.setItem(trackedSelectedSlot, player.getOffhandItem().copy());
			inv.setItem(Inventory.SLOT_OFFHAND, selectedStack);
		} else {
			int slot = getSlotWithStack(player, stack);
			if (slot == -1) {
				return false;
			}
			if (Inventory.isHotbarSlot(slot)) {
				if (usedSlots.get(slot) != null) {
					MessageHolder.sendOrderMessage("Hotbar slot should have been handled before, so it must be error!");
					MessageHolder.sendOrderMessage("Expected : " + usedSlots.get(slot) + " but current client handles : " + stack.getItem());
					return false;
				}
				getInventory(player).setSelectedSlot(slot);
				trackedSelectedSlot = slot;
				MessageHolder.sendOrderMessage("Selected hotbar Slot " + slot);
				lastCount = isCreative(player) ? 65536 : getInventory(player).getItem(slot).getCount();
				client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
			} else {
				int selectedSlot = getAvailableSlot(stack.getItem());
				if (selectedSlot == -1) {
					MessageHolder.sendOrderMessage("All hotbar slots are used");
					return false;
				}
				lastCount = isCreative(player) ? 65536 : getInventory(player).getItem(slot).getCount();
				MessageHolder.sendOrderMessage("Slot at " + slot + "(" + getInventory(player).getItem(slot).getItem() + ")" + " is swapped with " + selectedSlot + "(" + getInventory(player).getNonEquipmentItems().get(selectedSlot) + ")");
				client.gameMode.handleContainerInput(player.inventoryMenu.containerId, slot, selectedSlot, ContainerInput.SWAP, player);
				getInventory(player).setSelectedSlot(selectedSlot);
				trackedSelectedSlot = selectedSlot;
				client.getConnection().send(new ServerboundSetCarriedItemPacket(selectedSlot));
			}
		}
		if (!areItemsExact(getMainHandStack(player), stack)) {
			MessageHolder.sendMessageUncheckedUnique(player, stack.toString() + " does not match with " + player.getMainHandItem().toString() + "!");
			return false;
		}
		return true;
	}

	public static List<ItemStack> getRequiredStackInSchematic(Level schematicWorld, Minecraft minecraftClient, BlockPos pos) {
		return getRequiredStackInSchematic(schematicWorld, minecraftClient, pos, -1);
	}

	public static List<ItemStack> getRequiredStackInSchematic(Level schematicWorld, Minecraft minecraftClient, BlockPos pos, int expectedSize) {
		List<ItemStack> result = new ArrayList<>();
		final LocalPlayer player = minecraftClient == null ? null : minecraftClient.player;
		if (schematicWorld == null || player == null) {
			return result;
		}
		BlockEntity blockEntity = schematicWorld.getBlockEntity(pos);
		if (expectedSize > 0) {
			List<ItemStack> combinedChest = getDoubleChestRequiredStacks(schematicWorld, player, pos, expectedSize);
			if (!combinedChest.isEmpty()) {
				return combinedChest;
			}
		}
		if (blockEntity == null) {
			if (expectedSize > 0) {
				return emptyContainerStacks(expectedSize);
			}
			return result;
		}
		if (!(blockEntity instanceof Container)) {
			return result;
		}
		Container containerBlockEntity = (Container) blockEntity;
		result.addAll(copyContainerItems(containerBlockEntity, player, pos, expectedSize > 0));
		return result;
	}

	private static List<ItemStack> emptyContainerStacks(int size) {
		List<ItemStack> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(ItemStack.EMPTY);
		}
		return result;
	}

	private static List<ItemStack> getDoubleChestRequiredStacks(Level schematicWorld, LocalPlayer player, BlockPos pos, int expectedSize) {
		List<ItemStack> result = new ArrayList<>();
		BlockState state = schematicWorld.getBlockState(pos);
		if (!(state.getBlock() instanceof ChestBlock chestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
			return result;
		}
		BlockPos otherPos = ChestBlock.getConnectedBlockPos(pos, state);
		BlockState otherState = schematicWorld.getBlockState(otherPos);
		if (!chestBlock.chestCanConnectTo(otherState) || otherState.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
			return result;
		}
		BlockEntity firstEntity;
		BlockEntity secondEntity;
		BlockPos firstPos;
		BlockPos secondPos;
		if (state.getValue(ChestBlock.TYPE) == ChestType.RIGHT) {
			firstEntity = schematicWorld.getBlockEntity(pos);
			secondEntity = schematicWorld.getBlockEntity(otherPos);
			firstPos = pos;
			secondPos = otherPos;
		} else {
			firstEntity = schematicWorld.getBlockEntity(otherPos);
			secondEntity = schematicWorld.getBlockEntity(pos);
			firstPos = otherPos;
			secondPos = pos;
		}
		if ((firstEntity != null && !(firstEntity instanceof Container))
			|| (secondEntity != null && !(secondEntity instanceof Container))) {
			return result;
		}
		result.addAll(copyChestHalfItems((Container) firstEntity, player, firstPos));
		result.addAll(copyChestHalfItems((Container) secondEntity, player, secondPos));
		return result.size() == expectedSize ? result : Collections.emptyList();
	}

	private static List<ItemStack> copyChestHalfItems(Container container, LocalPlayer player, BlockPos pos) {
		if (container == null) {
			return emptyContainerStacks(27);
		}
		return copyContainerItems(container, player, pos, true);
	}

	private static List<ItemStack> copyContainerItems(Container containerBlockEntity, LocalPlayer player, BlockPos pos, boolean includeEmptyContainer) {
		List<ItemStack> result = new ArrayList<>();
		if (containerBlockEntity.isEmpty() && !includeEmptyContainer) {
			return result;
		}
		if (canReadContainer(containerBlockEntity, player)) {
			for (int i = 0; i < containerBlockEntity.getContainerSize(); i++) {
				result.add(containerBlockEntity.getItem(i).copy());
			}
		} else {
			MessageHolder.sendMessageUncheckedUnique(player, "Container at " + pos.toShortString() + " can't be opened by player!");
		}
		return result;
	}

	private static boolean canReadContainer(Container container, LocalPlayer player) {
		if (!container.stillValid(player)) {
			return false;
		}
		if (container instanceof BaseContainerBlockEntity) {
			return ((BaseContainerBlockEntity) container).canOpen(player);
		}
		return true;
	}

	public static boolean hasItemInSchematic(Level schematicWorld, BlockPos pos) {
		if (schematicWorld == null) {
			return false;
		}
		BlockEntity blockEntity = schematicWorld.getBlockEntity(pos);
		if (blockEntity == null) {
			return false;
		}
		if (blockEntity instanceof Container) {
			Container containerBlockEntity = (Container) blockEntity;
			if (containerBlockEntity.isEmpty()) {
				return false;
			}
			for (int i = 0; i < (containerBlockEntity).getContainerSize(); i++) {
				if (!containerBlockEntity.getItem(i).isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
}
