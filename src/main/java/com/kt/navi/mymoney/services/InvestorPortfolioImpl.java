package com.kt.navi.mymoney.services;
import java.time.Month;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.kt.navi.mymoney.data.PortfolioData;
import com.kt.navi.mymoney.entity.FundEntity;
import com.kt.navi.mymoney.entity.FundProtfolio;
import com.kt.navi.mymoney.enums.Assets;
import static com.kt.navi.mymoney.Constants.constants.CANNOT_REBALANCE;
import static com.kt.navi.mymoney.Constants.constants.FUNDORDER_SET;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InvestorPortfolioImpl implements InvestorPortfolioInterface{
    private final PortfolioData portfolioData;

    public InvestorPortfolioImpl(PortfolioData portfolioData) {
        this.portfolioData = portfolioData;
    }

    /* 
     * The function allocates initial amount to the fund portfolio and also calculates 
     * fund initial weightage.
     * 
     * @param allocation
     * @throws DataFormatException
     */
    @Override
    public void allocate(List<Double> allocation) throws DataFormatException {
        if (Objects.nonNull(portfolioData.initAllocation)) {
            throw new IllegalStateException("The funds have been already allocated");
        }
        portfolioData.initAllocation = createPortfolioFund(allocation);
        portfolioData.desiredWeightsOfFunds = calculateDesiredWeights();
        log.debug(
        "initial allocation and weights are {} {}",
        portfolioData.initAllocation,
        portfolioData.desiredWeightsOfFunds);
        
    }

    private Map<Assets, Double> calculateDesiredWeights() {
        if (Objects.isNull(portfolioData.initAllocation)) {
            throw new IllegalStateException("The funds are not yet Allocated");
          }
        return portfolioData.initAllocation.getFunds().stream()
          .collect(Collectors.toMap(FundEntity::getAsset , 
          e -> e.getAmount() * 100 / portfolioData.initAllocation.getTotalInvestment()));
    }

    /**
     * This method takes allocations across all assets and create portfolio
     * @param allocation
     * @return
     * @throws DataFormatException
     */
    private FundProtfolio createPortfolioFund(List<Double> allocation) throws DataFormatException { 
        validateInputs(FUNDORDER_SET, allocation);
        List<FundEntity> fundEntityList =
        Streams.zip(FUNDORDER_SET.stream(), allocation.stream(), FundEntity::new)
            .collect(Collectors.toList());
        return new FundProtfolio(fundEntityList);
    }

    private void validateInputs(Set<Assets> assetOrderForIO, List<Double> allocations)
      throws DataFormatException {
    if (Objects.isNull(allocations) || allocations.size() != assetOrderForIO.size()) {
      throw new DataFormatException("The input is not in the desired format");
    }
  }
    /* 
    * this function is used to add sip for funds for the given month
    * params sip
    */
    @Override
    public void sip(List<Double> sip) throws DataFormatException {
        // Since sip always starts from Feb, multiple entries are not allowed
        if (Objects.nonNull(portfolioData.initSip)) {
            throw new IllegalStateException("SIP is already recorded");
        }
        portfolioData.initSip = portfolioFundSipIntialization(sip);
        log.debug("Portfolio initialized with a monthly sip of {} ", portfolioData.initSip);
    }

    private FundProtfolio portfolioFundSipIntialization(List<Double> sip) throws DataFormatException {
        validateInputs(FUNDORDER_SET, sip);
        List<FundEntity> fundEntityList =
        Streams.zip(FUNDORDER_SET.stream(), sip.stream(), FundEntity::new)
            .collect(Collectors.toList());
        return new FundProtfolio(fundEntityList);
    }


    /* 
     * function takes channge rate for a given month and makes changes to the Fundportfolio for that month
     * @params changeRate 
     * @params month
     */
    @Override
    public void change(List<Double> changeRate, Month month)
    throws IllegalStateException, DataFormatException {
        if (Objects.nonNull(portfolioData.monthsChangeRate.getOrDefault(month, null))) {
            throw new IllegalStateException(
                "The Rate of Change for month " + month.name() + " is already recorded");
        }
        if (Objects.isNull(changeRate) || Objects.isNull(month)) {
            throw new InputMismatchException("One of the supplied parameter is null.");
        }
        if (changeRate.size() != FUNDORDER_SET.size()) {
            throw new DataFormatException("The input is not in the desired format");
          }
        Map<Assets, Double> change =
        Streams.zip(FUNDORDER_SET.stream(), changeRate.stream(), Maps::immutableEntry)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        portfolioData.monthsChangeRate.put(month, change);
        updateBalance(month);
    
    }
    /**
     * On receiving the change this funtion updates the balances for the month along with sip and change
     */
    private void updateBalance(Month month) {
        FundProtfolio sip;
        FundProtfolio previousData;
        if (month == Month.JANUARY) {
            sip = null;
            previousData = portfolioData.initAllocation;
        } else {
            sip = portfolioData.initSip;
            previousData = portfolioData.monthsBalance.lastEntry().getValue().clone();
        }
        FundProtfolio Fund =
            calculateBalance(
                previousData,
                sip,
                portfolioData.monthsChangeRate.get(month));
        portfolioData.monthsBalance.put(month, Fund);
        if(month == Month.JUNE || month == Month.DECEMBER){
            fundReBalance(Fund);
        }
    }

    private void fundReBalance(FundProtfolio currentFunds) {
        List<FundEntity> funds = currentFunds.getFunds();
        double totalInvestment = currentFunds.getTotalInvestment();
        funds.forEach(
            entity -> {
            double desiredWeight = portfolioData.desiredWeightsOfFunds.get(entity.getAsset());
            entity.setAmount(Math.floor(totalInvestment * desiredWeight / 100));
            });
        log.debug(
            "Re-balanced the current total balance of {} to desired weights of {} to {}",
            currentFunds.getTotalInvestment(),
            portfolioData.desiredWeightsOfFunds,
            currentFunds);
    }

    private FundProtfolio calculateBalance(FundProtfolio previousBalance,
                                           FundProtfolio monthlySip ,
                                            Map<Assets, Double> changeRate) {

        log.debug(
            "Updating current balance of {}, with a sip of {} and market change rate of {}",
            previousBalance,
            monthlySip,
            changeRate);
        FundProtfolio balAfterSip = applySipInvestment(previousBalance, monthlySip);
        return applyMarketChange(balAfterSip, changeRate);
        
    }

    private FundProtfolio applyMarketChange(FundProtfolio balAfterSip, Map<Assets, Double> changeRate) {
        List<FundEntity> funds = balAfterSip.getFunds();
        funds.forEach(
            entity -> {
            double rate = changeRate.get(entity.getAsset());
            double updatedAmount = entity.getAmount() * (1 + rate / 100);
            entity.setAmount(Math.floor(updatedAmount));
            });
        return balAfterSip;
    }

    private FundProtfolio applySipInvestment(FundProtfolio previousBalance, FundProtfolio monthlySip) {
        List<FundEntity> funds = previousBalance.getFunds();
        if (Objects.nonNull(monthlySip)) {
        IntStream.range(0, funds.size())
            .forEach(
                index -> {
                    FundEntity fundEntity = funds.get(index);
                    double sipAmount = monthlySip.getFunds().get(index).getAmount();
                    fundEntity.setAmount(Math.floor(fundEntity.getAmount() + sipAmount));
                });
        }
        return previousBalance;
    }

    /* 
     * Function prints balance for the requested month
     * @params month
     */
    @Override
    public String balance(Month month) {
        FundProtfolio fund =
            Optional.ofNullable(portfolioData.monthsBalance.get(month))
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "The balance is requested for the month  "
                                + month.name()
                                + "with no data"));
        // System.out.println(fund.toString()); 
        return fund.toString();
    }

    @Override
    public String reBalance() {
        Month lastUpdatedMonth = portfolioData.monthsBalance.lastEntry().getKey();
        Month lastRebalancedMonth = getLastReBalancedMonth(lastUpdatedMonth);
        FundProtfolio balance = portfolioData.monthsBalance.getOrDefault(lastRebalancedMonth, null);
        // System.out.println(Objects.nonNull(balance) ? balance.toString() : CANNOT_REBALANCE);
        return Objects.nonNull(balance) ? balance.toString() : CANNOT_REBALANCE;
    }

    private Month getLastReBalancedMonth(Month month) {
        return month == Month.DECEMBER ? month : Month.JUNE;
    }    

    
}
