package fr.welltale.rank;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface RankRepository {
    Exception ERR_RANK_ALREADY_EXISTS = new Exception("rank already exists");
    Exception ERR_RANK_NOT_FOUND = new Exception("rank not found");

    void addRankConfig(@NonNull Rank rank) throws Exception;

    @Nullable Rank getRankConfig(@NonNull UUID rankId);

    List<Rank> getCachedRanksConfigs();

    void updateRankConfig(@NonNull Rank rank) throws Exception;

    void deleteRankConfig(@NonNull UUID rankId) throws Exception;
}
