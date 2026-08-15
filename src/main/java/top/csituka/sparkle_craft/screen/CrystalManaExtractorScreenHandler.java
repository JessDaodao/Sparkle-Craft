package top.csituka.sparkle_craft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import top.csituka.sparkle_craft.block.entity.CrystalManaExtractorBlockEntity;
import top.csituka.sparkle_craft.item.ModItems;

public class CrystalManaExtractorScreenHandler extends ScreenHandler {

    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public CrystalManaExtractorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(MACHINE_SLOT_COUNT), new ArrayPropertyDelegate(4));
    }

    public CrystalManaExtractorScreenHandler(int syncId, PlayerInventory playerInventory,
                                             Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.CRYSTAL_MANA_EXTRACTOR, syncId);
        checkSize(inventory, MACHINE_SLOT_COUNT);
        checkDataCount(propertyDelegate, 4);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        addSlot(new Slot(inventory, 0, 80, 51) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.MAGIC_CRYSTAL);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addProperties(propertyDelegate);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!insertItem(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.isOf(ModItems.MAGIC_CRYSTAL)) {
            if (!insertItem(stack, 0, MACHINE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!insertItem(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    public int getMana() {
        return propertyDelegate.get(0);
    }

    public int getMaxMana() {
        return CrystalManaExtractorBlockEntity.MAX_MANA;
    }

    public int getScaledMana(int width) {
        return Math.min(width, getMana() * width / getMaxMana());
    }

    public boolean isConverting() {
        return propertyDelegate.get(1) > 0;
    }

    public int getInputPerSecond() {
        return propertyDelegate.get(2);
    }

    public int getOutputPerSecond() {
        return propertyDelegate.get(3);
    }
}
