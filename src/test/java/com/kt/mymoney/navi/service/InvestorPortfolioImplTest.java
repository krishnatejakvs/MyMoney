package com.kt.mymoney.navi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.zip.DataFormatException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static java.time.Month.*;

import com.kt.navi.mymoney.data.PortfolioData;
import com.kt.navi.mymoney.services.InvestorPortfolioImpl;
import static com.kt.navi.mymoney.Constants.constants.CANNOT_REBALANCE;

public class InvestorPortfolioImplTest {
    @Mock private PortfolioData portfolioData;
    @Mock private InvestorPortfolioImpl investorPortolioService;

    @BeforeEach
    public void setUp() {
        portfolioData = new PortfolioData();
        investorPortolioService = new InvestorPortfolioImpl(portfolioData);    
    }
    @Test
    void testAllocateNull() {
        assertThrows(
            DataFormatException.class,
            () -> investorPortolioService.allocate(null),
            "Expected Allocate method to throw Exception, but it didn't.");
    }
    @Test
    void testAllocateCorrectValues() throws DataFormatException {
        List<Double> initialAllocation = Arrays.asList(10d, 20d, 30d);
        investorPortolioService.allocate(initialAllocation);
        assertEquals(initialAllocation.size(), portfolioData.initAllocation.getFunds().size());
        assertEquals(
            initialAllocation.stream().mapToDouble(Double::doubleValue).sum(),
            portfolioData.initAllocation.getTotalInvestment());
        assertEquals(
            100, portfolioData.desiredWeightsOfFunds.values().stream().mapToDouble(Double::doubleValue).sum());
    }
    @Test
    void testAllocateInCorrectValues() {
        assertThrows(
            DataFormatException.class,
            () -> investorPortolioService.allocate(Arrays.asList(10d, 20d, 30d, 40d)),
            "Expected method to throw Exception, but it didn't.");
    }
    @Test
    void testAllocateAlreadyAllocated() throws DataFormatException {
        List<Double> initialAllocation = Arrays.asList(10d, 20d, 30d);
        investorPortolioService.allocate(initialAllocation);
        assertThrows(
            IllegalStateException.class,
            () -> investorPortolioService.allocate(initialAllocation),
            "Expected Allocate method to throw Exception, but it didn't.");
    }
    @Test
    void testSipWithNullValues() {
        assertThrows(
            DataFormatException.class,
            () -> investorPortolioService.sip(null),
            "Expected Sip method to throw Exception, but it didn't.");
    }
    @Test
    void testSipWithInCorrectValues() {
        assertThrows(
            DataFormatException.class,
            () -> investorPortolioService.sip(Arrays.asList(10d, 20d, 30d, 40d)),
            "Expected Sip method to throw Exception, but it didn't.");
    }
    @Test
    void testSipWithCorrectValues() throws DataFormatException {
        List<Double> sipAmounts = Arrays.asList(10d, 20d, 30d);
        investorPortolioService.sip(sipAmounts);
        assertEquals(sipAmounts.size(), portfolioData.initSip.getFunds().size());
        assertEquals(
            sipAmounts.stream().mapToDouble(Double::doubleValue).sum(),
            portfolioData.initSip.getTotalInvestment());
    }
    @Test
    void testSipAlreadyAllocated() throws DataFormatException {
        List<Double> sipAmounts = Arrays.asList(10d, 20d, 30d);
        investorPortolioService.sip(sipAmounts);
        assertThrows(
            IllegalStateException.class,
            () -> investorPortolioService.sip(sipAmounts),
            "Expected Sip method to throw Exception, but it didn't.");
    }
    @Test
    void testChangeWithNullValues() {
        assertThrows(
            InputMismatchException.class,
            () -> investorPortolioService.change(null, JANUARY),
            "Expected Change method to throw Exception, but it didn't.");
    }
    @Test
    void testChangeWithInCorrectValues() {
        assertThrows(
            DataFormatException.class,
            () -> investorPortolioService.change(Arrays.asList(10d, 20d, 30d, 40d), JANUARY),
            "Expected Change method to throw Exception, but it didn't.");
    }
    @Test
    void testChangeWithCorrectValues() throws DataFormatException {
        List<Double> changeRate = Arrays.asList(10d, 20d, 30d);
        List<Double> initialAllocation = Arrays.asList(10d, 20d, 30d);
        investorPortolioService.allocate(initialAllocation);
        investorPortolioService.change(changeRate, JANUARY);
        assertEquals(changeRate.size(), portfolioData.monthsChangeRate.get(JANUARY).size());
    }
    @Test
    void testChangeAlreadyAllocatedForMonth() throws DataFormatException {
        List<Double> changeRate = Arrays.asList(10d, 20d, 30d);
        List<Double> initialAllocation = Arrays.asList(10d, 20d, 30d);
        investorPortolioService.allocate(initialAllocation);
        investorPortolioService.change(changeRate, JANUARY);
        assertThrows(
            IllegalStateException.class,
            () -> investorPortolioService.change(changeRate, JANUARY),
            "Expected Change method to throw Exception, but it didn't.");
    }
    @Test
    void testBalanceInSufficientData() throws DataFormatException {
        investorPortolioService.allocate(Arrays.asList(6000d, 3000d, 1000d));
        investorPortolioService.sip(Arrays.asList(2000d, 1000d, 500d));
        assertThrows(
            IllegalStateException.class,
            () -> investorPortolioService.balance(JANUARY),
            "Expected Change method to throw Exception, but it didn't.");
    }
    @Test
    void testBalance() throws DataFormatException {
        initializePortfolio();
        assertEquals("10593 7897 2272", investorPortolioService.balance(MARCH));
    }
    private void initializePortfolio() throws DataFormatException {
        investorPortolioService.allocate(Arrays.asList(6000d, 3000d, 1000d));
        investorPortolioService.sip(Arrays.asList(2000d, 1000d, 500d));
        investorPortolioService.change(Arrays.asList(4d, 10d, 2d), JANUARY);
        investorPortolioService.change(Arrays.asList(-10.00d, 40.00d, 0.00d), FEBRUARY);
        investorPortolioService.change(Arrays.asList(12.50d, 12.50d, 12.50d), MARCH);
        investorPortolioService.change(Arrays.asList(8.00d, -3.00d, 7.00d), APRIL);
        investorPortolioService.change(Arrays.asList(13.00d, 21.00d, 10.50d), MAY);
        investorPortolioService.change(Arrays.asList(10.00d, 8.00d, -5.00d), JUNE);
    }
    @Test
    void testReBalance() throws DataFormatException {
        initializePortfolio();
        assertEquals("23619 11809 3936", investorPortolioService.reBalance());
    }
    @Test
    void testReBalanceWithInsufficientData() throws DataFormatException {
        investorPortolioService.allocate(Arrays.asList(6000d, 3000d, 1000d));
        investorPortolioService.sip(Arrays.asList(2000d, 1000d, 500d));
        investorPortolioService.change(Arrays.asList(4d, 10d, 2d), JANUARY);
        String result = investorPortolioService.reBalance();
        assertEquals(CANNOT_REBALANCE, result);
    }
    

}
