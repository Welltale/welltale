package fr.chosmoz.clazz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor // <- Required for Jackson
@Getter
public class Class {
    private UUID uuid;
    private String name;
    private String color;
    private String defaultItemId;
}
