package com.kt.navi.mymoney.entity;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class FundProtfolio implements Cloneable{
    @NonNull private final List<FundEntity> funds;

    @Override
    public FundProtfolio clone() {
        return new FundProtfolio (
            funds.stream()
                .map(e -> new FundEntity(e.getAsset(), e.getAmount()))
                .collect(Collectors.toList()));
        
    }

    @Override
    public String toString() {
        return funds.stream()
        .map(entity -> Integer.toString((int) Math.floor(entity.getAmount())))
        .collect(Collectors.joining(" "));
    }

    /**
   * This method sums the investment made across all asset class and returns total amount invested
   *
   * @return total investment across all asset class
   */
    public double getTotalInvestment() {
        return funds.stream().mapToDouble(FundEntity::getAmount).sum();
    }

}
