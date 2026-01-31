package fr.welltale.clazz.spell.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

public class DamageSystem extends EntityEventSystem<EntityStore, Damage> {
    private final PlayerRepository playerRepository;

    public DamageSystem(@NonNull PlayerRepository playerRepository, HytaleLogger logger) {
        super(Damage.class);
        this.playerRepository = playerRepository;
    }

    @Override
    public void handle(
            int i,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer,
            @NonNull Damage damage
    ) {
        //TODO IF DAMAGED ENTITY IS PLAYER CHECK PLAYER RESISTANCE
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) {
            return;
        }

        Ref<EntityStore> damageSourceRef = entitySource.getRef();
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(damageSourceRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) return;

        UUIDComponent damageSourceUuidComponent = store.getComponent(damageSourceRef, UUIDComponent.getComponentType());
        if (damageSourceUuidComponent == null) return;


        Player playerData = this.playerRepository.getPlayerByUuid(damageSourceUuidComponent.getUuid());
        if (playerData == null) return;

        float damageAmount = damage.getAmount();
        player.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));

        //TODO ADD PLAYER CRITICAL DAMAGE TO NEW DAMAGE AMOUNT
        float newDamageAmount = damageAmount + playerData.getCharacteristic().getDamage();
        player.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));

        damage.setAmount(newDamageAmount);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
