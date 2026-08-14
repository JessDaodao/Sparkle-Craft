package top.csituka.sparkle_craft.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import top.csituka.sparkle_craft.client.component.ManaBarParticleEffect;
import top.csituka.sparkle_craft.screen.CrystalManaExtractorScreenHandler;
import top.csituka.sparkle_craft.sparkle_craft;

public class CrystalManaExtractorScreen extends HandledScreen<CrystalManaExtractorScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(sparkle_craft.MOD_ID,
            "textures/gui/crystal_mana_extractor.png");
    private static final int MANA_X = 52;
    private static final int MANA_Y = 29;
    private static final int MANA_WIDTH = 72;
    private static final int MANA_HEIGHT = 10;
    private static final int MANA_TEXTURE_X = 176;

    private final ManaBarParticleEffect manaBarParticleEffect =
            new ManaBarParticleEffect(7, 0x80FFFFFF);

    public CrystalManaExtractorScreen(CrystalManaExtractorScreenHandler handler,
                                      PlayerInventory inventory, Text title) {
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
                    Text.translatable("gui.sparkle-craft.crystal_mana_extractor.mana",
                            handler.getMana(), handler.getMaxMana()), mouseX, mouseY);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int manaWidth = handler.getScaledMana(MANA_WIDTH);
        manaBarParticleEffect.setMode(handler.isConverting()
                ? ManaBarParticleEffect.Mode.INCREASING
                : ManaBarParticleEffect.Mode.IDLE);
        if (manaWidth > 0) {
            context.drawTexture(TEXTURE, x + MANA_X, y + MANA_Y,
                    MANA_TEXTURE_X, 0, manaWidth, MANA_HEIGHT);
            manaBarParticleEffect.render(context, x + MANA_X, y + MANA_Y, manaWidth, MANA_HEIGHT);
        }
    }
}
