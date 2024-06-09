package com.github.wohaopa.tc4helper.autoplay;

import java.util.ArrayList;
import java.util.List;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.HexUtils;

public abstract class Action {

    public static Action create(HexUtils.Hex hex, Aspect aspect, AspectData aspectData, GridData gridData) {
        if (aspectData.getAmount(aspect) > 0) return new DirectAction(hex, aspect, aspectData, gridData);
        if (!aspect.isPrimal()) return new CombinedAction(hex, aspect, aspectData, gridData);
        return new MissingAction(hex, aspect, aspectData, gridData);
    }

    public abstract void execute(GuiResearchTableHelperInterface object);

    private static class Update {

        protected List<GridData.Node> nodes;
        protected int groupNew;
        protected int groupOld;

        public Update(List<GridData.Node> nodes, int groupNew, int groupOld) {
            this.nodes = nodes;
            this.groupNew = groupNew;
            this.groupOld = groupOld;
        }
    }

    private final List<Update> updates = new ArrayList<>();

    protected final Aspect aspect;
    protected final AspectData aspectData;

    protected final HexUtils.Hex hex;
    private final GridData gridData;

    private Action(HexUtils.Hex hex, Aspect aspect, AspectData aspectData, GridData gridData) {
        this.aspect = aspect;
        this.hex = hex;
        this.aspectData = aspectData;
        this.gridData = gridData;
    }

    // 只有地图更新逻辑
    public double progress() {
        // 只有有地址的操作才进行地图更新，否则只是消耗要素
        if (hex != null) {
            GridData.Node node = gridData.get(hex.toString());

            // 第一步：地图更新
            node.neighbor.forEach(node1 -> {
                if (node1.type != 0 && AspectData.canBeConnected(aspect, node1.aspect)) {
                    if (node.group == -1) node.group = node1.group;
                    else if (node.group != node1.group) {
                        int newV = Math.min(node1.group, node.group);
                        int oldV = Math.max(node1.group, node.group);

                        updates.add(new Update(gridData.update(newV, oldV), newV, oldV));
                    }
                }
            });

            if (node.type != 0) return 0;
            // 第二步：放置要素
            gridData.place(hex.toString(), aspect, node.group);
        }
        return 1000;
    }

    public void traceback() {
        // 只有有地址的操作才进行地图更新，否则只是消耗要素
        if (hex != null) {
            GridData.Node node = gridData.get(hex.toString());

            if (node.type != 2) throw new RuntimeException("错误");

            // 第一步：恢复地图
            for (int i = updates.size() - 1; i >= 0; i--) {
                Update update = updates.get(i);
                gridData.rollback(update.nodes, update.groupOld);
            }

            // 第二步：清理要素
            gridData.destroy(hex.toString());
        }
    }

    private static class DirectAction extends Action {

        private DirectAction(HexUtils.Hex hex, Aspect aspect, AspectData aspectData, GridData gridData) {
            super(hex, aspect, aspectData, gridData);
        }

        @Override
        public void execute(GuiResearchTableHelperInterface object) {
            if (hex != null) object.place(this.hex, aspect);
        }

        @Override
        public double progress() {
            // 只有地图更新逻辑
            super.progress();
            // 第三步，消耗要素
            return this.aspectData.decrease(aspect);
        }

        @Override
        public void traceback() {
            // 只有地图更新逻辑
            super.traceback();
            // 第三步，恢复要素
            this.aspectData.increase(aspect);
        }
    }

    private static class MissingAction extends Action {

        @Override
        public void execute(GuiResearchTableHelperInterface object) {
            throw new RuntimeException("无法做到！");
        }

        private MissingAction(HexUtils.Hex hex, Aspect aspect, AspectData aspectData, GridData gridData) {
            super(hex, aspect, aspectData, gridData);
        }

        @Override
        public double progress() {
            return 1000;
        }

        @Override
        public void traceback() {

        }

    }

    private static class CombinedAction extends Action {

        private Action action1;
        private Action action2;

        @Override
        public void execute(GuiResearchTableHelperInterface object) {
            if (action1 instanceof CombinedAction) {
                action1.execute(object);
            }
            if (action2 instanceof CombinedAction) {
                action2.execute(object);
            }
            object.combine(action1.aspect, action2.aspect);

            if (hex != null) object.place(this.hex, aspect);
        }

        private CombinedAction(HexUtils.Hex hex, Aspect aspect, AspectData aspectData, GridData gridData) {
            super(hex, aspect, aspectData, gridData);
        }

        @Override
        public double progress() {
            super.progress();

            this.action1 = Action.create(null, aspect.getComponents()[0], this.aspectData, null);
            this.action2 = Action.create(null, aspect.getComponents()[1], this.aspectData, null);

            return this.action1.progress() + this.action2.progress();
        }

        @Override
        public void traceback() {
            super.traceback();

            this.action1.traceback();
            this.action2.traceback();
        }
    }
}
