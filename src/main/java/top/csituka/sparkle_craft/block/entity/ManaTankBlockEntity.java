package top.csituka.sparkle_craft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
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
import net.minecraft.world.World;
import top.csituka.sparkle_craft.screen.ManaTankScreenHandler;

public class ManaTankBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    public static final int MAX_MANA = 5000;

    private int mana;
    private int inputPerSecond;
    private int outputPerSecond;
    private int inputAccumulator;
    private int outputAccumulator;
    private int flowTick;
    private boolean manaSyncPending;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getMana();
                case 1 -> inputPerSecond;
                case 2 -> outputPerSecond;
                case 3 -> getMaxMana();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> mana = Math.max(0, Math.min(getMaxMana(), value));
                case 1 -> inputPerSecond = Math.max(0, value);
                case 2 -> outputPerSecond = Math.max(0, value);
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ManaTankBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MANA_TANK, pos, state);
    }

    protected ManaTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return MAX_MANA;
    }

    public static void tick(World world, BlockPos pos, BlockState state,
                            ManaTankBlockEntity blockEntity) {
        blockEntity.flowTick++;
        if (blockEntity.flowTick >= 20) {
            blockEntity.inputPerSecond = blockEntity.inputAccumulator;
            blockEntity.outputPerSecond = blockEntity.outputAccumulator;
            blockEntity.inputAccumulator = 0;
            blockEntity.outputAccumulator = 0;
            blockEntity.flowTick = 0;
        }
        blockEntity.syncManaIfNeeded();
    }

    public int receiveMana(int amount) {
        int inserted = Math.min(Math.max(0, amount), getMaxMana() - mana);
        if (inserted > 0) {
            mana += inserted;
            inputAccumulator += inserted;
            onManaChanged();
        }
        return inserted;
    }

    public int extractMana(int amount) {
        int extracted = Math.min(Math.max(0, amount), mana);
        if (extracted > 0) {
            mana -= extracted;
            outputAccumulator += extracted;
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
            manaSyncPending = true;
        }
    }

    private void syncManaIfNeeded() {
        if (!manaSyncPending || world == null || world.isClient) {
            return;
        }
        manaSyncPending = false;
        BlockState state = getCachedState();
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
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
        mana = Math.max(0, Math.min(getMaxMana(), nbt.getInt("Mana")));
    }
}
