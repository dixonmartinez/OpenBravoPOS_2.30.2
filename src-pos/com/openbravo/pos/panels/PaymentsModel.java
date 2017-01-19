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

import java.util.*;
import javax.swing.table.AbstractTableModel;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.util.StringUtils;

/**
 *
 * @author adrianromero
 */
public class PaymentsModel {

    private String m_sHost;
    private int m_iSeq;
    private Date m_dDateStart;
    private Date m_dDateEnd;       
            
    private Integer m_iPayments;
    private Double m_dPaymentsTotal;
    //  Dixon Martinez
    private Integer m_ProductSalesRows;
    private Double m_ProductSalesTotalUnits;
    private Double m_ProductSalesTotal;
    private java.util.List<ProductSalesLine> m_ProductSales;
    private String m_User;
    private List<TicketsSalesLine> m_TicketsSalesLine;
    //  End Dixon Martinez
    private java.util.List<PaymentsLine> m_lpayments;
    
    private final static String[] PAYMENTHEADERS = {"Label.Payment", "label.totalcash"};
    
    private Integer m_iSales;
    private Double m_dSalesBase;
    private Double m_dSalesTaxes;
    private java.util.List<SalesLine> m_lsales;
    
    private final static String[] SALEHEADERS = {"label.taxcash", "label.totalcash"};

    private PaymentsModel() {
    }    
    
    public static PaymentsModel emptyInstance() {
        
        PaymentsModel p = new PaymentsModel();
        
        p.m_iPayments = 0;
        p.m_dPaymentsTotal = 0.0;
        p.m_lpayments = new ArrayList<>();
        //  Dixon Martinez
        p.m_ProductSalesRows = 0;
        p.m_ProductSalesTotalUnits = 0.0;
        p.m_ProductSalesTotal = 0.0;
        p.m_ProductSales = new ArrayList<>();
        //  End Dixon Martinez
        p.m_iSales = null;
        p.m_dSalesBase = null;
        p.m_dSalesTaxes = null;
        p.m_lsales = new ArrayList<>();
        
        return p;
    }
    
    public static PaymentsModel loadInstance(AppView app) throws BasicException {
        
        PaymentsModel p = new PaymentsModel();
        
        // Propiedades globales
        p.m_sHost = app.getProperties().getHost();
        p.m_iSeq = app.getActiveCashSequence();
        p.m_dDateStart = app.getActiveCashDateStart();
        p.m_dDateEnd = null;
        p.m_User = app.getAppUserView().getUser().getName();
        
        // Pagos
        Object[] valtickets = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT(*), SUM(PAYMENTS.TOTAL) " +
              "FROM PAYMENTS, RECEIPTS " +
              "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND RECEIPTS.MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
            
        if (valtickets == null) {
            p.m_iPayments = 0;
            p.m_dPaymentsTotal = 0.0;
        } else {
            p.m_iPayments = (Integer) valtickets[0];
            p.m_dPaymentsTotal = (Double) valtickets[1];
        }  
        
        List l = new StaticSentence(app.getSession()            
            , "SELECT PAYMENTS.PAYMENT, SUM(PAYMENTS.TOTAL) " +
              "FROM PAYMENTS, RECEIPTS " +
              "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND RECEIPTS.MONEY = ? " +
              "GROUP BY PAYMENTS.PAYMENT"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.PaymentsLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex()); 
        
        if (l == null) {
            p.m_lpayments = new ArrayList();
        } else {
            p.m_lpayments = l;
        }        
        
        // Sales
        Object[] recsales = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(DISTINCT RECEIPTS.ID), SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) " +
            "FROM RECEIPTS, TICKETLINES WHERE RECEIPTS.ID = TICKETLINES.TICKET AND RECEIPTS.MONEY = ?",
            SerializerWriteString.INSTANCE,
            new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
        if (recsales == null) {
            p.m_iSales = null;
            p.m_dSalesBase = null;
        } else {
            p.m_iSales = (Integer) recsales[0];
            p.m_dSalesBase = (Double) recsales[1];
        }             
        
        // Taxes
        Object[] rectaxes = (Object []) new StaticSentence(app.getSession(),
            "SELECT SUM(TAXLINES.AMOUNT) " +
            "FROM RECEIPTS, TAXLINES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND RECEIPTS.MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE}))
            .find(app.getActiveCashIndex());            
        if (rectaxes == null) {
            p.m_dSalesTaxes = null;
        } else {
            p.m_dSalesTaxes = (Double) rectaxes[0];
        } 
                
        List<SalesLine> asales = new StaticSentence(app.getSession(),
                "SELECT TAXCATEGORIES.NAME, SUM(TAXLINES.AMOUNT) " +
                "FROM RECEIPTS, TAXLINES, TAXES, TAXCATEGORIES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND TAXLINES.TAXID = TAXES.ID AND TAXES.CATEGORY = TAXCATEGORIES.ID " +
                "AND RECEIPTS.MONEY = ?" +
                "GROUP BY TAXCATEGORIES.NAME"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(PaymentsModel.SalesLine.class))
                .list(app.getActiveCashIndex());
        if (asales == null) {
            p.m_lsales = new ArrayList<>();
        } else {
            p.m_lsales = asales;
        }
        //  Dixon Martinez 
        // Product Sales
        Object[] valproductsales = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT(*), SUM(TICKETLINES.UNITS), SUM((TICKETLINES.PRICE + TICKETLINES.PRICE * TAXES.RATE ) * TICKETLINES.UNITS) - SUM(TICKETLINES.RATEDISCOUNT)" +
              "FROM TICKETLINES, TICKETS, RECEIPTS, TAXES " +
              "WHERE TICKETLINES.TICKET = TICKETS.ID AND TICKETS.ID = RECEIPTS.ID AND TICKETLINES.TAXID = TAXES.ID AND TICKETLINES.PRODUCT IS NOT NULL AND RECEIPTS.MONEY = ? " +
              "GROUP BY RECEIPTS.MONEY"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
 
        if (valproductsales == null) {
            p.m_ProductSalesRows = 0;
            p.m_ProductSalesTotalUnits = 0.0;
            p.m_ProductSalesTotal = 0.0;
        } else {
            p.m_ProductSalesRows = (Integer) valproductsales[0];
            p.m_ProductSalesTotalUnits = (Double) valproductsales[1];
            p.m_ProductSalesTotal= (Double) valproductsales[2];
        }
        
        List products = new StaticSentence(app.getSession()
            , "SELECT PRODUCTS.NAME, SUM(TICKETLINES.UNITS), TICKETLINES.PRICE, TAXES.RATE, SUM(TICKETLINES.RATEDISCOUNT) RATEDISCOUNT " +
              "FROM TICKETLINES, TICKETS, RECEIPTS, PRODUCTS, TAXES " +
              "WHERE TICKETLINES.PRODUCT = PRODUCTS.ID AND TICKETLINES.TICKET = TICKETS.ID AND TICKETS.ID = RECEIPTS.ID AND TICKETLINES.TAXID = TAXES.ID AND RECEIPTS.MONEY = ? " +
              "GROUP BY PRODUCTS.NAME, TICKETLINES.PRICE, TAXES.RATE"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.ProductSalesLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex());
 
        if (products == null) {
            p.m_ProductSales = new ArrayList();
        } else {
            p.m_ProductSales = products;
        }
        
        List tickets = new StaticSentence(app.getSession()
            , "SELECT TICKETS.TICKETID, PRODUCTS.NAME, SUM(TICKETLINES.RATEDISCOUNT) RATEDISCOUNT "
                    + "FROM TICKETLINES, TICKETS, RECEIPTS, PRODUCTS, TAXES "
                    + "WHERE "
                    + "    TICKETLINES.PRODUCT = PRODUCTS.ID "
                    + "    AND TICKETLINES.TICKET = TICKETS.ID "
                    + "    AND TICKETS.ID = RECEIPTS.ID "
                    + "    AND TICKETLINES.TAXID = TAXES.ID "
                    + "    AND RECEIPTS.MONEY = ? "
                    + "GROUP BY TICKETS.ID, TICKETS.TICKETID, PRODUCTS.NAME "
                    + "HAVING SUM(TICKETLINES.RATEDISCOUNT) > 0 "
                    + "ORDER BY  TICKETS.TICKETID"
                , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.TicketsSalesLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex());
 
        if (tickets == null) {
            p.m_TicketsSalesLine = new ArrayList();
        } else {
            p.m_TicketsSalesLine = tickets;
        }
        //  End Dixon Martinez
        return p;
    }
    
    //  Dixon Martinez
    public double getProductSalesRows() {
        return m_ProductSalesRows;
    }
    
    public String printUser() {
        return Formats.STRING.formatValue(m_User);
    }
 
    public String printProductSalesRows() {
        return Formats.INT.formatValue(m_ProductSalesRows);
    }
 
    public double getProductSalesTotalUnits() {
        return m_ProductSalesTotalUnits;
    }
 
    public String printProductSalesTotalUnits() {
        return Formats.DOUBLE.formatValue(m_ProductSalesTotalUnits);
    }
 
    public double getProductSalesTotal() {
        return m_ProductSalesTotal;
    }
 
    public String printProductSalesTotal() {
        return Formats.CURRENCY.formatValue(m_ProductSalesTotal);
    }
 
    public List<ProductSalesLine> getProductSalesLines() {
        return m_ProductSales;        
    }
    
    public List<TicketsSalesLine> getTicketsSalesLine() {
        return m_TicketsSalesLine;
    }
    //  Dixon Martinez

    public int getPayments() {
        return m_iPayments;
    }
    public double getTotal() {
        return m_dPaymentsTotal;
    }
    public String getHost() {
        return m_sHost;
    }
    public int getSequence() {
        return m_iSeq;
    }
    public Date getDateStart() {
        return m_dDateStart;
    }
    public void setDateEnd(Date dValue) {
        m_dDateEnd = dValue;
    }
    public Date getDateEnd() {
        return m_dDateEnd;
    }
    
    public String printHost() {
        return StringUtils.encodeXML(m_sHost);
    }
    public String printSequence() {
        return Formats.INT.formatValue(m_iSeq);
    }
    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(m_dDateStart);
    }
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(m_dDateEnd);
    }  
    
    public String printPayments() {
        return Formats.INT.formatValue(m_iPayments);
    }

    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }     
    
    public List<PaymentsLine> getPaymentLines() {
        return m_lpayments;
    }
    
    public int getSales() {
        return m_iSales == null ? 0 : m_iSales;
    }    
    public String printSales() {
        return Formats.INT.formatValue(m_iSales);
    }
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }     
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }     
    public String printSalesTotal() {            
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes);
    }     
    public List<SalesLine> getSaleLines() {
        return m_lsales;
    }
    
    public AbstractTableModel getPaymentsModel() {
        return new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return AppLocal.getIntString(PAYMENTHEADERS[column]);
            }
            @Override
            public int getRowCount() {
                return m_lpayments.size();
            }
            @Override
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }
            @Override
            public Object getValueAt(int row, int column) {
                PaymentsLine l = m_lpayments.get(row);
                switch (column) {
                case 0: return l.getType();
                case 1: return l.getValue();
                default: return null;
                }
            }  
        };
    }
    
    public static class SalesLine implements SerializableRead {
        
        private String m_SalesTaxName;
        private Double m_SalesTaxes;
        
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_SalesTaxName = dr.getString(1);
            m_SalesTaxes = dr.getDouble(2);
        }
        public String printTaxName() {
            return m_SalesTaxName;
        }      
        public String printTaxes() {
            return Formats.CURRENCY.formatValue(m_SalesTaxes);
        }
        public String getTaxName() {
            return m_SalesTaxName;
        }
        public Double getTaxes() {
            return m_SalesTaxes;
        }        
    }

    public AbstractTableModel getSalesModel() {
        return new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return AppLocal.getIntString(SALEHEADERS[column]);
            }
            @Override
            public int getRowCount() {
                return m_lsales.size();
            }
            @Override
            public int getColumnCount() {
                return SALEHEADERS.length;
            }
            @Override
            public Object getValueAt(int row, int column) {
                SalesLine l = m_lsales.get(row);
                switch (column) {
                case 0: return l.getTaxName();
                case 1: return l.getTaxes();
                default: return null;
                }
            }  
        };
    }
    
    public static class PaymentsLine implements SerializableRead {
        
        private String m_PaymentType;
        private Double m_PaymentValue;
        
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_PaymentType = dr.getString(1);
            m_PaymentValue = dr.getDouble(2);
        }
        
        public String printType() {
            return AppLocal.getIntString("transpayment." + m_PaymentType);
        }
        public String getType() {
            return m_PaymentType;
        }
        public String printValue() {
            return Formats.CURRENCY.formatValue(m_PaymentValue);
        }
        public Double getValue() {
            return m_PaymentValue;
        }        
    }
    //  Dixon Martinez
    public static class ProductSalesLine implements SerializableRead {

        private String m_ProductName;
        private Double m_ProductUnits;
        private Double m_ProductPrice;
        private Double m_TaxRate;
        private Double m_ProductPriceTax;
        private Double m_ProductRateDiscount;

        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_ProductName = dr.getString(1);
            m_ProductUnits = dr.getDouble(2);
            m_ProductPrice = dr.getDouble(3);
            m_TaxRate = dr.getDouble(4);
            m_ProductRateDiscount = dr.getDouble(5);
            m_ProductPriceTax = m_ProductPrice + m_ProductPrice*m_TaxRate;
        }

        public String printProductName() {
            return StringUtils.encodeXML(m_ProductName);
        }

        public String printProductUnits() {
            return Formats.DOUBLE.formatValue(m_ProductUnits);
        }

        public Double getProductUnits() {
            return m_ProductUnits;
        }

        public String printProductPrice() {
            return Formats.CURRENCY.formatValue(m_ProductPrice);
        }

        public Double getProductPrice() {
            return m_ProductPrice;
        }

        public String printTaxRate() {
            return Formats.PERCENT.formatValue(m_TaxRate);
        }

        public Double getTaxRate() {
            return m_TaxRate;
        }

        public String printProductPriceTax() {
            return Formats.CURRENCY.formatValue(m_ProductPriceTax);
        }

        public String printProductSubValue() {
            return Formats.CURRENCY.formatValue(m_ProductPriceTax*m_ProductUnits);
        }
        
        public String printProductSubValueWithDiscount() {
            return Formats.CURRENCY.formatValue((m_ProductPriceTax*m_ProductUnits) - (m_ProductRateDiscount ));
        }
        
        public String printProductRateDiscount() {
            return Formats.PERCENT.formatValue(m_ProductRateDiscount);
        }

        public Double getProductRateDiscount() {
            return m_ProductRateDiscount;
        }

    }
    
    public static class TicketsSalesLine implements SerializableRead {

        private String m_TicketID;
        private String m_ProductName;
        private Double m_ProductRateDiscount;

        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_TicketID = dr.getString(1);
            m_ProductName = dr.getString(2);
            m_ProductRateDiscount = dr.getDouble(3);
            
        }
        
        public String printTicketID() {
            return StringUtils.encodeXML(m_TicketID);
        }

        public String printProductName() {
            return StringUtils.encodeXML(m_ProductName);
        }
        
        public String printProductRateDiscount() {
            return Formats.PERCENT.formatValue(m_ProductRateDiscount);
        }

        public Double getProductRateDiscount() {
            return m_ProductRateDiscount;
        }

    }
    //  End Dixon Martinez
}    