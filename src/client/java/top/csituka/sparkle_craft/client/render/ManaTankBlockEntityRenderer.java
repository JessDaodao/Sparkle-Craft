package top.csituka.sparkle_craft.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.custom.LargeManaTankBlock;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.custom.ManaTankBlock;
import top.csituka.sparkle_craft.block.entity.LargeManaTankBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;

public class ManaTankBlockEntityRenderer<T extends ManaTankBlockEntity>
        implements BlockEntityRenderer<T> {

    private static final float MIN_XZ = 3.5f / 16.0f;
    private static final float MAX_XZ = 12.5f / 16.0f;
    private static final float MIN_Y = 3.25f / 16.0f;
    private static final float MAX_Y = 12.75f / 16.0f;
    private static final float LARGE_MAX_Y = 1.0f + MAX_Y;
    private static final float MIN_VISIBLE_HEIGHT = 1.0f / 16.0f;

    public ManaTankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(T blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = blockEntity.getCachedState();
        if (state.isOf(ModBlocks.MANA_TANK)) {
            renderSingleTank(blockEntity, state, matrices, vertexConsumers, overlay);
        } else if (state.isOf(ModBlocks.LARGE_MANA_TANK)
                && blockEntity instanceof LargeManaTankBlockEntity largeTank
                && largeTank.isController()) {
            renderLargeTank(largeTank, state, matrices, vertexConsumers, overlay);
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(T blockEntity) {
        return blockEntity.getCachedState().isOf(ModBlocks.LARGE_MANA_TANK);
    }

    private static void renderSingleTank(ManaTankBlockEntity blockEntity, BlockState state,
                                         MatrixStack matrices,
                                         VertexConsumerProvider vertexConsumers, int overlay) {
        if (blockEntity.getMana() <= 0) {
            return;
        }
        VertexConsumer vertices = ManaRenderHelper.getVertices(vertexConsumers);
        renderFluid(vertices, matrices.peek(), blockEntity.getMana(), blockEntity.getMaxMana(),
                MIN_XZ, MAX_XZ, MIN_XZ, MAX_XZ, MAX_Y, overlay);
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (state.get(ManaTankBlock.getConnectionProperty(direction))
                    && hasManaContinuation(blockEntity, blockEntity.getPos(), direction)) {
                ManaRenderHelper.renderArm(vertices, matrices.peek(), direction, false, overlay);
            }
        }
    }

    private static void renderLargeTank(LargeManaTankBlockEntity blockEntity, BlockState state,
                                        MatrixStack matrices,
                                        VertexConsumerProvider vertexConsumers, int overlay) {
        if (blockEntity.getMana() <= 0 || blockEntity.getWorld() == null) {
            return;
        }

        BlockPos controllerPos = blockEntity.getPos();
        BlockPos[] positions = LargeManaTankBlock.getStructurePositions(controllerPos,
                state.get(LargeManaTankBlock.FACING));
        int minX = positions[0].getX();
        int minZ = positions[0].getZ();
        for (BlockPos partPos : positions) {
            minX = Math.min(minX, partPos.getX());
            minZ = Math.min(minZ, partPos.getZ());
        }

        VertexConsumer vertices = ManaRenderHelper.getVertices(vertexConsumers);
        renderFluid(vertices, matrices.peek(), blockEntity.getMana(), blockEntity.getMaxMana(),
                minX - controllerPos.getX() + MIN_XZ,
                minX - controllerPos.getX() + 2.0f - (1.0f - MAX_XZ),
                minZ - controllerPos.getZ() + MIN_XZ,
                minZ - controllerPos.getZ() + 2.0f - (1.0f - MAX_XZ),
                LARGE_MAX_Y, overlay);

        for (BlockPos partPos : positions) {
            BlockState partState = blockEntity.getWorld().getBlockState(partPos);
            if (!partState.isOf(ModBlocks.LARGE_MANA_TANK)) {
                continue;
            }
            matrices.push();
            matrices.translate(partPos.getX() - controllerPos.getX(),
                    partPos.getY() - controllerPos.getY(),
                    partPos.getZ() - controllerPos.getZ());
            for (Direction direction : Direction.Type.HORIZONTAL) {
                if (partState.get(ManaTankBlock.getConnectionProperty(direction))
                        && hasManaContinuation(blockEntity, partPos, direction)) {
                    ManaRenderHelper.renderArm(vertices, matrices.peek(), direction, false, overlay);
                }
            }
            matrices.pop();
        }
    }

    private static void renderFluid(VertexConsumer vertices, MatrixStack.Entry entry,
                                    int mana, int maxMana,
                                    float minX, float maxX, float minZ, float maxZ,
                                    float maxY, int overlay) {
        float fillRatio = mana / (float) maxMana;
        float fillHeight = Math.max(MIN_VISIBLE_HEIGHT, (maxY - MIN_Y) * fillRatio);
        float manaTop = Math.min(maxY, MIN_Y + fillHeight);
        for (Direction face : Direction.values()) {
            ManaRenderHelper.renderFace(vertices, entry, face, minX, MIN_Y, minZ,
                    maxX, manaTop, maxZ, overlay);
        }
    }

    private static boolean hasManaContinuation(ManaTankBlockEntity blockEntity, BlockPos pos,
                                               Direction direction) {
        if (blockEntity.getWorld() == null) {
            return false;
        }
        BlockState neighborState = blockEntity.getWorld().getBlockState(pos.offset(direction));
        return neighborState.isOf(ModBlocks.MANA_PIPE)
                && neighborState.get(ManaPipeBlock.HAS_MANA);
    }
}
