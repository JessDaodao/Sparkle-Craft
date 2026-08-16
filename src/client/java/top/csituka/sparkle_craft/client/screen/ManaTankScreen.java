package top.csituka.sparkle_craft.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

        int manaHeight = handler.getScaledMana(MANA_HEIGHT);
        manaBarParticleEffect.setMode(ManaBarParticleEffect.Mode.IDLE);
        if (manaHeight > 0) {
            int manaY = MANA_Y + MANA_HEIGHT - manaHeight;
            int textureY = MANA_TEXTURE_Y + MANA_HEIGHT - manaHeight;
            context.drawTexture(TEXTURE, x + MANA_X, y + manaY,
                    MANA_TEXTURE_X, textureY, MANA_WIDTH, manaHeight);
            manaBarParticleEffect.render(context, x + MANA_X, y + manaY,
                    MANA_WIDTH, manaHeight);
        }
    }
}
