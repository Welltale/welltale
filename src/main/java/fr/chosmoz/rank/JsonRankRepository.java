package fr.chosmoz.rank;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonRankRepository implements RankRepository {
    private List<Rank> cachedRanks;

    @Override
    public void addRank(@NonNull Rank rank) throws Exception {
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
    public @Nullable Rank getRank(@NonNull UUID rankId) {
        return cachedRanks.stream()
                .filter(r -> r.getId().equals(rankId))
                .findFirst()
                .orElse(null);
    }

    public List<Rank> getCachedRanks() {
        return this.cachedRanks;
    }

    @Override
    public void updateRank(@NonNull Rank rank) throws Exception {
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
    public void deleteRank(@NonNull UUID rankId) throws Exception {
        Rank rank = this.getRank(rankId);
        this.cachedRanks.remove(rank);
    }
}
