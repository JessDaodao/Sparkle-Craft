package org.example.sparkle_craft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.example.sparkle_craft.block.ModBlocks;
import org.example.sparkle_craft.block.entity.FlyBeaconBlockEntity;

public class FlyBeaconScreenHandler extends ScreenHandler {

    public static final int TOGGLE_BUTTON_ID = 0;
    private static final int PLAYER_INVENTORY_END = 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;
    private final BlockPos blockPos;
    private final FlyBeaconBlockEntity blockEntity;

    public FlyBeaconScreenHandler(int syncId, PlayerInventory playerInventory,
                                  PacketByteBuf buf) {
        this(syncId, playerInventory, new ArrayPropertyDelegate(2),
                ScreenHandlerContext.EMPTY, buf.readBlockPos(), null);
    }

    public FlyBeaconScreenHandler(int syncId, PlayerInventory playerInventory,
                                  FlyBeaconBlockEntity blockEntity,
                                  PropertyDelegate propertyDelegate) {
        this(syncId, playerInventory, propertyDelegate,
                ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos()),
                blockEntity.getPos(), blockEntity);
    }

    private FlyBeaconScreenHandler(int syncId, PlayerInventory playerInventory,
                                   PropertyDelegate propertyDelegate,
                                   ScreenHandlerContext context, BlockPos blockPos,
                                   FlyBeaconBlockEntity blockEntity) {
        super(ModScreenHandlers.FLY_BEACON, syncId);
        checkDataCount(propertyDelegate, 2);
        this.propertyDelegate = propertyDelegate;
        this.context = context;
        this.blockPos = blockPos;
        this.blockEntity = blockEntity;

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
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id != TOGGLE_BUTTON_ID || blockEntity == null || !canUse(player)) {
            return false;
        }
        blockEntity.toggleEnabled();
        return true;
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
        return canUse(context, player, ModBlocks.FLY_BEACON);
    }

    public int getMana() {
        return propertyDelegate.get(0);
    }

    public int getMaxMana() {
        return FlyBeaconBlockEntity.MAX_MANA;
    }

    public int getScaledMana(int width) {
        return Math.min(width, getMana() * width / getMaxMana());
    }

    public boolean isEnabled() {
        return propertyDelegate.get(1) != 0;
    }

    public boolean isActive() {
        return isEnabled() && getMana() > 0;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
