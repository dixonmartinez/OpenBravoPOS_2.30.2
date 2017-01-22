/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.currency;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author tt-01
 */
public class ConversionRatePanel extends JPanelTable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7389329411432913570L;

	private TableDefinition t_ConversionRate;
    
    private ConversionRateView jEditor;
    
    @Override
    protected void init() {
        DataLogicConversionRate dlConvRate = (DataLogicConversionRate) app.getBean(DataLogicConversionRate.class.getName());
        t_ConversionRate = dlConvRate.getTableConversionRate();
        jEditor = new ConversionRateView(app, dirty);
    }

    @Override
    public void activate() throws BasicException {    
        jEditor.activate();         
        super.activate();
    }
    
    @Override
    public EditorRecord getEditor() {
        return jEditor;
    }

    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(t_ConversionRate);
    }

    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(t_ConversionRate, new int [] {0,1,2,3,4,5,6,7});
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Currency.ConversionRate");
    }
    
    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(t_ConversionRate.getRenderStringBasic(new int[]{1,2}));
    }
    
}
