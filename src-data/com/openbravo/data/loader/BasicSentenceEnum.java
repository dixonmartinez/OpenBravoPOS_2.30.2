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

import com.openbravo.basic.BasicException;

/**
 *
 * @author adrian
 */
public class BasicSentenceEnum implements SentenceEnum {
    
    BaseSentence sent;
    DataResultSet SRS;
    
    /** 
     * Creates a new instance of AbstractSentenceEnum
     * @param sent 
     */
    public BasicSentenceEnum(BaseSentence sent) {
        this.sent = sent;
        this.SRS = null;
    }
    
    @Override
    public void load() throws BasicException {
        load(null);
    }
    @Override
    public void load(Object params) throws BasicException {
        SRS = sent.openExec(params);
    }

    @Override
    public Object getCurrent() throws BasicException {
        if (SRS == null) {
            throw new BasicException(LocalRes.getIntString("exception.nodataset"));
        } 
        
        return SRS.getCurrent();  
    }
    
    @Override
    public boolean next() throws BasicException {
        if (SRS == null) {
            throw new BasicException(LocalRes.getIntString("exception.nodataset"));
        } 
        
        if (SRS.next()) {
            return true;  
        } else {
            SRS.close();
            SRS = null;
            sent.closeExec();
            return false;
        }
    }
}
