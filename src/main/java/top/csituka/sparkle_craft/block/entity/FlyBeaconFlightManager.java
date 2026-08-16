package top.csituka.sparkle_craft.block.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class FlyBeaconFlightManager {

    private static final Map<UUID, FlightGrant> GRANTS = new HashMap<>();
    private static long currentTick;

    private FlyBeaconFlightManager() {
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> currentTick++);
        ServerTickEvents.END_SERVER_TICK.register(server -> revokeStaleGrants());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                revokeGrant(GRANTS.remove(handler.player.getUuid()), false));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> revokeAllGrants());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentTick = 0);
    }

    public static void refreshFlight(ServerPlayerEntity player) {
        PlayerAbilities abilities = player.getAbilities();
        FlightGrant grant = GRANTS.get(player.getUuid());
        if (grant == null || grant.player != player) {
            grant = new FlightGrant(player, currentTick, abilities);
        } else {
            grant.refresh(currentTick, abilities);
        }
        if (!abilities.allowFlying) {
            abilities.allowFlying = true;
            player.sendAbilitiesUpdate();
            grant.takeOwnership();
        }
        grant.recordAppliedState(abilities);
        GRANTS.put(player.getUuid(), grant);
    }

    private static void revokeStaleGrants() {
        Iterator<FlightGrant> iterator = GRANTS.values().iterator();
        while (iterator.hasNext()) {
            FlightGrant grant = iterator.next();
            if (grant.lastTick == currentTick) {
                continue;
            }
            revokeGrant(grant, !grant.player.isRemoved());
            iterator.remove();
        }
    }

    private static void revokeAllGrants() {
        GRANTS.values().forEach(grant -> revokeGrant(grant, false));
        GRANTS.clear();
    }

    private static void revokeGrant(FlightGrant grant, boolean updateClient) {
        if (grant == null || !grant.ownsPermission || grant.externallyModified
                || grant.player.isCreative()
                || grant.player.isSpectator()) {
            return;
        }
        PlayerAbilities abilities = grant.player.getAbilities();
        if (abilities.allowFlying != grant.lastAppliedAllowFlying
                || Float.compare(abilities.getFlySpeed(), grant.lastFlySpeed) != 0) {
            return;
        }
        abilities.allowFlying = grant.restoreAllowFlying;
        abilities.flying = grant.restoreFlying;
        if (updateClient) {
            grant.player.sendAbilitiesUpdate();
        }
    }

    private static final class FlightGrant {

        private final ServerPlayerEntity player;
        private boolean restoreAllowFlying;
        private boolean restoreFlying;
        private long lastTick;
        private boolean ownsPermission;
        private boolean externallyModified;
        private boolean lastAppliedAllowFlying;
        private float lastFlySpeed;

        private FlightGrant(ServerPlayerEntity player, long lastTick, PlayerAbilities abilities) {
            this.player = player;
            this.lastTick = lastTick;
            this.restoreAllowFlying = abilities.allowFlying;
            this.restoreFlying = abilities.flying;
            this.ownsPermission = !abilities.allowFlying;
            recordAppliedState(abilities);
        }

        private void refresh(long tick, PlayerAbilities abilities) {
            lastTick = tick;
            if ((abilities.allowFlying && abilities.allowFlying != lastAppliedAllowFlying)
                    || Float.compare(abilities.getFlySpeed(), lastFlySpeed) != 0) {
                externallyModified = true;
            }
        }

        private void takeOwnership() {
            ownsPermission = true;
            restoreAllowFlying = false;
            restoreFlying = false;
        }

        private void recordAppliedState(PlayerAbilities abilities) {
            lastAppliedAllowFlying = abilities.allowFlying;
            lastFlySpeed = abilities.getFlySpeed();
        }
    }
}
