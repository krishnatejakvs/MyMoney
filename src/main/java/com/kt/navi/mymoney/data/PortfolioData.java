package com.kt.navi.mymoney.data;
import java.time.Month;
import java.util.*;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kt.navi.mymoney.entity.FundProtfolio;
import com.kt.navi.mymoney.enums.Assets;

import lombok.Getter;

@Component
@Getter
@Scope("singleton")
public class PortfolioData {
    public TreeMap<Month, FundProtfolio> monthsBalance = new TreeMap<>();
    public TreeMap<Month, Map<Assets, Double>> monthsChangeRate = new TreeMap<>();
    public FundProtfolio initAllocation;
    public FundProtfolio initSip;
    public Map<Assets, Double> desiredWeightsOfFunds = new HashMap<>();
}
