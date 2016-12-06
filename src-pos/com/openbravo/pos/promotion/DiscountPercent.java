/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.promotion;

import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.RoundUtils;

/**
 *
 * @author Dixon Martinez
 */
public class DiscountPercent {
    
    public TicketLineInfo lineDiscPercent (TicketLineInfo p_TicketLineInfo, double p_Discount) {
        double lineDiscount = p_TicketLineInfo.getDiscountRate();
        double linePrice = p_TicketLineInfo.getPriceTax();
        double lineNoDiscount = p_TicketLineInfo.getPriceTaxNoDiscount();
        
        if (lineDiscount == 0.0
                || lineDiscount != p_Discount) {
        	if(lineDiscount != 0.0){
        		p_TicketLineInfo.setPriceTax(RoundUtils.getValue(lineNoDiscount - lineNoDiscount * p_Discount));
        	}else {
                p_TicketLineInfo.setPriceTax(RoundUtils.getValue(linePrice - linePrice * p_Discount));
            }
        } 
        p_TicketLineInfo.setProperty("discountRate", Double.toString(p_Discount));
        p_TicketLineInfo.setRateDiscount(p_Discount);
        return p_TicketLineInfo;
    }
}
