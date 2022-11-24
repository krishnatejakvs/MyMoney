package com.kt.navi.mymoney.entity;

import org.springframework.lang.NonNull;
import com.kt.navi.mymoney.enums.Assets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class FundEntity {
    @NonNull private Assets asset;
    @NonNull private Double amount;
}
