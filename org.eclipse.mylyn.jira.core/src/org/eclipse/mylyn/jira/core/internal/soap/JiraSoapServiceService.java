/**
 * JiraSoapServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.eclipse.mylar.jira.core.internal.soap;

public interface JiraSoapServiceService extends javax.xml.rpc.Service {
    public java.lang.String getJirasoapserviceV2Address();

    public org.eclipse.mylar.jira.core.internal.soap.JiraSoapService getJirasoapserviceV2() throws javax.xml.rpc.ServiceException;

    public org.eclipse.mylar.jira.core.internal.soap.JiraSoapService getJirasoapserviceV2(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}