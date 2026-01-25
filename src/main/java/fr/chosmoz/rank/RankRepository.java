package fr.chosmoz.rank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface RankRepository {
    Exception ERR_RANK_ALREADY_EXISTS = new Exception("rank already exists");
    Exception ERR_RANK_NOT_FOUND = new Exception("rank not found");

    void addRank(@Nonnull Rank rank) throws Exception;

    @Nullable Rank getRank(@Nonnull UUID rankId);

    List<Rank> getCachedRanks();

    void updateRank(@Nonnull Rank rank) throws Exception;

    void deleteRank(@Nonnull UUID rankId) throws Exception;
}
