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
package com.openbravo.pos.panels;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.ThumbNailBuilder;

/**
 *
 * @author adrianromero
 */
public class JPaymentCashPosDetail extends javax.swing.JPanel implements JCloseCashInterface {
    
    private BigDecimal m_Total;
    JLabel lblTotal = null;
    private ArrayList<JTextField> arrayTextField = new ArrayList<>();
    /**
     * Creates new form JPaymentCash
     * @param dlSystem
     */
    public JPaymentCashPosDetail(DataLogicSystem dlSystem) {
        initComponents();        
        String code = dlSystem.getResourceAsXML("payment.cash");
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                script.put("payment", new ScriptPaymentCash(dlSystem));
                script.eval(code);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                msg.show(this);
            }
        }
        JLabel lbl = new JLabel("Total:");
        jPanel6.add(lbl);
        lblTotal = new JLabel(Formats.CURRENCY.formatValue(m_Total), Formats.CURRENCY.getAlignment());
        jPanel6.add(lblTotal);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void activate(BigDecimal dTotal, String transactionID) {
        
    }
    
    public class ScriptPaymentCash {

        private final DataLogicSystem dlSystem;
        private final ThumbNailBuilder tnbbutton;

        public ScriptPaymentCash(DataLogicSystem dlSystem) {
            this.dlSystem = dlSystem;
            tnbbutton = new ThumbNailBuilder(10, 10, "com/openbravo/images/cash.png");
        }

        public void addButton(String image, double amount) {
            JLabel lbl = new JLabel(Formats.CURRENCY.formatValue(amount));
            JTextField tf = new JTextField();
            GridLayout gridLayout = new GridLayout(0, 2, 5, 2);
            jPanel6.setLayout(gridLayout);            
            tf.setName(amount + "") ;
            lbl.setIcon(new ImageIcon(tnbbutton.getThumbNailText(dlSystem.getResourceAsImage(image), "")));
            lbl.setHorizontalTextPosition(SwingConstants.CENTER);
            lbl.setVerticalTextPosition(SwingConstants.BOTTOM);
            tf.setMargin(new Insets(2, 2, 2, 2));
            tf.addKeyListener(new KeyListenerTextField(false));
            arrayTextField.add(tf);
            jPanel6.add(lbl);
            jPanel6.add(tf);
        }
    }

    private class KeyListenerTextField implements KeyListener {

        private final boolean isDecimal;
        public KeyListenerTextField(boolean isDecimal) {
            this.isDecimal = isDecimal;
        }       
        
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if(this.isDecimal) {
                if((c < '0' || c > '9') 
                        && c != '.' && c != ' ') {
                    e.consume();
                } 
            } else {
                if((c < '0' || c > '9')) {
                    e.consume();
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
           
        }

        @Override
        public void keyReleased(KeyEvent e) {
        	calculateAmount();
        }
    }
    
    @Override
    public BigDecimal calculateAmount() {
        BigDecimal qty;
        BigDecimal amt = BigDecimal.ZERO;
        for (JTextField jTextField : arrayTextField) {        
            qty = jTextField.getText() == null || jTextField.getText().trim().length() == 0 ? BigDecimal.ZERO : new BigDecimal(jTextField.getText());
            if(qty != BigDecimal.ZERO) {
                amt = amt.add(qty.multiply(new BigDecimal(jTextField.getName())));
            }
        }
        try {
            lblTotal.setText(Formats.CURRENCY.parseValue(amt.toString()).toString());
        } catch (BasicException ex) {
            Logger.getLogger(JPaymentCashPosDetail.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_Total = amt;
        return m_Total;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();

        setAutoscrolls(true);
        setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        java.awt.GridBagLayout jPanel6Layout = new java.awt.GridBagLayout();
        jPanel6Layout.columnWidths = new int[] {0};
        jPanel6Layout.rowHeights = new int[] {0};
        jPanel6.setLayout(jPanel6Layout);
        jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

        add(jPanel5, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    // End of variables declaration//GEN-END:variables

}
