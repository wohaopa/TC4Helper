package com.github.wohaopa.tc4helper.autoplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.HexUtils;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;

public class GridData {

    private final Map<String, Node> map;
    private final List<Node> origins;
    private final Map<HexUtils.Hex, Integer> priorities;

    public GridData(GridData gridData) {
        this.map = new HashMap<>(gridData.map);
        this.origins = new ArrayList<>(gridData.origins);
        this.priorities = new HashMap<>(gridData.priorities);
    }

    public GridData(ResearchNoteData noteData) {
        this.map = new HashMap<>();
        this.origins = new ArrayList<>();
        this.priorities = new HashMap<>();
        noteData.hexEntries.forEach((s, hexEntry) -> map.put(s, new Node(s, hexEntry)));
        Node.count = 0;
        map.forEach((s, node) -> {
            if (node.type == 1) origins.add(node);
            for (int i = 0; i < 6; i++) {
                HexUtils.Hex hex = node.hex.getNeighbour(i);
                Node node1 = map.get(hex.toString());
                if (node1 != null) {
                    node.neighbor.add(node1);
                }
            }
        });

        for (Node node : map.values()) {
            if (node.type == 0) {
                int priority = 0;
                for (Node node1 : origins) priority += HexUtils.getDistance(node.hex, node1.hex);
                priorities.put(node.hex, priority);
            } else priorities.put(node.hex, 1000);
        }
    }

    public List<Node> sort(Set<Node> set) {
        return set.stream()
            .sorted((o1, o2) -> Integer.compare(priorities.get(o1.hex), priorities.get(o2.hex)))
            .collect(Collectors.toList());
    }

    protected Node get(String s) {
        return map.get(s);
    }

    public void place(String hex, Aspect aspect, int group) {
        Node node = map.get(hex);
        node.aspect = aspect;
        node.type = 2;
        node.group = group;
    }

    public void destroy(String hex) {
        Node node = map.get(hex);
        node.aspect = null;
        node.group = -1;
        node.type = 0;
    }

    public List<Node> update(int newValue, int oldValue) {

        List<Node> list = new ArrayList<>();
        for (Node node : map.values()) {
            if (node.group == oldValue) {
                node.group = newValue;
                list.add(node);
            }
        }
        return list;
    }

    public void rollback(List<Node> list, int origin) {
        for (Node node : list) {
            node.group = origin;
        }
    }

    public boolean complete() {
        for (Node node : origins) if (node.group != 0) return false;
        return true;
    }

    public Node getFirst() {
        for (Node node : origins) {
            if (node.group == 0) return node;
        }
        return null;
    }

    public List<Node> getOrigins() {
        return origins;
    }

    public static class Node {

        private static int count = 0;

        private Node(String hexStr, ResearchManager.HexEntry hexEntry) {
            this.type = hexEntry.type;
            String[] str = hexStr.split(":");
            this.hex = new HexUtils.Hex(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
            if (type != 0) this.aspect = hexEntry.aspect;
            if (type == 1) group = count++;
            neighbor = new ArrayList<>(6);
        }

        public int group = -1; // 用于区分各原始要素，同一个组表示连一起
        public int type; // 0 是空，1是原始，2是放置
        public HexUtils.Hex hex; // 网格Hex
        public Aspect aspect; // 要素，null是没有
        public List<Node> neighbor;// 周围的节点

        @Override
        public String toString() {
            if (aspect == null) return hex.toString();
            return hex + "=" + aspect.getName() + "(" + group + ")";
        }
    }
}
