package com.github.wohaopa.tc4helper.autoplay;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.HexUtils;

public interface GuiResearchTableHelperInterface {

    void combine(Aspect aspect1, Aspect aspect2);

    void place(HexUtils.Hex hex, Aspect aspect);
}
