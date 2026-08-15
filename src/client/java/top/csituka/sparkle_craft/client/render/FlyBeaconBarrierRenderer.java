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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBarrierTracker;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBarrierTracker.Barrier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FlyBeaconBarrierRenderer {

    private static final Identifier FORCEFIELD = new Identifier("textures/misc/forcefield.png");
    private static final Comparator<double[]> BY_START = Comparator.comparingDouble(hole -> hole[0]);

    private static final int SEGMENTS = 128;
    private static final long SCROLL_PERIOD_MS = 3000L;
    private static final float TEXTURE_SCALE = 0.5f;
    private static final float RED = 0.65f;
    private static final float GREEN = 0.29f;
    private static final float BLUE = 0.98f;
    private static final float ALPHA = 0.42f;
    private static final double MAX_RENDER_DISTANCE = 128.0;
    private static final double EPSILON = 1.0E-4;

    private final List<double[]> holes = new ArrayList<>();
    private final List<double[]> spans = new ArrayList<>();
    private boolean building;

    private FlyBeaconBarrierRenderer() {
    }

    public static void register() {
        FlyBeaconBarrierRenderer renderer = new FlyBeaconBarrierRenderer();
        WorldRenderEvents.AFTER_TRANSLUCENT.register(renderer::render);
    }

    private void render(WorldRenderContext context) {
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
            buildWall(buffer, matrix, barrier, barriers, camera, scroll);
            buildCap(buffer, matrix, barrier, barriers, camera, scroll, barrier.getMaxY(), true);
            buildCap(buffer, matrix, barrier, barriers, camera, scroll, barrier.getMinY(), false);
        }
        if (building) {
            draw(buffer);
        }
    }

    private void buildWall(BufferBuilder buffer, Matrix4f matrix, Barrier barrier,
                           List<Barrier> all, Vec3d camera, float scroll) {
        double radius = barrier.getRadius();
        double step = 2.0 * Math.PI / SEGMENTS;
        double textureStep = Math.round(2.0 * Math.PI * radius * TEXTURE_SCALE) / (double) SEGMENTS;

        for (int segment = 0; segment < SEGMENTS; segment++) {
            double angle = segment * step;
            double nextAngle = angle + step;
            double sampleX = barrier.getCenterX() + Math.cos(angle + step * 0.5) * radius;
            double sampleZ = barrier.getCenterZ() + Math.sin(angle + step * 0.5) * radius;

            holes.clear();
            for (Barrier other : all) {
                if (other == barrier || !containsHorizontally(other, sampleX, sampleZ)) {
                    continue;
                }
                addHole(Math.max(barrier.getMinY(), other.getMinY()),
                        Math.min(barrier.getMaxY(), other.getMaxY()));
            }
            subtract(barrier.getMinY(), barrier.getMaxY());
            if (spans.isEmpty()) {
                continue;
            }

            double x0 = barrier.getCenterX() + Math.cos(angle) * radius;
            double z0 = barrier.getCenterZ() + Math.sin(angle) * radius;
            double x1 = barrier.getCenterX() + Math.cos(nextAngle) * radius;
            double z1 = barrier.getCenterZ() + Math.sin(nextAngle) * radius;
            float u0 = scroll - (float) (segment * textureStep);
            float u1 = scroll - (float) ((segment + 1) * textureStep);

            for (double[] span : spans) {
                float v0 = scroll - (float) span[0] * TEXTURE_SCALE;
                float v1 = scroll - (float) span[1] * TEXTURE_SCALE;
                vertex(buffer, matrix, camera, x0, span[0], z0, u0, v0);
                vertex(buffer, matrix, camera, x1, span[0], z1, u1, v0);
                vertex(buffer, matrix, camera, x1, span[1], z1, u1, v1);
                vertex(buffer, matrix, camera, x0, span[1], z0, u0, v1);
            }
        }
    }

    private void buildCap(BufferBuilder buffer, Matrix4f matrix, Barrier barrier, List<Barrier> all,
                          Vec3d camera, float scroll, double y, boolean top) {
        double radius = barrier.getRadius();
        double step = 2.0 * Math.PI / SEGMENTS;

        for (int segment = 0; segment < SEGMENTS; segment++) {
            double angle = segment * step;
            double nextAngle = angle + step;
            double directionX = Math.cos(angle + step * 0.5);
            double directionZ = Math.sin(angle + step * 0.5);

            holes.clear();
            for (Barrier other : all) {
                if (other == barrier || !coversLevel(other, barrier, y, top)) {
                    continue;
                }
                double offsetX = barrier.getCenterX() - other.getCenterX();
                double offsetZ = barrier.getCenterZ() - other.getCenterZ();
                double projection = offsetX * directionX + offsetZ * directionZ;
                double limit = other.getRadius() - EPSILON;
                double discriminant = projection * projection
                        - (offsetX * offsetX + offsetZ * offsetZ - limit * limit);
                if (discriminant <= 0.0) {
                    continue;
                }
                double root = Math.sqrt(discriminant);
                addHole(Math.max(0.0, -projection - root), Math.min(radius, -projection + root));
            }
            subtract(0.0, radius);
            if (spans.isEmpty()) {
                continue;
            }

            double cos0 = Math.cos(angle);
            double sin0 = Math.sin(angle);
            double cos1 = Math.cos(nextAngle);
            double sin1 = Math.sin(nextAngle);

            for (double[] span : spans) {
                double innerX0 = barrier.getCenterX() + cos0 * span[0];
                double innerZ0 = barrier.getCenterZ() + sin0 * span[0];
                double innerX1 = barrier.getCenterX() + cos1 * span[0];
                double innerZ1 = barrier.getCenterZ() + sin1 * span[0];
                double outerX0 = barrier.getCenterX() + cos0 * span[1];
                double outerZ0 = barrier.getCenterZ() + sin0 * span[1];
                double outerX1 = barrier.getCenterX() + cos1 * span[1];
                double outerZ1 = barrier.getCenterZ() + sin1 * span[1];
                capVertex(buffer, matrix, camera, innerX0, y, innerZ0, scroll);
                capVertex(buffer, matrix, camera, innerX1, y, innerZ1, scroll);
                capVertex(buffer, matrix, camera, outerX1, y, outerZ1, scroll);
                capVertex(buffer, matrix, camera, outerX0, y, outerZ0, scroll);
            }
        }
    }

    private static boolean coversLevel(Barrier other, Barrier barrier, double y, boolean top) {
        if (y > other.getMinY() + EPSILON && y < other.getMaxY() - EPSILON) {
            return true;
        }
        double plane = top ? other.getMaxY() : other.getMinY();
        return Math.abs(y - plane) < EPSILON && other.getKey() < barrier.getKey();
    }

    private static boolean containsHorizontally(Barrier barrier, double x, double z) {
        double dx = x - barrier.getCenterX();
        double dz = z - barrier.getCenterZ();
        double limit = barrier.getRadius() - EPSILON;
        return dx * dx + dz * dz < limit * limit;
    }

    private static boolean isTooFar(Barrier barrier, Vec3d camera) {
        double dx = barrier.getCenterX() - camera.x;
        double dz = barrier.getCenterZ() - camera.z;
        double dy = MathHelper.clamp(camera.y, barrier.getMinY(), barrier.getMaxY()) - camera.y;
        double limit = MAX_RENDER_DISTANCE + barrier.getRadius();
        return dx * dx + dz * dz > limit * limit || Math.abs(dy) > MAX_RENDER_DISTANCE;
    }

    private void addHole(double from, double to) {
        if (to - from > EPSILON) {
            holes.add(new double[]{from, to});
        }
    }

    private void subtract(double from, double to) {
        spans.clear();
        if (holes.isEmpty()) {
            spans.add(new double[]{from, to});
            return;
        }

        holes.sort(BY_START);
        double cursor = from;
        for (double[] hole : holes) {
            if (hole[0] - cursor > EPSILON) {
                spans.add(new double[]{cursor, hole[0]});
            }
            cursor = Math.max(cursor, hole[1]);
            if (to - cursor <= EPSILON) {
                return;
            }
        }
        spans.add(new double[]{cursor, to});
    }

    private void capVertex(BufferBuilder buffer, Matrix4f matrix, Vec3d camera,
                           double x, double y, double z, float scroll) {
        vertex(buffer, matrix, camera, x, y, z,
                scroll - (float) x * TEXTURE_SCALE, scroll - (float) z * TEXTURE_SCALE);
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
