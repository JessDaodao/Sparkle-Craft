package top.csituka.sparkle_craft.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.csituka.sparkle_craft.screen.FlyBeaconScreenHandler;

import java.util.List;

public class FlyBeaconBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {

    public static final int MAX_MANA = 1000;
    public static final int MANA_PER_CONSUMPTION = 1;
    public static final int MANA_CONSUMPTION_INTERVAL_TICKS = 2;
    public static final int FLIGHT_RADIUS = 10;
    public static final int FLIGHT_BELOW = 5;
    public static final int FLIGHT_ABOVE = 50;

    private int mana;
    private boolean enabled;
    private int consumptionTick;
    private boolean syncedActive;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mana;
                case 1 -> enabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                mana = value;
            } else if (index == 1) {
                enabled = value != 0;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public FlyBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLY_BEACON, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state,
                            FlyBeaconBlockEntity blockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        boolean active = blockEntity.isActive();
        if (active != blockEntity.syncedActive) {
            blockEntity.syncedActive = active;
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
        }
        if (!active) {
            return;
        }

        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double minY = pos.getY() - FLIGHT_BELOW;
        double maxY = pos.getY() + FLIGHT_ABOVE;
        List<ServerPlayerEntity> players = serverWorld.getPlayers(player -> {
            if (player.isSpectator() || player.isCreative()) {
                return false;
            }
            if (player.getY() < minY || player.getY() > maxY) {
                return false;
            }
            double dx = player.getX() - centerX;
            double dz = player.getZ() - centerZ;
            return Math.abs(dx) <= FLIGHT_RADIUS && Math.abs(dz) <= FLIGHT_RADIUS;
        });
        if (players.isEmpty()) {
            return;
        }

        players.forEach(FlyBeaconFlightManager::refreshFlight);
        blockEntity.consumptionTick++;
        if (blockEntity.consumptionTick >= MANA_CONSUMPTION_INTERVAL_TICKS) {
            blockEntity.mana = Math.max(0, blockEntity.mana - MANA_PER_CONSUMPTION);
            blockEntity.consumptionTick = 0;
            blockEntity.markDirty();
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state,
                                  FlyBeaconBlockEntity blockEntity) {
        FlyBeaconBarrierTracker.update(world, pos, blockEntity.isActive());
    }

    public boolean isActive() {
        return enabled && mana > 0;
    }

    public int receiveMana(int amount) {
        int inserted = Math.min(Math.max(0, amount), MAX_MANA - mana);
        if (inserted > 0) {
            mana += inserted;
            markDirty();
        }
        return inserted;
    }

    public void toggleEnabled() {
        enabled = !enabled;
        markDirty();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.sparkle-craft.fly_beacon");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory,
                                    PlayerEntity player) {
        return new FlyBeaconScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
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
    public void markRemoved() {
        super.markRemoved();
        if (world != null && world.isClient) {
            FlyBeaconBarrierTracker.remove(pos);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Mana", mana);
        nbt.putBoolean("Enabled", enabled);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = Math.max(0, Math.min(MAX_MANA, nbt.getInt("Mana")));
        enabled = nbt.getBoolean("Enabled");
    }
}
