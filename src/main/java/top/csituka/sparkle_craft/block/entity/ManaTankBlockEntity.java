package top.csituka.sparkle_craft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import top.csituka.sparkle_craft.screen.ManaTankScreenHandler;

public class ManaTankBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    public static final int MAX_MANA = 5000;

    private int mana;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return index == 0 ? mana : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                mana = Math.max(0, Math.min(MAX_MANA, value));
            }
        }

        @Override
        public int size() {
            return 1;
        }
    };

    public ManaTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_TANK, pos, state);
    }

    public int getMana() {
        return mana;
    }

    public int receiveMana(int amount) {
        int inserted = Math.min(Math.max(0, amount), MAX_MANA - mana);
        if (inserted > 0) {
            mana += inserted;
            onManaChanged();
        }
        return inserted;
    }

    public int extractMana(int amount) {
        int extracted = Math.min(Math.max(0, amount), mana);
        if (extracted > 0) {
            mana -= extracted;
            onManaChanged();
        }
        return extracted;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.sparkle-craft.mana_tank");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ManaTankScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    private void onManaChanged() {
        markDirty();
        if (world != null && !world.isClient) {
            BlockState state = getCachedState();
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Mana", mana);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = Math.max(0, Math.min(MAX_MANA, nbt.getInt("Mana")));
    }
}
