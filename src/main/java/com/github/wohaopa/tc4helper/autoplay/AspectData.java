package com.github.wohaopa.tc4helper.autoplay;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class AspectData {

    public static Map<Aspect, List<Aspect>> relation = new HashMap<>();
    static {
        for (Aspect aspect : Aspect.aspects.values()) {
            if (aspect.isPrimal()) continue;
            List<Aspect> list2 = Arrays.asList(aspect.getComponents());
            relation.computeIfAbsent(aspect, k -> new LinkedList<>())
                .addAll(list2);
            list2.forEach(
                aspect1 -> relation.computeIfAbsent(aspect1, k -> new LinkedList<>())
                    .add(aspect));
        }
    }

    public static List<Aspect> getAspects(Aspect aspect) {
        return relation.get(aspect);
    }

    public static boolean canBeConnected(Aspect aspect1, Aspect aspect2) {
        return relation.get(aspect1)
            .contains(aspect2);
    }

    Map<Aspect, Integer> aspects = new HashMap<>();

    public AspectData(AspectList aspectList) {
        aspects.putAll(aspectList.aspects);
    }

    public int getAmount(Aspect aspect) {
        return aspects.getOrDefault(aspect, 0);
    }

    public double decrease(Aspect aspect) {
        if (aspects.containsKey(aspect)) {
            int i = aspects.get(aspect);
            aspects.put(aspect, i - 1);
            return (double) 1 / i;
        }
        return 1000;
    }

    public void increase(Aspect aspect) {
        aspects.put(aspect, aspects.get(aspect) + 1);
    }
}
