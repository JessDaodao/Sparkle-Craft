package top.csituka.sparkle_craft.integration.jade;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;
import top.csituka.sparkle_craft.block.custom.CrystalManaExtractorBlock;
import top.csituka.sparkle_craft.block.custom.FlyBeaconBlock;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.custom.ManaTankBlock;
import top.csituka.sparkle_craft.client.component.ManaBarParticleEffect;

public enum ManaContainerJadeClientProvider implements IBlockComponentProvider {

    INSTANCE;

    private static final int MANA_BAR_COLOR = 0xFF8000FF;
    private static final int MANA_BAR_WIDTH = 120;
    private static final int MANA_BAR_HEIGHT = 14;
    private static final IElement MANA_BAR_OVERLAY = new ManaBarOverlayElement();

    public static void register(IWailaClientRegistration registration) {
        registration.registerBlockComponent(INSTANCE, ManaPipeBlock.class);
        registration.registerBlockComponent(INSTANCE, ManaTankBlock.class);
        registration.registerBlockComponent(INSTANCE, CrystalManaExtractorBlock.class);
        registration.registerBlockComponent(INSTANCE, FlyBeaconBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtCompound data = accessor.getServerData();
        if (!data.contains(ManaContainerComponentProvider.MANA_TAG)
                || !data.contains(ManaContainerComponentProvider.MAX_MANA_TAG)) {
            return;
        }

        int mana = data.getInt(ManaContainerComponentProvider.MANA_TAG);
        int maxMana = data.getInt(ManaContainerComponentProvider.MAX_MANA_TAG);
        float progress = maxMana > 0
                ? MathHelper.clamp(mana / (float) maxMana, 0.0f, 1.0f)
                : 0.0f;
        IElementHelper elements = tooltip.getElementHelper();
        IProgressStyle style = elements.progressStyle()
                .color(MANA_BAR_COLOR)
                .overlay(MANA_BAR_OVERLAY);
        IElement progressElement = elements.progress(progress,
                Text.translatable("jade.sparkle-craft.mana_container.mana", mana, maxMana),
                style, BoxStyle.DEFAULT, false);
        tooltip.add(progressElement.size(new Vec2f(MANA_BAR_WIDTH, MANA_BAR_HEIGHT)));
    }

    @Override
    public Identifier getUid() {
        return ManaContainerComponentProvider.UID;
    }

    private static final class ManaBarOverlayElement extends Element {

        private final ManaBarParticleEffect particleEffect =
                new ManaBarParticleEffect(7, 0x80FFFFFF);

        @Override
        public Vec2f getSize() {
            return Vec2f.ZERO;
        }

        @Override
        public void render(DrawContext context, float x, float y, float width, float height) {
            int left = Math.round(x);
            int top = Math.round(y);
            int barWidth = Math.max(0, Math.round(width));
            int barHeight = Math.max(0, Math.round(height));
            if (barWidth == 0 || barHeight == 0) {
                return;
            }

            context.fill(left, top, left + barWidth, top + barHeight, MANA_BAR_COLOR);
            particleEffect.setMode(ManaBarParticleEffect.Mode.IDLE);
            particleEffect.render(context, left, top, barWidth, barHeight);
        }
    }
}
