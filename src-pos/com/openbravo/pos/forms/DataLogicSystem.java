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

package com.openbravo.pos.forms;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.imageio.ImageIO;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 *
 * @author adrianromero
 */
public class DataLogicSystem extends BeanFactoryDataSingle {
    
    protected String m_sInitScript;
    private SentenceFind m_version;       
    private SentenceExec m_dummy;
    
    protected SentenceList m_peoplevisible;  
    protected SentenceFind m_peoplebycard;  
    protected SentenceFind m_peoplebyid;  
    protected SerializerRead peopleread;
    
    private SentenceFind m_rolepermissions; 
    private SentenceExec m_changepassword;    
    private SentenceFind m_locationfind;
    
    private SentenceFind m_resourcebytes;
    private SentenceExec m_resourcebytesinsert;
    private SentenceExec m_resourcebytesupdate;

    protected SentenceFind m_sequencecash;
    protected SentenceFind m_activecash;
    protected SentenceExec m_insertcash;
    
    private Map<String, byte[]> resourcescache;
    private PreparedSentence m_peoplebyName;
    
    /** Creates a new instance of DataLogicSystem */
    public DataLogicSystem() {            
    }
    
    @Override
    public void init(Session s){

        m_sInitScript = "/com/openbravo/pos/scripts/" + s.DB.getName();

        m_version = new PreparedSentence(s, "SELECT VERSION FROM APPLICATIONS WHERE ID = ?", SerializerWriteString.INSTANCE, SerializerReadString.INSTANCE);
        m_dummy = new StaticSentence(s, "SELECT * FROM PEOPLE WHERE 1 = 0");
         
        final ThumbNailBuilder tnb = new ThumbNailBuilder(32, 32, "com/openbravo/images/yast_sysadmin.png");        
        peopleread = (DataRead dr) -> new AppUser(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getString(4),
                dr.getString(5),
                new ImageIcon(tnb.getThumbNail(ImageUtils.readImage(dr.getBytes(6)))),
                dr.getString(7)
        
        );

        m_peoplevisible = new StaticSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, SUPERVISOR FROM PEOPLE WHERE VISIBLE = " + s.DB.TRUE()
            , null
            , peopleread);

        m_peoplebycard = new PreparedSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, SUPERVISOR FROM PEOPLE WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE()
            , SerializerWriteString.INSTANCE
            , peopleread);
        
        m_peoplebyid = new PreparedSentence(s
                , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, SUPERVISOR FROM PEOPLE WHERE ID = ? AND VISIBLE = " + s.DB.TRUE()
                , SerializerWriteString.INSTANCE
                , peopleread);
            
        
        m_peoplebyName = new PreparedSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE, SUPERVISOR FROM PEOPLE WHERE NAME = ? AND VISIBLE = " + s.DB.TRUE()
            , SerializerWriteString.INSTANCE
            , peopleread);
         
        m_resourcebytes = new PreparedSentence(s
            , "SELECT CONTENT FROM RESOURCES WHERE NAME = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadBytes.INSTANCE);
        
        Datas[] resourcedata = new Datas[] {Datas.STRING, Datas.STRING, Datas.INT, Datas.BYTES};
        m_resourcebytesinsert = new PreparedSentence(s
                , "INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES (?, ?, ?, ?)"
                , new SerializerWriteBasic(resourcedata));
        m_resourcebytesupdate = new PreparedSentence(s
                , "UPDATE RESOURCES SET NAME = ?, RESTYPE = ?, CONTENT = ? WHERE NAME = ?"
                , new SerializerWriteBasicExt(resourcedata, new int[] {1, 2, 3, 1}));
        
        m_rolepermissions = new PreparedSentence(s
                , "SELECT PERMISSIONS FROM ROLES WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadBytes.INSTANCE);     
        
        m_changepassword = new StaticSentence(s
                , "UPDATE PEOPLE SET APPPASSWORD = ? WHERE ID = ?"
                ,new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}));

        m_sequencecash = new StaticSentence(s,
                "SELECT MAX(HOSTSEQUENCE) FROM CLOSEDCASH WHERE PERSON = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadInteger.INSTANCE);
        m_activecash = new StaticSentence(s
                , "SELECT A.MONEY, A.HOSTSEQUENCE, A.DATESTART, A.DATEEND, A.PERSON FROM CLOSEDCASH A INNER JOIN " +
                    "(SELECT PERSON, MAX(HOSTSEQUENCE) HOSTSEQUENCE FROM CLOSEDCASH WHERE PERSON = ? AND DATEEND IS NULL GROUP BY PERSON) B " +
                    "ON A.HOSTSEQUENCE = B.HOSTSEQUENCE AND A.PERSON = B.PERSON "
                , SerializerWriteString.INSTANCE
                , new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP, Datas.STRING}));            
        m_insertcash = new StaticSentence(s
                , "INSERT INTO CLOSEDCASH(MONEY, HOSTSEQUENCE, DATESTART, DATEEND, PERSON) " +
                  "VALUES (?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP, Datas.STRING}));
            
        m_locationfind = new StaticSentence(s
                , "SELECT NAME FROM LOCATIONS WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , SerializerReadString.INSTANCE);   
        
        resetResourcesCache();        
    }


    public String getInitScript() {
        return m_sInitScript;
    }
    
//    public abstract BaseSentence getShutdown();
    
    public final String findVersion() throws BasicException {
        return (String) m_version.find(AppLocal.APP_ID);
    }
    public final void execDummy() throws BasicException {
        m_dummy.exec();
    }
    public final List listPeopleVisible() throws BasicException {
        return m_peoplevisible.list();
    }      
    public final AppUser findPeopleByCard(String card) throws BasicException {
        return (AppUser) m_peoplebycard.find(card);
    }
    
    public final AppUser findPeopleByID(String id) throws BasicException {
        return (AppUser) m_peoplebyid.find(id);
    }
    
    public final AppUser findPeopleByName(String name) throws BasicException {
        return (AppUser) m_peoplebyName.find(name);
    } 
    
    public final String findRolePermissions(String sRole) {
        
        try {
            return Formats.BYTEA.formatValue(m_rolepermissions.find(sRole));        
        } catch (BasicException e) {
            return null;                    
        }             
    }
    
    public final void execChangePassword(Object[] userdata) throws BasicException {
        m_changepassword.exec(userdata);
    }
    
    public final void resetResourcesCache() {
        resourcescache = new HashMap<>();      
    }
    
    private byte[] getResource(String name) {

        byte[] resource;
        
        resource = resourcescache.get(name);
        
        if (resource == null) {       
            // Primero trato de obtenerlo de la tabla de recursos
            try {
                resource = (byte[]) m_resourcebytes.find(name);
                resourcescache.put(name, resource);
            } catch (BasicException e) {
                resource = null;
            }
        }
        
        return resource;
    }
    
    public final void setResource(String name, int type, byte[] data) {
        
        Object[] value = new Object[] {UUID.randomUUID().toString(), name, type, data};
        try {
            if (m_resourcebytesupdate.exec(value) == 0) {
                m_resourcebytesinsert.exec(value);
            }
            resourcescache.put(name, data);
        } catch (BasicException e) {
        }
    }
    
    public final void setResourceAsBinary(String sName, byte[] data) {
        setResource(sName, 2, data);
    }
    
    public final byte[] getResourceAsBinary(String sName) {
        return getResource(sName);
    }
    
    public final String getResourceAsText(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }
    
    public final String getResourceAsXML(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }    
    
    public final BufferedImage getResourceAsImage(String sName) {
        try {
            byte[] img = getResource(sName); // , ".png"
            return img == null ? null : ImageIO.read(new ByteArrayInputStream(img));
        } catch (IOException e) {
            return null;
        }
    }
    
    public final void setResourceAsProperties(String sName, Properties p) {
        if (p == null) {
            setResource(sName, 0, null); // texto
        } else {
            try {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                p.storeToXML(o, AppLocal.APP_NAME, "UTF8");
                setResource(sName, 0, o.toByteArray()); // El texto de las propiedades   
            } catch (IOException e) { // no deberia pasar nunca
            }            
        }
    }
    
    public final Properties getResourceAsProperties(String sName) {
        
        Properties p = new Properties();
        try {
            byte[] img = getResourceAsBinary(sName);
            if (img != null) {
                p.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        return p;
    }
    // carga desde un archivo binario desde la base de datos y devuelve un archivo con EXTENSION .properties
     public final Properties getResourceBDAsProperties(String sName) {
        
        Properties p = new Properties();
        try {
            byte[] img = getResourceAsBinary(sName);
            if (img != null) {
                p.load(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        return p;
    }

    public final int getSequenceCash(String person) throws BasicException {
        Integer i = (Integer) m_sequencecash.find(person);
        return (i == null) ? 1 : i;
    }

    public final Object[] findActiveCash(String sUserID) throws BasicException {
        return (Object[]) m_activecash.find(sUserID);
    }
    
    public final void execInsertCash(Object[] cash) throws BasicException {
        m_insertcash.exec(cash);
    } 
    
    public final String findLocationName(String iLocation) throws BasicException {
        return (String) m_locationfind.find(iLocation);
    }    
}
