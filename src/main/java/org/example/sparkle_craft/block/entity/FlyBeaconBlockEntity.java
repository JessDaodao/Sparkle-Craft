package org.example.sparkle_craft.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.example.sparkle_craft.screen.FlyBeaconScreenHandler;

import java.util.List;

public class FlyBeaconBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {

    public static final int MAX_MANA = 1000;
    public static final int MANA_PER_TICK = 1;
    public static final int RANGE = 16;

    private int mana;
    private boolean enabled;
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
        if (!(world instanceof ServerWorld serverWorld)
                || !blockEntity.enabled
                || blockEntity.mana < MANA_PER_TICK) {
            return;
        }

        Vec3d center = Vec3d.ofCenter(pos);
        List<ServerPlayerEntity> players = serverWorld.getPlayers(
                player -> !player.isSpectator()
                        && !player.isCreative()
                        && player.squaredDistanceTo(center) <= RANGE * RANGE);
        if (players.isEmpty()) {
            return;
        }

        players.forEach(FlyBeaconFlightManager::refreshFlight);
        blockEntity.mana -= MANA_PER_TICK;
        blockEntity.markDirty();
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
