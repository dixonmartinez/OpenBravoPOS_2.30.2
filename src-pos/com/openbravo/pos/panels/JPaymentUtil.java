package com.openbravo.pos.panels;

import com.openbravo.pos.payment.PaymentInfo;

public interface JPaymentUtil {
	abstract void addTabs();

    abstract void setStatusPanel(boolean isPositive, boolean isComplete);

    abstract PaymentInfo getDefaultPayment(double total);
}
