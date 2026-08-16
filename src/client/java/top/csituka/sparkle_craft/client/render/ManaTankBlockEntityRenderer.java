package top.csituka.sparkle_craft.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.custom.ManaTankBlock;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;

public class ManaTankBlockEntityRenderer implements BlockEntityRenderer<ManaTankBlockEntity> {

    private static final float MIN_XZ = 3.5f / 16.0f;
    private static final float MAX_XZ = 12.5f / 16.0f;
    private static final float MIN_Y = 3.25f / 16.0f;
    private static final float MAX_Y = 12.75f / 16.0f;
    private static final float MIN_VISIBLE_HEIGHT = 1.0f / 16.0f;

    public ManaTankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(ManaTankBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = blockEntity.getCachedState();
        if (!state.isOf(ModBlocks.MANA_TANK) || blockEntity.getMana() <= 0) {
            return;
        }

        float fillRatio = blockEntity.getMana() / (float) ManaTankBlockEntity.MAX_MANA;
        float fillHeight = Math.max(MIN_VISIBLE_HEIGHT, (MAX_Y - MIN_Y) * fillRatio);
        float manaTop = Math.min(MAX_Y, MIN_Y + fillHeight);
        VertexConsumer vertices = ManaRenderHelper.getVertices(vertexConsumers);
        MatrixStack.Entry entry = matrices.peek();
        for (Direction face : Direction.values()) {
            ManaRenderHelper.renderFace(vertices, entry, face, MIN_XZ, MIN_Y, MIN_XZ,
                    MAX_XZ, manaTop, MAX_XZ, overlay);
        }
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()
                    && state.get(ManaTankBlock.getConnectionProperty(direction))
                    && hasManaContinuation(blockEntity, direction)) {
                ManaRenderHelper.renderArm(vertices, entry, direction, false, overlay);
            }
        }
    }

    private static boolean hasManaContinuation(ManaTankBlockEntity blockEntity,
                                               Direction direction) {
        if (blockEntity.getWorld() == null) {
            return false;
        }
        BlockState neighborState = blockEntity.getWorld().getBlockState(
                blockEntity.getPos().offset(direction));
        return neighborState.isOf(ModBlocks.MANA_PIPE)
                && neighborState.get(ManaPipeBlock.HAS_MANA);
    }

}
