package dev.tomwmth.troytrack.tracker.score;

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
        float kda = participant.getTakedowns() / (float) participant.getDeaths();

        if (Float.isNaN(kda)) {
            kda = 0.0F;
        }

        return kda;
    }

    public float calculateKP(@NotNull Participant participant) {
        int totalTeamKills = this.participants.stream()
                .filter(x -> x.getTeamId() == participant.getTeamId())
                .map(Participant::getKills)
                .reduce(0, Integer::sum);
        return (float) participant.getTakedowns() / totalTeamKills;
    }

    public float calculateCSPM(@NotNull Participant participant) {
        float gameMinutes = this.match.getInfo().getDuration() / 60.0F;
        return calculateCS(participant) / gameMinutes;
    }

    public int calculateCS(@NotNull Participant participant) {
        return participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled();
    }
}
