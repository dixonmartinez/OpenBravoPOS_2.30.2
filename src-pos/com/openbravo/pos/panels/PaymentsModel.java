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
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.util.CurrencyChange;
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
    private Double m_dPaymentsDollarTotal;
    private Integer m_iDollarPayments;
    
    private Double m_dRegisteredCash;
    private Double m_dRegisteredDollar;
    private Double m_dDifferenceCash;
    private Double m_dDifferenceDollar;
    
    //  Dixon Martinez
    private Integer m_ProductSalesRows;
    private Double m_ProductSalesTotalUnits;
    private Double m_ProductSalesTotal;
    private java.util.List<ProductSalesLine> m_ProductSales;
    private java.util.List<ProductSalesByCategory> m_ProductSalesByCategory;
    private String m_User;
    private List<TicketsSalesLine> m_TicketsSalesLine;
    //  End Dixon Martinez
    private java.util.List<PaymentsLine> m_lpayments;
    
    private final static String[] PAYMENTHEADERS = {"Label.Payment", "label.totalcash"};
    
    private Integer m_iSales;
    private Double m_dSalesBase;
    private Integer m_iSalesDollar;
    private Double m_dSalesBaseDollar;
    private Double m_dSalesTaxes;
    private java.util.List<SalesLine> m_lsales;
    
    private final static String[] SALEHEADERS = {"label.taxcash", "label.totalcash"};

    private DataLogicSystem m_dlSystem;
    private Double m_dSalesDollarTaxes;
    static protected SentenceFind m_activecash;
    private boolean printDollarAmt;

    private PaymentsModel() {
    }    
    
    public static PaymentsModel emptyInstance() {
        
        PaymentsModel p = new PaymentsModel();
        
        p.m_iPayments = 0;
        p.m_dPaymentsTotal = 0.0;
        p.m_lpayments = new ArrayList<>();
        //  Dixon Martinez
        p.m_dPaymentsDollarTotal = 0.0;
        p.m_iDollarPayments = 0;
        
        p.m_ProductSalesRows = 0;
        p.m_ProductSalesTotalUnits = 0.0;
        p.m_ProductSalesTotal = 0.0;
        p.m_ProductSales = new ArrayList<>();
        //  End Dixon Martinez
        p.m_iSales = null;
        p.m_iSalesDollar = null;
        p.m_dSalesBase = null;
        p.m_dSalesBaseDollar = null;
        p.m_dSalesTaxes = null;
        p.m_dSalesDollarTaxes = null;
        p.m_lsales = new ArrayList<>();
        
        return p;
    }
    
    public static PaymentsModel loadInstance(final AppView app, String user) throws BasicException {
        
        PaymentsModel p = new PaymentsModel();
        
        Object[]  activeCash = (Object []) new StaticSentence(app.getSession()
            , "SELECT A.MONEY, A.HOSTSEQUENCE, A.DATESTART, A.DATEEND, A.PERSON, A.DIFFERENCECASH FROM CLOSEDCASH A INNER JOIN " +
                "(SELECT PERSON, MAX(HOSTSEQUENCE) HOSTSEQUENCE FROM CLOSEDCASH WHERE PERSON = ? AND DATEEND IS NULL GROUP BY PERSON) B " +
                "ON A.HOSTSEQUENCE = B.HOSTSEQUENCE AND A.PERSON = B.PERSON "
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP, Datas.STRING, Datas.DOUBLE}))
            .find(user);
        String activeCashIndex = activeCash == null ? "0" : activeCash[0].toString();

        // global Properties
        p.m_sHost = app.getProperties().getHost();
        p.m_iSeq = Integer.parseInt(activeCash == null ? "0" : activeCash[1].toString());
        p.m_dDateStart = app.getActiveCashDateStart();
        p.m_dDateEnd = null;
        p.m_dDifferenceCash = (Double) activeCash[5];
        
        // Pagos
        Object[] valtickets = (Object []) new StaticSentence(app.getSession(),
              "SELECT COUNT(*), COALESCE(SUM(PAYMENTS.TOTAL), 0) " +
              "FROM PAYMENTS, RECEIPTS, TICKETS " +
              "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND TICKETS.ID = RECEIPTS.ID AND PAYMENT NOT IN ('cash_dollar') " +
              "AND RECEIPTS.MONEY = ? AND TICKETS.PERSON = ? "
            , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(activeCashIndex, user);
            
        if (valtickets == null) {
            p.m_iPayments = 0;
            p.m_dPaymentsTotal = 0.0;
        } else {
            p.m_iPayments = (Integer) valtickets[0];
            p.m_dPaymentsTotal = (Double) valtickets[1];
        }  
        
        valtickets = (Object []) new StaticSentence(app.getSession(),
                "SELECT COUNT(*), CAST(COALESCE(SUM(PAYMENTS.TOTAL),0) + 0.005 AS DECIMAL(15,2)) " +
                "FROM PAYMENTS, RECEIPTS, TICKETS " +
                "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND TICKETS.ID = RECEIPTS.ID AND PAYMENT = 'cash_dollar' " +
                "AND RECEIPTS.MONEY = ? AND TICKETS.PERSON = ? "
              , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
              , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
              .find(activeCashIndex, user);
              
          if (valtickets == null) {
              p.m_iDollarPayments = 0;
              p.m_dPaymentsDollarTotal = 0.0;
          } else {
              p.m_iDollarPayments = (Integer) valtickets[0];
              p.m_dPaymentsDollarTotal = (Double) valtickets[1];
          }  
          
        
        List l = new StaticSentence(app.getSession()            
            , "SELECT PAYMENTS.PAYMENT, SUM(PAYMENTS.TOTAL) " +
              "FROM PAYMENTS, RECEIPTS, TICKETS " +
              "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND TICKETS.ID = RECEIPTS.ID " +
              "AND RECEIPTS.MONEY = ? AND TICKETS.PERSON = ? " +
              "GROUP BY PAYMENTS.PAYMENT"
            , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
            , new SerializerReadClass(PaymentsModel.PaymentsLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(activeCashIndex, user); 
        
        if (l == null) {
            p.m_lpayments = new ArrayList();
        } else {
            p.m_lpayments = l;
        }  

        // Sales
        Object[] recsales = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(r.ID), SUM(p.TOTAL), t.PERSON " +
            "FROM PAYMENTS p " + 
            "INNER JOIN RECEIPTS r ON (p.RECEIPT = r.ID) " +
            "INNER JOIN TICKETS t ON (r.ID = t.ID) " +
            " WHERE r.MONEY = ? AND t.PERSON = ? AND p.PAYMENT NOT IN ('cash_dollar')" +
            "GROUP BY t.PERSON " ,
            new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}),
            new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE, Datas.STRING}))
            .find(activeCashIndex, user);
        if (recsales == null) {
            p.m_iSales = null;
            p.m_dSalesBase = null;
        } else {
            p.m_iSales = (Integer) recsales[0];
            p.m_dSalesBase = (Double) recsales[1];
        }
        
        recsales = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(r.ID), SUM(p.TOTAL), t.PERSON  " +
            "FROM PAYMENTS p " + 
            "INNER JOIN RECEIPTS r ON (p.RECEIPT = r.ID) " +
            "INNER JOIN TICKETS t ON (r.ID = t.ID) " +
            " WHERE r.MONEY = ? AND t.PERSON = ? AND p.PAYMENT IN ('cash_dollar')" +
            "GROUP BY t.PERSON  " ,
            new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}),
            new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE, Datas.STRING}))
            .find(activeCashIndex, user);
        if (recsales == null) {
            p.m_iSalesDollar = null;
            p.m_dSalesBaseDollar = null;
        } else {
            p.m_iSalesDollar = (Integer) recsales[0];
            p.m_dSalesBaseDollar = (Double) recsales[1];
        }
    
        // Taxes
        Object[] rectaxes = (Object []) new StaticSentence(app.getSession(),
            "SELECT SUM(tl.AMOUNT) " +
            "FROM PAYMENTS p " +
            "INNER JOIN RECEIPTS r ON (p.RECEIPT = r.ID) " +
            "INNER JOIN TAXLINES tl ON (r.ID = tl.RECEIPT) " +
            "INNER JOIN TICKETS t ON (r.ID = t.ID) " +
            "WHERE r.MONEY = ? AND t.PERSON = ? AND p.PAYMENT NOT IN ('cash_dollar')" +
            "GROUP BY t.PERSON "                
            , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE}))
            .find(activeCashIndex, user);            
        if (rectaxes == null) {
            p.m_dSalesTaxes = null;
        } else {
            p.m_dSalesTaxes = (Double) rectaxes[0];
        } 
        /*
        rectaxes = (Object []) new StaticSentence(app.getSession(),
            "SELECT SUM(tl.AMOUNT) " +
            "FROM PAYMENTS p " +
            "INNER JOIN RECEIPTS r ON (p.RECEIPT = r.ID) " +
            "INNER JOIN TAXLINES tl ON (r.ID = tl.RECEIPT) " +
            "INNER JOIN TICKETS t ON (r.ID = t.ID) " +
            "WHERE r.MONEY = ? AND t.PERSON = ? AND p.PAYMENT IN ('cash_dollar')" +
            "GROUP BY t.PERSON,p.PAYMENT "
            , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE}))
            .find(activeCashIndex, user);            
        if (rectaxes = null) {
            p.m_dSalesDollarTaxes = 0.0;
        } else {
            p.m_dSalesDollarTaxes = (Double) rectaxes[0];
        } */
                
        p.m_dSalesDollarTaxes = 0.0;
        List<SalesLine> asales = new StaticSentence(app.getSession(),
                "SELECT TAXCATEGORIES.NAME, SUM(TAXLINES.AMOUNT) " +
                "FROM RECEIPTS, TAXLINES, TAXES, TAXCATEGORIES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND TAXLINES.TAXID = TAXES.ID AND TAXES.CATEGORY = TAXCATEGORIES.ID " +
                "AND RECEIPTS.MONEY = ?" +
                "GROUP BY TAXCATEGORIES.NAME"
                , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING})
                , new SerializerReadClass(PaymentsModel.SalesLine.class))
                .list(activeCashIndex, user);
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
        
        
        //  Products by Category
        List productsByCategory = new StaticSentence(app.getSession()
            , "SELECT \n" +
                "    t.ID,\n" +
                "    tl.LINE,\n" +
                "    p.PRODUCTNAME,\n" +
                "    p.CATEGORYNAME,\n" +
                "    SUM(tl.UNITS) UNITS,\n" +
                "    tl.PRICE\n" +
                "FROM TICKETS t \n" +
                "INNER JOIN TICKETLINES tl ON (t.ID = tl.TICKET)\n" +
                "INNER JOIN RECEIPTS r ON (t.ID = r.ID )\n" +
                "LEFT JOIN (\n" +
                "    SELECT \n" +
                "        p.\"NAME\" PRODUCTNAME,\n" +
                "        c.\"NAME\" CATEGORYNAME,\n" +
                "        p.ID PRODUCTID,\n" +
                "        c.ID CATEGORI_ID\n" +
                "    FROM PRODUCTS p\n" +
                "    INNER JOIN CATEGORIES c ON (p.CATEGORY = c.ID)\n" +
                ") p ON (tl.PRODUCT = p.PRODUCTID)\n" +
                "WHERE r.MONEY = ? \n" +
                "GROUP BY\n" +
                "    p.CATEGORI_ID,\n" +
                "    t.ID,\n" +
                "    tl.LINE,\n" +
                "    p.PRODUCTNAME,\n" +
                "    p.CATEGORYNAME,\n" +
                "    tl.PRICE"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.ProductSalesByCategory.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex());
 
        if (products == null) {
            p.m_ProductSalesByCategory = new ArrayList();
        } else {
            p.m_ProductSalesByCategory = productsByCategory;
        }
        
        
        
        //  End Dixon Martinez
        return p;
    }
    
    //  Dixon Martinez
    public double getProductSalesRows() {
        return m_ProductSalesRows;
    }
    
    public void setUserName(TaxCategoryInfo m_People) {
        this.m_User = m_People.getName();
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
    
    public List<ProductSalesByCategory> getProductSalesByCategory() {
        return m_ProductSalesByCategory;        
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
    
    public int getDollarPayments() {
        return m_iPayments;
    }
    public double getPaymentsTotal() {
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

    public String printDollarPayments() {
        return Formats.INT.formatValue(m_iDollarPayments);
    }

    
    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }  
    
    public String printPaymentsDollarTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsDollarTotal);
    }  
    
    //Traer el total sin formato 
    public Double getM_dPaymentsTotal() {
        return m_dPaymentsTotal;
    }

    public void setM_dPaymentsTotal(Double m_dPaymentsTotal) {
        this.m_dPaymentsTotal = m_dPaymentsTotal;
    }
    
    public Double getPaymentsDollarTotal() {
        return m_dPaymentsDollarTotal;
    }
    
    public void setPaymentsDollarTotal( double m_dPaymentsDollarTotal) {
        this.m_dPaymentsDollarTotal = m_dPaymentsDollarTotal;
    }
    
    
    public List<PaymentsLine> getPaymentLines() {
        return m_lpayments;
    }
    
    public int getSales() {
        return m_iSales == null ? 0 : m_iSales;
    }    
    
    public int getSalesDollar() {
        return m_iSalesDollar == null ? 0 : m_iSalesDollar;
    }    
    
    public String printSales() {
        return Formats.INT.formatValue(m_iSales);
    }
    
    public String printSalesDollar() {
        return Formats.INT.formatValue(m_iSalesDollar);
    }
    
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }     
    
    public String printSalesBaseDollar() {
        if (m_dSalesBaseDollar == null) {
            return CurrencyChange.formatDollarValue(null);
        } else {
            return CurrencyChange.formatDollarValue(m_dSalesBaseDollar);
        }
    }     
    
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }     
    
    public String printSalesDollarTaxes() {
        return CurrencyChange.formatDollarValue(m_dSalesDollarTaxes);
    }     
    
    public String printSalesTotal() {            
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes);
    }
    
    public String printSalesDollarTotal() {
        return CurrencyChange.formatDollarValue((m_dSalesBaseDollar == null || m_dSalesDollarTaxes == null)
                ? null
                : m_dSalesBaseDollar + m_dSalesDollarTaxes);        
    }
        
    public String printDifferenceDollar() {
        return CurrencyChange.formatDollarValue((m_dDifferenceDollar == null)
                ? null
                : m_dDifferenceDollar);        
    }
    
    public double getDifferenceDollar() {
        return m_dDifferenceDollar;
    }
    
    public void setDifferenceDollar(Double m_dDifferenceDollar) {
        this.m_dDifferenceDollar = m_dDifferenceDollar;
    }
    
    
    public String printDifferenceCash() {
        return CurrencyChange.formatLocaleValue(m_dDifferenceCash == null 
                ? null
                : m_dDifferenceCash);        
    }
    
    public double getDifferenceCash() {
        return m_dDifferenceCash;
    }
    
    public void setDifferenceCash(Double m_dDifferenceCash) {
        this.m_dDifferenceCash = m_dDifferenceCash;
    }
    
    
    public Double getRegisteredCash() {
        return m_dRegisteredCash;
    }
    
    public Double getRegisteredDollar() {
        return m_dRegisteredDollar;
    }
    
    public void setRegisteredCash(Double m_dRegisteredCash) {
        this.m_dRegisteredCash = m_dRegisteredCash;
    }
    
    public void setRegisteredDollar(Double m_dRegisteredDollar) {
        this.m_dRegisteredDollar = m_dRegisteredDollar;
    }
    
    public String printRegisteredCash() {
        return CurrencyChange.formatLocaleValue(m_dRegisteredCash == null 
                ? null
                : m_dRegisteredCash);
    }
    
    public String printRegisteredDollar() {
        return CurrencyChange.formatDollarValue(m_dRegisteredDollar == null 
                ? null
                : m_dRegisteredDollar);        
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

    public void setPrintDollarAmt(boolean isPrint) {
        this.printDollarAmt = isPrint;
    }
    
    public boolean getPrintDollarAmt() {
        return this.printDollarAmt ;
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

    public static class ProductSalesByCategory implements SerializableRead {

        private String m_ProductName;
        private Double m_ProductUnits;
        private Double m_ProductPrice;
        private String m_CategoryName;
                
        
        @Override
        public void readValues(DataRead dr) throws BasicException {
            m_ProductName = dr.getString(3);
            m_CategoryName = dr.getString(4);
            m_ProductUnits = dr.getDouble(5);
            m_ProductPrice = dr.getDouble(6);


        }

        public String printProductName() {
            return StringUtils.encodeXML(m_ProductName);
        }
        
        public String printCategoryName() {
            return StringUtils.encodeXML(m_CategoryName);
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