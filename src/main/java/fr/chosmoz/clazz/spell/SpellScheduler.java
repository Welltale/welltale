package fr.chosmoz.clazz.spell;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpellScheduler extends EntityTickingSystem<EntityStore> {
    static final Duration SECURITY_SPELL_LIFESPAN = Duration.ofSeconds(30);

    public interface Condition {
        boolean test(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cmdBuffer);
    }

    public interface Action {
        void run(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cmdBuffer);
    }

    public static class Task {
        final Condition condition;
        final Action action;
        final Instant startHas = Instant.now();
        boolean done;

        Task(@Nonnull Condition condition, @Nonnull Action action) {
            this.condition = condition;
            this.action = action;
        }
    }

    private final List<Task> tasks = new CopyOnWriteArrayList<>();

    public void schedule(@Nonnull Condition condition, @Nonnull Action action) {
        tasks.add(new Task(condition, action));
    }

    public void tick(float v, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        for (Task task : tasks) {
            if (task.done) continue;

            if (task.condition.test(store, commandBuffer)) {
                task.action.run(store, commandBuffer);
                task.done = true;
            }
        }
        tasks.removeIf(t -> t.done || Instant.now().isAfter(t.startHas.plus(SECURITY_SPELL_LIFESPAN)));
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(SpellComponent.getComponentType());
    }
}