package org.example.sparkle_craft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.example.sparkle_craft.item.ModItems;
import org.example.sparkle_craft.screen.CrystalManaExtractorScreenHandler;

public class CrystalManaExtractorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory {

    public static final int MAX_MANA = 1000;
    public static final int MANA_PER_CRYSTAL = 200;
    public static final int CONVERSION_TIME = 20 * 20;
    private static final int INPUT_SLOT = 0;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int conversionTicksRemaining;
    private int mana;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mana;
                case 1 -> conversionTicksRemaining;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                mana = value;
            } else if (index == 1) {
                conversionTicksRemaining = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public CrystalManaExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_MANA_EXTRACTOR, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state,
                            CrystalManaExtractorBlockEntity blockEntity) {
        boolean changed = false;
        if (blockEntity.conversionTicksRemaining > 0) {
            int manaAppliedBeforeTick = (CONVERSION_TIME - blockEntity.conversionTicksRemaining)
                    * MANA_PER_CRYSTAL / CONVERSION_TIME;
            blockEntity.conversionTicksRemaining--;
            int manaAppliedThisTick = (CONVERSION_TIME - blockEntity.conversionTicksRemaining)
                    * MANA_PER_CRYSTAL / CONVERSION_TIME;
            blockEntity.mana = Math.min(MAX_MANA,
                    blockEntity.mana + manaAppliedThisTick - manaAppliedBeforeTick);
            changed = true;
            if (blockEntity.conversionTicksRemaining > 0) {
                blockEntity.markDirty();
                return;
            }
        }

        ItemStack input = blockEntity.items.get(INPUT_SLOT);
        if (input.isOf(ModItems.MAGIC_CRYSTAL)
                && MAX_MANA - blockEntity.mana >= MANA_PER_CRYSTAL) {
            input.decrement(1);
            blockEntity.conversionTicksRemaining = CONVERSION_TIME;
            changed = true;
        }

        if (changed) {
            blockEntity.markDirty();
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.sparkle-craft.crystal_mana_extractor");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CrystalManaExtractorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);
        nbt.putInt("Mana", mana);
        nbt.putInt("ConversionTicksRemaining", conversionTicksRemaining);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        items.set(INPUT_SLOT, ItemStack.EMPTY);
        Inventories.readNbt(nbt, items);
        mana = Math.max(0, Math.min(MAX_MANA, nbt.getInt("Mana")));
        conversionTicksRemaining = Math.max(0,
                Math.min(CONVERSION_TIME, nbt.getInt("ConversionTicksRemaining")));
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.get(INPUT_SLOT).isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = Inventories.splitStack(items, slot, amount);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = Inventories.removeStack(items, slot);
        if (!removed.isEmpty()) {
            markDirty();
        }
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot == INPUT_SLOT && stack.isOf(ModItems.MAGIC_CRYSTAL);
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }
}
