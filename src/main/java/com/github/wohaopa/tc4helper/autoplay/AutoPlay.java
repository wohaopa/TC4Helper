package com.github.wohaopa.tc4helper.autoplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.minecraft.entity.player.EntityPlayer;

import com.github.wohaopa.tc4helper.TC4Helper;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.HexUtils;
import thaumcraft.common.lib.research.ResearchNoteData;

public class AutoPlay extends Thread {
    static Set<GridData.Node> EMPTY = new HashSet<>();

    // 第一位表示是否在运行，1停止，0运行
    // 第二位表示是否有结果，1有，0无
    // 第三位表示四否结束
    private boolean aborted = false;
    private boolean result = false;
    private boolean completed = false;

    private final GuiResearchTableHelperInterface object;
    private final GridData gridData;
    private final AspectData aspectData;

    private final Stack<Action> actions = new Stack<>();

    private final Stack<Action> completeActions = new Stack<>();
    private double completeCost = 10000;

    private int dis = 0;

    public AutoPlay(GuiResearchTableHelperInterface object, EntityPlayer player, ResearchNoteData note) {
        this.object = object;
        aspectData = new AspectData(
            Thaumcraft.proxy.getPlayerKnowledge()
                .getAspectsDiscovered(player.getCommandSenderName()));
        gridData = new GridData(note);
    }

    @Override
    public void run() {
        search();
    }

    private void search() {
        long l1 = System.currentTimeMillis();

        List<GridData.Node> origins = gridData.getOrigins();
        HexUtils.Hex hex = new HexUtils.Hex(0, 0);
        Set<GridData.Node> nodes = new HashSet<>();
        for (GridData.Node node1 : origins) {
            nodes.addAll(node1.neighbor);
            dis += HexUtils.getDistance(hex, node1.hex);
        }
        dis += origins.size();
        if (dis > 10) dis = 10;

        search0(nodes, 0);

        completed = true;
        long l2 = System.currentTimeMillis();
        TC4Helper.LOG.info("Time: {}ms", (l2 - l1));
    }

    // List表示此步可以用的节点
    private void search0(Set<GridData.Node> current, double cost) { // 当前代价
        {
            StringBuilder stringBuilder = new StringBuilder();
            actions.forEach(action -> stringBuilder.append(action.hex.toString()).append("=").append(action.aspect.getName()).append(";"));
            TC4Helper.LOG.info(stringBuilder);
        }

        if (gridData.complete()) {
            // 成功。保存所有行为
            if (cost < completeCost || (cost == completeCost && completeActions.size() > actions.size())) {
                result = true;
                // 如果当前成本小于先前的或者步骤比当前的多，则保存当前行为
                completeActions.clear();
                completeActions.addAll(actions);
                completeCost = cost;
            }
            return;
        }
        if (actions.size() >= dis) return;

        if (current.isEmpty()) return;
        if (aborted) return; // 终止

        // node表示当前的空节点
        Collection<GridData.Node> nodes = current.size() > 1 ? gridData.sort(current) : current;
        for (GridData.Node node : nodes) {
            if (node.type != 0) continue; // 非空白节点

            Map<Aspect, Integer> aspectsMap = new LinkedHashMap<>();

            for (GridData.Node node1 : node.neighbor) // 空节点一圈已经存在的节点可以连接的要素
                if (node1.type != 0) {
                    for (Aspect aspect1 : AspectData.getAspects(node1.aspect)) {
                        int i = aspectsMap.computeIfAbsent(aspect1, aspect -> 0);
                        aspectsMap.put(aspect1, i + 1);
                    }
                }
            List<Aspect> aspects = new ArrayList<>(aspectsMap.keySet());
            aspects.sort(Comparator.comparingInt(aspectsMap::get));
            for (Aspect aspect : aspects) {
                if (aborted) return; // 终止

                Action action = Action.create(node.hex, aspect, aspectData, gridData);
                actions.push(action);
                double cost1 = action.progress() + cost;// 本步代价

                if (cost1 < completeCost) {
                    Set<GridData.Node> next = EMPTY;
                    if (actions.size() != dis) {
                        next = new LinkedHashSet<>(current);
                        next.remove(node);
                        for (GridData.Node node1 : node.neighbor) {
                            if (node1.type == 0) next.add(node1);
                        }
                    }
                    search0(next, cost1); // 剩余直到完成所需要的代价
                }
                action.traceback();
                actions.pop();
            }

        }
    }

    public void execute() {
        if (result) {
            for (Action action : completeActions) {
                action.execute(object);
            }
        }
    }

    public void abort() {
        aborted = true;
    }

    public boolean isAborted() {
        return aborted;
    }

    public boolean isResult() {
        return result;
    }

    public boolean isCompleted() {
        return completed;
    }
}
