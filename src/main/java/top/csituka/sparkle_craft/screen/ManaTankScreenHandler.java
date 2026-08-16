package top.csituka.sparkle_craft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;

public class ManaTankScreenHandler extends ScreenHandler {

    private static final int PLAYER_INVENTORY_END = 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;

    public ManaTankScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new ArrayPropertyDelegate(1), ScreenHandlerContext.EMPTY);
    }

    public ManaTankScreenHandler(int syncId, PlayerInventory playerInventory,
                                 ManaTankBlockEntity blockEntity,
                                 PropertyDelegate propertyDelegate) {
        this(syncId, playerInventory, propertyDelegate,
                ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos()));
    }

    private ManaTankScreenHandler(int syncId, PlayerInventory playerInventory,
                                  PropertyDelegate propertyDelegate,
                                  ScreenHandlerContext context) {
        super(ModScreenHandlers.MANA_TANK, syncId);
        checkDataCount(propertyDelegate, 1);
        this.propertyDelegate = propertyDelegate;
        this.context = context;

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
        if (slotIndex < PLAYER_INVENTORY_END) {
            if (!insertItem(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, 0, PLAYER_INVENTORY_END, false)) {
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
        return canUse(context, player, ModBlocks.MANA_TANK);
    }

    public int getMana() {
        return propertyDelegate.get(0);
    }

    public int getMaxMana() {
        return ManaTankBlockEntity.MAX_MANA;
    }

    public int getScaledMana(int height) {
        return Math.min(height, getMana() * height / getMaxMana());
    }
}
