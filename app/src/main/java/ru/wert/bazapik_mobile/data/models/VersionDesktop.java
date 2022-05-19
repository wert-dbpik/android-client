package ru.wert.bazapik_mobile.data.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.wert.bazapik_mobile.data.interfaces.Item;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class VersionDesktop extends _BaseEntity implements Item, Comparable<VersionDesktop> {

    private String data;
    private String name;
    private String path;
    private String note;

    @Override
    public String toUsefulString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull VersionDesktop o) {
        String[] nn1 = getName().split("\\.", -1);
        String[] nn2 = o.getName().split("\\.", -1);
        for(int i = 0; i < nn1.length; i ++){
            int res = nn1[i].compareTo(nn2[i]);
            if(res != 0)
                return res;
        }
        return 0;
    }
}
