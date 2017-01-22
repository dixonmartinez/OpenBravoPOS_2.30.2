/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.currency;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;

/**
 *
 * @author tt-01
 */
public class DataLogicConversionRate extends BeanFactoryDataSingle {

    /** Session                 */
    protected Session m_Session;
    /** Table Currency          */
    private TableDefinition t_ConversionRate;
    /** Table Name              */
    public final static String TABLE_NAME = "CONVERSIONRATE";
    /** ConversionRate Data           */
    private static final Datas[] FIELD_DATAS = 
            new Datas[] {
                Datas.STRING,
                Datas.STRING,
                Datas.STRING,
                Datas.DOUBLE,
                Datas.STRING,
                Datas.DOUBLE,
                Datas.TIMESTAMP,
                Datas.TIMESTAMP
            
            };  
    /** ConversionRate Field Name     */
    private static final String [] FIELD_NAMES = 
            new String[] {
                "CONVERSIONRATE_ID",
                "CURRENCY_ID",
                "CURRENCYTO_ID",
                "DIVIDERATE",
                "ISACTIVE",
                "MULTIPLYRATE",
                "VALIDFROM",
                "VALIDTO"
            };
    
    /** ConversionRate Field Formats  */
    private static final Formats[] FIELD_FORMATS =
            new Formats[] {
                Formats.STRING,
                Formats.STRING,
                Formats.STRING,
                Formats.DOUBLE,
                Formats.STRING,
                Formats.DOUBLE,
                Formats.TIMESTAMP,
                Formats.TIMESTAMP
            };
    
    @Override
    public void init(Session s) {
        this.m_Session = s;
        
        t_ConversionRate = new TableDefinition(s, TABLE_NAME, FIELD_NAMES, FIELD_DATAS, FIELD_FORMATS, new int[] {1});
                
    }
    /**
     * Get TableDefinition
     * @return 
     */
    public TableDefinition getTableConversionRate() {
        return t_ConversionRate;
    }
    
    public List<ConversionRateInfo> getConversionRate() throws BasicException {
        StringBuilder sql = new StringBuilder(" select ");
        for(int i = 0; i < FIELD_NAMES.length; i++) {
        	if(i > 0) {
        		sql.append(",");
        	}
        	sql.append(FIELD_NAMES[i]);        		
        }
        sql.append(" from ")
        	.append(TABLE_NAME);
        
        return new PreparedSentence(
                m_Session, 
                sql.toString(),
                null,
                ConversionRateInfo.getSerializerRead()
            ).list();
    }
    
    public List<CurrencyInfo> getCurrencyOfConversion() throws BasicException {
        String sql = "SELECT Currency_ID FROM ConversionRate WHERE IsActive ='Y'";
        return new PreparedSentence(
                m_Session, 
                sql,
                null,
                CurrencyInfo.getSerializerRead()).list();        
    }
}
