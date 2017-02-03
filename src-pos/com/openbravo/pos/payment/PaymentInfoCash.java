//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2008-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.payment;

import com.openbravo.format.Formats;
import com.openbravo.pos.util.CurrencyChange;

public class PaymentInfoCash extends PaymentInfo {
    
    private final double m_dPaid;
    private final double m_dTotal;
    private boolean isDollarCash;
    
    /** 
     * Creates a new instance of PaymentInfoCash
     * @param dTotal
     * @param dPaid 
     * @param isDollarCash 
     */
    public PaymentInfoCash(double dTotal, double dPaid, boolean isDollarCash) {
        m_dTotal = dTotal;
        m_dPaid = dPaid;
        this.isDollarCash = isDollarCash;
    }
    
    @Override
    public PaymentInfo copyPayment(){
        return new PaymentInfoCash(m_dTotal, m_dPaid, isDollarCash);
    }
    
    @Override
    public String getName() {
    	if(isDollarCash) {
    		return "cash_dollar";
    	} else {
    		return "cash";
    	}
    }   
    @Override
    public double getTotal() {
        return m_dTotal;
    }
    @Override
    public String getTransactionID(){
        return "no ID";
    }
    
    public String printPaid() {
        return Formats.CURRENCY.formatValue(m_dPaid);
    }   
    public String printChange() {
    	if(isDollarCash) {
    		return String.valueOf(CurrencyChange.FORMAT_LOCALE.format(CurrencyChange.changeDollarToPeso(m_dPaid - m_dTotal)));
    	} else {
    		return Formats.CURRENCY.formatValue(m_dPaid - m_dTotal);
    	}
    }    
}
