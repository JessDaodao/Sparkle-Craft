package top.csituka.sparkle_craft.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.entity.ManaPipeBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;

public class ManaPipeBlockEntityRenderer implements BlockEntityRenderer<ManaPipeBlockEntity> {

    public ManaPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(ManaPipeBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = blockEntity.getCachedState();
        if (!state.isOf(ModBlocks.MANA_PIPE) || !state.get(ManaPipeBlock.HAS_MANA)) {
            return;
        }

        VertexConsumer vertices = ManaRenderHelper.getVertices(vertexConsumers);
        MatrixStack.Entry entry = matrices.peek();
        for (Direction direction : Direction.values()) {
            if (state.get(ManaPipeBlock.getConnectionProperty(direction))) {
                ManaRenderHelper.renderArm(vertices, entry, direction,
                        !hasManaContinuation(blockEntity, direction), overlay);
            } else {
                ManaRenderHelper.renderCoreFace(vertices, entry, direction, overlay);
            }
        }
    }

    private static boolean hasManaContinuation(ManaPipeBlockEntity blockEntity,
                                               Direction direction) {
        if (blockEntity.getWorld() == null) {
            return false;
        }
        BlockState neighborState = blockEntity.getWorld().getBlockState(
                blockEntity.getPos().offset(direction));
        BlockEntity neighbor = blockEntity.getWorld().getBlockEntity(
                blockEntity.getPos().offset(direction));
        return neighborState.isOf(ModBlocks.CRYSTAL_MANA_EXTRACTOR)
                || (neighborState.isOf(ModBlocks.MANA_PIPE)
                && neighborState.get(ManaPipeBlock.HAS_MANA))
                || (neighbor instanceof ManaTankBlockEntity tank && tank.getMana() > 0);
    }

}
