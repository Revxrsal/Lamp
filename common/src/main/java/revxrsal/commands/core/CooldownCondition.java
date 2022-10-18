package revxrsal.commands.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CooldownException;
import revxrsal.commands.process.CommandCondition;

enum CooldownCondition implements CommandCondition {

  INSTANCE;

  private static final ScheduledExecutorService COOLDOWN_POOL = Executors.newSingleThreadScheduledExecutor();
  private final Map<UUID, Map<Integer, Long>> cooldowns = new ConcurrentHashMap<>();

  @Override
  public void test(@NotNull CommandActor actor, @NotNull ExecutableCommand command,
      @NotNull @Unmodifiable List<String> arguments) {
    Cooldown cooldown = command.getAnnotation(Cooldown.class);
    if (cooldown == null || cooldown.value() == 0) {
      return;
    }
    UUID uuid = actor.getUniqueId();
    Map<Integer, Long> spans = get(uuid);
    Long created = spans.get(command.getId());
    if (created == null) {
      spans.put(command.getId(), System.currentTimeMillis());
      COOLDOWN_POOL.schedule(() -> spans.remove(command.getId()), cooldown.value(),
          cooldown.unit());
      return;
    }
    long passed = System.currentTimeMillis() - created;
    long left = cooldown.unit().toMillis(cooldown.value()) - passed;
    if (left > 0 && left < 1000) {
      left = 1000L; // for formatting
    }
    throw new CooldownException(left);
  }

  private Map<Integer, Long> get(@NotNull UUID uuid) {
    return cooldowns.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
  }
}
