package top.csituka.sparkle_craft.client.component;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ManaFlowIndicator {

    private static final String ARROW_UP = "\u2191";
    private static final String ARROW_DOWN = "\u2193";
    private static final int IN_COLOR = 0xFF55FF55;
    private static final int OUT_COLOR = 0xFFFF5555;
    private static final int GAP = 4;
    private static final int HEIGHT = 10;

    private int inputPerSecond;
    private int outputPerSecond;
    private int width;

    public void update(int inputPerSecond, int outputPerSecond, TextRenderer textRenderer) {
        this.inputPerSecond = inputPerSecond;
        this.outputPerSecond = outputPerSecond;
        width = textRenderer.getWidth(ARROW_UP + inputPerSecond + "/s")
                + GAP + textRenderer.getWidth(ARROW_DOWN + outputPerSecond + "/s");
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public void draw(DrawContext context, TextRenderer textRenderer, int x, int y) {
        context.drawText(textRenderer, Text.literal(ARROW_UP + inputPerSecond + "/s"),
                x, y, IN_COLOR, true);
        int inWidth = textRenderer.getWidth(ARROW_UP + inputPerSecond + "/s");
        context.drawText(textRenderer, Text.literal(ARROW_DOWN + outputPerSecond + "/s"),
                x + inWidth + GAP, y, OUT_COLOR, true);
    }

    public List<Text> buildTooltip() {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal(ARROW_UP + " ")
                .append(Text.translatable("gui.sparkle-craft.mana_rate", inputPerSecond))
                .formatted(Formatting.GREEN));
        lines.add(Text.literal(ARROW_DOWN + " ")
                .append(Text.translatable("gui.sparkle-craft.mana_rate", outputPerSecond))
                .formatted(Formatting.RED));
        return lines;
    }
}
