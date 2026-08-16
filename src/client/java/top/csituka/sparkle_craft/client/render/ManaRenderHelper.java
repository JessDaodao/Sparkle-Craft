package top.csituka.sparkle_craft.client.render;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import top.csituka.sparkle_craft.sparkle_craft;

final class ManaRenderHelper {

    private static final Identifier MANA_TEXTURE = new Identifier(sparkle_craft.MOD_ID,
            "textures/block/mana.png");
    private static final float PIPE_MIN = 5.0f / 16.0f;
    private static final float PIPE_MAX = 11.0f / 16.0f;

    private ManaRenderHelper() {
    }

    static VertexConsumer getVertices(VertexConsumerProvider vertexConsumers) {
        return vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(MANA_TEXTURE));
    }

    static void renderCoreFace(VertexConsumer vertices, MatrixStack.Entry entry,
                               Direction face, int overlay) {
        renderFace(vertices, entry, face, PIPE_MIN, PIPE_MIN, PIPE_MIN,
                PIPE_MAX, PIPE_MAX, PIPE_MAX, overlay);
    }

    static void renderArm(VertexConsumer vertices, MatrixStack.Entry entry,
                          Direction direction, boolean renderEnd, int overlay) {
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
            case UP -> {
                minY = PIPE_MAX;
                maxY = 1.0f;
            }
            case DOWN -> {
                minY = 0.0f;
                maxY = PIPE_MIN;
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

    static void renderFace(VertexConsumer vertices, MatrixStack.Entry entry,
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
