package fr.welltale.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor // <- Required for Jackson
@Getter
public class Rank {
    @Setter
    private UUID id;

    private String name;
    private String prefix;
    private String color;
    private List<String> permissions;
}
