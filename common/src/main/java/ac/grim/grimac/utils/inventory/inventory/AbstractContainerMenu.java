package ac.grim.grimac.utils.inventory.inventory;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.inventory.ClickAction;
import ac.grim.grimac.utils.inventory.Inventory;
import ac.grim.grimac.utils.inventory.slot.ResultSlot;
import ac.grim.grimac.utils.inventory.slot.Slot;
import ac.grim.grimac.utils.math.GrimMath;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractContainerMenu {
    protected final GrimPlayer player;
    // Quick crafting/dragging
    private int quickcraftStatus = 0;
    private int quickcraftType = -1;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    @Setter(AccessLevel.PROTECTED)
    private Inventory playerInventory;
    @Getter
    protected final List<Slot> slots = new ArrayList<>();
    @Getter
    private @NotNull ItemStack carried = ItemStack.EMPTY;

    public AbstractContainerMenu(GrimPlayer player, Inventory playerInventory) {
        this.player = player;
        this.playerInventory = playerInventory;
    }

    public AbstractContainerMenu(GrimPlayer player) {
        this.player = player;
    }

    public static int getQuickcraftHeader(int p_38948_) {
        return p_38948_ & 3;
    }

    public static int getQuickcraftMask(int p_38931_, int p_38932_) {
        return p_38931_ & 3 | (p_38932_ & 3) << 2;
    }

    public static int getQuickcraftType(int p_38929_) {
        return p_38929_ >> 2 & 3;
    }

    public static boolean canItemQuickReplace(@Nullable Slot p_38900_, ItemStack p_38901_, boolean p_38902_) {
        boolean flag = p_38900_ == null || !p_38900_.hasItem();
        if (!flag && ItemStack.isSameItemSameTags(p_38901_, p_38900_.getItem())) {
            return p_38900_.getItem().getAmount() + (p_38902_ ? 0 : p_38901_.getAmount()) <= p_38901_.getMaxStackSize();
        } else {
            return flag;
        }
    }

    public static int getQuickCraftSlotCount(Set<Slot> slots, int type, ItemStack itemStack) {
        switch (type) {
            case 0:
                return GrimMath.floor((float) itemStack.getAmount() / (float) slots.size());
            case 1:
                return 1;
            case 2:
                return itemStack.getType().getMaxAmount();
            default:
                return itemStack.getAmount();
        }
    }

    public Slot addSlot(Slot slot) {
        slot.slotListIndex = this.slots.size();
        this.slots.add(slot);
        return slot;
    }

    public void addFourRowPlayerInventory() {
        for (int slot = Inventory.ITEMS_START; slot < Inventory.ITEMS_END; slot++) {
            addSlot(new Slot(playerInventory.getInventoryStorage(), slot));
        }
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public boolean isValidQuickcraftType(int quickcraftType) {
        return quickcraftType == 0
                || quickcraftType == 1
                || quickcraftType == 2 && player.gamemode == GameMode.CREATIVE;
    }

    public void setCarried(ItemStack stack) {
        // Cannot be null
        carried = stack == null ? ItemStack.EMPTY : stack;
    }

    public ItemStack getPlayerInventoryItem(int slot) {
        return playerInventory.getInventoryStorage().getItem(slot);
    }

    public void setPlayerInventoryItem(int slot, ItemStack stack) {
        playerInventory.getInventoryStorage().setItem(slot, stack);
    }

    public void doClick(int button, int slotID, WrapperPlayClientClickWindow.WindowClickType clickType) {
        if (clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_CRAFT) {
            int i = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(button);
            if ((i != 1 || this.quickcraftStatus != 2) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(button);
                if (isValidQuickcraftType(this.quickcraftType)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                if (slotID < 0) return;
                Slot slot = slots.get(slotID);
                ItemStack itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && (this.quickcraftType == 2 || itemstack.getAmount() > this.quickcraftSlots.size()) && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int l = (this.quickcraftSlots.iterator().next()).slotListIndex;
                        this.resetQuickCraft();
                        this.doClick(this.quickcraftType, l, WrapperPlayClientClickWindow.WindowClickType.PICKUP);
                        return;
                    }

                    ItemStack itemstack3 = this.getCarried().copy();
                    int j1 = this.getCarried().getAmount();

                    for (Slot slot1 : this.quickcraftSlots) {
                        ItemStack itemstack1 = this.getCarried();
                        if (slot1 != null && canItemQuickReplace(slot1, itemstack1, true) && slot1.mayPlace(itemstack1) && (this.quickcraftType == 2 || itemstack1.getAmount() >= this.quickcraftSlots.size()) && this.canDragTo(slot1)) {
                            ItemStack itemstack2 = itemstack3.copy();
                            int j = slot1.hasItem() ? slot1.getItem().getAmount() : 0;
                            itemstack2.setAmount(getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemstack2) + j);
                            int k = Math.min(itemstack2.getMaxStackSize(), slot1.getMaxStackSize(itemstack2));
                            if (itemstack2.getAmount() > k) {
                                itemstack2.setAmount(k);
                            }

                            j1 -= itemstack2.getAmount() - j;
                            slot1.set(itemstack2);
                        }
                    }

                    itemstack3.setAmount(j1);
                    this.setCarried(itemstack3);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((clickType == WrapperPlayClientClickWindow.WindowClickType.PICKUP || clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
            ClickAction clickAction = ClickAction.values()[button];
            if (slotID == -999) { // Drop item
                if (!getCarried().isEmpty()) {
                    if (clickAction == ClickAction.PRIMARY) { // Drops the entire stack
                        setCarried(ItemStack.EMPTY);
                    } else { // Drops a single item
                        getCarried().split(1);
                    }
                }
            } else if (clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                if (slotID < 0) return;

                Slot stack = getSlot(slotID);
                if (!stack.mayPickup(player)) {
                    return;
                }

                ItemStack itemstack9 = this.quickMoveStack(slotID);
                while (!itemstack9.isEmpty() && ItemStack.isSameItemSameTags(stack.getItem(), itemstack9)) {
                    itemstack9 = this.quickMoveStack(slotID);
                }
            } else {
                if (slotID < 0) return;

                Slot slot = getSlot(slotID);
                ItemStack slotItem = slot.getItem();
                ItemStack carriedItem = getCarried();

                // TODO: What do we do with crafting? I think this is overkill and we shouldn't attempt to track crafting, and just resync inventory.
                // 1.17+ clients send changed itemstacks anyways, so just hack around stuff until people stop using decade old versions.
                if (slot instanceof ResultSlot) {
                    player.inventory.isPacketInventoryActive = false;
                }

                // TODO: Bundle support
                //if (!carriedItem.overrideStackedOnOther(slotItem, clickAction) && !slotItem.overrideOtherStackedOnMe(slotItem, clickAction, carriedItem)) {
                if (slotItem.isEmpty()) {
                    if (!carriedItem.isEmpty()) {
                        int l2 = clickAction == ClickAction.PRIMARY ? carriedItem.getAmount() : 1;
                        this.setCarried(slot.safeInsert(carriedItem, l2));
                    }
                } else if (slot.mayPickup(player)) {
                    if (carriedItem.isEmpty()) {
                        int i3 = clickAction == ClickAction.PRIMARY ? slotItem.getAmount() : (slotItem.getAmount() + 1) / 2;
                        Optional<ItemStack> optional1 = slot.tryRemove(i3, Integer.MAX_VALUE, player);
                        optional1.ifPresent((p_150421_) -> {
                            this.setCarried(p_150421_);
                            slot.onTake(player, p_150421_);
                        });
                    } else if (slot.mayPlace(carriedItem)) {
                        if (ItemStack.isSameItemSameTags(slotItem, carriedItem)) {
                            int j3 = clickAction == ClickAction.PRIMARY ? carriedItem.getAmount() : 1;
                            this.setCarried(slot.safeInsert(carriedItem, j3));
                        } else if (carriedItem.getAmount() <= slot.getMaxStackSize(carriedItem)) {
                            slot.set(carriedItem);
                            this.setCarried(slotItem);
                        }
                    } else if (ItemStack.isSameItemSameTags(slotItem, carriedItem)) {
                        Optional<ItemStack> optional = slot.tryRemove(slotItem.getAmount(), carriedItem.getMaxStackSize() - carriedItem.getAmount(), player);
                        optional.ifPresent((p_150428_) -> {
                            carriedItem.grow(p_150428_.getAmount());
                            slot.onTake(player, p_150428_);
                        });
                    }
                }
                //}
            }
        } else if (clickType == WrapperPlayClientClickWindow.WindowClickType.SWAP) {
            Slot hoveringSlot = slots.get(slotID);

            // How the fuck did the player SWAP with true slot 38 (chestplate?)??
            // A vanilla client can't do this... what cheat does this?
            // TODO: What cheat does this?
            if (button != 40 && (button < 0 || button >= 9)) return;

            button = button == 40 ? Inventory.SLOT_OFFHAND : button + Inventory.HOTBAR_OFFSET;

            // 40 is offhand
            ItemStack hotbarKeyStack = getPlayerInventoryItem(button);
            ItemStack hoveringItem2 = hoveringSlot.getItem();

            if (!hotbarKeyStack.isEmpty() || !hoveringItem2.isEmpty()) {
                if (hotbarKeyStack.isEmpty()) {
                    if (hoveringSlot.mayPickup(player)) {
                        setPlayerInventoryItem(button, hoveringItem2);
                        hoveringSlot.set(ItemStack.EMPTY);
                        hoveringSlot.onTake(player, hoveringItem2);
                    }
                } else if (hoveringItem2.isEmpty()) {
                    if (hoveringSlot.mayPlace(hotbarKeyStack)) {
                        int l1 = hoveringSlot.getMaxStackSize(hotbarKeyStack);
                        if (hotbarKeyStack.getAmount() > l1) {
                            hoveringSlot.set(hotbarKeyStack.split(l1));
                        } else {
                            hoveringSlot.set(hotbarKeyStack);
                            setPlayerInventoryItem(button, ItemStack.EMPTY);
                        }
                    }
                } else if (hoveringSlot.mayPickup(player) && hoveringSlot.mayPlace(hotbarKeyStack)) {
                    int i2 = hoveringSlot.getMaxStackSize(hotbarKeyStack);
                    if (hotbarKeyStack.getAmount() > i2) {
                        hoveringSlot.set(hotbarKeyStack.split(i2));
                        hoveringSlot.onTake(player, hoveringItem2);
                        playerInventory.add(hoveringItem2);
                    } else {
                        hoveringSlot.set(hotbarKeyStack);
                        setPlayerInventoryItem(button, hoveringItem2);
                        hoveringSlot.onTake(player, hoveringItem2);
                    }
                }
            }
        } else if (clickType == WrapperPlayClientClickWindow.WindowClickType.CLONE && player.gamemode == GameMode.CREATIVE && slotID >= 0 && getCarried().isEmpty()) {
            Slot slot5 = getSlot(slotID);
            if (slot5.hasItem()) {
                ItemStack itemstack6 = slot5.getItem().copy();
                itemstack6.setAmount(itemstack6.getMaxStackSize());
                this.setCarried(itemstack6);
            }
        } else if (clickType == WrapperPlayClientClickWindow.WindowClickType.THROW && getCarried().isEmpty() && slotID >= 0) {
            Slot slot = getSlot(slotID);
            int amount = button == 0 ? 1 : slot.getItem().getAmount();
            slot.safeTake(amount, Integer.MAX_VALUE, player);
        } else if (clickType == WrapperPlayClientClickWindow.WindowClickType.PICKUP_ALL && slotID >= 0) {
            if (getCarried().isEmpty()) {
                return;
            } else {
                Slot slot = getSlot(slotID);
                if (slot.hasItem() && slot.mayPickup(player)) return;
            }

            int start = button == 0 ? 0 : this.slots.size() - 1;
            int step = button == 0 ? 1 : -1;

            for (int pass = 0; pass < 2; pass++) {
                for (int i = start; i >= 0 && i < this.slots.size() && getCarried().getAmount() < getCarried().getMaxStackSize(); i += step) {
                    Slot slot = this.slots.get(i);
                    if (slot.hasItem()
                            && canItemQuickReplace(slot, getCarried(), true)
                            && slot.mayPickup(player)
                            && this.canTakeItemForPickAll(getCarried(), slot)) {
                        ItemStack itemStack = slot.getItem();
                        if (pass != 0 || itemStack.getAmount() != itemStack.getMaxStackSize()) {
                            ItemStack removed = slot.safeTake(
                                    itemStack.getAmount(),
                                    getCarried().getMaxStackSize() - getCarried().getAmount(),
                                    player
                            );
                            getCarried().grow(removed.getAmount());
                        }
                    }
                }
            }
        }
    }

    protected boolean moveItemStackTo(ItemStack toMove, int start, int end, boolean reverse) {
        boolean changed = false;
        int slotID = reverse ? end - 1 : start;

        if (toMove.getType().getMaxAmount() > 1) {
            while (!toMove.isEmpty()) {
                if (reverse && slotID < start) break;
                if (!reverse && slotID >= end) break;

                Slot slot = this.slots.get(slotID);
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(toMove, stack)) {
                    int amount = stack.getAmount() + toMove.getAmount();
                    if (amount <= toMove.getMaxStackSize()) {
                        toMove.setAmount(0);
                        stack.setAmount(amount);
                        changed = true;
                    } else if (stack.getAmount() < toMove.getMaxStackSize()) {
                        toMove.shrink(toMove.getMaxStackSize() - stack.getAmount());
                        stack.setAmount(toMove.getMaxStackSize());
                        changed = true;
                    }
                }

                if (reverse) slotID--;
                else slotID++;
            }
        }

        if (!toMove.isEmpty()) {
            slotID = reverse ? end - 1 : start;

            while (true) {
                if (reverse && slotID < start) break;
                if (!reverse && slotID >= end) break;

                Slot slot = this.slots.get(slotID);
                ItemStack stack = slot.getItem();
                if (stack.isEmpty() && slot.mayPlace(toMove)) {
                    if (toMove.getAmount() > slot.getMaxStackSize()) {
                        slot.set(toMove.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(toMove.split(toMove.getAmount()));
                    }

                    changed = true;
                    break;
                }

                if (reverse) slotID--;
                else slotID++;
            }
        }

        return changed;
    }

    public boolean canTakeItemForPickAll(ItemStack p_38908_, Slot p_38909_) {
        return true;
    }

    public ItemStack quickMoveStack(int slotID) {
        return this.slots.get(slotID).getItem();
    }

    public Slot getSlot(int slotID) {
        try {
            return this.slots.get(slotID);
        } catch (IndexOutOfBoundsException e) {
            LogUtil.error("Tried to get slot " + slotID + " in a container with only " + this.slots.size() + " slots, container type: " + this.getClass().getName(), e);
            throw e;
        }
    }

    public boolean canDragTo(Slot slot) {
        return true;
    }

    public int getMaxStackSize() {
        return 64;
    }
}
