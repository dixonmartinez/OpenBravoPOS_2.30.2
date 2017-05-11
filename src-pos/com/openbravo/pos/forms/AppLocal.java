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

import com.openbravo.beans.LocaleResources;

/**
 *
 * @author adrianromero
 */
public class AppLocal {
    
    public static final String APP_NAME = "Openbravo POS";
    public static final String APP_ID = "openbravopos";
    public static final String APP_VERSION = "2.30.3";
  
    // private static List<ResourceBundle> m_messages;
    private static final LocaleResources M_RESOURCES;
    
    static {
        M_RESOURCES = new LocaleResources();
        M_RESOURCES.addBundleName("pos_messages");
        M_RESOURCES.addBundleName("erp_messages");
    }
    
    /** Creates a new instance of AppLocal */
    private AppLocal() {
    }
    
    public static String getIntString(String sKey) {
        return M_RESOURCES.getString(sKey);
    }
    
    public static String getIntString(String sKey, Object ... sValues) {
        return M_RESOURCES.getString(sKey, sValues);
    }
}
