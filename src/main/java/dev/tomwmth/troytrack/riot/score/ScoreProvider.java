package dev.tomwmth.troytrack.riot.score;

import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.match.v5.obj.Participant;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public abstract class ScoreProvider {
    protected final Match match;
    protected final List<Participant> participants;

    protected ScoreProvider(@NotNull Match match) {
        this.match = match;
        this.participants = match.getInfo().getParticipants();
    }

    public abstract int calculateScore(@NotNull Participant participant);

    public float calculateKDA(@NotNull Participant participant) {
        return (participant.getKills() + participant.getAssists()) / (float) participant.getDeaths();
    }

    public float calculateCSPM(@NotNull Participant participant) {
        float gameMinutes = this.match.getInfo().getDuration() / 60.0F;
        return calculateCS(participant) / gameMinutes;
    }

    public int calculateCS(@NotNull Participant participant) {
        return participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled();
    }
}
