/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.25 at 11:25:28 AM EDT 
//

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * Java class for databaseType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="databaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="defaultDatabasePort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="extraOptionsHelpUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shortName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="supportedAccessTypes" type="{http://www.pentaho.com/schema/}databaseAccessType"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="defaultDatabaseName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultOptions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "databaseType", propOrder = { "defaultDatabasePort", "extraOptionsHelpUrl", "name", "shortName",
  "supportedAccessTypes", "defaultDatabaseName", "defaultOptions" } )
public class DatabaseType {

  protected int defaultDatabasePort;
  protected String extraOptionsHelpUrl;
  protected String name;
  protected String shortName;
  @XmlElement( nillable = true )
  protected List<DatabaseAccessType> supportedAccessTypes;
  protected String defaultDatabaseName;
  @XmlElement( required = true )
  protected DatabaseType.DefaultOptions defaultOptions;

  /**
   * Gets the value of the defaultDatabasePort property.
   */
  public int getDefaultDatabasePort() {
    return defaultDatabasePort;
  }

  /**
   * Sets the value of the defaultDatabasePort property.
   */
  public void setDefaultDatabasePort( int value ) {
    this.defaultDatabasePort = value;
  }

  /**
   * Gets the value of the extraOptionsHelpUrl property.
   *
   * @return possible object is {@link String }
   */
  public String getExtraOptionsHelpUrl() {
    return extraOptionsHelpUrl;
  }

  /**
   * Sets the value of the extraOptionsHelpUrl property.
   *
   * @param value allowed object is {@link String }
   */
  public void setExtraOptionsHelpUrl( String value ) {
    this.extraOptionsHelpUrl = value;
  }

  /**
   * Gets the value of the name property.
   *
   * @return possible object is {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value allowed object is {@link String }
   */
  public void setName( String value ) {
    this.name = value;
  }

  /**
   * Gets the value of the shortName property.
   *
   * @return possible object is {@link String }
   */
  public String getShortName() {
    return shortName;
  }

  /**
   * Sets the value of the shortName property.
   *
   * @param value allowed object is {@link String }
   */
  public void setShortName( String value ) {
    this.shortName = value;
  }

  /**
   * Gets the value of the supportedAccessTypes property.
   * <p/>
   * <p/>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
   * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
   * the supportedAccessTypes property.
   * <p/>
   * <p/>
   * For example, to add a new item, do as follows:
   * <p/>
   * <pre>
   * getSupportedAccessTypes().add( newItem );
   * </pre>
   * <p/>
   * <p/>
   * <p/>
   * Objects of the following type(s) are allowed in the list {@link DatabaseAccessType }
   */
  public List<DatabaseAccessType> getSupportedAccessTypes() {
    if ( supportedAccessTypes == null ) {
      supportedAccessTypes = new ArrayList<>();
    }
    return this.supportedAccessTypes;
  }

  /**
   * Gets the value of the defaultDatabaseName property.
   *
   * @return possible object is {@link String }
   */
  public String getDefaultDatabaseName() {
    return defaultDatabaseName;
  }

  /**
   * Sets the value of the defaultDatabaseName property.
   */
  public void setDefaultDatabaseName( String value ) {
    this.defaultDatabaseName = value;
  }

  /**
   * Gets the value of the defaultOptions property.
   *
   * @return possible object is {@link DatabaseType.DefaultOptions }
   */
  public DatabaseType.DefaultOptions getDefaultOptions() {
    return defaultOptions;
  }

  /**
   * Sets the value of the defaultOptions property.
   *
   * @param value allowed object is {@link DatabaseType.DefaultOptions }
   */
  public void setDefaultOptions( DatabaseType.DefaultOptions value ) {
    this.defaultOptions = value;
  }

  /**
   * <p>Defining an inner class to support the DefaultOptions'Map object from the original class.</p>
   * {@inheritDoc}
   */
  public static class DefaultOptions extends MapExport {
  }
}
