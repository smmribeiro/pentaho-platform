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


package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides system settings data for system configuration files located in the system folder of the repository.
 * System settings for </code>PentahoSystem</code> are hardcoded to exist in <repository>/system/pentaho.xml.
 * Provides a settings cache so that settings are read from the file once, and the associated DOM document is
 * cached in memory for future lookups.
 *
 * @author unknown
 *
 */
public class SystemSettings extends PentahoBase implements ISystemSettings {
  /**
   *
   */
  private static final long serialVersionUID = 3727605230748352557L;

  /**
   * This constant is for the overall system settings file name.
   */
  public static final String PENTAHOSETTINGSFILENAME = "pentaho.xml"; //$NON-NLS-1$

  private static final String LOG_NAME = Messages.getInstance().getString( "SYSTEMSETTINGS.CODE_LOG_NAME" ); //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( SystemSettings.class );

  private final Map settingsDocumentMap = new ConcurrentHashMap();

  String logId;

  public SystemSettings() {
    // TODO apply session-base security to the system settings
    logId = SystemSettings.LOG_NAME + ":"; //$NON-NLS-1$
    // TODO sbarkdull clean up
    // Document doc = getSystemSettingsDocument(DEFAULT_PENTAHOSETTINGSFILENAME);
    // if (doc == null) {
    //        throw new IllegalArgumentException(Messages.getInstance().getErrorString("SYSTEMSETTINGS.ERROR_0003_INVALID_OR_MISSING_FILE")); //$NON-NLS-1$
    // }
  }

  public String getSystemSetting( final String path, final String settingName, final String defaultValue ) {
    debug( Messages.getInstance().getString( "SYSTEMSETTINGS.DEBUG_GET_SYSTEM_SETTING_PATH", File.separator + path ) ); //$NON-NLS-1$
    Document doc = getSystemSettingsDocument( path );
    if ( doc == null ) {
      return defaultValue;
    }

    if ( isXPathUnSafe( settingName ) ) {
      error( Messages.getInstance().getString( "SYSTEMSETTINGS.XPATH_VIOLATION_DEFAULT", settingName ) );
      return defaultValue;
    }

    Node node = doc.selectSingleNode( "//" + settingName ); //$NON-NLS-1$
    if ( node == null ) {
      return defaultValue;
    }
    return node.getText();
  }

  public String getSystemSetting( final String settingName, final String defaultValue ) {
    return getSystemSetting( SystemSettings.PENTAHOSETTINGSFILENAME, settingName, defaultValue );
  }

  public List getSystemSettings( final String path, final String settingName ) {
    List settings = (List) settingsDocumentMap.get( path + settingName );
    if ( settings != null ) {
      return settings;
    }
    Document doc = getSystemSettingsDocument( path );
    if ( doc == null ) {
      return null;
    }
    if ( isXPathUnSafe( settingName ) ) {
      error( Messages.getInstance().getString( "SYSTEMSETTINGS.XPATH_VIOLATION", settingName ) );
      return null;
    }
    settings = doc.selectNodes( "//" + settingName ); //$NON-NLS-1$
    settingsDocumentMap.put( path + settingName, settings );
    return settings;
  }

  public List getSystemSettings( final String settingName ) {
    return getSystemSettings( SystemSettings.PENTAHOSETTINGSFILENAME, settingName );
  }

  public Document getSettingsDocumentFromFile( File f ) throws IOException, DocumentException {
    return XmlDom4JHelper.getDocFromFile( f, null );
  }

  /**
   * Get the DOM document initialized by the file specified in the <code>actionPath</code> parameter. If this is
   * the first time the document associated with <code>actionPath</code> has been requested, cache the DOM
   * document. If this is not the first time the document has been requested, return the document from the cache.
   */
  public Document getSystemSettingsDocument( final String actionPath ) {
    // S logId =
    // runtimeContext.getInstanceId()+":"+LOG_NAME+":"+runtimeContext.getActionName(); //$NON-NLS-1$ //$NON-NLS-2$
    Document systemSettingsDocument = (Document) settingsDocumentMap.get( actionPath );
    if ( systemSettingsDocument == null ) {
      File f = getFile( actionPath );
      if ( f == null ) {
        return null;
      }
      try {
        systemSettingsDocument = getSettingsDocumentFromFile( f );
        settingsDocumentMap.put( actionPath, systemSettingsDocument );
      } catch ( DocumentException e ) {
        // todo log this
        e.printStackTrace();
      } catch ( IOException e ) {
        // todo log this
        e.printStackTrace();
      }
    }
    return systemSettingsDocument;
  }

  /**
   * Get a <code>File</code> object that references a file in the system folder of the repository. The
   * <code>path</code> parameter is relative to the system folder in the repository.
   *
   * @param path
   *          String containing the path of a file relative to the system folder in the repository
   * @return File referencing the file specified by <code>path</code> relative to repository's system folder.
   */
  private File getFile( final String path ) {
    File f = new File( getAbsolutePath( path ) );

    if ( !f.exists() ) {
      error( Messages.getInstance().getErrorString(
          "SYSTEMSETTINGS.ERROR_0002_FILE_NOT_IN_SOLUTION", f.getAbsolutePath() ) ); //$NON-NLS-1$
      return null;
    }
    debug( Messages.getInstance().getString( "SYSTEMSETTINGS.DEBUG_SYSTEM_SETTINGS_GET_FILE", f.getAbsolutePath() ) ); //$NON-NLS-1$

    return f;
  }

  /**
   * Create a String containing the complete path to the system folder in the repository, and append the parameter
   * <code>path</code> to it.
   *
   * @param path
   *          String containing the path of a file relative to the system folder in the repository
   * @return String containing the path
   */
  protected String getAbsolutePath( final String path ) {
    return getSolutionPath() + File.separator + path;
  }

  protected static String getSolutionPath() {
    String solutionPath;
    if ( PentahoSystem.getApplicationContext() != null ) {
      solutionPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" );
    } else {
      solutionPath = System.getProperty( "PentahoSystemPath" );
    }

    return solutionPath;
  }

  @Override
  public Log getLogger() {
    return SystemSettings.logger;
  }

  public void resetSettingsCache() {
    settingsDocumentMap.clear();
  }

  // TODO sbarkdull, this props could be cached in a map similar to how the xml docs are cached
  public Properties getSystemSettingsProperties( final String path ) {
    String fullPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" + File.separator + path ); //$NON-NLS-1$
    File propsFile = new File( fullPath );
    if ( !propsFile.exists() ) {
      return null;
    }
    try {
      Properties props = new Properties();
      InputStream fileInputStream = new BufferedInputStream( new FileInputStream( propsFile ) );
      try {
        props.load( fileInputStream );
        return props;
      } finally {
        fileInputStream.close();
      }
    } catch ( FileNotFoundException e ) {
      SystemSettings.logger.error( Messages.getInstance().getErrorString(
          "SystemSettings.ERROR_0003_FAILED_INITIALIZE", path ), e ); //$NON-NLS-1$
    } catch ( IOException ioe ) {
      SystemSettings.logger.error( Messages.getInstance().getErrorString(
          "SystemSettings.ERROR_0003_FAILED_INITIALIZE", path ), ioe ); //$NON-NLS-1$
    }
    return null;
  }

  public String getSystemCfgSourceName() {
    return getAbsolutePath( SystemSettings.PENTAHOSETTINGSFILENAME );
  }

  // We rely on passing in values with characters that prevent us from using variables in the query, which
  // would be the standard way of preventing dangerous user input. Instead, as a stop-gap, we can check that
  // the strings we concatenate don't contain any of the other characters that would allow problematic queries.
  private boolean isXPathUnSafe( String settingName ) {
    if ( settingName == null ) {
      return false;
    }

    // We need to allow "/", "-", "*", and "."
    return settingName.contains( "'" ) ||
      settingName.contains( "\"" ) ||
      settingName.contains( "<" ) ||
      settingName.contains( ">" ) ||
      settingName.contains( "[" ) ||
      settingName.contains( "]" ) ||
      settingName.contains( "{" ) ||
      settingName.contains( "}" ) ||
      settingName.contains( "(" ) ||
      settingName.contains( ")" ) ||
      settingName.contains( "#" ) ||
      settingName.contains( "%" ) ||
      settingName.contains( ";" ) ||
      settingName.contains( ":" ) ||
      settingName.contains( "," ) ||
      settingName.contains( "?" ) ||
      settingName.contains( "\\" ) ||
      settingName.contains( "|" ) ||
      settingName.contains( "!" ) ||
      settingName.contains( "@" ) ||
      settingName.contains( "$" ) ||
      settingName.contains( "&" ) ||
      settingName.contains( "+" ) ||
      settingName.contains( "=" ) ||
      settingName.contains( "~" );
  }
}
