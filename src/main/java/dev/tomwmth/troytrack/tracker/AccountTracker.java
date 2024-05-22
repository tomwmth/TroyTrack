package dev.tomwmth.troytrack.tracker;

import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.obj.CachedGuildChannel;
import dev.tomwmth.troytrack.obj.TrackedAccount;
import dev.tomwmth.troytrack.tracker.api.RiotGames;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public abstract class AccountTracker {
    protected final @NotNull RiotGames api = RiotGames.getInstance();

    @Getter
    protected final @NotNull TrackedAccount trackedAccount;

    public AccountTracker(@NotNull TrackedAccount trackedAccount) {
        this.trackedAccount = trackedAccount;
    }

    public abstract @NotNull String heartbeat() throws Exception;

    protected void sendMessage(@NotNull MessageEmbed embed) {
        JDA jda = TroyTrack.getInstance().getJda();
        for (CachedGuildChannel ids : this.trackedAccount.getChannels()) {
            if (ids.getGuildId() != 0L && ids.getChannelId() != 0L) {
                Guild trackingGuild = jda.getGuildById(ids.getGuildId());
                if (trackingGuild != null) {
                    GuildChannel trackingChannel = trackingGuild.getGuildChannelById(ids.getChannelId());
                    if (trackingChannel instanceof GuildMessageChannel channel) {
                        channel.sendMessageEmbeds(embed).queue();
                    }
                }
            }
        }
    }
}
