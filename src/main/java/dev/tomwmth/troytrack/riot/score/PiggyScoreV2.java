package dev.tomwmth.troytrack.riot.score;

import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.match.v5.obj.Participant;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 16/09/2023
 */
public class PiggyScoreV2 extends ScoreProvider {
    private static final float HEAL_MULTIPLIER = 1.5F;
    private static final float SHIELD_MULTIPLIER = 2.0F;
    private static final float EPIC_MONSTER_MULTIPLIER = 0.4F;

    private static final float MAX_CHAMPION_HEALTH_SHARE = 0.3F;
    private static final float MAX_OBJECTIVE_DAMAGE_SHARE = 0.25F;
    private static final float MAX_KDA = 6.0F;
    private static final float MAX_CSPM = 10.0F;
    private static final float WEIGHT_CHAMPION_HEALTH_SHARE = 0.5F * 100.0F;
    private static final float WEIGHT_OBJECTIVE_DAMAGE_SHARE = 0.2F * 100.0F;
    private static final float WEIGHT_KDA = 0.3F * 100.0F;
    private static final float WEIGHT_CSPM = 0.0F * 100.0F;

    private int championHealthTotal = 0;
    private int objectiveDamageTotal = 0;

    public PiggyScoreV2(@NotNull Match match) {
        super(match);
        for (Participant participant : this.participants) {
            int championHealth = this.getTotalHealthToChampions(participant);
            this.championHealthTotal += championHealth;
            int objectiveDamage = this.getTotalDamageDealtToObjectives(participant);
            this.objectiveDamageTotal += objectiveDamage;
        }
    }

    @Override
    public int calculateScore(@NotNull Participant participant) {
        int teamId = participant.getTeamId();
        float individualChampionHealth = this.getTotalHealthToChampions(participant);
        float individualObjectiveDamage = this.getTotalDamageDealtToObjectives(participant);

        float championHealthShare = Math.min((individualChampionHealth / this.championHealthTotal), MAX_CHAMPION_HEALTH_SHARE);
        float objectiveDamageShare = Math.min((individualObjectiveDamage / this.objectiveDamageTotal), MAX_OBJECTIVE_DAMAGE_SHARE);
        float kda = Math.min(this.calculateKDA(participant), MAX_KDA);
        float cspm = Math.min(this.calculateCSPM(participant), MAX_CSPM);

        float score = 0.0F;

        score += WEIGHT_CHAMPION_HEALTH_SHARE * (championHealthShare / MAX_CHAMPION_HEALTH_SHARE);
        score += WEIGHT_OBJECTIVE_DAMAGE_SHARE * (objectiveDamageShare / MAX_OBJECTIVE_DAMAGE_SHARE);
        score += WEIGHT_KDA * (kda / MAX_KDA);
        score += WEIGHT_CSPM * (cspm / MAX_CSPM);

        return this.clamp(score);
    }

    @Override
    public String generateVerdict(int individualScore, int teamAverageScore) {
        int delta = individualScore - teamAverageScore;
        boolean positive = delta >= 0;
        delta = Math.abs(delta);
        return "With a delta of %s, it is %s likely you are %s".formatted(
                (positive ? "+" : "-") + delta,
                (delta >= 10 ? "highly" : "moderately"),
                (positive ? "the goat. \uD83D\uDC10" : "a piggy. \uD83D\uDC37")
        );
    }

    private int getTotalHealthToChampions(@NotNull Participant participant) {
        return participant.getTotalDamageDealtToChampions() +
                Math.round(participant.getTotalHealsOnTeammates() * HEAL_MULTIPLIER) +
                Math.round(participant.getTotalDamageShieldedOnTeammates() * SHIELD_MULTIPLIER);
    }

    private int getTotalDamageDealtToObjectives(@NotNull Participant participant) {
        return Math.round(participant.getDamageDealtToObjectives() * EPIC_MONSTER_MULTIPLIER) +
                participant.getDamageDealtToTurrets() +
                participant.getDamageDealtToBuildings();
    }

    private int clamp(float val) {
        return (int) Math.max(0.0F, Math.min(100.0F, Math.round(val)));
    }
}
