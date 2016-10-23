/**
 * SIPR_RBTPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.onmobile.apps.ringbacktones.thirdparty.tefspain;

public interface SIPR_RBTPortType extends java.rmi.Remote {

    /**
     * Realiza el alta de un suscriptor de una empresa
     */
    public int altaSuscriptorEmpresa(java.lang.String MSISDN, int melodia, int tipoUsuario, java.lang.String franjaHoraria) throws java.rmi.RemoteException;

    /**
     * Realiza la consulta de un suscriptor de una empresa
     */
    public java.lang.String[] consultaSuscriptorEmpresa(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Realiza la modificacion de un suscriptor de una empresa
     */
    public int modificaSuscriptorEmpresa(java.lang.String MSISDN, int melodia, int tipoUsuario, java.lang.String franjaHoraria) throws java.rmi.RemoteException;

    /**
     * Un suscriptor deja de pertenecer a una empresa
     */
    public int bajaSuscriptorEmpresa(java.lang.String MSISDN, int melodia) throws java.rmi.RemoteException;

    /**
     * Confirmacion de alta de un suscriptor por parte de sistemas.
     */
    public int confirmAlta(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Confirmacion de baja de un suscriptor por parte de sistemas.
     */
    public int confirmBaja(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Solicitud de cambio de MSISDN para un suscriptor.
     */
    public int cambioMSISDN(java.lang.String viejoMSISDN, java.lang.String nuevoMSISDN) throws java.rmi.RemoteException;

    /**
     * Solicitud de baja en el servicio de un suscriptor
     */
    public int bajaServicio(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Solicitud de consulta de estado de un suscriptor.
     */
    public int consultaEstado(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Confirmacion de alta de un suscriptor por parte de sistemas.
     */
    public int respuestaAlta(java.lang.String MSISDN, int resul) throws java.rmi.RemoteException;

    /**
     * Confirmacion de baja de un suscriptor por parte de sistemas.
     */
    public int respuestaBaja(java.lang.String MSISDN, int resul) throws java.rmi.RemoteException;

    /**
     * Solicitud de alta de un suscriptor desde Canal Cliente.
     */
    public int altaRBTCC(java.lang.String MSISDN, java.lang.String id_melodia) throws java.rmi.RemoteException;

    /**
     * Solicitud de baja de un suscriptor desde Canal Cliente.
     */
    public int bajaRBTCC(java.lang.String MSISDN, java.lang.String indEmpresa) throws java.rmi.RemoteException;

    /**
     * Solicitud de compra de un suscriptor desde Canal Cliente.
     */
    public int compraRBTCC(java.lang.String MSISDN, java.lang.String fecCompra, java.lang.String id_melodia) throws java.rmi.RemoteException;

    /**
     * Solicitud de alta en publicitario de un suscriptor desde Canal
     * Cliente.
     */
    public int altaRBTPubliCC(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Solicitud de baja en publicitario de un suscriptor desde Canal
     * Cliente.
     */
    public int bajaRBTPubliCC(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Consulta del precio de una melodia para un suscriptor.
     */
    public java.lang.String[] consultaRBTPrecio(java.lang.String MSISDN, java.lang.String id_melodia) throws java.rmi.RemoteException;

    /**
     * Consulta de la melodia por defecto de un suscriptor.
     */
    public java.lang.String consultaRBTDefecto(java.lang.String MSISDN) throws java.rmi.RemoteException;

    /**
     * Solicitud de regalo de un suscriptor a otro desde Canal Cliente.
     */
    public int regaloYavoyCC(java.lang.String MSISDN, java.lang.String MSISDNDEST) throws java.rmi.RemoteException;
}
