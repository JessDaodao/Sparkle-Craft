package top.csituka.sparkle_craft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.csituka.sparkle_craft.block.custom.LargeManaTankBlock;

public class LargeManaTankBlockEntity extends ManaTankBlockEntity {

    public static final int MAX_MANA = 25000;

    public LargeManaTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LARGE_MANA_TANK, pos, state);
    }

    @Override
    public int getMana() {
        LargeManaTankBlockEntity controller = getController();
        return controller == null ? 0 : controller == this ? super.getMana() : controller.getMana();
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int receiveMana(int amount) {
        LargeManaTankBlockEntity controller = getController();
        return controller == null ? 0
                : controller == this ? super.receiveMana(amount) : controller.receiveMana(amount);
    }

    @Override
    public int extractMana(int amount) {
        LargeManaTankBlockEntity controller = getController();
        return controller == null ? 0
                : controller == this ? super.extractMana(amount) : controller.extractMana(amount);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.sparkle-craft.large_mana_tank");
    }

    public boolean isController() {
        return LargeManaTankBlock.isController(getCachedState());
    }

    public static void tick(World world, BlockPos pos, BlockState state,
                            LargeManaTankBlockEntity blockEntity) {
        if (LargeManaTankBlock.isController(state)) {
            ManaTankBlockEntity.tick(world, pos, state, blockEntity);
        }
    }

    private LargeManaTankBlockEntity getController() {
        if (isController()) {
            return this;
        }
        if (world == null) {
            return null;
        }
        BlockPos controllerPos = LargeManaTankBlock.getControllerPos(pos, getCachedState());
        BlockEntity blockEntity = world.getBlockEntity(controllerPos);
        if (blockEntity instanceof LargeManaTankBlockEntity controller
                && controller.isController()) {
            return controller;
        }
        return null;
    }
}
