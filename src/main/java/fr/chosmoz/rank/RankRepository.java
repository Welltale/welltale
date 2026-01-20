package fr.chosmoz.rank;

import java.util.List;
import java.util.UUID;

public interface RankRepository {
    Exception ERR_RANK_ALREADY_EXISTS = new Exception("rank already exists");
    Exception ERR_RANK_NOT_FOUND = new Exception("rank not found");

    void addRank(Rank rank) throws Exception;

    Rank getRank(UUID rankId) throws Exception;

    List<Rank> getCachedRanks() throws Exception;

    void updateRank(Rank rank) throws Exception;

    void deleteRank(UUID rankId) throws Exception;
}
