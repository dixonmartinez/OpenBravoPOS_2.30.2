//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2008-2009 Openbravo, S.L.
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

package com.openbravo.data.model;

/**
 *
 * @author adrian
 */
public class Table {
    
    private final String name;
    private final Column[] columns;
    
    public Table(String name, Column... columns) {
        this.name = name;
        this.columns = columns;
    }
    
    public String getName() {
        return name;
    }
    
    public Column[] getColumns() {
        return columns;
    }
    
    public String getListSQL() {
        StringBuilder sent = new StringBuilder();
        sent.append("select ");

        for (int i = 0; i < columns.length; i ++) {
            if (i > 0) {
                sent.append(", ");
            }
            sent.append(columns[i].getName());
        }        
        
        sent.append(" from ");        
        sent.append(name);
        
        return sent.toString();          
    }   
    
    public String getInsertSQL() {
        
        StringBuilder sent = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        sent.append("insert into ");
        sent.append(name);
        sent.append(" (");        
        
        for (int i = 0; i < columns.length; i ++) {
            if (i > 0) {
                sent.append(", ");
                values.append(", ");
            }
            sent.append(columns[i].getName());
            values.append("?");
        }
        
        sent.append(") values (");
        sent.append(values.toString());
        sent.append(")");

        return sent.toString();       
    }    
    
    public String getUpdateSQL() {
        
        StringBuffer values = new StringBuffer();
        StringBuffer filter = new StringBuffer();
        
        for (Column column : columns) {
            if (column.isPK()) {
                if (filter.length() == 0) {
                    filter.append(" where ");
                } else  {
                    filter.append(" and ");
                }
                filter.append(column.getName());
                filter.append(" = ?");                
            } else {
                if (values.length() > 0) {
                    values.append(", ");
                }
                values.append(column.getName());
                values.append(" = ?");                
            }
        }
        
        return "update " + name + " set " + values + filter;             
    }   
    
    public String getDeleteSQL() {
        
        StringBuffer filter = new StringBuffer();

        for (Column column : columns) {
            if (column.isPK()) {
                if (filter.length() == 0) {
                    filter.append(" where ");
                } else  {
                    filter.append(" and ");
                }
                filter.append(column.getName());
                filter.append(" = ?"); 
            }
        }
        
        return "delete from " + name + filter;     
    }    
}
