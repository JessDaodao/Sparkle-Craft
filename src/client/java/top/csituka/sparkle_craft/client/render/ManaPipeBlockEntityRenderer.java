package top.csituka.sparkle_craft.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.entity.ManaPipeBlockEntity;
import top.csituka.sparkle_craft.sparkle_craft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ManaPipeBlockEntityRenderer implements BlockEntityRenderer<ManaPipeBlockEntity> {

    private static final Identifier MANA_TEXTURE = new Identifier(sparkle_craft.MOD_ID,
            "textures/block/mana.png");
    private static final float MIN = 5.0f / 16.0f;
    private static final float MAX = 11.0f / 16.0f;

    public ManaPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(ManaPipeBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = blockEntity.getCachedState();
        if (!state.isOf(ModBlocks.MANA_PIPE) || !state.get(ManaPipeBlock.HAS_MANA)) {
            return;
        }

        VertexConsumer vertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(MANA_TEXTURE));
        MatrixStack.Entry entry = matrices.peek();
        for (Direction direction : Direction.values()) {
            if (state.get(ManaPipeBlock.getConnectionProperty(direction))) {
                renderArm(vertices, entry, direction,
                        !hasManaContinuation(blockEntity, direction), overlay);
            } else {
                renderFace(vertices, entry, direction, MIN, MIN, MIN, MAX, MAX, MAX, overlay);
            }
        }
    }

    private static void renderArm(VertexConsumer vertices, MatrixStack.Entry entry,
                                  Direction direction, boolean renderEnd, int overlay) {
        float minX = MIN;
        float minY = MIN;
        float minZ = MIN;
        float maxX = MAX;
        float maxY = MAX;
        float maxZ = MAX;

        switch (direction) {
            case NORTH -> {
                minZ = 0.0f;
                maxZ = MIN;
            }
            case EAST -> {
                minX = MAX;
                maxX = 1.0f;
            }
            case SOUTH -> {
                minZ = MAX;
                maxZ = 1.0f;
            }
            case WEST -> {
                minX = 0.0f;
                maxX = MIN;
            }
            case UP -> {
                minY = MAX;
                maxY = 1.0f;
            }
            case DOWN -> {
                minY = 0.0f;
                maxY = MIN;
            }
        }

        for (Direction face : Direction.values()) {
            if (face.getAxis() != direction.getAxis()) {
                renderFace(vertices, entry, face, minX, minY, minZ,
                        maxX, maxY, maxZ, overlay);
            }
        }
        if (renderEnd) {
            renderFace(vertices, entry, direction, minX, minY, minZ,
                    maxX, maxY, maxZ, overlay);
        }
    }

    private static boolean hasManaContinuation(ManaPipeBlockEntity blockEntity,
                                               Direction direction) {
        if (blockEntity.getWorld() == null) {
            return false;
        }
        BlockState neighborState = blockEntity.getWorld().getBlockState(
                blockEntity.getPos().offset(direction));
        return neighborState.isOf(ModBlocks.CRYSTAL_MANA_EXTRACTOR)
                || (neighborState.isOf(ModBlocks.MANA_PIPE)
                && neighborState.get(ManaPipeBlock.HAS_MANA));
    }

    private static void renderFace(VertexConsumer vertices, MatrixStack.Entry entry,
                                   Direction face, float minX, float minY, float minZ,
                                   float maxX, float maxY, float maxZ, int overlay) {
        switch (face) {
            case DOWN -> {
                vertex(vertices, entry, face, minX, minY, minZ, overlay);
                vertex(vertices, entry, face, maxX, minY, minZ, overlay);
                vertex(vertices, entry, face, maxX, minY, maxZ, overlay);
                vertex(vertices, entry, face, minX, minY, maxZ, overlay);
            }
            case UP -> {
                vertex(vertices, entry, face, minX, maxY, minZ, overlay);
                vertex(vertices, entry, face, minX, maxY, maxZ, overlay);
                vertex(vertices, entry, face, maxX, maxY, maxZ, overlay);
                vertex(vertices, entry, face, maxX, maxY, minZ, overlay);
            }
            case NORTH -> {
                vertex(vertices, entry, face, minX, minY, minZ, overlay);
                vertex(vertices, entry, face, minX, maxY, minZ, overlay);
                vertex(vertices, entry, face, maxX, maxY, minZ, overlay);
                vertex(vertices, entry, face, maxX, minY, minZ, overlay);
            }
            case SOUTH -> {
                vertex(vertices, entry, face, minX, minY, maxZ, overlay);
                vertex(vertices, entry, face, maxX, minY, maxZ, overlay);
                vertex(vertices, entry, face, maxX, maxY, maxZ, overlay);
                vertex(vertices, entry, face, minX, maxY, maxZ, overlay);
            }
            case WEST -> {
                vertex(vertices, entry, face, minX, minY, minZ, overlay);
                vertex(vertices, entry, face, minX, minY, maxZ, overlay);
                vertex(vertices, entry, face, minX, maxY, maxZ, overlay);
                vertex(vertices, entry, face, minX, maxY, minZ, overlay);
            }
            case EAST -> {
                vertex(vertices, entry, face, maxX, minY, minZ, overlay);
                vertex(vertices, entry, face, maxX, maxY, minZ, overlay);
                vertex(vertices, entry, face, maxX, maxY, maxZ, overlay);
                vertex(vertices, entry, face, maxX, minY, maxZ, overlay);
            }
        }
    }

    private static void vertex(VertexConsumer vertices, MatrixStack.Entry entry, Direction face,
                               float x, float y, float z, int overlay) {
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
        vertices.vertex(positionMatrix, x, y, z)
                .color(255, 255, 255, 255)
                .texture(getU(face, x, z), getV(face, y, z))
                .overlay(overlay)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(normalMatrix, 0.0f, 1.0f, 0.0f)
                .next();
    }

    private static float getU(Direction face, float x, float z) {
        return switch (face) {
            case DOWN, UP, SOUTH -> x;
            case NORTH -> 1.0f - x;
            case WEST -> z;
            case EAST -> 1.0f - z;
        };
    }

    private static float getV(Direction face, float y, float z) {
        return switch (face) {
            case DOWN -> 1.0f - z;
            case UP -> z;
            case NORTH, SOUTH, WEST, EAST -> 1.0f - y;
        };
    }
}
