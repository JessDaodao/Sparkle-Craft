package org.example.sparkle_craft.block.entity;

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
        FlightGrant previous = GRANTS.get(player.getUuid());
        boolean managed = previous == null || previous.player != player
                ? !player.getAbilities().allowFlying
                : previous.managed;
        PlayerAbilities abilities = player.getAbilities();
        if (!abilities.allowFlying) {
            abilities.allowFlying = true;
            player.sendAbilitiesUpdate();
            managed = true;
        }
        GRANTS.put(player.getUuid(), new FlightGrant(player, currentTick, managed));
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
        if (grant == null || !grant.managed || grant.player.isCreative()
                || grant.player.isSpectator()) {
            return;
        }
        PlayerAbilities abilities = grant.player.getAbilities();
        abilities.allowFlying = false;
        abilities.flying = false;
        if (updateClient) {
            grant.player.sendAbilitiesUpdate();
        }
    }

    private static final class FlightGrant {

        private final ServerPlayerEntity player;
        private final long lastTick;
        private final boolean managed;

        private FlightGrant(ServerPlayerEntity player, long lastTick, boolean managed) {
            this.player = player;
            this.lastTick = lastTick;
            this.managed = managed;
        }
    }
}
