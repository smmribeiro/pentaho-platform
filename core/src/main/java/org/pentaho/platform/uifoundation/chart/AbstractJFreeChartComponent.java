/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.uifoundation.chart;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.jfree.data.general.Dataset;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJFreeChartComponent extends AbstractChartComponent {

  private static final long serialVersionUID = 1244685089926020547L;
  private static final int DEFAULT_HEIGHT = 125;
  private static final int DEFAULT_WIDTH = 125;

  protected int chartType;
  protected ActionSequenceJCRHelper jcrHelper;
  protected Dataset dataDefinition;

  protected AbstractJFreeChartComponent( final int chartType, final String definitionPath, final int width,
      final int height, final IPentahoUrlFactory urlFactory, final List messages ) {
    this( urlFactory, messages );
    this.chartType = chartType;
    this.definitionPath = definitionPath;
    this.width = width;
    this.height = height;
    setSourcePath( definitionPath );
  }

  /**
   * @param definitionPath
   * @param urlFactory
   * @param messages
   */
  protected AbstractJFreeChartComponent( final String definitionPath, final IPentahoUrlFactory urlFactory,
      final ArrayList messages ) {
    this( urlFactory, messages );
    this.definitionPath = definitionPath;
    setSourcePath( definitionPath );
  }

  protected AbstractJFreeChartComponent( final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages );
    if( getSession() != null ) {
      jcrHelper = new ActionSequenceJCRHelper( getSession() );
    } else {
      jcrHelper = new ActionSequenceJCRHelper( );  
    }
    
    AbstractChartComponent.logger = LogFactory.getLog( this.getClass() );
  }

  /**
   * Creates a Dataset object (actaully one of it's subclasses from the XML doc
   * 
   * @param doc
   *          XML document that describes the chart
   * @return the Dataset Implementation
   */
  public abstract Dataset createChart( Document doc );

  /**
   * @return Returns the dataSet.
   */
  public Dataset getDataDefinitiont() {
    return dataDefinition;
  }

  /**
   * @param dataSet
   *          The dataSet to set.
   */
  public void setDataDefinition( final Dataset dataSet ) {
    this.dataDefinition = dataSet;
  }

  /**
   * @return Returns the chartType.
   */
  public int getChartType() {
    return chartType;
  }

  /**
   * @param chartType
   *          The chartType to set.
   */
  public void setChartType( final int chartType ) {
    this.chartType = chartType;
  }

  @Override
  public boolean setDataAction( final String chartDefinition ) {
    try {
      Document dataActionDocument = jcrHelper.getSolutionDocument( chartDefinition, RepositoryFilePermission.READ );
      if ( dataActionDocument == null ) {
        return false;
      }

      Node dataNode = dataActionDocument.selectSingleNode( "chart/data" ); //$NON-NLS-1$

      if ( dataNode == null ) {
        // No data here
        return false;
      }
      chartType = (int) XmlDom4JHelper.getNodeText( "chart-type", dataNode, -1 ); //$NON-NLS-1$
      actionPath = XmlDom4JHelper.getNodeText( "data-path", dataNode ); //$NON-NLS-1$
      actionOutput = XmlDom4JHelper.getNodeText( "data-output", dataNode ); //$NON-NLS-1$
      byRow = XmlDom4JHelper.getNodeText( "data-orientation", dataNode, "rows" ).equals( "rows" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if ( width == 0 ) {
        width = (int) XmlDom4JHelper.getNodeText( "chart/width", dataActionDocument, DEFAULT_WIDTH ); //$NON-NLS-1$
      }
      if ( height == 0 ) {
        height = (int) XmlDom4JHelper.getNodeText( "chart/height", dataActionDocument, DEFAULT_HEIGHT ); //$NON-NLS-1$
      }
    } catch ( Exception e ) {
      error( Messages.getInstance().getString(
          "CategoryDatasetChartComponent.ERROR_0001_INVALID_CHART_DEFINITION", chartDefinition ), e ); //$NON-NLS-1$
      return false;
    }
    return true;
  }
}
