/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;

/**
 *
 * @author dixon
 */
public class CurrencyInfo implements Serializable, IKeyed{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3323917701630773351L;
	
	private String isoCode;
	private String symbol;
	private double fractionDigit;
    public CurrencyInfo() {
    }
    
    public CurrencyInfo(Locale loc) {
        this(Currency.getInstance(loc).getCurrencyCode(), Currency.getInstance(loc).getSymbol());
    }
    
    public CurrencyInfo (String isoCode, String symbol) {
    	this.isoCode = isoCode;
    	this.symbol = symbol;
    	Currency cur = Currency.getInstance(isoCode);
    	this.fractionDigit = cur.getDefaultFractionDigits();
    	
    	
    }
    
    @Override
    public Object getKey() {
        return isoCode;
    }

    /**
     * @return the m_IsoCode
     */
    public String getIsoCode() {
        return isoCode;
    }

    public String getSymbol() {
        return symbol;
    }
    
    public Double getFractionDigit() {
    	return fractionDigit;
    }
    
    @Override
    public String toString() {
        return isoCode + " - " + symbol;
    }
    
    public static List<CurrencyInfo> getCurrenciesAvailable() {
    	
    	ArrayList<CurrencyInfo> list = new ArrayList<>();
    	
    	for (Currency currency : new ArrayList<> (Currency.getAvailableCurrencies())) {
			list.add(new CurrencyInfo(currency.getCurrencyCode(), currency.getSymbol()));
		}
        return list ;
    }
    
    public static SerializerRead getSerializerRead() {
        return (DataRead dr) -> new CurrencyInfo(dr.getString(1), dr.getString(2));
    }
    
}
