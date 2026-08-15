package top.csituka.sparkle_craft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBarrierTracker;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBarrierTracker.Barrier;
import top.csituka.sparkle_craft.config.ModConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FlyBeaconBarrierRenderer {

    private static final Identifier FORCEFIELD = new Identifier("textures/misc/forcefield.png");

    private static final int AXIS_X = FlyBeaconBarrierTracker.AXIS_X;
    private static final int AXIS_Y = FlyBeaconBarrierTracker.AXIS_Y;
    private static final int AXIS_Z = FlyBeaconBarrierTracker.AXIS_Z;
    private static final int[] TANGENT_U = {AXIS_Z, AXIS_X, AXIS_X};
    private static final int[] TANGENT_V = {AXIS_Y, AXIS_Z, AXIS_Y};

    private static final long SCROLL_PERIOD_MS = 3000L;
    private static final float TEXTURE_SCALE = 0.5f;
    private static final float RED = 0.65f;
    private static final float GREEN = 0.29f;
    private static final float BLUE = 0.98f;
    private static final float ALPHA = 0.42f;
    private static final double MAX_RENDER_DISTANCE = 128.0;
    private static final double EPSILON = 1.0E-4;

    private final List<double[]> holes = new ArrayList<>();
    private final double[] point = new double[3];
    private boolean building;

    private FlyBeaconBarrierRenderer() {
    }

    public static void register() {
        FlyBeaconBarrierRenderer renderer = new FlyBeaconBarrierRenderer();
        WorldRenderEvents.AFTER_TRANSLUCENT.register(renderer::render);
    }

    private void render(WorldRenderContext context) {
        if (!ModConfig.showFlyBeaconBarrier()) {
            return;
        }
        ClientWorld world = context.world();
        if (world == null) {
            return;
        }

        List<Barrier> barriers = FlyBeaconBarrierTracker.collect(world);
        if (barriers.isEmpty()) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();
        float scroll = (float) (Util.getMeasuringTimeMs() % SCROLL_PERIOD_MS) / SCROLL_PERIOD_MS;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        building = false;
        for (Barrier barrier : barriers) {
            if (isTooFar(barrier, camera)) {
                continue;
            }
            for (int axis = AXIS_X; axis <= AXIS_Z; axis++) {
                buildFace(buffer, matrix, barrier, barriers, camera, scroll, axis, false);
                buildFace(buffer, matrix, barrier, barriers, camera, scroll, axis, true);
            }
        }
        if (building) {
            draw(buffer);
        }
    }

    private void buildFace(BufferBuilder buffer, Matrix4f matrix, Barrier barrier, List<Barrier> all,
                           Vec3d camera, float scroll, int axis, boolean positive) {
        int axisU = TANGENT_U[axis];
        int axisV = TANGENT_V[axis];
        double plane = positive ? barrier.getMax(axis) : barrier.getMin(axis);
        double minU = barrier.getMin(axisU);
        double maxU = barrier.getMax(axisU);
        double minV = barrier.getMin(axisV);
        double maxV = barrier.getMax(axisV);

        holes.clear();
        for (Barrier other : all) {
            if (other == barrier || !occludes(other, barrier, axis, plane, positive)) {
                continue;
            }
            double holeMinU = Math.max(minU, other.getMin(axisU));
            double holeMaxU = Math.min(maxU, other.getMax(axisU));
            double holeMinV = Math.max(minV, other.getMin(axisV));
            double holeMaxV = Math.min(maxV, other.getMax(axisV));
            if (holeMaxU - holeMinU > EPSILON && holeMaxV - holeMinV > EPSILON) {
                holes.add(new double[]{holeMinU, holeMaxU, holeMinV, holeMaxV});
            }
        }

        double[] gridU = new double[2 + holes.size() * 2];
        double[] gridV = new double[2 + holes.size() * 2];
        int countU = collectEdges(gridU, minU, maxU, 0);
        int countV = collectEdges(gridV, minV, maxV, 2);

        for (int i = 0; i + 1 < countU; i++) {
            for (int j = 0; j + 1 < countV; j++) {
                if (isCovered((gridU[i] + gridU[i + 1]) * 0.5, (gridV[j] + gridV[j + 1]) * 0.5)) {
                    continue;
                }
                faceVertex(buffer, matrix, camera, axis, plane, gridU[i], gridV[j], scroll);
                faceVertex(buffer, matrix, camera, axis, plane, gridU[i + 1], gridV[j], scroll);
                faceVertex(buffer, matrix, camera, axis, plane, gridU[i + 1], gridV[j + 1], scroll);
                faceVertex(buffer, matrix, camera, axis, plane, gridU[i], gridV[j + 1], scroll);
            }
        }
    }

    private static boolean occludes(Barrier other, Barrier barrier, int axis, double plane,
                                    boolean positive) {
        double otherMin = other.getMin(axis);
        double otherMax = other.getMax(axis);
        if (plane > otherMin + EPSILON && plane < otherMax - EPSILON) {
            return true;
        }
        if (Math.abs(plane - (positive ? otherMin : otherMax)) < EPSILON) {
            return true;
        }
        return Math.abs(plane - (positive ? otherMax : otherMin)) < EPSILON
                && other.getKey() < barrier.getKey();
    }

    private int collectEdges(double[] edges, double from, double to, int offset) {
        int count = 0;
        edges[count++] = from;
        edges[count++] = to;
        for (double[] hole : holes) {
            for (int side = 0; side < 2; side++) {
                double edge = hole[offset + side];
                if (edge > from + EPSILON && edge < to - EPSILON) {
                    edges[count++] = edge;
                }
            }
        }

        Arrays.sort(edges, 0, count);
        int unique = 1;
        for (int i = 1; i < count; i++) {
            if (edges[i] - edges[unique - 1] > EPSILON) {
                edges[unique++] = edges[i];
            }
        }
        return unique;
    }

    private boolean isCovered(double u, double v) {
        for (double[] hole : holes) {
            if (u > hole[0] && u < hole[1] && v > hole[2] && v < hole[3]) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTooFar(Barrier barrier, Vec3d camera) {
        return axisDistance(camera.x, barrier, AXIS_X) > MAX_RENDER_DISTANCE
                || axisDistance(camera.y, barrier, AXIS_Y) > MAX_RENDER_DISTANCE
                || axisDistance(camera.z, barrier, AXIS_Z) > MAX_RENDER_DISTANCE;
    }

    private static double axisDistance(double value, Barrier barrier, int axis) {
        return Math.max(0.0, Math.max(barrier.getMin(axis) - value, value - barrier.getMax(axis)));
    }

    private void faceVertex(BufferBuilder buffer, Matrix4f matrix, Vec3d camera, int axis,
                            double plane, double u, double v, float scroll) {
        point[axis] = plane;
        point[TANGENT_U[axis]] = u;
        point[TANGENT_V[axis]] = v;
        vertex(buffer, matrix, camera, point[AXIS_X], point[AXIS_Y], point[AXIS_Z],
                scroll - (float) u * TEXTURE_SCALE, scroll - (float) v * TEXTURE_SCALE);
    }

    private void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3d camera,
                        double x, double y, double z, float u, float v) {
        if (!building) {
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            building = true;
        }
        buffer.vertex(matrix, (float) (x - camera.x), (float) (y - camera.y),
                        (float) (z - camera.z))
                .texture(u, v)
                .next();
    }

    private void draw(BufferBuilder buffer) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        RenderSystem.setShaderTexture(0, FORCEFIELD);
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.setShaderColor(RED, GREEN, BLUE, ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.polygonOffset(-3.0f, -3.0f);
        RenderSystem.enablePolygonOffset();
        RenderSystem.disableCull();

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        building = false;

        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }
}
