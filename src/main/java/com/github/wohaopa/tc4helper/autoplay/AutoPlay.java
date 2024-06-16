package com.github.wohaopa.tc4helper.autoplay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;

public class AutoPlay extends Thread {

    public enum Status {
        Leisure,
        Searching,
        CanExecute,
        Execute,
        Done
    }

    private static final Map<Aspect, List<Aspect>> aspectMaps = new HashMap<>();
    static {

        for (Aspect aspect : Aspect.aspects.values()) {
            List<Aspect> list = aspectMaps.get(aspect);
            if (list == null) {
                aspectMaps.put(aspect, list = new LinkedList<>());
            }
            if (!aspect.isPrimal()) for (Aspect aspect1 : aspect.getComponents()) {
                list.add(aspect1);
                List<Aspect> list1 = aspectMaps.get(aspect1);
                if (list1 == null) {
                    aspectMaps.put(aspect, list1 = new LinkedList<>());
                }
                list1.add(aspect);
            }
        }
    }

    public AutoPlay() {
        this.setName("AutoPlay");
    }

    private GuiResearchTableHelperInterface object;
    private AspectList aspectList;
    private ResearchNoteData note;
    private Status status = Status.Leisure;
    private List<Edge> result;

    public boolean set(GuiResearchTableHelperInterface object, EntityPlayer player, ResearchNoteData note) {
        if (status != Status.Leisure && status != Status.Done || note == null) {
            return false;
        }
        this.aspectList = Thaumcraft.proxy.getPlayerKnowledge()
            .getAspectsDiscovered(player.getCommandSenderName());
        this.object = object;
        this.note = note;
        return true;
    }

    public Status getStatus() {
        return status;
    }

    public static boolean started = false;

    @Override
    public synchronized void start() {
        status = Status.Searching;
        if (!started) {
            started = true;
            super.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!this.isAlive() || this.isInterrupted()) break;
            if (status == Status.Searching) search();

            Thread.yield();
            try {
                Thread.sleep(10);
            } catch (Exception e) {}
        }

    }

    public static class Node {

        Set<Node> neighbor = new HashSet<>(6); // neighbor[0] = length
        HexUtils.Hex hex = null;
        Aspect aspect = null;
        int type = 0;
        List<Edge> edges = new LinkedList<>();

        @Override
        public String toString() {
            return "[" + hex + "]" + "(" + type + ')';
        }

    }

    public static class Edge {

        Node point1, point2;
        List<Node> pathNode = new LinkedList<>();
        List<AspectEntry> aspects;
        double value;
        int distanceNull, distanceAspect;

        public Edge(Node point1, Node point2) {
            this.point1 = point1;
            this.point2 = point2;
        }

        @Override
        public String toString() {
            return point1 + " -> "
                + point2
                + " D: "
                + pathNode.size()
                + " dN: "
                + distanceNull
                + " dA: "
                + distanceAspect
                + " V: "
                + value;
        }

    }

    private static class AspectEntry {

        Aspect aspect;
        List<Aspect> path = new LinkedList<>();
        double value;
    }

    private void search() {

        List<Node> roots = new ArrayList<>();
        List<Edge> edges = new LinkedList<>();
        Map<String, Node> map = new HashMap<>();

        // 算法
        // 第一步: 广度优先搜索(研究笔记) 得到距离图 计算两两节点的真实可达距离(地图距离)
        {
            // 地图构建
            {
                for (Map.Entry<String, ResearchManager.HexEntry> entry : note.hexEntries.entrySet()) {
                    Node node = map.get(entry.getKey());
                    if (node == null) {
                        map.put(entry.getKey(), node = new Node());
                    }

                    ResearchManager.HexEntry entry1 = entry.getValue();
                    node.aspect = entry1.aspect;
                    node.type = entry1.type;
                    node.hex = note.hexes.get(entry.getKey());

                    for (int j = 0; j < 6; j++) {
                        HexUtils.Hex hex = node.hex.getNeighbour(j);
                        String hexString = hex.toString();
                        if (note.hexes.containsKey(hexString)) {
                            Node node1 = map.get(hexString);
                            if (node1 == null) {
                                map.put(hexString, node1 = new Node());
                            }
                            node.neighbor.add(node1);
                        }
                    }

                    if (node.type == 1) {
                        roots.add(node);
                    }
                }
            }
            // 边构建
            {
                for (Node root : roots) {
                    Queue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(o -> o.edges.size()));
                    Map<Node, List<Node>> paths = new HashMap<>();

                    queue.add(root);
                    paths.put(root, new LinkedList<>());
                    paths.get(root)
                        .add(root);

                    while (!queue.isEmpty()) {
                        Node node1 = queue.poll();
                        List<Node> path1 = paths.get(node1);
                        for (Node node2 : node1.neighbor) {
                            List<Node> path2 = paths.get(node2);
                            if ((path2 == null || path2.size() > path1.size() + 1)) {
                                path2 = new LinkedList<>(path1);
                                path2.add(node2);
                                paths.put(node2, path2); // 更新路径
                                queue.add(node2);

                            }
                        }
                    }
                    for (Node node : roots) {
                        if (node == root) continue;

                        boolean flag = false;
                        for (Edge edge : edges) {
                            if (edge.point1 == node && edge.point2 == root
                                || edge.point1 == root && edge.point2 == node) {
                                flag = true;
                                break;
                            }
                        }
                        if (flag) continue;

                        Edge edge = new Edge(root, node);
                        edge.pathNode = paths.get(node);
                        edge.distanceNull = edge.pathNode.size();
                        for (Node path : edge.pathNode) {
                            if (path.type != 0) edge.distanceNull--;
                            path.edges.add(edge);
                        }

                        edges.add(edge);
                    }
                }

            }
        }
        // 第二步: 广度优先搜索(要素组成) 得到路线图 在最短可达距离的基础上计算两两节点之间的链接距离
        // 价值计算
        {
            for (Edge edge : edges) {
                if (edge.distanceNull != edge.pathNode.size() - 2) { // 有特殊节点 比如玩家放置的，或者原始有的

                }

                Queue<AspectEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(o -> o.value));
                AspectEntry root = new AspectEntry();
                queue.add(root);
                root.aspect = edge.point1.aspect;
                root.path.add(root.aspect);
                root.value = getAspectValue(root.aspect);

                List<AspectEntry> completes = new LinkedList<>();
                int length = edge.pathNode.size();
                double min = Double.MAX_VALUE;

                while (!queue.isEmpty()) {
                    AspectEntry entry = queue.poll();
                    if (entry.aspect == edge.point2.aspect && entry.path.size() >= length) {
                        completes.add(entry);
                        min = Math.min(min, entry.value);
                        continue;
                    }
                    if (entry.path.size() > 2 * length || entry.value > min) continue;;

                    for (Aspect aspect : aspectMaps.get(entry.aspect)) {
                        AspectEntry aspectEntry = new AspectEntry();
                        aspectEntry.aspect = aspect;
                        aspectEntry.path = new LinkedList<>(entry.path);
                        aspectEntry.path.add(aspect);
                        aspectEntry.value = getAspectValue(aspect) + entry.value;
                        queue.add(aspectEntry);
                    }
                }
                edge.aspects = completes;
                edge.aspects.sort(Comparator.comparingDouble(o -> o.value));
                edge.value = edge.aspects.get(0).value;
            }
        }

        // 第三步: 最小生成树(要素路线图) 得到生成树

        {
            edges.sort(Comparator.comparingDouble(o -> o.value));

            List<Edge> selected = new LinkedList<>();
            Map<Node, Integer> groups = new HashMap<>();
            for (int i = 0; i < roots.size(); i++) {
                groups.put(roots.get(i), i);
            }

            for (Edge edge : edges) {

                int group1 = groups.get(edge.point1);
                int group2 = groups.get(edge.point2);
                if (group1 != group2) {
                    selected.add(edge);
                    for (Node node : groups.keySet()) {
                        int group3 = groups.get(node);
                        if (group3 == group2) {
                            groups.put(node, group1);
                        }
                    }
                }

            }

            result = selected;
        }

        // 修改放置路径，避免冲突
        {
            // for (Edge edge : result) {
            // if (edge.pathNode.size() != edge.aspects.get(0).path.size()) {
            //
            // Queue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(o -> o.edges.size()));
            // Map<Node, List<Node>> paths = new HashMap<>();
            //
            // int length = edge.aspects.get(0).path.size();
            // queue.add(edge.point1);
            // paths.put(edge.point1, new LinkedList<>());
            // paths.get(edge.point1)
            // .add(edge.point1);
            //
            // while (!queue.isEmpty()) {
            // Node node1 = queue.poll();
            // List<Node> path1 = paths.get(node1);
            // for (Node node2 : node1.neighbor) {
            // List<Node> path2 = paths.get(node2);
            // if ((path2 == null || path2.size() > length)) {
            // path2 = new LinkedList<>(path1);
            // path2.add(node2);
            // paths.put(node2, path2); // 更新路径
            // queue.add(node2);
            // }
            // }
            // }
            // edge.pathNode = paths.get(edge.point2);
            //
            // } // 需要重新规划路线
            // }

        }

        status = Status.Execute;
    }

    private double getAspectValue(Aspect aspect) {
        int amount = aspectList.getAmount(aspect);
        return amount < 1 ? 1000 : (double) 1 / amount;
    }

    public void execute() {
        if (status != Status.Execute && status != Status.CanExecute) {
            return;
        }

        result.forEach(edge -> {

            for (int i = 1; i < edge.pathNode.size() - 1; i++) {
                Node node1 = edge.pathNode.get(i);
                if (node1.aspect != null) continue;
                Aspect aspect = edge.aspects.get(0).path.get(i);
                node1.aspect = aspect;

                object.place(node1.hex, aspect);
            }
        });

        status = Status.Done;
    }

    public void abort() {
        status = Status.Done;
    }

}
