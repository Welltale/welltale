package fr.welltale.rank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface RankRepository {
    Exception ERR_RANK_ALREADY_EXISTS = new Exception("rank already exists");
    Exception ERR_RANK_NOT_FOUND = new Exception("rank not found");

    void addRankConfig(@Nonnull Rank rank) throws Exception;

    @Nullable Rank getRankConfig(@Nonnull UUID rankId);

    List<Rank> getCachedRanksConfigs();

    void updateRankConfig(@Nonnull Rank rank) throws Exception;

    void deleteRankConfig(@Nonnull UUID rankId) throws Exception;
}
