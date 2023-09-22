package dev.tomwmth.troytrack.riot.score;

import com.hawolt.dto.match.v5.match.MatchDto;
import com.hawolt.dto.match.v5.match.ParticipantDto;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public abstract class ScoreProvider {
    protected final MatchDto match;
    protected final List<ParticipantDto> participants;

    protected ScoreProvider(@NotNull MatchDto match) {
        this.match = match;
        this.participants = match.getInfoDto().getParticipants();
    }

    public abstract int calculateScore(@NotNull ParticipantDto participant);

    public abstract String generateVerdict(int individualScore, int teamAverageScore);

    protected float calculateKDA(@NotNull ParticipantDto participant) {
        return (participant.getKills() + participant.getAssists()) / (float) participant.getDeaths();
    }

    protected float calculateCSPM(@NotNull ParticipantDto participant) {
        float gameMinutes = this.match.getInfoDto().getGameDuration() / 60.0F;
        return participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled() / gameMinutes;
    }
}
