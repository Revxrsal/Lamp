/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.exception.CooldownException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

enum CooldownCondition implements CommandCondition {

    INSTANCE;

    private static final ScheduledExecutorService COOLDOWN_POOL = Executors.newSingleThreadScheduledExecutor();
    private final Map<UUID, Map<Integer, Long>> cooldowns = new ConcurrentHashMap<>();

    @Override public void test(@NotNull CommandActor actor, @NotNull ExecutableCommand command, @NotNull @Unmodifiable List<String> arguments) {
        Cooldown cooldown = command.getAnnotation(Cooldown.class);
        if (cooldown == null || cooldown.value() == 0) return;
        UUID uuid = actor.getUniqueId();
        Map<Integer, Long> spans = get(uuid);
        Long created = spans.get(command.getId());
        if (created == null) {
            spans.put(command.getId(), System.currentTimeMillis());
            COOLDOWN_POOL.schedule(() -> spans.remove(command.getId()), cooldown.value(), cooldown.unit());
            return;
        }
        long passed = System.currentTimeMillis() - created;
        long left = cooldown.unit().toMillis(cooldown.value()) - passed;
        if (left > 0 && left < 1000) left = 1000L; // for formatting
        throw new CooldownException(left);
    }

    private Map<Integer, Long> get(@NotNull UUID uuid) {
        return cooldowns.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
    }
}
