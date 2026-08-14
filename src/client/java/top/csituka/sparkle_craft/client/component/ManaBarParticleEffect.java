package top.csituka.sparkle_craft.client.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.util.Random;

public class ManaBarParticleEffect {

    private static final float TRANSITION_SPEED = 5.0f;

    private final Random random = new Random();
    private final ManaDot[] dots;
    private final int color;
    private boolean active;
    private boolean initialized;
    private float activeAmount;
    private long lastAnimationTime;

    public ManaBarParticleEffect(int dotCount, int color) {
        dots = new ManaDot[dotCount];
        this.color = color;
        for (int index = 0; index < dots.length; index++) {
            dots[index] = new ManaDot();
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void render(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        long animationTime = Util.getMeasuringTimeMs();
        if (!initialized) {
            initializeDots(width, height);
            activeAmount = active ? 1.0f : 0.0f;
            lastAnimationTime = animationTime;
            initialized = true;
        }

        float elapsedSeconds = Math.min(Math.max(animationTime - lastAnimationTime, 0L) / 1000.0f, 0.1f);
        lastAnimationTime = animationTime;
        float targetActiveAmount = active ? 1.0f : 0.0f;
        float easing = 1.0f - (float) Math.exp(-TRANSITION_SPEED * elapsedSeconds);
        activeAmount += (targetActiveAmount - activeAmount) * easing;
        if (Math.abs(targetActiveAmount - activeAmount) < 0.001f) {
            activeAmount = targetActiveAmount;
        }

        for (ManaDot dot : dots) {
            updateDot(dot, width, height, elapsedSeconds);
            int dotX = Math.round(dot.x + (float) Math.sin(dot.swayPhase) * dot.swayAmount);
            int dotY = Math.round(dot.y);
            drawClippedDot(context, x, y, width, height, dotX, dotY, dot.size);
        }
    }

    private void initializeDots(int width, int height) {
        for (ManaDot dot : dots) {
            randomizeDot(dot);
            dot.x = random.nextFloat() * width;
            dot.y = random.nextFloat() * height;
        }
    }

    private void updateDot(ManaDot dot, int width, int height, float elapsedSeconds) {
        dot.swayPhase += dot.swaySpeed * elapsedSeconds;
        dot.x += dot.horizontalSpeed * activeAmount * elapsedSeconds;
        dot.y -= dot.risingSpeed * (1.0f - activeAmount) * elapsedSeconds;

        if (dot.x >= width + dot.size || dot.y + dot.size < 0) {
            respawnDot(dot, width, height);
        }
    }

    private void respawnDot(ManaDot dot, int width, int height) {
        randomizeDot(dot);
        if (activeAmount >= 0.5f) {
            dot.x = 0;
            dot.y = random.nextFloat() * height;
        } else {
            dot.x = random.nextFloat() * width;
            dot.y = height - dot.size;
        }
    }

    private void randomizeDot(ManaDot dot) {
        dot.size = random.nextBoolean() ? 1 : 2;
        dot.horizontalSpeed = 12.0f + random.nextFloat() * 12.0f;
        dot.risingSpeed = 2.0f + random.nextFloat() * 2.0f;
        dot.swayAmount = 0.35f + random.nextFloat() * 0.9f;
        dot.swaySpeed = 1.5f + random.nextFloat() * 2.5f;
        dot.swayPhase = random.nextFloat() * (float) (Math.PI * 2.0);
    }

    private void drawClippedDot(DrawContext context, int x, int y, int width, int height,
                                int dotX, int dotY, int size) {
        int left = Math.max(0, dotX);
        int top = Math.max(0, dotY);
        int right = Math.min(width, dotX + size);
        int bottom = Math.min(height, dotY + size);
        if (left < right && top < bottom) {
            context.fill(x + left, y + top, x + right, y + bottom, color);
        }
    }

    private static class ManaDot {
        private float x;
        private float y;
        private float horizontalSpeed;
        private float risingSpeed;
        private float swayPhase;
        private float swaySpeed;
        private float swayAmount;
        private int size;
    }
}
