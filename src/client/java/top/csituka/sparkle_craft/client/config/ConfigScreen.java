package top.csituka.sparkle_craft.client.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import top.csituka.sparkle_craft.config.ModConfig;

public class ConfigScreen extends Screen {

    private static final int LEFT_WIDTH = 100;
    private static final int PADDING = 10;
    private static final int LIST_TOP = 50;
    private static final int LIST_BOTTOM_MARGIN = 40;
    private static final int ITEM_HEIGHT = 28;

    private final Screen parent;
    private String modVersion = "Unknown";
    private ButtonWidget barrierToggleButton;
    private ConfigList configList;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("gui.sparkle-craft.config.title"));
        this.parent = parent;
        FabricLoader.getInstance().getModContainer("sparkle-craft").ifPresent(container ->
                modVersion = container.getMetadata().getVersion().getFriendlyString());
    }

    @Override
    protected void init() {
        super.init();
        int rightX = LEFT_WIDTH + PADDING;
        int rightWidth = width - LEFT_WIDTH - PADDING * 2;
        int rowWidth = Math.min(250, rightWidth - 20);
        configList = new ConfigList(MinecraftClient.getInstance(), rightWidth, height,
                LIST_TOP, height - LIST_BOTTOM_MARGIN, ITEM_HEIGHT);
        configList.setLeftPos(rightX);
        barrierToggleButton = ButtonWidget.builder(getBarrierText(), button -> {
            ModConfig.setShowFlyBeaconBarrier(!ModConfig.showFlyBeaconBarrier());
            button.setMessage(getBarrierText());
        }).width(rowWidth).build();
        configList.addOption(barrierToggleButton);
        addDrawableChild(configList);
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        return configList != null && configList.isMouseOver(mouseX, mouseY)
                && configList.mouseScrolled(mouseX, mouseY, amount);
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
