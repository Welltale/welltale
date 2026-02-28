package fr.welltale.rank;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonRankRepository implements RankRepository {
    private ArrayList<Rank> cachedRanks;

    @Override
    public void addRankConfig(@NonNull Rank rank) throws Exception {
        rank.setId(UUID.randomUUID());

        for (Rank cachedRank : this.cachedRanks) {
            if (!cachedRank.getName().equals(rank.getName())) {
                continue;
            }

            throw ERR_RANK_ALREADY_EXISTS;
        }

        this.cachedRanks.add(rank);
    }

    @Override
    public @Nullable Rank getRankConfig(@NonNull UUID rankId) {
        return cachedRanks.stream()
                .filter(r -> r.getId().equals(rankId))
                .findFirst()
                .orElse(null);
    }

    public List<Rank> getCachedRanksConfigs() {
        return List.copyOf(this.cachedRanks);
    }

    @Override
    public void updateRankConfig(@NonNull Rank rank) throws Exception {
        for (int i = 0; i < this.cachedRanks.size(); i++) {
            if (!this.cachedRanks.get(i).getId().equals(rank.getId())) {
                continue;
            }

            this.cachedRanks.set(i, rank);
            return;
        }
        throw new Exception(ERR_RANK_NOT_FOUND);
    }

    @Override
    public void deleteRankConfig(@NonNull UUID rankId) throws Exception {
        Rank rank = this.getRankConfig(rankId);
        this.cachedRanks.remove(rank);
    }
}
