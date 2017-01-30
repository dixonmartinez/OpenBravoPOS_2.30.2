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
package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;

/**
 *
 * @author adrianromero
 */
public class UserInfo implements SerializableRead, IKeyed {

    private String m_sId;
    private String m_sName;
    private String m_sSupervisor;

    /** 
     * Creates a new instance of UserInfoBasic
     * @param id
     * @param name 
     */
    public UserInfo(String id, String name) {
        m_sId = id;
        m_sName = name;
    }

    public UserInfo() {
        m_sId = null;
        m_sName = null;
    }

    public String getId() {
        return m_sId;
    }

    public String getName() {
        return m_sName;
    }

    @Override
    public String toString() {
        return getName();
    }

	@Override
	public Object getKey() {
		return m_sId;
	}

	@Override
	public void readValues(DataRead dr) throws BasicException {
		m_sId = dr.getString(1);
        m_sName = dr.getString(2);
	}

	/**
	 * @return the m_sSupervisor
	 */
	public String getSupervisor() {
		return m_sSupervisor;
	}

	/**
	 * @param m_sSupervisor the m_sSupervisor to set
	 */
	public void setSupervisor(String m_sSupervisor) {
		this.m_sSupervisor = m_sSupervisor;
	}
    
    
}
