package top.csituka.sparkle_craft.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import top.csituka.sparkle_craft.client.component.ManaBarParticleEffect;
import top.csituka.sparkle_craft.screen.ManaTankScreenHandler;
import top.csituka.sparkle_craft.sparkle_craft;

public class ManaTankScreen extends HandledScreen<ManaTankScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(sparkle_craft.MOD_ID,
            "textures/gui/mana_tank.png");
    private static final int MANA_X = 74;
    private static final int MANA_Y = 19;
    private static final int MANA_WIDTH = 28;
    private static final int MANA_HEIGHT = 48;
    private static final int MANA_TEXTURE_X = 176;
    private static final int MANA_TEXTURE_Y = 0;
    private static final int WAVE_DEPTH = 2;

    private final ManaBarParticleEffect manaBarParticleEffect =
            new ManaBarParticleEffect(7, 0x80FFFFFF);

    public ManaTankScreen(ManaTankScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        if (isPointWithinBounds(MANA_X, MANA_Y, MANA_WIDTH, MANA_HEIGHT, mouseX, mouseY)) {
            context.drawTooltip(textRenderer,
                    Text.translatable("gui.sparkle-craft.mana_tank.mana",
                            handler.getMana(), handler.getMaxMana()), mouseX, mouseY);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int manaHeight = handler.getMana() > 0
                ? Math.max(1, handler.getScaledMana(MANA_HEIGHT))
                : 0;
        manaBarParticleEffect.setMode(ManaBarParticleEffect.Mode.IDLE);
        if (manaHeight > 0) {
            drawMana(context, manaHeight);
        }
    }

    private void drawMana(DrawContext context, int manaHeight) {
        int waveDepth = Math.min(WAVE_DEPTH, Math.max(0, manaHeight - 1));
        double animationSeconds = Util.getMeasuringTimeMs() / 1000.0D;
        int manaBottom = MANA_Y + MANA_HEIGHT;

        for (int column = 0; column < MANA_WIDTH; column++) {
            int waveOffset = getWaveOffset(column, waveDepth, animationSeconds);
            int columnHeight = manaHeight - waveOffset;
            int columnY = manaBottom - columnHeight;
            int textureY = MANA_TEXTURE_Y + MANA_HEIGHT - columnHeight;
            context.drawTexture(TEXTURE, x + MANA_X + column, y + columnY,
                    MANA_TEXTURE_X + column, textureY, 1, columnHeight);
        }

        int particleY = manaBottom - manaHeight + waveDepth;
        int particleHeight = manaHeight - waveDepth;
        manaBarParticleEffect.render(context, x + MANA_X, y + particleY,
                MANA_WIDTH, particleHeight);
    }

    private static int getWaveOffset(int column, int waveDepth, double animationSeconds) {
        if (waveDepth == 0) {
            return 0;
        }

        double primaryWave = Math.sin(column * 0.55D + animationSeconds * 4.2D);
        double secondaryWave = Math.sin(column * 0.27D - animationSeconds * 2.6D);
        double wave = primaryWave * 0.7D + secondaryWave * 0.3D;
        return (int) Math.round((wave + 1.0D) * waveDepth / 2.0D);
    }
}
