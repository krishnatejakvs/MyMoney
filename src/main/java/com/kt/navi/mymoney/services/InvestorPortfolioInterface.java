package com.kt.navi.mymoney.services;

import java.time.Month;
import java.util.List;
import java.util.zip.DataFormatException;

public interface InvestorPortfolioInterface {
    public void allocate(List<Double> allocation) throws DataFormatException;
    public void sip(List<Double> sip) throws DataFormatException;
    public void change(List<Double> changeRate, Month month) throws DataFormatException;
    public String balance(Month Month);
    public String reBalance();
}
