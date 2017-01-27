/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.payment;

/**
 * modificacion para guardar las vueltas en los pagos y para guardar informacion de las tarjetas de credito
 * change for save payment return and credit card information
 * @author Carlos Prieto - SmartJSP S.A.S.
 */
public class PaymentInfoCheque extends PaymentInfoTicket{
    private String checkNo;
    private String accountNo;
    private String micr;//sucursal

    public PaymentInfoCheque(double dTicket, String sName) {
        super(dTicket, sName);
    }

    
    
    
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getCheckNo() {
        return checkNo;
    }

    public void setCheckNo(String checkNo) {
        this.checkNo = checkNo;
    }

    public String getMicr() {
        return micr;
    }

    public void setMicr(String micr) {
        this.micr = micr;
    }
    
    
    
}
