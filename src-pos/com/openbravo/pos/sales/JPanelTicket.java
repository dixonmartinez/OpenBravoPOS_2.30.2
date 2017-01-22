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

package com.openbravo.pos.sales;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JNumberDialog;
import com.openbravo.beans.JPercentDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.promotion.DiscountPercent;
import com.openbravo.pos.sales.currency.ConversionRateInfo;
import com.openbravo.pos.sales.currency.DataLogicConversionRate;
import com.openbravo.pos.scale.ScaleException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.ProductInfoEdit;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.CurrencyChange;
import com.openbravo.pos.util.JRPrinterAWT300;
import com.openbravo.pos.util.ReportUtils;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 *
 * @author adrianromero
 */
public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor {
   
    // Variable numerica
    private final static int NUMBERZERO = 0;
    private final static int NUMBERVALID = 1;
    
    private final static int NUMBER_INPUTZERO = 0;
    private final static int NUMBER_INPUTZERODEC = 1;
    private final static int NUMBER_INPUTINT = 2;
    private final static int NUMBER_INPUTDEC = 3; 
    private final static int NUMBER_PORZERO = 4; 
    private final static int NUMBER_PORZERODEC = 5; 
    private final static int NUMBER_PORINT = 6; 
    private final static int NUMBER_PORDEC = 7; 

    protected JTicketLines m_ticketlines;
        
    // private Template m_tempLine;
    private TicketParser m_TTP;
    
    protected TicketInfo m_oTicket; 
    protected Object m_oTicketExt; 
    
    // Estas tres variables forman el estado...
    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusPor;
    private StringBuffer m_sBarcode;
            
    private JTicketsBag m_ticketsbag;
    
    private SentenceList senttax;
    private ListKeyed taxcollection;
    // private ComboBoxValModel m_TaxModel;
    
    private SentenceList senttaxcategories;
    private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    
    private TaxesLogic taxeslogic;
    
//    private ScriptObject scriptobjinst;
    protected JPanelButtons m_jbtnconfig;
    
    protected AppView m_App;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;
    
    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;
    private CurrencyChange curChange = null;
    
    /** Creates new form JTicketView */
    public JPanelTicket() {
        
        initComponents ();
    }
   
    @Override
    public void init(AppView app) throws BeanFactoryException {
        
        m_App = app;
        dlSystem = (DataLogicSystem) m_App.getBean(DataLogicSystem.class.getName());
        dlSales = (DataLogicSales) m_App.getBean(DataLogicSales.class.getName());
        dlCustomers = (DataLogicCustomers) m_App.getBean(DataLogicCustomers.class.getName());
        dlConvRate = (DataLogicConversionRate) app.getBean(DataLogicConversionRate.class.getName());
                    
        // borramos el boton de bascula si no hay bascula conectada
        if (!m_App.getDeviceScale().existsScale()) {
            m_jbtnScale.setVisible(false);
        }
        
        m_ticketsbag = getJTicketsBag();
        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");

        m_ticketlines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"));
        m_ticketlines.addListSelectionListener(new CatalogSelectionListener());
        m_jPanelCentral.add(m_ticketlines, java.awt.BorderLayout.CENTER);
        
        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
               
        //  Dixon Martinez
        //  Set Text Buttons of Discount
        // Los botones configurables...
        String configButtonsRes = dlSystem.getResourceAsXML("Ticket.Buttons");
        m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
        m_jButtonsExt.add(m_jbtnconfig);
        propConfig = new PropertiesConfig(configButtonsRes);
        
        m_jbtnDiscountRate.setText(AppLocal.getIntString("button.ticketDiscount"));
        typeDiscRate = false;
        // El panel de los productos o de las lineas...        
        catcontainer.add(getSouthComponent(), BorderLayout.CENTER);
        if(propConfig.getPropertyAsBoolean("cat-visible")) {
            m_jImage.setVisible(true);
        }else {
            m_jImage.setVisible(false);
        }
        
        //  This panel of Discount
        m_jDiscountRatePanel.setVisible(propConfig.getPropertyAsBoolean("discount-rate-visible"));
        m_DiscRate1 = Double.parseDouble(propConfig.getProperty("discount-rate-1", "5")) / 100;
        m_DiscRate2 = Double.parseDouble(propConfig.getProperty("discount-rate-2", "10")) / 100;
        m_DiscRate3 = Double.parseDouble(propConfig.getProperty("discount-rate-3", "15")) / 100;
        
        //  This multi control
        isMultiplyControl = propConfig.getPropertyAsBoolean("refmultcontrol");
        
        // El modelo de impuestos
        senttax = dlSales.getTaxList();
        senttaxcategories = dlSales.getTaxCategoriesList();
        
        taxcategoriesmodel = new ComboBoxValModel();    
              
        // ponemos a cero el estado
        stateToZero();  
        
        // inicializamos
        m_oTicket = null;
        m_oTicketExt = null;      
    }
    
    @Override
    public Object getBean() {
        return this;
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
    
    private DataLogicConversionRate dlConvRate;
    private ComboBoxValModel currenBoxValMod;
    
    @Override
    public void activate() throws BasicException {

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this); 
        paymentdialogrefund.init(m_App);
        
        // impuestos incluidos seleccionado ?
        m_jaddtax.setSelected("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

        // Inicializamos el combo de los impuestos.
        java.util.List<TaxInfo> taxlist = senttax.list();
        taxcollection = new ListKeyed<>(taxlist);
        java.util.List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();
        taxcategoriescollection = new ListKeyed<>(taxcategorieslist);
        
        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        m_jTax.setModel(taxcategoriesmodel);

        String taxesid = m_jbtnconfig.getProperty("taxcategoryid");
        if (taxesid == null) {
            if (m_jTax.getItemCount() > 0) {
                m_jTax.setSelectedIndex(0);
            }
        } else {
            taxcategoriesmodel.setSelectedKey(taxesid);
        }              
                
        taxeslogic = new TaxesLogic(taxlist);
        
        // Show taxes options
        if (m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions")) {
            m_jTax.setVisible(true);
            m_jaddtax.setVisible(true);
        } else {
            m_jTax.setVisible(false);
            m_jaddtax.setVisible(false);
        }
        
        // Authorization for buttons
        btnSplit.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jDelete.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setMinusEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setEqualsEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());  
               
        //  Authorization for buttons discount
        m_jDiscount1.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Discount"));
        m_jDiscount2.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Discount"));
        m_jDiscount3.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Discount"));
        m_jDisableDiscountRate.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Discount"));
        m_jbtnDiscountRate.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Discount"));
        m_jKeypadDiscountRate.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Discount"));
        
        // Set text for buttons discount
        m_jDiscount1.setText(Formats.PERCENT.formatValue(m_DiscRate1));
        m_jDiscount2.setText(Formats.PERCENT.formatValue(m_DiscRate2));
        m_jDiscount3.setText(Formats.PERCENT.formatValue(m_DiscRate3));
        m_jDisableDiscountRate.setText(Formats.PERCENT.formatValue(0.0));
        m_ticketsbag.activate();        
        
        List a = dlConvRate.getConversionRate();
        currenBoxValMod = new ComboBoxValModel(a);
        jCmbCurrency.setModel(currenBoxValMod);        
    }
    
    @Override
    public boolean deactivate() {

        return m_ticketsbag.deactivate();
    }
    
    protected abstract JTicketsBag getJTicketsBag();
    protected abstract Component getSouthComponent();
    protected abstract void resetSouthComponent();
     
    @Override
    public void setActiveTicket(TicketInfo oTicket, Object oTicketExt) {
       
        m_oTicket = oTicket;
        m_oTicketExt = oTicketExt;
        
        if (m_oTicket != null) {            
            // Asign preeliminary properties to the receipt
            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            m_oTicket.setActiveCash(m_App.getActiveCashIndex());
            m_oTicket.setDate(new Date()); // Set the edition date.
        }
        
        executeEvent(m_oTicket, m_oTicketExt, "ticket.show");
        
        refreshTicket();               
    }
    
    @Override
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }
    
    private void refreshTicket() {
        
        CardLayout cl = (CardLayout)(getLayout());
        
        if (m_oTicket == null) {        
            m_jTicketId.setText(null);            
            m_ticketlines.clearTicketLines();
           
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null); 
        
            stateToZero();
            
            // Muestro el panel de nulos.
            cl.show(this, "null");  
            resetSouthComponent();

        } else {
            if(isMultiplyControl) {
                if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                    //Make disable Search and Edit Buttons and other
                    m_jEditLine.setVisible(false);
                    m_jList.setVisible(false);
                    m_jDelete.setVisible(false);
                    jEditAttributes.setVisible(false);
                    m_jDiscountRatePanel.setVisible(false);
                } else {
                    m_jDelete.setVisible(true);
                    jEditAttributes.setVisible(true);
                    m_jEditLine.setVisible(true);
                    m_jList.setVisible(true);
                }
            }
            
            // Refresh ticket taxes
            m_oTicket.getLines().stream().forEach((line) -> {
                line.setTaxInfo(taxeslogic.getTaxInfo(line.getProductTaxCategoryID(), m_oTicket.getDate(), m_oTicket.getCustomer()));
            });  
        
            // The ticket name
            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));

            // Limpiamos todas las filas y anadimos las del ticket actual
            m_ticketlines.clearTicketLines();

            for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                m_ticketlines.addTicketLine(m_oTicket.getLine(i));
            }
            printPartialTotals();
            stateToZero();
            
            // Muestro el panel de tickets.
            cl.show(this, "ticket");
            resetSouthComponent();
            
            // activo el tecleador...
            m_jKeyFactory.setText(null);       
            java.awt.EventQueue.invokeLater(() -> {
                m_jKeyFactory.requestFocus();
            });
        }
    }
       
    private void printPartialTotals(){
        
        if (m_oTicket.getLinesCount() == 0) {
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);
        } else {
            /*if(curChange != null) {
        	//appli = true;
                m_jSubtotalEuros.setText(Formats.CURRENCY.formatValue(curChange.changeBaseToOther(m_oTicket.getSubTotal())));
                m_jTaxesEuros.setText(Formats.CURRENCY.formatValue(curChange.changeBaseToOther(m_oTicket.getTax())));
                m_jTotalEuros.setText(Formats.CURRENCY.formatValue(curChange.changeBaseToOther(m_oTicket.getTotal())));
            } else {*/
                m_jSubtotalEuros.setText(m_oTicket.printSubTotal());
                m_jTaxesEuros.setText(m_oTicket.printTax());
                m_jTotalEuros.setText(m_oTicket.printTotal());
            //}
            
        }
    }
    
    private void paintTicketLine(int index, TicketLineInfo oLine){
        
        if (executeEventAndRefresh("ticket.setline", new ScriptArg("index", index), new ScriptArg("line", oLine)) == null) {

            m_oTicket.setLine(index, oLine);
            m_ticketlines.setTicketLine(index, oLine);
            m_ticketlines.setSelectedIndex(index);

            visorTicketLine(oLine); // Y al visor tambien...
            printPartialTotals();   
            stateToZero();  

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
   }

    private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {   
        
        TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),  m_oTicket.getDate(), m_oTicket.getCustomer());
                
        addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone())));
    }
    
    protected void addTicketLine(TicketLineInfo oLine) {   
        
        if (executeEventAndRefresh("ticket.addline", new ScriptArg("line", oLine)) == null) {
        
            if (oLine.isProductCom()) {
                // Comentario entonces donde se pueda
                int i = m_ticketlines.getSelectedIndex();

                // me salto el primer producto normal...
                if (i >= 0 && !m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }

                // me salto todos los productos auxiliares...
                while (i >= 0 && i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }

                if (i >= 0) {
                    oLine = getUpdateLine(m_oTicket, oLine, i);
                } else {
                    Toolkit.getDefaultToolkit().beep();                                   
                }
            } else {
            	getUpdateLine(m_oTicket, oLine);
            }

            visorTicketLine(oLine);
            printPartialTotals();   
            stateToZero();  

            //  Dixon Martinez
            //  Display attributes windows
            if(propConfig.getPropertyAsBoolean("attributes-autoset")) {
                int i = m_ticketlines.getSelectedIndex();
                try {
                    TicketLineInfo lineInfo = m_oTicket.getLine(i);
                    JProductAttEdit productAttEdit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                    productAttEdit.editAttributes(lineInfo.getProductAttSetId(), lineInfo.getProductAttSetInstId());
                    productAttEdit.setVisible(true);
                    if(productAttEdit.isOK()) {
                        lineInfo.setProductAttSetInstId(productAttEdit.getAttributeSetInst());
                        lineInfo.setProductAttSetInstDesc(productAttEdit.getAttributeSetInstDescription());
                        paintTicketLine(i, lineInfo);
                    } else {
                        removeTicketLine(i);
                    }
                } catch (BasicException e) {
                }
            }
            
            // event receipt
            executeEventAndRefresh("ticket.change");
        }
    }    
    
    private TicketLineInfo getUpdateLine(TicketInfo m_oTicket, TicketLineInfo oLine) {
        return getUpdateLine(m_oTicket, oLine, -1);
    }
    
    private void refreshTicket(TicketInfo m_TicketInfo, ConversionRateInfo cri) {
        
        int i = 0;
        while ( i < m_TicketInfo.getLinesCount()) {
            Double qty = m_TicketInfo.getLine(i).getMultiply();
            m_TicketInfo.getLine(i).setMultiply(qty);
            ProductInfoExt prod;
			try {
				prod = dlSales.getProductInfo(m_TicketInfo.getLine(i).getProductID());
				System.out.println(cri.getCurrency());
				if(cri.getCurrency().equals("MXN")) {
					m_TicketInfo.getLine(i).setPrice(cri.changeBaseToOther(prod.getPriceSell()));
				}else {
					m_TicketInfo.getLine(i).setPrice(cri.changeOtherToBase(cri.changeBaseToOther(prod.getPriceSell())));
				}
				
//				System.out.println(changeEurosToPts(30));
//				System.out.println(changePtsToEuros(changeEurosToPts(30)));
				
	            paintTicketLine(i, m_TicketInfo.getLine(i));
			} catch (BasicException e) {
			}
            i++;
        }
    }
        
    private TicketLineInfo getUpdateLine(TicketInfo m_oTicket, TicketLineInfo oLine, int index) {
        int i=0;               
        boolean update = false;
        while (i < m_oTicket.getLinesCount()) {
            if(m_oTicket.getLine(i).getProductID().equals(oLine.getProductID())) {
                update = true;
                Double cant = m_oTicket.getLine(i).getMultiply()+ oLine.getMultiply();
                m_oTicket.getLine(i).setMultiply(cant);
                setPrice(m_oTicket.getLine(i));
                paintTicketLine(i,m_oTicket.getLine(i));
                i = m_oTicket.getLinesCount();
            }
            i++;
        }
        if(!update){
            // Producto normal, entonces al finalnewline.getMultiply()
            m_oTicket.addLine(oLine);            
            m_ticketlines.addTicketLine(oLine); // Pintamos la linea en la vista...
            setPrice(oLine);
        }
        return oLine;
    }
    
    private TicketLineInfo setPrice(TicketLineInfo oLine) {
    //  Evalue Price distinct of zero
        if(propConfig.getPropertyAsBoolean("price-is-zero")) {
            if(oLine.getPrice() == 0.0) {
                int index = m_ticketlines.getSelectedIndex();
                Double setValuePrice = JNumberDialog.showEditNumber(this, AppLocal.getIntString("message.setPrice"));
                if(setValuePrice == null) {
                    removeTicketLine(index);
                } else {
                    oLine.setPrice(setValuePrice);
                    //oLine = getUpdateLine(m_oTicket, oLine, index);
                    paintTicketLine(index, oLine);
                    //visorTicketLine(oLine);
                }
            }
        }
        return oLine;
    }
    
    protected void removeTicketLine(int i){
        
        if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {
        
            if (m_oTicket.getLine(i).isProductCom()) {
                // Es un producto auxiliar, lo borro y santas pascuas.
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i);   
            } else {
                // Es un producto normal, lo borro.
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i); 
                // Y todos lo auxiliaries que hubiera debajo.
                while(i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    m_oTicket.removeLine(i);
                    m_ticketlines.removeTicketLine(i); 
                }
            }

            visorTicketLine(null); // borro el visor 
            printPartialTotals(); // pinto los totales parciales...                           
            stateToZero(); // Pongo a cero    

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
    }
    
    private ProductInfoExt getInputProduct() {
        ProductInfoExt oProduct = new ProductInfoExt(); // Es un ticket
        oProduct.setReference(null);
        oProduct.setCode(null);
        oProduct.setName("");
        oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
        
        oProduct.setPriceSell(includeTaxes(oProduct.getTaxCategoryID(), getInputValue()));
        
        return oProduct;
    }
    
    private double includeTaxes(String tcid, double dValue) {
        if (m_jaddtax.isSelected()) {
            TaxInfo tax = taxeslogic.getTaxInfo(tcid,  m_oTicket.getDate(), m_oTicket.getCustomer());
            double dTaxRate = tax == null ? 0.0 : tax.getRate();           
            return dValue / (1.0 + dTaxRate);      
        } else {
            return dValue;
        }
    }
    
    private double getInputValue() {
        try {
            return Double.parseDouble(m_jPrice.getText());
        } catch (NumberFormatException e){
            return 0.0;
        }
    }

    private double getPorValue() {
        try {
            return Double.parseDouble(m_jPor.getText().substring(1));                
        } catch (NumberFormatException | StringIndexOutOfBoundsException e){
            return 1.0;
        }
    }
    
    private void stateToZero(){
        m_jPor.setText("");
        m_jPrice.setText("");
        m_sBarcode = new StringBuffer();

        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusPor = NUMBERZERO;
    }
    
    private void incProductByCode(String sCode) {
    // precondicion: sCode != null
        
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {                  
                Toolkit.getDefaultToolkit().beep();                   
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);           
                stateToZero();
            } else {
                // Se anade directamente una unidad con el precio y todo
                incProduct(oProduct);
            }
        } catch (BasicException eData) {
            stateToZero();           
            new MessageInf(eData).show(this);           
        }
    }
    
    private void incProductByCodePrice(String sCode, double dPriceSell) {
    // precondicion: sCode != null
        
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {                  
                Toolkit.getDefaultToolkit().beep();                   
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);           
                stateToZero();
            } else {
                // Se anade directamente una unidad con el precio y todo
                if (m_jaddtax.isSelected()) {
                    // debemos quitarle los impuestos ya que el precio es con iva incluido...
                    TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),  m_oTicket.getDate(), m_oTicket.getCustomer());
                    addTicketLine(oProduct, 1.0, dPriceSell / (1.0 + tax.getRate()));
                } else {
                    addTicketLine(oProduct, 1.0, dPriceSell);
                }                
            }
        } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);               
        }
    }
    
    private void incProduct(ProductInfoExt prod) {
        
        if (prod.isScale() && m_App.getDeviceScale().existsScale()) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    incProduct(value, prod);
                }
            } catch (ScaleException e) {
                Toolkit.getDefaultToolkit().beep();                
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                stateToZero(); 
            }
        } else {
            // No es un producto que se pese o no hay balanza
            incProduct(1.0, prod);
        }
    }
    
    private void incProduct(double dPor, ProductInfoExt prod) {
        // precondicion: prod != null
        addTicketLine(prod, dPor, prod.getPriceSell());       
    }
    
    protected void buttonTransition(ProductInfoExt prod) {
    // precondicion: prod != null
         if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
            incProduct(prod);
        } else if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
            incProduct(getInputValue(), prod);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }       
    }
    
    private void stateTransition(char cTrans) {

        try {
            if ((m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) && isMultiplyControl && !(cTrans == '=')) {
                Toolkit.getDefaultToolkit().beep();
                throw new BasicException(AppLocal.getIntString("message.refcontrolenabled"));
            }
            
            if (cTrans == '\n') {
                // Codigo de barras introducido
                if (m_sBarcode.length() > 0) {            
                    String sCode = m_sBarcode.toString();
                    if (sCode.startsWith("c")) {
                        // barcode of a customers card
                        try {
                            CustomerInfoExt newcustomer = dlSales.findCustomerExt(sCode);
                            if (newcustomer == null) {
                                Toolkit.getDefaultToolkit().beep();                   
                                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.nocustomer")).show(this);           
                            } else {
                                m_oTicket.setCustomer(newcustomer);
                                m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
                            }
                        } catch (BasicException e) {
                            Toolkit.getDefaultToolkit().beep();                   
                            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.nocustomer"), e).show(this);           
                        }
                        stateToZero();
                    } else if (sCode.length() == 13 && sCode.startsWith("250")) {
                        // barcode of the other machine
                        ProductInfoExt oProduct = new ProductInfoExt(); // Es un ticket
                        oProduct.setReference(null); // para que no se grabe
                        oProduct.setCode(sCode);
                        oProduct.setName("Ticket " + sCode.substring(3, 7));
                        oProduct.setPriceSell(Double.parseDouble(sCode.substring(7, 12)) / 100);   
                        oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                        // Se anade directamente una unidad con el precio y todo
                        addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                    } else if (sCode.length() == 13 && sCode.startsWith("210")) {
                        // barcode of a weigth product
                        incProductByCodePrice(sCode.substring(0, 7), Double.parseDouble(sCode.substring(7, 12)) / 100);
                    } else {
                        incProductByCode(sCode);
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } else {
                // otro caracter
                // Esto es para el codigo de barras...
                m_sBarcode.append(cTrans);

                // Esto es para el los productos normales...
                if (cTrans == '\u007f') { 
                    stateToZero();

                } else if ((cTrans == '0') 
                        && (m_iNumberStatus == NUMBER_INPUTZERO)) {
                    m_jPrice.setText("0");            
                } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                        && (m_iNumberStatus == NUMBER_INPUTZERO)) { 
                    // Un numero entero
                    m_jPrice.setText(Character.toString(cTrans));
                    m_iNumberStatus = NUMBER_INPUTINT;    
                    m_iNumberStatusInput = NUMBERVALID;
                } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                           && (m_iNumberStatus == NUMBER_INPUTINT)) { 
                    // Un numero entero
                    m_jPrice.setText(m_jPrice.getText() + cTrans);

                } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTZERO) {
                    m_jPrice.setText("0.");
                    m_iNumberStatus = NUMBER_INPUTZERODEC;            
                } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTINT) {
                    m_jPrice.setText(m_jPrice.getText() + ".");
                    m_iNumberStatus = NUMBER_INPUTDEC;

                } else if ((cTrans == '0')
                           && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) { 
                    // Un numero decimal
                    m_jPrice.setText(m_jPrice.getText() + cTrans);
                } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                           && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) { 
                    // Un numero decimal
                    m_jPrice.setText(m_jPrice.getText() + cTrans);
                    m_iNumberStatus = NUMBER_INPUTDEC;
                    m_iNumberStatusInput = NUMBERVALID;

                } else if (cTrans == '*' 
                        && (m_iNumberStatus == NUMBER_INPUTINT || m_iNumberStatus == NUMBER_INPUTDEC)) {
                    m_jPor.setText("x");
                    m_iNumberStatus = NUMBER_PORZERO;            
                } else if (cTrans == '*' 
                        && (m_iNumberStatus == NUMBER_INPUTZERO || m_iNumberStatus == NUMBER_INPUTZERODEC)) {
                    m_jPrice.setText("0");
                    m_jPor.setText("x");
                    m_iNumberStatus = NUMBER_PORZERO;       

                } else if ((cTrans == '0') 
                        && (m_iNumberStatus == NUMBER_PORZERO)) {
                    m_jPor.setText("x0");            
                } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                        && (m_iNumberStatus == NUMBER_PORZERO)) { 
                    // Un numero entero
                    m_jPor.setText("x" + Character.toString(cTrans));
                    m_iNumberStatus = NUMBER_PORINT;            
                    m_iNumberStatusPor = NUMBERVALID;
                } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                           && (m_iNumberStatus == NUMBER_PORINT)) { 
                    // Un numero entero
                    m_jPor.setText(m_jPor.getText() + cTrans);

                } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORZERO) {
                    m_jPor.setText("x0.");
                    m_iNumberStatus = NUMBER_PORZERODEC;            
                } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORINT) {
                    m_jPor.setText(m_jPor.getText() + ".");
                    m_iNumberStatus = NUMBER_PORDEC;

                } else if ((cTrans == '0')
                           && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) { 
                    // Un numero decimal
                    m_jPor.setText(m_jPor.getText() + cTrans);
                } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                           && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) { 
                    // Un numero decimal
                    m_jPor.setText(m_jPor.getText() + cTrans);
                    m_iNumberStatus = NUMBER_PORDEC;            
                    m_iNumberStatusPor = NUMBERVALID;  

                } else if (cTrans == '\u00a7' 
                        && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                    // Scale button pressed and a number typed as a price
                    if (m_App.getDeviceScale().existsScale() && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                        try {
                            Double value = m_App.getDeviceScale().readWeight();
                            if (value != null) {
                                ProductInfoExt product = getInputProduct();
                                addTicketLine(product, value, product.getPriceSell());
                            }
                        } catch (ScaleException e) {
                            Toolkit.getDefaultToolkit().beep();
                            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                            stateToZero(); 
                        }
                    } else {
                        // No existe la balanza;
                        Toolkit.getDefaultToolkit().beep();
                    }
                } else if (cTrans == '\u00a7' 
                        && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
                    // Scale button pressed and no number typed.
                    int i = m_ticketlines.getSelectedIndex();
                    if (i < 0){
                        Toolkit.getDefaultToolkit().beep();
                    } else if (m_App.getDeviceScale().existsScale()) {
                        try {
                            Double value = m_App.getDeviceScale().readWeight();
                            if (value != null) {
                                TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                                newline.setMultiply(value);
                                newline.setPrice(Math.abs(newline.getPrice()));
                                paintTicketLine(i, newline);
                            }
                        } catch (ScaleException e) {
                            // Error de pesada.
                            Toolkit.getDefaultToolkit().beep();
                            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                            stateToZero(); 
                        }
                    } else {
                        // No existe la balanza;
                        Toolkit.getDefaultToolkit().beep();
                    }      

                // Add one product more to the selected line
                } else if (cTrans == '+' 
                        && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
                    int i = m_ticketlines.getSelectedIndex();
                    if (i < 0){
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                        //If it's a refund + button means one unit less
                        if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                            newline.setMultiply(newline.getMultiply() - 1.0);
                            paintTicketLine(i, newline);                   
                        }
                        else {
                            // add one unit to the selected line
                            newline.setMultiply(newline.getMultiply() + 1.0);
                            paintTicketLine(i, newline); 
                        }
                    }

                // Delete one product of the selected line
                } else if (cTrans == '-' 
                        && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {

                    int i = m_ticketlines.getSelectedIndex();
                    if (i < 0){
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                        //If it's a refund - button means one unit more
                        if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                            newline.setMultiply(newline.getMultiply() + 1.0);
                            if (newline.getMultiply() >= 0) {
                                removeTicketLine(i);
                            } else {
                                paintTicketLine(i, newline);
                            }
                        } else {
                            // substract one unit to the selected line
                            newline.setMultiply(newline.getMultiply() - 1.0);
                            if (newline.getMultiply() <= 0.0) {                   
                                removeTicketLine(i); // elimino la linea
                            } else {
                                paintTicketLine(i, newline);                   
                            }
                        }
                    }

                // Set n products to the selected line
                } else if (cTrans == '+' 
                        && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERVALID) {
                    int i = m_ticketlines.getSelectedIndex();
                    if (i < 0){
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        double dPor = getPorValue();
                        TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i)); 
                        if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                            newline.setMultiply(-dPor);
                            newline.setPrice(Math.abs(newline.getPrice()));
                            paintTicketLine(i, newline);                
                        } else {
                            newline.setMultiply(dPor);
                            newline.setPrice(Math.abs(newline.getPrice()));
                            paintTicketLine(i, newline);
                        }
                    }

                // Set n negative products to the selected line
                } else if (cTrans == '-' 
                        && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERVALID
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {

                    int i = m_ticketlines.getSelectedIndex();
                    if (i < 0){
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        double dPor = getPorValue();
                        TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                        if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_NORMAL) {
                            newline.setMultiply(dPor);
                            newline.setPrice(-Math.abs(newline.getPrice()));
                            paintTicketLine(i, newline);
                        }           
                    }

                // Anadimos 1 producto
                } else if (cTrans == '+' 
                        && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, 1.0, product.getPriceSell());

                // Anadimos 1 producto con precio negativo
                } else if (cTrans == '-' 
                        && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, 1.0, -product.getPriceSell());

                // Anadimos n productos
                } else if (cTrans == '+' 
                        && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERVALID
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, getPorValue(), product.getPriceSell());

                // Anadimos n productos con precio negativo ?
                } else if (cTrans == '-' 
                        && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERVALID
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, getPorValue(), -product.getPriceSell());

                // Totals() Igual;
                } else if (cTrans == ' ' || cTrans == '=') {
                    if (m_oTicket.getLinesCount() > 0) {

                        if (closeTicket(m_oTicket, m_oTicketExt)) {
                            // Ends edition of current receipt
                            m_ticketsbag.deleteTicket();  
                        } else {
                            // repaint current ticket
                            refreshTicket();
                        }
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        } catch (NumberFormatException | BasicException eData) {
            new MessageInf(eData).show(this);
        }
        
        
    }
    
    private boolean closeTicket(TicketInfo ticket, Object ticketext) throws BasicException {
    
        boolean resultok = false;
        
        if (m_App.getAppUserView().getUser().hasPermission("sales.Total")) {  
            
            try {
                // reset the payment info
                taxeslogic.calculateTaxes(ticket);
                if (ticket.getTotal()>=0.0){
                    ticket.resetPayments(); //Only reset if is sale
                }
                
                if (executeEvent(ticket, ticketext, "ticket.total") == null) {

                    // Muestro el total
                    printTicket("Printer.TicketTotal", ticket, ticketext);
                    
                    
                    // Select the Payments information
                    JPaymentSelect paymentdialog = ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                            ? paymentdialogreceipt
                            : paymentdialogrefund;
                    paymentdialog.setPrintSelected("true".equals(m_jbtnconfig.getProperty("printselected", "true")));

                    paymentdialog.setTransactionID(ticket.getTransactionID());

                    CustomerInfoExt customer = ticket.getCustomer();
                    if (customer != null) {
                        dlSales.loadCustomerExt(ticket.getCustomer().getId());
                    }
                    
                    if (paymentdialog.showDialog(ticket.getTotal(), customer)) {

                        // assign the payments selected and calculate taxes.         
                        ticket.setPayments(paymentdialog.getSelectedPayments());

                        // Asigno los valores definitivos del ticket...
                        ticket.setUser(m_App.getAppUserView().getUser().getUserInfo()); // El usuario que lo cobra
                        ticket.setActiveCash(m_App.getActiveCashIndex());
                        ticket.setDate(new Date()); // Le pongo la fecha de cobro

                        if (executeEvent(ticket, ticketext, "ticket.save") == null) {
                            // Save the receipt and assign a receipt number
                            try {
                                dlSales.saveTicket(ticket, m_App.getInventoryLocation());                       
                            } catch (BasicException eData) {
                                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                                msg.show(this);
                            }

                            executeEvent(ticket, ticketext, "ticket.close", new ScriptArg("print", paymentdialog.isPrintSelected()));

                            // Print receipt.
                            printTicket(paymentdialog.isPrintSelected()
                                    ? "Printer.Ticket"
                                    : "Printer.Ticket2", ticket, ticketext);
                            resultok = true;
                        }
                    }
                }
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
                resultok = false;
            }
            
            // reset the payment info
            m_oTicket.resetTaxes();
            m_oTicket.resetPayments();
        }
        
        // cancelled the ticket.total script
        // or canceled the payment dialog
        // or canceled the ticket.close script
        return resultok;        
    }
       
    private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext) {

        String sresource = dlSystem.getResourceAsXML(sresourcename);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(JPanelTicket.this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("taxes", taxcollection);
                script.put("taxeslogic", taxeslogic);
                script.put("ticket", ticket);
                script.put("place", ticketext);
                m_TTP.printTicket(script.eval(sresource).toString());
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            }
        }
    }
    
    private void printReport(String resourcefile, TicketInfo ticket, Object ticketext) {
        
        try {     
         
            JasperReport jr;
           
            InputStream in = getClass().getResourceAsStream(resourcefile + ".ser");
            if (in == null) {      
                // read and compile the report
                JasperDesign jd = JRXmlLoader.load(getClass().getResourceAsStream(resourcefile + ".jrxml"));            
                jr = JasperCompileManager.compileReport(jd);    
            } else {
                try ( // read the compiled reporte
                        ObjectInputStream oin = new ObjectInputStream(in)) {
                    jr = (JasperReport) oin.readObject();
                }
            }
           
            // Construyo el mapa de los parametros.
            Map reportparams = new HashMap();
            // reportparams.put("ARG", params);
            try {
                reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(resourcefile + ".properties"));
            } catch (MissingResourceException e) {
            }
            reportparams.put("TAXESLOGIC", taxeslogic); 
            
            Map reportfields = new HashMap();
            reportfields.put("TICKET", ticket);
            reportfields.put("PLACE", ticketext);

            JasperPrint jp = JasperFillManager.fillReport(jr, reportparams, new JRMapArrayDataSource(new Object[] { reportfields } ));
            
            PrintService service = ReportUtils.getPrintService(m_App.getProperties().getProperty("machine.printername"));
            
            JRPrinterAWT300.printPages(jp, 0, jp.getPages().size() - 1, service);
            
        } catch (JRException | IOException | ClassNotFoundException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadreport"), e);
            msg.show(this);
        }               
    }

    private void visorTicketLine(TicketLineInfo oLine){
        if (oLine == null) { 
             m_App.getDeviceTicket().getDeviceDisplay().clearVisor();
        } else {                 
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticketline", oLine);
                m_TTP.printTicket(script.eval(dlSystem.getResourceAsXML("Printer.TicketLine")).toString());
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
                msg.show(JPanelTicket.this);
            }
        } 
    }    
    
    
    private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {
        
        // resource here is guaratied to be not null
         try {
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            return scr.evalScript(dlSystem.getResourceAsXML(resource), args);                
        } catch (ScriptException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e);
            msg.show(this);
            return msg;
        } 
    }
        
    public void evalScriptAndRefresh(String resource, ScriptArg... args) {

        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);            
        } else {
            try {
                // calculate taxes
                taxeslogic.calculateTaxes(m_oTicket);
                // execute script
                ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
                scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
                evalScript(scr, resource, args);
                refreshTicket();
                setSelectedIndex(scr.getSelectedIndex());
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
            }
        }
    }  
    
    public void printTicket(String resource) {
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);
        } else {
            try {
                // calculate taxes
                taxeslogic.calculateTaxes(m_oTicket);
                printTicket(resource, m_oTicket, m_oTicketExt);
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
            }
        }
    }
    
    private Object executeEventAndRefresh(String eventkey, ScriptArg ... args) {
        
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            Object result = evalScript(scr, resource, args);   
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
            return result;
        }
    }
   
    private Object executeEvent(TicketInfo ticket, Object ticketext, String eventkey, ScriptArg ... args) {
        
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(ticket, ticketext);
            return evalScript(scr, resource, args);
        }
    }
    
    public String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    public BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }
    
    private void setSelectedIndex(int i) {
        
        if (i >= 0 && i < m_oTicket.getLinesCount()) {
            m_ticketlines.setSelectedIndex(i);
        } else if (m_oTicket.getLinesCount() > 0) {
            m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
        }    
    }
     
    public static class ScriptArg {
        private final String key;
        private final Object value;
        
        public ScriptArg(String key, Object value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }
    
    public class ScriptObject {
        
        private final TicketInfo ticket;
        private final Object ticketext;
        
        private int selectedindex;
        
        private ScriptObject(TicketInfo ticket, Object ticketext) {
            this.ticket = ticket;
            this.ticketext = ticketext;
        }
        
        public double getInputValue() {
            if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                return JPanelTicket.this.getInputValue();
            } else {
                return 0.0;
            }
        }
        
        public int getSelectedIndex() {
            return selectedindex;
        }
        
        public void setSelectedIndex(int i) {
            selectedindex = i;
        }  
        
        public void printReport(String resourcefile) {
            JPanelTicket.this.printReport(resourcefile, ticket, ticketext);
        }
        
        public void printTicket(String sresourcename) {
            JPanelTicket.this.printTicket(sresourcename, ticket, ticketext);   
        }              
        
        public Object evalScript(String code, ScriptArg... args) throws ScriptException {
            
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            script.put("hostname", m_App.getProperties().getProperty("machine.hostname"));
            script.put("ticket", ticket);
            script.put("place", ticketext);
            script.put("taxes", taxcollection);
            script.put("taxeslogic", taxeslogic);             
            script.put("user", m_App.getAppUserView().getUser());
            script.put("sales", this);
            script.put("logicsales", dlSales);
            script.put("logicsystem", dlSystem);

            // more arguments
            for(ScriptArg arg : args) {
                script.put(arg.getKey(), arg.getValue());
            }             

            return script.eval(code);
        }            
    }
     
/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        m_jPanContainer = new javax.swing.JPanel();
        m_jOptions = new javax.swing.JPanel();
        m_jButtons = new javax.swing.JPanel();
        m_jTicketId = new javax.swing.JLabel();
        btnCustomer = new javax.swing.JButton();
        btnSplit = new javax.swing.JButton();
        m_jPanelScripts = new javax.swing.JPanel();
        m_jButtonsExt = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jbtnScale = new javax.swing.JButton();
        m_jPanelBag = new javax.swing.JPanel();
        m_jPanTicket = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jUp = new javax.swing.JButton();
        m_jDown = new javax.swing.JButton();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        jEditAttributes = new javax.swing.JButton();
        m_jDiscountRatePanel = new javax.swing.JPanel();
        m_jbtnDiscountRate = new javax.swing.JButton();
        m_jDisableDiscountRate = new javax.swing.JButton();
        m_jDiscount1 = new javax.swing.JButton();
        m_jDiscount2 = new javax.swing.JButton();
        m_jDiscount3 = new javax.swing.JButton();
        m_jKeypadDiscountRate = new javax.swing.JButton();
        m_jPanelCentral = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jPanTotals = new javax.swing.JPanel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jSubtotalEuros = new javax.swing.JLabel();
        m_jTaxesEuros = new javax.swing.JLabel();
        m_jLblTotalEuros2 = new javax.swing.JLabel();
        m_jLblTotalEuros3 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jCmbCurrency = new javax.swing.JComboBox<>();
        m_jContEntries = new javax.swing.JPanel();
        m_jPanEntries = new javax.swing.JPanel();
        m_jNumberKeys = new com.openbravo.beans.JNumberKeys();
        jPanel9 = new javax.swing.JPanel();
        m_jPrice = new javax.swing.JLabel();
        m_jPor = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        m_jTax = new javax.swing.JComboBox();
        m_jaddtax = new javax.swing.JToggleButton();
        m_jKeyFactory = new javax.swing.JTextField();
        m_jPanelImageViewer = new javax.swing.JPanel();
        m_jImage = new com.openbravo.data.gui.JImageViewer();
        catcontainer = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 204, 153));
        setLayout(new java.awt.CardLayout());

        m_jPanContainer.setLayout(new java.awt.BorderLayout());

        m_jOptions.setLayout(new java.awt.BorderLayout());

        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTicketId.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(160, 25));
        m_jTicketId.setRequestFocusEnabled(false);
        m_jButtons.add(m_jTicketId);

        btnCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/kuser.png"))); // NOI18N
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        m_jButtons.add(btnCustomer);

        btnSplit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/editcut.png"))); // NOI18N
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });
        m_jButtons.add(btnSplit);

        m_jOptions.add(m_jButtons, java.awt.BorderLayout.LINE_START);

        m_jPanelScripts.setLayout(new java.awt.BorderLayout());

        m_jButtonsExt.setLayout(new javax.swing.BoxLayout(m_jButtonsExt, javax.swing.BoxLayout.LINE_AXIS));

        m_jbtnScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ark216.png"))); // NOI18N
        m_jbtnScale.setText(AppLocal.getIntString("button.scale")); // NOI18N
        m_jbtnScale.setFocusPainted(false);
        m_jbtnScale.setFocusable(false);
        m_jbtnScale.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnScale.setRequestFocusEnabled(false);
        m_jbtnScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnScaleActionPerformed(evt);
            }
        });
        jPanel1.add(m_jbtnScale);

        m_jButtonsExt.add(jPanel1);

        m_jPanelScripts.add(m_jButtonsExt, java.awt.BorderLayout.LINE_END);

        m_jOptions.add(m_jPanelScripts, java.awt.BorderLayout.LINE_END);

        m_jPanelBag.setLayout(new java.awt.BorderLayout());
        m_jOptions.add(m_jPanelBag, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jOptions, java.awt.BorderLayout.NORTH);

        m_jPanTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanTicket.setLayout(new java.awt.BorderLayout());

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout();
        flowLayout1.setAlignOnBaseline(true);
        jPanel5.setLayout(flowLayout1);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1uparrow22.png"))); // NOI18N
        m_jUp.setFocusPainted(false);
        m_jUp.setFocusable(false);
        m_jUp.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jUp.setRequestFocusEnabled(false);
        m_jUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jUpActionPerformed(evt);
            }
        });
        jPanel2.add(m_jUp);

        m_jDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1downarrow22.png"))); // NOI18N
        m_jDown.setFocusPainted(false);
        m_jDown.setFocusable(false);
        m_jDown.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDown.setRequestFocusEnabled(false);
        m_jDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDownActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDown);

        m_jDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/locationbar_erase.png"))); // NOI18N
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDelete);

        m_jList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search22.png"))); // NOI18N
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListActionPerformed(evt);
            }
        });
        jPanel2.add(m_jList);

        m_jEditLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/color_line.png"))); // NOI18N
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditLine);

        jEditAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/colorize.png"))); // NOI18N
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed(evt);
            }
        });
        jPanel2.add(jEditAttributes);

        jPanel5.add(jPanel2);

        m_jDiscountRatePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 3, 0, 2));
        m_jDiscountRatePanel.setMinimumSize(new java.awt.Dimension(68, 301));
        m_jDiscountRatePanel.setPreferredSize(new java.awt.Dimension(68, 301));
        m_jDiscountRatePanel.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jbtnDiscountRate.setFocusPainted(false);
        m_jbtnDiscountRate.setFocusable(false);
        m_jbtnDiscountRate.setRequestFocusEnabled(false);
        m_jbtnDiscountRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnDiscountRateActionPerformed(evt);
            }
        });
        m_jDiscountRatePanel.add(m_jbtnDiscountRate);

        m_jDisableDiscountRate.setFocusPainted(false);
        m_jDisableDiscountRate.setFocusable(false);
        m_jDisableDiscountRate.setRequestFocusEnabled(false);
        m_jDisableDiscountRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDisableDiscountRateActionPerformed(evt);
            }
        });
        m_jDiscountRatePanel.add(m_jDisableDiscountRate);

        m_jDiscount1.setFocusPainted(false);
        m_jDiscount1.setFocusable(false);
        m_jDiscount1.setRequestFocusEnabled(false);
        m_jDiscount1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDiscount1ActionPerformed(evt);
            }
        });
        m_jDiscountRatePanel.add(m_jDiscount1);

        m_jDiscount2.setFocusPainted(false);
        m_jDiscount2.setFocusable(false);
        m_jDiscount2.setRequestFocusEnabled(false);
        m_jDiscount2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDiscount2ActionPerformed(evt);
            }
        });
        m_jDiscountRatePanel.add(m_jDiscount2);

        m_jDiscount3.setFocusPainted(false);
        m_jDiscount3.setFocusable(false);
        m_jDiscount3.setRequestFocusEnabled(false);
        m_jDiscount3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDiscount3ActionPerformed(evt);
            }
        });
        m_jDiscountRatePanel.add(m_jDiscount3);

        m_jKeypadDiscountRate.setText("...");
        m_jKeypadDiscountRate.setFocusPainted(false);
        m_jKeypadDiscountRate.setFocusable(false);
        m_jKeypadDiscountRate.setRequestFocusEnabled(false);
        m_jKeypadDiscountRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeypadDiscountRateActionPerformed(evt);
            }
        });
        m_jDiscountRatePanel.add(m_jKeypadDiscountRate);

        jPanel5.add(m_jDiscountRatePanel);

        m_jPanTicket.add(jPanel5, java.awt.BorderLayout.LINE_END);

        m_jPanelCentral.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        m_jPanTotals.setLayout(new java.awt.GridBagLayout());

        m_jTotalEuros.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jTotalEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jTotalEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jTotalEuros, gridBagConstraints);

        m_jLblTotalEuros1.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros1, gridBagConstraints);

        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jSubtotalEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jSubtotalEuros.setOpaque(true);
        m_jSubtotalEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jSubtotalEuros, gridBagConstraints);

        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jTaxesEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTaxesEuros.setOpaque(true);
        m_jTaxesEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jTaxesEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        m_jPanTotals.add(m_jTaxesEuros, gridBagConstraints);

        m_jLblTotalEuros2.setText(AppLocal.getIntString("label.taxcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros2, gridBagConstraints);

        m_jLblTotalEuros3.setText(AppLocal.getIntString("label.subtotalcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros3, gridBagConstraints);

        jPanel4.add(m_jPanTotals, java.awt.BorderLayout.LINE_END);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("Label.Currency.ConversionRate")); // NOI18N
        jPanel7.add(jLabel1);

        jCmbCurrency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCmbCurrencyActionPerformed(evt);
            }
        });
        jPanel7.add(jCmbCurrency);

        jPanel4.add(jPanel7, java.awt.BorderLayout.CENTER);

        m_jPanelCentral.add(jPanel4, java.awt.BorderLayout.SOUTH);

        m_jPanTicket.add(m_jPanelCentral, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jPanTicket, java.awt.BorderLayout.CENTER);

        m_jContEntries.setLayout(new java.awt.BorderLayout());

        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        m_jNumberKeys.addJNumberEventListener(new com.openbravo.beans.JNumberEventListener() {
            public void keyPerformed(com.openbravo.beans.JNumberEvent evt) {
                m_jNumberKeysKeyPerformed(evt);
            }
        });
        m_jPanEntries.add(m_jNumberKeys);

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPrice.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 22));
        m_jPrice.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel9.add(m_jPrice, gridBagConstraints);

        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPor.setOpaque(true);
        m_jPor.setPreferredSize(new java.awt.Dimension(22, 22));
        m_jPor.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jPor, gridBagConstraints);

        m_jEnter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jEnter, gridBagConstraints);

        m_jTax.setFocusable(false);
        m_jTax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel9.add(m_jTax, gridBagConstraints);

        m_jaddtax.setText("+");
        m_jaddtax.setFocusPainted(false);
        m_jaddtax.setFocusable(false);
        m_jaddtax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel9.add(m_jaddtax, gridBagConstraints);

        m_jPanEntries.add(jPanel9);

        m_jKeyFactory.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setPreferredSize(new java.awt.Dimension(1, 1));
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
        });
        m_jPanEntries.add(m_jKeyFactory);

        m_jContEntries.add(m_jPanEntries, java.awt.BorderLayout.NORTH);

        m_jPanelImageViewer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelImageViewer.setLayout(new java.awt.BorderLayout());
        m_jPanelImageViewer.add(m_jImage, java.awt.BorderLayout.CENTER);

        m_jContEntries.add(m_jPanelImageViewer, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jContEntries, java.awt.BorderLayout.LINE_END);

        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        catcontainer.setLayout(new java.awt.BorderLayout());
        m_jPanContainer.add(catcontainer, java.awt.BorderLayout.SOUTH);

        add(m_jPanContainer, "ticket");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnScaleActionPerformed
    stateTransition('\u00a7');    
    }//GEN-LAST:event_m_jbtnScaleActionPerformed

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditLineActionPerformed
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0){
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo newline = JProductLineEdit.showMessage(this, m_App, m_oTicket.getLine(i));
                if (newline != null) {
                    // line has been modified
                    paintTicketLine(i, newline);
                }
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }
    }//GEN-LAST:event_m_jEditLineActionPerformed

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnterActionPerformed
        stateTransition('\n');
    }//GEN-LAST:event_m_jEnterActionPerformed

    private void m_jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {//GEN-FIRST:event_m_jNumberKeysKeyPerformed
        stateTransition(evt.getKey());
    }//GEN-LAST:event_m_jNumberKeysKeyPerformed

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jKeyFactoryKeyTyped
        m_jKeyFactory.setText(null);
        stateTransition(evt.getKeyChar());
    }//GEN-LAST:event_m_jKeyFactoryKeyTyped

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDeleteActionPerformed
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0){
            Toolkit.getDefaultToolkit().beep(); // No hay ninguna seleccionada
        } else {               
            removeTicketLine(i); // elimino la linea           
        }           
    }//GEN-LAST:event_m_jDeleteActionPerformed

    private void m_jUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jUpActionPerformed
        m_ticketlines.selectionUp();
    }//GEN-LAST:event_m_jUpActionPerformed

    private void m_jDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDownActionPerformed
        m_ticketlines.selectionDown();
    }//GEN-LAST:event_m_jDownActionPerformed

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListActionPerformed
        ProductInfoExt prod = JProductFinder.showMessage(JPanelTicket.this, dlSales);    
        if (prod != null) {
            buttonTransition(prod);
        }        
    }//GEN-LAST:event_m_jListActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);
        finder.search(m_oTicket.getCustomer());
        finder.setVisible(true);       
        try {
            m_oTicket.setCustomer(finder.getSelectedCustomer() == null
                    ? null
                    : dlSales.loadCustomerExt(finder.getSelectedCustomer().getId()));
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
            msg.show(this);            
        }
        refreshTicket();       
}//GEN-LAST:event_btnCustomerActionPerformed

    private void btnSplitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSplitActionPerformed
        try {
            if (m_oTicket.getLinesCount() > 0) {
                ReceiptSplit splitdialog = ReceiptSplit.getDialog(this, dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);

                TicketInfo ticket1 = m_oTicket.copyTicket();
                TicketInfo ticket2 = new TicketInfo();
                ticket2.setCustomer(m_oTicket.getCustomer());

                if (splitdialog.showDialog(ticket1, ticket2, m_oTicketExt)) {
                    if (closeTicket(ticket2, m_oTicketExt)) { // already checked  that number of lines > 0
                        setActiveTicket(ticket1, m_oTicketExt);// set result ticket
                    }
                }
            }
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
        }
}//GEN-LAST:event_btnSplitActionPerformed

    private void jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEditAttributesActionPerformed
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo line = m_oTicket.getLine(i);
                JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    // The user pressed OK
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    paintTicketLine(i, line);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
            }
        }        
}//GEN-LAST:event_jEditAttributesActionPerformed

    private void m_jbtnDiscountRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnDiscountRateActionPerformed
        if (typeDiscRate) {
            m_jbtnDiscountRate.setText(AppLocal.getIntString("button.ticketDiscount"));
            typeDiscRate = false;
        } else {
            m_jbtnDiscountRate.setText(AppLocal.getIntString("button.rowdiscount"));
            typeDiscRate = true;
        }
    }//GEN-LAST:event_m_jbtnDiscountRateActionPerformed

    private void m_jDisableDiscountRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDisableDiscountRateActionPerformed
        performDiscRate(0.0);
    }//GEN-LAST:event_m_jDisableDiscountRateActionPerformed

    private void m_jDiscount1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDiscount1ActionPerformed
        performDiscRate(m_DiscRate1);
    }//GEN-LAST:event_m_jDiscount1ActionPerformed

    private void m_jDiscount2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDiscount2ActionPerformed
        performDiscRate(m_DiscRate2);
    }//GEN-LAST:event_m_jDiscount2ActionPerformed

    private void m_jDiscount3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDiscount3ActionPerformed
        performDiscRate(m_DiscRate3);
    }//GEN-LAST:event_m_jDiscount3ActionPerformed

    private void m_jKeypadDiscountRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeypadDiscountRateActionPerformed
        Double dPercent = JPercentDialog.showEditNumber(this, AppLocal.getIntString("message.setdiscountrate"));
        if (dPercent != null && dPercent != 0.0) {
            performDiscRate(dPercent);
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_m_jKeypadDiscountRateActionPerformed

    private void jCmbCurrencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCmbCurrencyActionPerformed
        ConversionRateInfo convRate = (ConversionRateInfo) jCmbCurrency.getSelectedItem();
        refreshTicket(m_oTicket, convRate);
    }//GEN-LAST:event_jCmbCurrencyActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnSplit;
    private javax.swing.JPanel catcontainer;
    private javax.swing.JComboBox<String> jCmbCurrency;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jButtonsExt;
    private javax.swing.JPanel m_jContEntries;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDisableDiscountRate;
    private javax.swing.JButton m_jDiscount1;
    private javax.swing.JButton m_jDiscount2;
    private javax.swing.JButton m_jDiscount3;
    private javax.swing.JPanel m_jDiscountRatePanel;
    private javax.swing.JButton m_jDown;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private com.openbravo.data.gui.JImageViewer m_jImage;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JButton m_jKeypadDiscountRate;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jLblTotalEuros2;
    private javax.swing.JLabel m_jLblTotalEuros3;
    private javax.swing.JButton m_jList;
    private com.openbravo.beans.JNumberKeys m_jNumberKeys;
    private javax.swing.JPanel m_jOptions;
    private javax.swing.JPanel m_jPanContainer;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanTicket;
    private javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelCentral;
    private javax.swing.JPanel m_jPanelImageViewer;
    private javax.swing.JPanel m_jPanelScripts;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTaxesEuros;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JButton m_jUp;
    private javax.swing.JToggleButton m_jaddtax;
    private javax.swing.JButton m_jbtnDiscountRate;
    private javax.swing.JButton m_jbtnScale;
    // End of variables declaration//GEN-END:variables

    private boolean typeDiscRate;
    private PropertiesConfig propConfig;
    private double m_DiscRate1;
    private double m_DiscRate2;
    private double m_DiscRate3;
    private boolean isMultiplyControl;
    
    /**
     * Perform Discount Rate
     * @param p_DiscRate 
     */
    private void performDiscRate (double p_DiscRate) {
        int index = m_ticketlines.getSelectedIndex();
        double total = m_oTicket.getTotal();
        TicketLineInfo m_TicketLineInfo;
        
        if(typeDiscRate) {
            if(index >= 0) {
                m_TicketLineInfo = new DiscountPercent().lineDiscPercent(m_oTicket.getLine(index), 0.0);
                paintTicketLine(index, new DiscountPercent().lineDiscPercent(m_TicketLineInfo, p_DiscRate));
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } else {
            if (total > 0.0) {
                for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                    m_TicketLineInfo = new DiscountPercent().lineDiscPercent(m_oTicket.getLine(i), 0.0);
                    paintTicketLine(i, new DiscountPercent().lineDiscPercent(m_TicketLineInfo, p_DiscRate));
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
        
        refreshTicket();
        
    }
    
     private class CatalogSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int i = m_ticketlines.getSelectedIndex();

                if (i >= 0) {
                    try {
                        String sProduct = m_oTicket.getLine(i).getProductID();
                        if (sProduct != null) {
                            ProductInfoExt prod = JPanelTicket.this.dlSales.getProductInfo(sProduct);
                            if (prod.getImage() != null) {
                                m_jImage.setImage(prod.getImage());
                            } else {
                                m_jImage.setImage(null);
                            }
                        }
                    } catch (BasicException ex) {
                        Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    m_jImage.setImage(null);
                }
            }
        }
    }

}
