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

package com.openbravo.data.loader;

import java.sql.*;
import java.util.*; 
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.JDBCSentence.JDBCDataResultSet;

public class MetaSentence extends JDBCSentence {
    
    private String m_sSentence;
    protected SerializerRead m_SerRead = null;
    protected SerializerWrite m_SerWrite = null;

    /** 
     * Creates a new instance of MetaDataSentence
     * @param s
     * @param sSentence
     * @param serwrite
     * @param serread 
     */
    public MetaSentence(Session s, String sSentence, SerializerWrite serwrite, SerializerRead serread) {
        super(s);
        m_sSentence = sSentence;
        m_SerWrite = serwrite;
        m_SerRead = serread;
    }
    public MetaSentence(Session s, String sSentence, SerializerRead serread) {
        this(s, sSentence, null, serread);
    }
    
    private static class MetaParameter implements DataWrite {

        private final ArrayList m_aParams;

        /** Creates a new instance of MetaParameter */
        public MetaParameter() {
            m_aParams = new ArrayList();
        }
        
        @Override
        public void setDouble(int paramIndex, Double dValue) throws BasicException {
            throw new BasicException(LocalRes.getIntString("exception.noparamtype"));
        }
        @Override
        public void setBoolean(int paramIndex, Boolean bValue) throws BasicException {
            throw new BasicException(LocalRes.getIntString("exception.noparamtype"));
        }
        @Override
        public void setInt(int paramIndex, Integer iValue) throws BasicException {
            throw new BasicException(LocalRes.getIntString("exception.noparamtype"));
        }   
        @Override
        public void setString(int paramIndex, String sValue) throws BasicException {
            ensurePlace(paramIndex - 1);
            m_aParams.set(paramIndex - 1, sValue);
        }
        @Override
        public void setTimestamp(int paramIndex, java.util.Date dValue) throws BasicException {
            throw new BasicException(LocalRes.getIntString("exception.noparamtype"));
        }
        @Override
        public void setBytes(int paramIndex, byte[] value) throws BasicException {
             throw new BasicException(LocalRes.getIntString("exception.noparamtype"));
        }
        @Override
        public void setObject(int paramIndex, Object value) throws BasicException {
            setString(paramIndex, (value == null) ? null : value.toString());
        }

        public String getString(int index) {
            return (String) m_aParams.get(index);
        }    
        
        private void ensurePlace(int i) {
            m_aParams.ensureCapacity(i);
            while (i >= m_aParams.size()){
                m_aParams.add(null);
            }
        }
    }

    @Override
    public DataResultSet openExec(Object params) throws BasicException {    
        closeExec();        
        try {
            DatabaseMetaData db = m_s.getConnection().getMetaData();
            MetaParameter mp = new MetaParameter();               
            if (params != null) {
                // si m_SerWrite fuera null deberiamos cascar
                m_SerWrite.writeValues(mp, params);  
            }
            // Catalogs Has Schemas Has Objects
            if (null != m_sSentence) // Lo generico de la base de datos
            switch (m_sSentence) {
                case "getCatalogs":
                    return new JDBCDataResultSet(db.getCatalogs(), m_SerRead);
                case "getSchemas":
                    return new JDBCDataResultSet(db.getSchemas(), m_SerRead);
                case "getTableTypes":
                    return new JDBCDataResultSet(db.getTableTypes(), m_SerRead);
                case "getTypeInfo":
                    return new JDBCDataResultSet(db.getTypeInfo(), m_SerRead);
                    
                    // Los objetos por catalogo, esquema
                    
                    // Los tipos definidos por usuario
                case "getUDTs":
                    return new JDBCDataResultSet(db.getUDTs(mp.getString(0), mp.getString(1), null, null), m_SerRead);
                case "getSuperTypes":
                    return new JDBCDataResultSet(db.getSuperTypes(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                    
                    // Los atributos
                case "getAttributes":
                    return new JDBCDataResultSet(db.getAttributes(mp.getString(0), mp.getString(1), null, null), m_SerRead);
                    
                    // Las Tablas y sus objetos relacionados
                case "getTables":
                    return new JDBCDataResultSet(db.getTables(mp.getString(0), mp.getString(1), null, null), m_SerRead);
                case "getSuperTables":
                    return new JDBCDataResultSet(db.getSuperTables(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                case "getTablePrivileges":
                    return new JDBCDataResultSet(db.getTablePrivileges(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                case "getBestRowIdentifier":
                    return new JDBCDataResultSet(db.getBestRowIdentifier(mp.getString(0), mp.getString(1), mp.getString(2), 0, true), m_SerRead);
                case "getPrimaryKeys":
                    return new JDBCDataResultSet(db.getPrimaryKeys(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                case "getColumnPrivileges":
                    return new JDBCDataResultSet(db.getColumnPrivileges(mp.getString(0), mp.getString(1), mp.getString(2), null), m_SerRead);
                case "getColumns":
                    return new JDBCDataResultSet(db.getColumns(mp.getString(0), mp.getString(1), mp.getString(2), null), m_SerRead);
                case "getVersionColumns":
                    return new JDBCDataResultSet(db.getVersionColumns(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                case "getIndexInfo":
                    return new JDBCDataResultSet(db.getIndexInfo(mp.getString(0), mp.getString(1), mp.getString(2), false, false), m_SerRead);
                case "getExportedKeys":
                    return new JDBCDataResultSet(db.getExportedKeys(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                case "getImportedKeys":
                    return new JDBCDataResultSet(db.getImportedKeys(mp.getString(0), mp.getString(1), mp.getString(2)), m_SerRead);
                case "getCrossReference":
                    return new JDBCDataResultSet(db.getCrossReference(mp.getString(0), mp.getString(1), mp.getString(2), null, null, null), m_SerRead);
                    
                    // Los procedimientos y sus objetos relacionados
                case "getProcedures":
                    return new JDBCDataResultSet(db.getProcedures(mp.getString(0), mp.getString(1), null), m_SerRead);
                case "getProcedureColumns":
                    return new JDBCDataResultSet(db.getProcedureColumns(mp.getString(0), mp.getString(1), mp.getString(2), null), m_SerRead);
                default:
                    return null;
            } else 
                return null;
        } catch (SQLException eSQL) {
            throw new BasicException(eSQL);
        }
    }  
    
    @Override
    public void closeExec() throws BasicException {
    }
    
    @Override
    public DataResultSet moreResults() throws BasicException {
        return null;
    }
}
