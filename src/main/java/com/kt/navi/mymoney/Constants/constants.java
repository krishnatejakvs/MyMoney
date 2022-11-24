package com.kt.navi.mymoney.Constants;

import java.util.EnumSet;
import java.util.Set;

import com.kt.navi.mymoney.enums.Assets;

public class constants {
    public static final String CANNOT_REBALANCE = "CANNOT_REBALANCE";
    public static final Set<Assets> FUNDORDER_SET = EnumSet.of(Assets.EQUITY, Assets.DEBT, Assets.GOLD);
}
