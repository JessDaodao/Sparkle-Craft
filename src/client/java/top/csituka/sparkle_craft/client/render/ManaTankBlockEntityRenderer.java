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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.custom.ManaTankBlock;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;
import top.csituka.sparkle_craft.sparkle_craft;

public class ManaTankBlockEntityRenderer implements BlockEntityRenderer<ManaTankBlockEntity> {

    private static final Identifier MANA_TEXTURE = new Identifier(sparkle_craft.MOD_ID,
            "textures/block/mana.png");
    private static final float MIN_XZ = 3.5f / 16.0f;
    private static final float MAX_XZ = 12.5f / 16.0f;
    private static final float MIN_Y = 3.25f / 16.0f;
    private static final float MAX_Y = 12.75f / 16.0f;
    private static final float MIN_VISIBLE_HEIGHT = 1.0f / 16.0f;
    private static final float PIPE_MIN = 5.0f / 16.0f;
    private static final float PIPE_MAX = 11.0f / 16.0f;

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
        VertexConsumer vertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(MANA_TEXTURE));
        MatrixStack.Entry entry = matrices.peek();
        for (Direction face : Direction.values()) {
            renderFace(vertices, entry, face, MIN_XZ, MIN_Y, MIN_XZ,
                    MAX_XZ, manaTop, MAX_XZ, overlay);
        }
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()
                    && state.get(ManaTankBlock.getConnectionProperty(direction))
                    && hasManaContinuation(blockEntity, direction)) {
                renderConnection(vertices, entry, direction, overlay);
            }
        }
    }

    private static void renderConnection(VertexConsumer vertices, MatrixStack.Entry entry,
                                         Direction direction, int overlay) {
        float minX = PIPE_MIN;
        float minY = PIPE_MIN;
        float minZ = PIPE_MIN;
        float maxX = PIPE_MAX;
        float maxY = PIPE_MAX;
        float maxZ = PIPE_MAX;

        switch (direction) {
            case NORTH -> {
                minZ = 0.0f;
                maxZ = PIPE_MIN;
            }
            case EAST -> {
                minX = PIPE_MAX;
                maxX = 1.0f;
            }
            case SOUTH -> {
                minZ = PIPE_MAX;
                maxZ = 1.0f;
            }
            case WEST -> {
                minX = 0.0f;
                maxX = PIPE_MIN;
            }
            default -> {
                return;
            }
        }

        for (Direction face : Direction.values()) {
            if (face.getAxis() != direction.getAxis()) {
                renderFace(vertices, entry, face, minX, minY, minZ,
                        maxX, maxY, maxZ, overlay);
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
                .normal(normalMatrix, face.getOffsetX(), face.getOffsetY(), face.getOffsetZ())
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
