package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Course {
    private int id;
    private UUID code;
    private String name;

    @Override
    public String toString() {
        return "\n{\n" +
                "id=" + id +
                ",\ncode=" + code +
                ",\nname='" + name + '\'' +
                "\n}";
    }
}
