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

import com.openbravo.pos.util.CurrencyChange;

public class PaymentInfoCashDollar extends PaymentInfo {
    
    private final double m_dPaid;
    private final double m_dTotal;
    
    /** 
     * Creates a new instance of PaymentInfoCash
     * @param dTotal
     * @param dPaid 
     */
    public PaymentInfoCashDollar(double dTotal, double dPaid) {
        m_dTotal = dTotal;
        m_dPaid = dPaid;
    }
    
    @Override
    public PaymentInfo copyPayment(){
        return new PaymentInfoCashDollar(m_dTotal, m_dPaid);
    }
    
    @Override
    public String getName() {
        return "cash_dollar";
    }   
    @Override
    public double getTotal() {
        return CurrencyChange.changeDollarToPeso(m_dTotal);
    }
    @Override
    public String getTransactionID(){
        return "no ID";
    }
    
    public String printPaid() {
        return CurrencyChange.formatDollarValue(m_dPaid);
    }   
    
    public String printChange() {
        return String.valueOf(CurrencyChange.FORMAT_LOCALE.format(CurrencyChange.changeDollarToPeso(m_dPaid - m_dTotal)));
    }    
}
