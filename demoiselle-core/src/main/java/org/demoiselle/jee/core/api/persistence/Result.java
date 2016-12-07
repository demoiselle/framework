/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.core.api.persistence;

import java.util.List;

/**
 *
 * @author SERPRO
 */
@SuppressWarnings("rawtypes")
public interface Result {

    public int getInit();

    public void setInit(int init);

    public int getQtde();

    public void setQtde(int qtde);

    public long getTotal();

    public void setTotal(long total);
   
	public List getContent();

    public void setContent(List content);

}
