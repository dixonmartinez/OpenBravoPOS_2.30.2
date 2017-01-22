//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
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

package com.openbravo.pos.util;

public final class CurrencyChange {

    public final static double EUROS_CHANGE = 166.386;

    private double m_DivRate;
    private double m_MultRate;
    
    public CurrencyChange(double m_DivRate, double m_MultRate) {
    	System.out.println("Div Rate " + m_DivRate);
    	System.out.println("Mult Rate " + m_MultRate);
    	this.m_DivRate = m_DivRate;
    	this.m_MultRate = m_MultRate;
    }
  
    public double changeBaseToOther(double dEuros) {  
        return dEuros * m_MultRate;
    }
    public double changeOtherToBase(double dPts) {        
        return Math.rint(100.0 * dPts / m_DivRate) / 100.0;
    }   
}
