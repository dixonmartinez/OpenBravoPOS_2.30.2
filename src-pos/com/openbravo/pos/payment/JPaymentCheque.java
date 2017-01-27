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

package com.openbravo.pos.payment;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.util.RoundUtils;

public class JPaymentCheque extends javax.swing.JPanel implements JPaymentInterface {
    
    private JPaymentNotifier m_notifier;

    private double m_dPaid;
    private double m_dTotal;
    private AppView appView;
    private String m_sTransactionID;
    
    /** Creates new form JPaymentCash */
    public JPaymentCheque(AppView app, JPaymentNotifier notifier) {
        
        m_notifier = notifier;
        
        initComponents();  
        
        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);
        
        m_jKeyString.addPropertyChangeListener("Edition", new RecalculateState());
        m_jKeyString.addEditorKeys(m_jKeys);
 
        setAppView(app);
    }
    
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {

        m_sTransactionID = transID;
        m_dTotal = dTotal;
        
        resetState();
        
    }
    public PaymentInfo executePayment() {
        
        PaymentInfoCheque payinfo = new PaymentInfoCheque(m_dPaid, "cheque");
        payinfo.setMicr(cmbPunto.getSelectedItem().toString());
        payinfo.setCheckNo(lblNroComp.getText());
 
        return payinfo;
    }
    public Component getComponent() {
        return this;
    }

    void setAppView(AppView app) {
        this.appView = app;
        DataLogicSystem dlSystem = (DataLogicSystem) appView.getBean("com.openbravo.pos.forms.DataLogicSystem");
        
        String posName = dlSystem.getResourceAsText("pointofsale.banklist");
        String cardType = dlSystem.getResourceAsText("card.type");
        
        String[] posNameList = posName.split(",");
        String[] cardTypeList = cardType.split(",");

        cmbPunto.removeAllItems();
        
        for(int i =0; i < posNameList.length; i++){
            cmbPunto.addItem(posNameList[i]);
        }
    }
    
        private void resetState() {      
        m_notifier.setStatus(false, false);  
        
        m_jTendered.reset();
        m_jTendered.activate();
        m_jKeyString.reset();
        m_jKeyString.activate(); 

        lblNroComp.setText(null);
        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dTotal)));

        cmbPunto.setSelectedIndex(-1);
    }


    private void printState() {
        
        Double value = m_jTendered.getDoubleValue();
        
        if(m_jKeyString.isVisible())
            lblNroComp.setText(m_jKeyString.getText());

        if (value == null)
            m_dPaid = m_dTotal;
        else
            m_dPaid = value;

        if(m_jTendered.isVisible()){
            m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
        }
        int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);

        // if iCompare > 0 then the payment is not valid
        m_notifier.setStatus((m_dPaid > 0.0 && iCompare <= 0) && validateFields(), iCompare == 0 && validateFields());

    }
    private class RecalculateState implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }     
    
   public boolean validateFields(){
     return (isValidMonto() && isValidNroComp() && isValidatecmbPunto());
   }

    private boolean isValidNroComp() {
        return !(lblNroComp.getText() == null || lblNroComp.getText().equals(""));
    }
    private boolean isValidMonto() {
        return !(m_jMoneyEuros.getText() == null || m_jMoneyEuros.getText().equals(""));
    }
    
    private boolean isValidatecmbPunto(){
         return !((String)cmbPunto.getSelectedItem() == null || ((String)cmbPunto.getSelectedItem()).equals(""));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();
        m_jKeyString = new com.openbravo.editor.JEditorString();
        jPanel4 = new javax.swing.JPanel();
        m_jMoneyEuros = new javax.swing.JLabel();
        cmbPunto = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblNroComp = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);
        jPanel3.add(m_jKeyString, java.awt.BorderLayout.PAGE_START);

        jPanel1.add(jPanel3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel4.setLayout(null);

        m_jMoneyEuros.setBackground(new java.awt.Color(153, 153, 255));
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jMoneyEuros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMoneyEurosMouseClicked(evt);
            }
        });
        jPanel4.add(m_jMoneyEuros);
        m_jMoneyEuros.setBounds(130, 70, 150, 25);

        cmbPunto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPuntoActionPerformed(evt);
            }
        });
        jPanel4.add(cmbPunto);
        cmbPunto.setBounds(130, 30, 150, 29);

        jLabel9.setText(AppLocal.getIntString("Label.InputCash")); // NOI18N
        jPanel4.add(jLabel9);
        jLabel9.setBounds(30, 70, 100, 19);

        jLabel10.setText("Banco");
        jPanel4.add(jLabel10);
        jLabel10.setBounds(30, 30, 100, 20);

        lblNroComp.setBackground(new java.awt.Color(153, 153, 255));
        lblNroComp.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNroComp.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        lblNroComp.setOpaque(true);
        lblNroComp.setPreferredSize(new java.awt.Dimension(150, 25));
        lblNroComp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNroCompMouseClicked(evt);
            }
        });
        jPanel4.add(lblNroComp);
        lblNroComp.setBounds(130, 110, 150, 25);

        jLabel8.setText("Número");
        jPanel4.add(jLabel8);
        jLabel8.setBounds(30, 110, 100, 19);

        add(jPanel4, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbPuntoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPuntoActionPerformed
        m_jKeyString.deactivate();
        m_jTendered.deactivate();
        m_jTendered.setVisible(false);
        m_jKeyString.setVisible(false);
        //printState();
    }//GEN-LAST:event_cmbPuntoActionPerformed

    private void lblNroCompMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNroCompMouseClicked
        m_jKeyString.activate();
        m_jTendered.deactivate();
        m_jTendered.setVisible(false);
        m_jKeyString.setVisible(true);
        //System.out.println("Clicked");
    }//GEN-LAST:event_lblNroCompMouseClicked

    private void lblMoneyEurosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoneyEurosMouseClicked
        m_jTendered.activate();
        m_jKeyString.deactivate();
        m_jKeyString.setVisible(false);
        m_jTendered.setVisible(true);
        //System.out.println("Clicked");
    }//GEN-LAST:event_lblMoneyEurosMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbPunto;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblNroComp;
    private com.openbravo.editor.JEditorString m_jKeyString;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    // End of variables declaration//GEN-END:variables
    
}
