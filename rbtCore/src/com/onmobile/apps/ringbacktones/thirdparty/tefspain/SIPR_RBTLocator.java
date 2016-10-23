/**
 * SIPR_RBTLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.onmobile.apps.ringbacktones.thirdparty.tefspain;

public class SIPR_RBTLocator extends org.apache.axis.client.Service implements SIPR_RBT {

/**
 * gSOAP 2.3.8 generated service definition
 */

    public SIPR_RBTLocator() {
    }


    public SIPR_RBTLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SIPR_RBTLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SIPR_RBT
    private java.lang.String SIPR_RBT_address = "http://localhost";

    public java.lang.String getSIPR_RBTAddress() {
        return SIPR_RBT_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SIPR_RBTWSDDServiceName = "SIPR_RBT";

    public java.lang.String getSIPR_RBTWSDDServiceName() {
        return SIPR_RBTWSDDServiceName;
    }

    public void setSIPR_RBTWSDDServiceName(java.lang.String name) {
        SIPR_RBTWSDDServiceName = name;
    }

    public SIPR_RBTPortType getSIPR_RBT() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SIPR_RBT_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSIPR_RBT(endpoint);
    }

    public SIPR_RBTPortType getSIPR_RBT(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            SIPR_RBTBindingStub _stub = new SIPR_RBTBindingStub(portAddress, this);
            _stub.setPortName(getSIPR_RBTWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSIPR_RBTEndpointAddress(java.lang.String address) {
        SIPR_RBT_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (SIPR_RBTPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                SIPR_RBTBindingStub _stub = new SIPR_RBTBindingStub(new java.net.URL(SIPR_RBT_address), this);
                _stub.setPortName(getSIPR_RBTWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SIPR_RBT".equals(inputPortName)) {
            return getSIPR_RBT();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://localhost/SIPR_RBT.wsdl", "SIPR_RBT");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://localhost/SIPR_RBT.wsdl", "SIPR_RBT"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SIPR_RBT".equals(portName)) {
            setSIPR_RBTEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
