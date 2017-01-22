/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.currency;

import com.openbravo.data.loader.DataRead;
import java.io.Serializable;
import java.util.Date;

import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;

/**
 *
 * @author tt-01
 */
public class ConversionRateInfo implements Serializable, IKeyed {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3561131609712827723L;
	
	private String m_ConversionRate_ID;
	private String m_Currency;
	private String m_CurrencyTo;
	private double m_DivideRate;
	private String m_IsActive;
	private double m_MultipyRate;
	private Date m_ValidFrom;
	private Date m_ValidTo;
        
	/**
	 * @param m_ConversionRate_ID
	 * @param m_Currency
	 * @param m_CurrencyTo
	 * @param m_DivideRate
	 * @param m_IsActive
	 * @param m_MultipyRate
	 * @param m_ValidFrom
	 * @param m_ValidTo
	 */
	public ConversionRateInfo(String m_ConversionRate_ID, String m_Currency,
			String m_CurrencyTo, double m_DivideRate, String m_IsActive, double m_MultipyRate, Date m_ValidFrom,
			Date m_ValidTo) {
		super();
		this.m_ConversionRate_ID = m_ConversionRate_ID;
		this.m_Currency = m_Currency;
		this.m_CurrencyTo = m_CurrencyTo;
		this.m_DivideRate = m_DivideRate;
		this.m_IsActive = m_IsActive;
		this.m_MultipyRate = m_MultipyRate;
		this.m_ValidFrom = m_ValidFrom;
		this.m_ValidTo = m_ValidTo;
	}

    public ConversionRateInfo(String isoCode, String symbol) {
        
    }
	/**
	 * @return the m_Currency
	 */
	public String getCurrency() {
		return m_Currency;
	}
	/**
	 * @param m_Currency the m_Currency to set
	 */
	public void setCurrency(String m_Currency) {
		this.m_Currency = m_Currency;
	}
	/**
	 * @return the m_CurrencyTo
	 */
	public String getCurrencyTo() {
		return m_CurrencyTo;
	}
	/**
	 * @param m_CurrencyTo the m_CurrencyTo to set
	 */
	public void setCurrencyTo(String m_CurrencyTo) {
		this.m_CurrencyTo = m_CurrencyTo;
	}
	/**
	 * @return the m_DivideRate
	 */
	public double getDivideRate() {
		return m_DivideRate;
	}
	/**
	 * @param m_DivideRate the m_DivideRate to set
	 */
	public void setDivideRate(double m_DivideRate) {
		this.m_DivideRate = m_DivideRate;
	}
	/**
	 * @return the m_IsActive
	 */
	public String getIsActive() {
		return m_IsActive;
	}
	/**
	 * @param m_IsActive the m_IsActive to set
	 */
	public void setIsActive(String m_IsActive) {
		this.m_IsActive = m_IsActive;
	}
	/**
	 * @return the m_MultipyRate
	 */
	public double getMultipyRate() {
		return m_MultipyRate;
	}
	/**
	 * @param m_MultipyRate the m_MultipyRate to set
	 */
	public void setMultipyRate(double m_MultipyRate) {
		this.m_MultipyRate = m_MultipyRate;
	}
	/**
	 * @return the m_ValidFrom
	 */
	public Date getValidFrom() {
		return m_ValidFrom;
	}
	/**
	 * @param m_ValidFrom the m_ValidFrom to set
	 */
	public void setValidFrom(Date m_ValidFrom) {
		this.m_ValidFrom = m_ValidFrom;
	}
	/**
	 * @return the m_ValidTo
	 */
	public Date getValidTo() {
		return m_ValidTo;
	}
	/**
	 * @param m_ValidTo the m_ValidTo to set
	 */
	public void setValidTo(Date m_ValidTo) {
		this.m_ValidTo = m_ValidTo;
	}
	@Override
	public Object getKey() {
		return null;
	}
	
    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "From =" + m_Currency + ", To=" + m_CurrencyTo;
	}
	
	public static SerializerRead getSerializerRead() {
        return (DataRead dr) -> new ConversionRateInfo(
                dr.getString(1), 
                dr.getString(2), 
                dr.getString(3), 
                dr.getDouble(4), 
                dr.getString(5), 
                dr.getDouble(6), 
                dr.getTimestamp(7), 
                dr.getTimestamp(8)
        );
        
    }	
        
    public double changeBaseToOther(double dEuros) {  
        return dEuros * m_MultipyRate;
    }
    public double changeOtherToBase(double dPts) {        
        return (100.0 * dPts / m_MultipyRate) / 100.0;
    }  
	

}
