package top.csituka.sparkle_craft.client.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import top.csituka.sparkle_craft.config.ModConfig;

public class ConfigScreen extends Screen {

    private static final int ROW_WIDTH = 200;
    private static final int ROW_HEIGHT = 20;

    private final Screen parent;
    private String modVersion = "Unknown";
    private ButtonWidget barrierToggleButton;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("gui.sparkle-craft.config.title"));
        this.parent = parent;
        FabricLoader.getInstance().getModContainer("sparkle-craft").ifPresent(container ->
                modVersion = container.getMetadata().getVersion().getFriendlyString());
    }

    @Override
    protected void init() {
        super.init();
        int centerX = width / 2;
        int centerY = height / 2;
        barrierToggleButton = addDrawableChild(ButtonWidget.builder(getBarrierText(), button -> {
            ModConfig.setShowFlyBeaconBarrier(!ModConfig.showFlyBeaconBarrier());
            button.setMessage(getBarrierText());
        }).dimensions(centerX - ROW_WIDTH / 2, centerY - ROW_HEIGHT - 5, ROW_WIDTH, ROW_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.sparkle-craft.config.done"),
                button -> close()).dimensions(10, height - 30, 80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.getMatrices().push();
        context.getMatrices().translate(20.0f, 20.0f, 0.0f);
        context.getMatrices().scale(0.5f, 0.5f, 1.0f);
        context.drawTextWithShadow(textRenderer,
                Text.literal("Sparkle Craft v" + modVersion + " \u00b7 Fabric"),
                0, 0, 0xAAAAAA);
        context.getMatrices().pop();
        context.drawTextWithShadow(textRenderer, title, 20, 28, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
        if (barrierToggleButton != null && barrierToggleButton.isHovered()) {
            context.drawTooltip(textRenderer,
                    Text.translatable("gui.sparkle-craft.config.show_barrier_hover"),
                    mouseX, mouseY);
        }
    }

    private Text getBarrierText() {
        boolean enabled = ModConfig.showFlyBeaconBarrier();
        return Text.translatable("gui.sparkle-craft.config.show_barrier").append(
                Text.translatable(enabled
                        ? "gui.sparkle-craft.config.barrier_on"
                        : "gui.sparkle-craft.config.barrier_off")
                        .formatted(enabled ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
