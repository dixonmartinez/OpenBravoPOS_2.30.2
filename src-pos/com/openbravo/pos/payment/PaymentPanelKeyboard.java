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

import com.openbravo.format.Formats;
import javax.swing.JComponent;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.util.RoundUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @contributor Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 */
public class PaymentPanelKeyboard extends javax.swing.JPanel implements PaymentPanel {
    
    private double m_dTotal;
    private String m_sTransactionID;
    private JPaymentNotifier m_notifier;
    private AppView appView;
    private double m_dPaid;
    
 
    public PaymentPanelKeyboard(JPaymentNotifier notifier) {
        
        m_notifier = notifier;
        
        initComponents();  
       
        m_jKeyString.addPropertyChangeListener("Edition", new Recalculate());
        m_jKeyString.addEditorKeys(m_jKeys);
        
        m_jkeyNumber.addPropertyChangeListener("Edition", new Recalculate());
        m_jkeyNumber.addEditorKeys(m_jKeys);
        m_jkeyNumber.activate();
   
    }
    
    @Override
    public JComponent getComponent(){
        return this;
    }
    
    @Override
    public void activate(String sTransaction, double dTotal) {
        
        m_sTransactionID = sTransaction;
        m_dTotal = dTotal;
        
        resetState();
    }
    

    
    private void resetState() {      
        m_notifier.setStatus(false, false);  
        m_jkeyNumber.setVisible(true);
        m_jkeyNumber.activate();
        m_jKeyString.setVisible(false);
        m_jkeyNumber.reset();
        m_jKeyString.setText("");
              
        lblNroComp.setText(null);
        lblMonto.setText(Formats.CURRENCY.formatValue(new Double(m_dTotal)));
        cmbPunto.setSelectedIndex(-1);
        cmbCard.setSelectedIndex(-1);
        printState();
    }
    
    @Override
    public PaymentInfoMagcard getPaymentInfoMagcard() {
        
        if (m_dTotal > 0.0) {
            return new PaymentInfoMagcard(
                    lblNroComp.getText(),
                    "", 
                    null,
                    null,
                    null,                    
                    null,                    
                    m_sTransactionID,
                    m_dPaid,
                    (String)cmbPunto.getSelectedItem(),
                    (String)cmbCard.getSelectedItem());
        } else {
            return new PaymentInfoMagcardRefund(
                    lblNroComp.getText(),
                    "", 
                    null,
                    null,
                    null,                    
                    null,                    
                    m_sTransactionID,
                    m_dPaid,
                    (String)cmbPunto.getSelectedItem(),
                    (String)cmbCard.getSelectedItem());
        }
    }    

    void setAppView(AppView app) {
        this.appView = app;
        DataLogicSystem dlSystem = (DataLogicSystem) appView.getBean("com.openbravo.pos.forms.DataLogicSystem");
        
        String posName = dlSystem.getResourceAsText("pointofsale.name");
        String cardType = dlSystem.getResourceAsText("card.type");
        
        String[] posNameList = posName.split(",");
        String[] cardTypeList = cardType.split(",");

        cmbPunto.removeAllItems();
        cmbCard.removeAllItems();
        
        for(int i =0; i < cardTypeList.length; i++){
            cmbCard.addItem(cardTypeList[i].trim());
        }
        
        for(int i =0; i < posNameList.length; i++){
            cmbPunto.addItem(posNameList[i]);
        }
    }
    
    private class Recalculate implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            RefreshLabels();
            printState();
        }
    }
   private void printState() {
       Double value = m_jkeyNumber.getDoubleValue();
            if (value == null || value == 0.0) {
                m_dPaid = m_dTotal;
            } else {            
                m_dPaid = value;
            } 
            
        int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);
        
        
        // if iCompare > 0 then the payment is not valid
        m_notifier.setStatus(m_dPaid > 0.0 && iCompare <= 0 &&validateFields(), iCompare == 0 && validateFields());
    }  
   
   public boolean validateFields(){
     return (isValidMonto() && isValidNroComp() && isValidatecmbPunto() &&isValidatecmbCard());
   }
    public void RefreshLabels(){
        
     if(m_jKeyString.isVisible())
          lblNroComp.setText(m_jKeyString.getText());
     if(m_jkeyNumber.isVisible()&& !m_jkeyNumber.getText().isEmpty()){
          lblMonto.setText(Formats.CURRENCY.formatValue(new Double(m_jkeyNumber.getText())));
          
     }
    
    }
    private boolean isValidNroComp() {
        return !(lblNroComp.getText() == null || lblNroComp.getText().equals(""));
    }
    private boolean isValidMonto() {
        return !(lblMonto.getText() == null || lblMonto.getText().equals(""));
    }
    
    private boolean isValidatecmbPunto(){
         return !((String)cmbPunto.getSelectedItem() == null || ((String)cmbPunto.getSelectedItem()).equals(""));
    }
          
        private boolean isValidatecmbCard(){
         return !((String)cmbCard.getSelectedItem() == null || ((String)cmbCard.getSelectedItem()).equals(""));
    }
   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbPunto = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        lblMonto = new javax.swing.JLabel();
        lblNroComp = new javax.swing.JLabel();
        lblTypeCard = new javax.swing.JLabel();
        cmbCard = new javax.swing.JComboBox();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jkeyNumber = new com.openbravo.editor.JEditorCurrencyPositive();
        m_jKeyString = new com.openbravo.editor.JEditorString();

        jLabel8.setText("Nro de Comp");

        jLabel6.setText("Monto");

        cmbPunto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPuntoActionPerformed(evt);
            }
        });

        jLabel9.setText("Punto");

        lblMonto.setBackground(new java.awt.Color(153, 153, 255));
        lblMonto.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMonto.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        lblMonto.setFocusCycleRoot(true);
        lblMonto.setOpaque(true);
        lblMonto.setPreferredSize(new java.awt.Dimension(150, 25));
        lblMonto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMontoMouseClicked(evt);
            }
        });

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

        lblTypeCard.setText("Tipo de Tarjeta");

        cmbCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblMonto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTypeCard))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNroComp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(cmbPunto, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cmbCard, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel6)
                        .addGap(17, 17, 17)
                        .addComponent(jLabel9))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(lblMonto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbPunto, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(lblNroComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbCard, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTypeCard)))
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(m_jkeyNumber, java.awt.BorderLayout.CENTER);
        jPanel3.add(m_jKeyString, java.awt.BorderLayout.PAGE_START);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(m_jKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCardActionPerformed
        printState();
    }//GEN-LAST:event_cmbCardActionPerformed

    private void lblNroCompMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNroCompMouseClicked
        m_jKeyString.activate();
        m_jkeyNumber.deactivate();
        m_jkeyNumber.setVisible(false);
        m_jKeyString.setVisible(true);

    }//GEN-LAST:event_lblNroCompMouseClicked

    private void lblMontoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMontoMouseClicked
        m_jkeyNumber.activate();
        m_jKeyString.deactivate();
        m_jkeyNumber.setVisible(true);
        m_jKeyString.setVisible(false);
    }//GEN-LAST:event_lblMontoMouseClicked

    private void cmbPuntoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPuntoActionPerformed
        printState();
    }//GEN-LAST:event_cmbPuntoActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbCard;
    private javax.swing.JComboBox cmbPunto;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblMonto;
    private javax.swing.JLabel lblNroComp;
    private javax.swing.JLabel lblTypeCard;
    private com.openbravo.editor.JEditorString m_jKeyString;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private com.openbravo.editor.JEditorCurrencyPositive m_jkeyNumber;
    // End of variables declaration//GEN-END:variables
    
}
