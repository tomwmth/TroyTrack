package dev.tomwmth.troytrack;

import com.google.common.reflect.ClassPath;
import dev.tomwmth.troytrack.command.base.Command;
import dev.tomwmth.troytrack.command.base.CommandRegistry;
import dev.tomwmth.troytrack.listener.base.EventListener;
import dev.tomwmth.troytrack.riot.RiotApi;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
public class TroyTrack {
    @Getter
    private static TroyTrack instance;

    @Getter
    private final JDA jda;

    @Getter
    private final CommandRegistry commandRegistry;

    @Getter
    private final List<EventListener> registeredListeners = new ArrayList<>();

    @Getter
    private final RiotApi riotApi;

    public TroyTrack() {
        this.jda = JDABuilder
                .createDefault(System.getProperty("discord.token"))
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .setActivity(Activity.watching("your games"))
                .build();

        this.registerListeners();

        this.commandRegistry = this.registerCommands();

        this.riotApi = new RiotApi();

        instance = this;
    }

    private void registerListeners() {
        Set<Class<?>> clazzes = this.scanClasspath();

        for (Class<?> clazz : clazzes) {
            if (EventListener.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    EventListener listener = (EventListener) clazz.getDeclaredConstructor(TroyTrack.class).newInstance(this);
                    this.jda.addEventListener(listener);
                    this.registeredListeners.add(listener);
                } catch (ReflectiveOperationException ex) {
                    Reference.LOGGER.error("Error instantiating " + clazz.getSimpleName(), ex);
                }
            }
        }
    }

    @NotNull
    private CommandRegistry registerCommands() {
        List<Command> commands = new ArrayList<>();
        Set<Class<?>> clazzes = this.scanClasspath();

        for (Class<?> clazz : clazzes) {
            if (Command.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    Command command = (Command) clazz.getDeclaredConstructor(TroyTrack.class).newInstance(this);
                    commands.add(command);
                } catch (ReflectiveOperationException ex) {
                    Reference.LOGGER.error("Error instantiating " + clazz.getSimpleName(), ex);
                }
            }
        }

        return new CommandRegistry(commands);
    }

    private Set<Class<?>> scanClasspath() {
        String packageName = this.getClass().getPackage().getName();
        Set<Class<?>> clazzes = Collections.emptySet();
        try {
            clazzes = ClassPath.from(this.getClass().getClassLoader()).getAllClasses().stream()
                    .filter(clazz -> clazz.getPackageName().contains(packageName))
                    .map(ClassPath.ClassInfo::load)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception ex) {
            Reference.LOGGER.error("Error scanning classpath", ex);
        }
        return clazzes;
    }
}
