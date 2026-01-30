package fr.welltale.clazz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor // <- Required for Jackson
@Getter
public class Class {
    private UUID uuid;
    private String name;
    private String color;
    private List<String> spellSlugs;
}