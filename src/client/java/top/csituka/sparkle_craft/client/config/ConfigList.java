package top.csituka.sparkle_craft.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;

public class ConfigList extends ElementListWidget<ConfigList.Entry> {

    private final int contentWidth;
    private double targetScrollAmount;
    private boolean isDraggingScrollbar;

    public ConfigList(MinecraftClient client, int contentWidth, int height, int top, int bottom,
                      int itemHeight) {
        super(client, contentWidth, height, top, bottom, itemHeight);
        this.contentWidth = contentWidth;
        setRenderBackground(false);
        setRenderHorizontalShadows(false);
    }

    public void addOption(ClickableWidget widget) {
        addEntry(new Entry(widget));
    }

    @Override
    public int getRowWidth() {
        return Math.min(250, contentWidth - 20);
    }

    @Override
    protected int getScrollbarPositionX() {
        return left + contentWidth - 6;
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, getMaxPosition() - (bottom - top - 4));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        targetScrollAmount = MathHelper.clamp(targetScrollAmount, 0.0, getMaxScroll());
        if (Math.abs(targetScrollAmount - getScrollAmount()) > 0.1) {
            setScrollAmount(MathHelper.lerp(0.3, getScrollAmount(), targetScrollAmount));
        } else {
            setScrollAmount(targetScrollAmount);
        }
        super.render(context, mouseX, mouseY, delta);
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int i = getScrollbarPositionX();
            int j = Math.max(32, (bottom - top) * (bottom - top) / getMaxPosition());
            int k = (int) getScrollAmount() * (bottom - top - j) / maxScroll + top;
            context.fill(i, k, i + 2, k + j, 0x99FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateScrollingState(mouseX, mouseY, button);
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            int i = getScrollbarPositionX();
            if (mouseX >= i && mouseX <= i + 6) {
                isDraggingScrollbar = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar) {
            int maxScroll = getMaxScroll();
            if (maxScroll > 0) {
                int j = Math.max(32, (bottom - top) * (bottom - top) / getMaxPosition());
                double d = Math.max(1.0, maxScroll / (double) (bottom - top - j));
                targetScrollAmount = MathHelper.clamp(targetScrollAmount + deltaY * d, 0.0, maxScroll);
                setScrollAmount(targetScrollAmount);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (getMaxScroll() <= 0) {
            return false;
        }
        targetScrollAmount = MathHelper.clamp(targetScrollAmount - amount * itemHeight, 0.0, getMaxScroll());
        return true;
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {

        public final ClickableWidget widget;

        public Entry(ClickableWidget widget) {
            this.widget = widget;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(widget);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Collections.singletonList(widget);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth,
                           int entryHeight, int mouseX, int mouseY, boolean hovered,
                           float tickDelta) {
            widget.setX(x + (entryWidth - widget.getWidth()) / 2);
            widget.setY(y);
            widget.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
