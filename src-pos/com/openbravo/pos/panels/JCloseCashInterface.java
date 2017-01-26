/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.panels;

import java.awt.Component;
import java.math.BigDecimal;

/**
 *
 * @author tt-01
 */
public interface JCloseCashInterface {
    public Component getComponent();
    public void activate(BigDecimal dTotal, String transactionID); 
    public BigDecimal calculateAmount();
}
