package top.csituka.sparkle_craft.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import top.csituka.sparkle_craft.client.component.ManaBarParticleEffect;
import top.csituka.sparkle_craft.screen.FlyBeaconScreenHandler;
import top.csituka.sparkle_craft.sparkle_craft;

public class FlyBeaconScreen extends HandledScreen<FlyBeaconScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(sparkle_craft.MOD_ID,
            "textures/gui/fly_beacon.png");
    private static final int PREVIEW_X = 7;
    private static final int PREVIEW_Y = 17;
    private static final int PREVIEW_WIDTH = 81;
    private static final int PREVIEW_HEIGHT = 52;
    private static final int PREVIEW_RADIUS = 10;
    private static final float PREVIEW_SCALE = 2.0F;
    private static final double PREVIEW_Y_OFFSET = -20.0;
    private static final int MANA_X = 95;
    private static final int MANA_Y = 28;
    private static final int MANA_WIDTH = 72;
    private static final int MANA_HEIGHT = 10;
    private static final int MANA_TEXTURE_X = 176;
    private static final int TOGGLE_X = 101;
    private static final int TOGGLE_Y = 49;
    private static final int TOGGLE_WIDTH = 66;
    private static final int TOGGLE_HEIGHT = 20;

    private final ManaBarParticleEffect manaBarParticleEffect =
            new ManaBarParticleEffect(7, 0x80FFFFFF);
    private ButtonWidget toggleButton;

    public FlyBeaconScreen(FlyBeaconScreenHandler handler, PlayerInventory inventory,
                           Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        toggleButton = addDrawableChild(ButtonWidget.builder(getToggleText(), button -> {
            if (client != null && client.interactionManager != null) {
                client.interactionManager.clickButton(handler.syncId,
                        FlyBeaconScreenHandler.TOGGLE_BUTTON_ID);
            }
        }).dimensions(x + TOGGLE_X, y + TOGGLE_Y, TOGGLE_WIDTH, TOGGLE_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (toggleButton != null) {
            toggleButton.setMessage(getToggleText());
        }
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        if (isPointWithinBounds(MANA_X, MANA_Y, MANA_WIDTH, MANA_HEIGHT,
                mouseX, mouseY)) {
            context.drawTooltip(textRenderer,
                    Text.translatable("gui.sparkle-craft.fly_beacon.mana",
                            handler.getMana(), handler.getMaxMana()), mouseX, mouseY);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        context.drawText(textRenderer, Text.translatable("gui.sparkle-craft.fly_beacon.mana_label"),
                MANA_X, 17, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int manaWidth = handler.getScaledMana(MANA_WIDTH);
        manaBarParticleEffect.setActive(handler.isActive());
        if (manaWidth > 0) {
            context.drawTexture(TEXTURE, x + MANA_X, y + MANA_Y,
                    MANA_TEXTURE_X, 0, manaWidth, MANA_HEIGHT);
            manaBarParticleEffect.render(context, x + MANA_X, y + MANA_Y,
                    manaWidth, MANA_HEIGHT);
        }

        renderTerrainPreview(context, delta);
    }

    private void renderTerrainPreview(DrawContext context, float delta) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.world == null) {
            return;
        }

        context.draw();
        context.enableScissor(x + PREVIEW_X + 1, y + PREVIEW_Y + 1,
                x + PREVIEW_X + PREVIEW_WIDTH - 1,
                y + PREVIEW_Y + PREVIEW_HEIGHT - 1);
        RenderSystem.enableDepthTest();
        DiffuseLighting.enableGuiDepthLighting();

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x + PREVIEW_X + PREVIEW_WIDTH / 2.0,
                y + PREVIEW_Y + PREVIEW_HEIGHT + PREVIEW_Y_OFFSET, 150.0);
        matrices.scale(PREVIEW_SCALE, -PREVIEW_SCALE, PREVIEW_SCALE);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
        float rotation = (minecraft.world.getTime() + delta) * 0.5F;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

        BlockPos beaconPos = handler.getBlockPos();
        for (int offsetZ = -PREVIEW_RADIUS; offsetZ <= PREVIEW_RADIUS; offsetZ++) {
            for (int offsetX = -PREVIEW_RADIUS; offsetX <= PREVIEW_RADIUS; offsetX++) {
                renderPreviewColumn(context, minecraft, matrices, beaconPos,
                        offsetX, offsetZ);
            }
        }

        matrices.pop();
        context.draw();
        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.disableDepthTest();
        context.disableScissor();
    }

    private void renderPreviewColumn(DrawContext context, MinecraftClient minecraft,
                                     MatrixStack matrices, BlockPos beaconPos,
                                     int offsetX, int offsetZ) {
        for (int offsetY = 10; offsetY >= -5; offsetY--) {
            BlockState state = minecraft.world.getBlockState(
                    beaconPos.add(offsetX, offsetY, offsetZ));
            if (state.isAir() || state.getRenderType() == BlockRenderType.INVISIBLE) {
                continue;
            }

            renderPreviewBlock(context, minecraft, matrices, state,
                    offsetX, offsetY, offsetZ);
        }
    }

    private void renderPreviewBlock(DrawContext context, MinecraftClient minecraft,
                                    MatrixStack matrices, BlockState state,
                                    int offsetX, int offsetY, int offsetZ) {
        if (state.isAir() || state.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }
        matrices.push();
        matrices.translate(offsetX - 0.5F, offsetY, offsetZ - 0.5F);
        minecraft.getBlockRenderManager().renderBlockAsEntity(state, matrices,
                context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }

    private Text getToggleText() {
        return Text.translatable(handler.isEnabled()
                ? "gui.sparkle-craft.fly_beacon.enabled"
                : "gui.sparkle-craft.fly_beacon.disabled");
    }
}
