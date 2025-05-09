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


package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Permissions tab sub panel of FilePropertiesDialog. GET ACL call is performed by FilePropertiesDialog and then
 * passed to sub panels to consolidate
 */
public class PermissionsPanel extends FlexTable implements IFileModifier {

  private static final String RECIPIENT_TYPE_ELEMENT_NAME = "recipientType"; //$NON-NLS-1$

  private static final String PERMISSIONS_ELEMENT_NAME = "permissions"; //$NON-NLS-1$

  private static final String RECIPIENT_ELEMENT_NAME = "recipient"; //$NON-NLS-1$

  private static final String MODIFIABLE_ELEMENT_NAME = "modifiable"; //$NON-NLS-1$

  private static final String ACES_ELEMENT_NAME = "aces"; //$NON-NLS-1$

  private static final String OWNER_NAME_ELEMENT_NAME = "owner"; //$NON-NLS-1$

  private static final String OWNER_TYPE_ELEMENT_NAME = "ownerType"; //$NON-NLS-1$

  public static final int USER_TYPE = 0;

  public static final int ROLE_TYPE = 1;

  public static final int PERM_READ = 0;

  public static final int PERM_WRITE = 1;

  public static final int PERM_DELETE = 2;

  public static final int PERM_GRANT_PERM = 3;

  public static final int PERM_ALL = 4;

  private static final String INHERITS_ELEMENT_NAME = "entriesInheriting"; //$NON-NLS-1$

  private static final String ERROR = "error";

  private static final String COULDNOTGETPERMISSIONS = "couldNotGetPermissions";

  boolean dirty = false;

  ArrayList<String> existingUsersAndRoles = new ArrayList<String>();

  RepositoryFile fileSummary;

  Document fileInfo;

  Document origFileInfo;

  boolean origInheritAclFlag = false;

  boolean isAdmin = false;

  ListBox usersAndRolesList = new ListBox( true );

  Label permissionsLabel = new Label( Messages.getString( "permissionsColon" ) ); //$NON-NLS-1$

  FlexTable permissionsTable = new FlexTable();

  Button removeButton = new Button( Messages.getString( "remove" ) ); //$NON-NLS-1$

  Button addButton = new Button( Messages.getString( "addPeriods" ) ); //$NON-NLS-1$

  final CheckBox readPermissionCheckBox = new CheckBox( Messages.getString( "read" ) ); //$NON-NLS-1$

  final CheckBox deletePermissionCheckBox = new CheckBox( Messages.getString( "delete" ) ); //$NON-NLS-1$

  final CheckBox writePermissionCheckBox = new CheckBox( Messages.getString( "write" ) ); //$NON-NLS-1$

  final CheckBox managePermissionCheckBox = new CheckBox( Messages.getString( "managePermissions" ) ); //$NON-NLS-1$

  final CheckBox inheritsCheckBox = new CheckBox( Messages.getString( "inherits" ) ); //$NON-NLS-1$

  /**
   * @param fileSummary
   */
  public PermissionsPanel( RepositoryFile theFileSummary ) {
    this.fileSummary = theFileSummary;

    removeButton.setStylePrimaryName( "pentaho-button" );
    addButton.setStylePrimaryName( "pentaho-button" );
    usersAndRolesList.getElement().setId( "sharePanelUsersAndRolesList" );
    addButton.getElement().setId( "sharePanelAddButton" );
    removeButton.getElement().setId( "sharePanelRemoveButton" );

    setAdmin();

    removeButton.addClickHandler( new ClickHandler() {

      public void onClick( ClickEvent clickEvent ) {
        // find list to remove
        if ( usersAndRolesList.getItemCount() == 0 ) {
          return;
        }
        dirty = true;

        for ( final String userOrRoleString : SelectUserOrRoleDialog.getSelectedItemsValue( usersAndRolesList ) ) {
          String recipientType = getRecipientTypeByValue( userOrRoleString );
          //"(user)".length() = "(role)".length() = 6
          String userOrRoleNameString = userOrRoleString.substring( 0, userOrRoleString.length() - 6 );
          removeRecipient( userOrRoleNameString, recipientType, fileInfo );
          usersAndRolesList.removeItem( usersAndRolesList.getSelectedIndex() );
          existingUsersAndRoles.remove( userOrRoleNameString );
        }
        if ( usersAndRolesList.getItemCount() > 0 ) {
          usersAndRolesList.setSelectedIndex( 0 );
        }
        buildPermissionsTable( fileInfo );
        updateVisibleItemsUserAndRolesList();
      }
    } );

    addButton.addClickHandler( new ClickHandler() {

      public void onClick( ClickEvent clickEvent ) {
        dirty = true;
        final SelectUserOrRoleDialog pickUserRoleDialog =
            new SelectUserOrRoleDialog( fileInfo, existingUsersAndRoles, new IUserRoleSelectedCallback() {

              public void roleSelected( String role ) {
                //this was done to distinguish users and roles in case they are identical
                usersAndRolesList.addItem( role, role + "(role)" ); //$NON-NLS-1$
                existingUsersAndRoles.add( role );
                usersAndRolesList.setSelectedIndex( usersAndRolesList.getItemCount() - 1 );
                addRecipient( role, ROLE_TYPE, fileInfo );
                updateVisibleItemsUserAndRolesList();
                buildPermissionsTable( fileInfo );
              }

              public void userSelected( String user ) {
                usersAndRolesList.addItem( user, user + "(user)" ); //$NON-NLS-1$
                existingUsersAndRoles.add( user );
                usersAndRolesList.setSelectedIndex( usersAndRolesList.getItemCount() - 1 );
                addRecipient( user, USER_TYPE, fileInfo );
                updateVisibleItemsUserAndRolesList();
                buildPermissionsTable( fileInfo );
              }
            } );
        pickUserRoleDialog.center();
      }

    } );

    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.add( addButton );
    buttonPanel.add( removeButton );
    updateVisibleItemsUserAndRolesList();
    usersAndRolesList.addClickHandler( new ClickHandler() {

      public void onClick( ClickEvent clickEvent ) {
        // update permissions list and permission label (put username in it)
        // rebuild permissionsTable settings based on selected mask
        buildPermissionsTable( fileInfo );
      }

    } );
    usersAndRolesList.setWidth( "100%" ); //$NON-NLS-1$
    buttonPanel.setWidth( "100%" ); //$NON-NLS-1$

    readPermissionCheckBox.getElement().setId( "sharePermissionRead" ); //$NON-NLS-1$
    deletePermissionCheckBox.getElement().setId( "sharePermissionDelete" ); //$NON-NLS-1$
    writePermissionCheckBox.getElement().setId( "sharePermissionWrite" ); //$NON-NLS-1$
    managePermissionCheckBox.getElement().setId( "sharePermissionManagePerm" ); //$NON-NLS-1$

    readPermissionCheckBox.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent clickEvent ) {
        updatePermissionMask( fileInfo, readPermissionCheckBox.getValue(), PERM_READ );
        refreshPermission();
      }
    } );
    deletePermissionCheckBox.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent clickEvent ) {
        setDeleteCheckBox( deletePermissionCheckBox.getValue() );
        refreshPermission();
      }
    } );
    writePermissionCheckBox.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent clickEvent ) {
        setWriteCheckBox( writePermissionCheckBox.getValue() );
        refreshPermission();
      }
    } );
    managePermissionCheckBox.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent clickEvent ) {
        setManageCheckBox( managePermissionCheckBox.getValue() );
        refreshPermission();
      }
    } );

    readPermissionCheckBox.setEnabled( false );

    inheritsCheckBox.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent clickEvent ) {
        dirty = true;
        String moduleBaseURL = GWT.getModuleBaseURL();
        String moduleName = GWT.getModuleName();
        final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

        if ( inheritsCheckBox.getValue() ) {
          // Get the state of add and remove button
          final boolean currRemoveButtonState = removeButton.isEnabled();
          final boolean currAddButtonState = addButton.isEnabled();
          final MessageDialogBox permissionsOverwriteConfirm = new MessageDialogBox(
            Messages.getString( "permissionsWillBeLostConfirmMessage" ),
            Messages.getString( "permissionsWillBeLostQuestion" ),
            false,
            Messages.getString( "ok" ),
            Messages.getString( "cancel" ) );

          final IDialogCallback callback = new IDialogCallback() {

            public void cancelPressed() {
              permissionsOverwriteConfirm.hide();
              inheritsCheckBox.setValue( false );
              dirty = false;
              // BACKLOG-15986 Set the button state to value before the confirmation dialog
              setInheritsAcls( inheritsCheckBox.getValue(), fileInfo );
              addButton.setEnabled( currAddButtonState );
              removeButton.setEnabled( currRemoveButtonState );
            }

            public void okPressed() {
              String path = fileSummary.getPath().substring( 0, fileSummary.getPath().lastIndexOf( "/" ) );
              String url = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId( path ) + "/acl"; //$NON-NLS-1$ //$NON-NLS-2$
              RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
              // This header is required to force Internet Explorer to not cache values from the GET response.
              builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
              try {
                builder.sendRequest( null, new RequestCallback() {
                  public void onResponseReceived( Request request, Response response ) {
                    if ( response.getStatusCode() == Response.SC_OK ) {
                      initializePermissionPanel( XMLParser.parse( response.getText() ) );
                      inheritsCheckBox.setValue( true );
                      refreshPermission();
                    } else {
                      inheritsCheckBox.setValue( false );
                      refreshPermission();
                      MessageDialogBox dialogBox = new MessageDialogBox(
                        Messages.getString( ERROR ),
                        Messages.getString( COULDNOTGETPERMISSIONS, response.getStatusText() ),
                        false );
                      dialogBox.center();
                    }
                  }

                  @Override
                  public void onError( Request request, Throwable exception ) {
                    inheritsCheckBox.setValue( false );
                    refreshPermission();
                    MessageDialogBox dialogBox = new MessageDialogBox(
                      Messages.getString( ERROR ),
                      Messages.getString( COULDNOTGETPERMISSIONS, exception.getLocalizedMessage() ),
                      false );
                    dialogBox.center();
                  }
                } );
              } catch ( RequestException e ) {
                inheritsCheckBox.setValue( false );
                refreshPermission();
                MessageDialogBox dialogBox = new MessageDialogBox(
                  Messages.getString( ERROR ),
                  Messages.getString( COULDNOTGETPERMISSIONS, e.getLocalizedMessage() ),
                  false );
                dialogBox.center();
              }
            }
          };

          permissionsOverwriteConfirm.setCallback( callback );
          permissionsOverwriteConfirm.center();
        }

        refreshPermission();
      }
    } );

    int row = 0;
    setWidget( row++, 0, inheritsCheckBox );
    setWidget( row++, 0, new Label( Messages.getString( "usersAndRoles" ) ) ); //$NON-NLS-1$
    setWidget( row++, 0, usersAndRolesList );

    // right justify button panel
    CellFormatter buttonPanelCellFormatter = new CellFormatter();
    buttonPanelCellFormatter.setHorizontalAlignment( row, 0, HasHorizontalAlignment.ALIGN_RIGHT );
    setCellFormatter( buttonPanelCellFormatter );
    setWidget( row++, 0, buttonPanel );

    setWidget( row++, 0, permissionsLabel );
    setWidget( row++, 0, permissionsTable );

    setCellPadding( 4 );

    setWidth( "100%" ); //$NON-NLS-1$

    permissionsTable.setWidget( 0, 0, managePermissionCheckBox );
    permissionsTable.setWidget( 1, 0, deletePermissionCheckBox );
    permissionsTable.setWidget( 2, 0, writePermissionCheckBox );
    permissionsTable.setWidget( 3, 0, readPermissionCheckBox );
    permissionsTable.setStyleName( "permissionsTable" ); //$NON-NLS-1$
    permissionsTable.setWidth( "100%" ); //$NON-NLS-1$
    permissionsTable.setHeight( "100%" ); //$NON-NLS-1$

    init();
  }

  private void setManageCheckBox( boolean value ) {
    managePermissionCheckBox.setValue( value );
    updatePermissionMask( fileInfo, value, PERM_GRANT_PERM );
    if ( value ) {
      setDeleteCheckBox( true );
    }
  }

  private void setDeleteCheckBox( boolean value ) {
    deletePermissionCheckBox.setValue( value );
    updatePermissionMask( fileInfo, value, PERM_DELETE );
    if ( value ) {
      setWriteCheckBox( true );
    }
  }

  private void setWriteCheckBox( boolean value ) {
    writePermissionCheckBox.setValue( value );
    updatePermissionMask( fileInfo, value, PERM_WRITE );
  }

  private void refreshPermission() {
    refreshPermission( inheritsCheckBox.getValue(), managePermissionCheckBox.getValue(), deletePermissionCheckBox
        .getValue() );
  }

  private void refreshPermission( Boolean inheritCheckBoxValue, Boolean managePermissionCheckBoxValue,
                                  Boolean deletePermissionCheckBoxValue ) {
    setInheritsAcls( inheritCheckBoxValue, fileInfo );
    managePermissionCheckBox.setEnabled( !inheritCheckBoxValue );
    deletePermissionCheckBox.setEnabled( !inheritCheckBoxValue && !managePermissionCheckBoxValue );
    writePermissionCheckBox.setEnabled( !inheritCheckBoxValue && !managePermissionCheckBoxValue
        && !deletePermissionCheckBoxValue );
    addButton.setEnabled( !inheritCheckBoxValue );
    removeButton.setEnabled( isRemovable() );
  }

  /**
   * Set the widgets according to what is currently in the DOM.
   */
  public void buildPermissionsTable( Document fileInfo ) {
    String userOrRoleString = ""; //$NON-NLS-1$
    String recipientType = ""; //$NON-NLS-1$
    if ( usersAndRolesList.getItemCount() == 0 ) {
      permissionsLabel.setText( Messages.getString( "permissionsColon" ) ); //$NON-NLS-1$
    } else {
      String value = usersAndRolesList.getValue( usersAndRolesList.getSelectedIndex() );
      recipientType = getRecipientTypeByValue( value );
      //"(user)".length() = "(role)".length() = 6
      userOrRoleString = value.substring( 0, value.length() - 6 );
      permissionsLabel.setText( Messages.getString( "permissionsFor", userOrRoleString ) ); //$NON-NLS-1$
    }

    List<Integer> perms = getPermissionsForUserOrRole( fileInfo, userOrRoleString, recipientType );

    // create checkboxes, with listeners who update the fileInfo lists

    if ( "".equals( userOrRoleString ) ) { //$NON-NLS-1$
      writePermissionCheckBox.setEnabled( false );
      deletePermissionCheckBox.setEnabled( false );
      managePermissionCheckBox.setEnabled( false );
    }

    if ( perms.contains( PERM_ALL ) ) {
      updatePermissionForUserOrRole( fileInfo, userOrRoleString, recipientType, false, PERM_ALL );
      updatePermissionForUserOrRole( fileInfo, userOrRoleString, recipientType, true, PERM_GRANT_PERM );
      updatePermissionForUserOrRole( fileInfo, userOrRoleString, recipientType, true, PERM_DELETE );
      updatePermissionForUserOrRole( fileInfo, userOrRoleString, recipientType, true, PERM_WRITE );
      updatePermissionForUserOrRole( fileInfo, userOrRoleString, recipientType, true, PERM_READ );
    }

    readPermissionCheckBox.setValue( perms.contains( PERM_READ ) || perms.contains( PERM_GRANT_PERM )
        || perms.contains( PERM_ALL ) );
    deletePermissionCheckBox.setValue( perms.contains( PERM_DELETE ) || perms.contains( PERM_GRANT_PERM )
        || perms.contains( PERM_ALL ) );
    writePermissionCheckBox.setValue( perms.contains( PERM_WRITE ) || perms.contains( PERM_GRANT_PERM )
        || perms.contains( PERM_ALL ) );
    managePermissionCheckBox.setValue( perms.contains( PERM_GRANT_PERM ) || perms.contains( PERM_ALL ) );
    inheritsCheckBox.setValue( isInheritsAcls( fileInfo ) );

    refreshPermission();

    if ( ( perms.contains( PERM_GRANT_PERM ) || perms.contains( PERM_ALL ) )
      && ( !isModifiableUserOrRole( fileInfo, userOrRoleString, recipientType )
        || ( !isAdmin && ( isOwner( userOrRoleString, USER_TYPE, fileInfo )
        || isOwner( userOrRoleString, ROLE_TYPE, fileInfo ) ) ) ) && !inheritsCheckBox.getValue() ) {
      managePermissionCheckBox.setEnabled( false );
    }

  }

  /**
   * @param grant
   * @param perm
   */
  public void updatePermissionMask( Document fileInfo, boolean grant, int perm ) {
    if ( usersAndRolesList.getSelectedIndex() >= 0 ) {
      dirty = true;
      final String value = usersAndRolesList.getValue( usersAndRolesList.getSelectedIndex() );
      final String recipientType = getRecipientTypeByValue( value );
      //"(user)".length() = "(role)".length() = 6
      final String userOrRoleString = value.substring( 0, value.length() - 6 );
      updatePermissionForUserOrRole( fileInfo, userOrRoleString, recipientType, grant, perm );
    }
  }

  /**
   * PUT acl changes back via REST call to /acl
   */
  public void apply() {
    // not used
  }

  /**
   * @return
   */
  public List<RequestBuilder> prepareRequests() {
    ArrayList<RequestBuilder> requestBuilders = new ArrayList<RequestBuilder>();

    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    String url = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId( fileSummary.getPath() ) + "/acl"; //$NON-NLS-1$//$NON-NLS-2$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.PUT, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    builder.setHeader( "Content-Type", "application/xml" );

    // At this point if we're inheriting we need to remove all the acls so that the inheriting flag isn't set by
    // default
    if ( isInheritsAcls( fileInfo ) ) {
      removeAllAces( fileInfo );
    } else {
      // Check if any of the permission sets should be replaced with ALL.
      // Any non-inherited Ace with a permission set containing PERM_GRANT_PERM should be replaced
      // with a single PERM_ALL.
      NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
      for ( int i = 0; i < aces.getLength(); i++ ) {
        Element ace = (Element) aces.item( i );
        NodeList perms = ace.getElementsByTagName( PERMISSIONS_ELEMENT_NAME );
        for ( int j = 0; j < perms.getLength(); j++ ) {
          Element perm = (Element) perms.item( j );
          if ( perm.getFirstChild() != null ) {
            if ( Integer.parseInt( perm.getFirstChild().getNodeValue() ) == PERM_GRANT_PERM ) {
              replacePermissionsWithAll( ace, fileInfo );
              break;
            }
          }
        }
      }
    }

    // set request data in builder itself
    builder.setRequestData( fileInfo.toString() );

    // add builder to list to return to parent for execution
    requestBuilders.add( builder );

    return requestBuilders;
  }

  /**
   * Take permissions from fileInfo response and create roles and users list
   *
   * @param fileSummary
   * @param fileInfo
   */
  public void init( RepositoryFile fileSummary, Document fileInfo ) {
    this.origFileInfo = fileInfo;
    this.origInheritAclFlag = isInheritsAcls( fileInfo );
    initializePermissionPanel( fileInfo );
  }

  private void initializePermissionPanel( Document fileInfo ) {

    this.fileInfo = fileInfo;

    usersAndRolesList.clear();
    existingUsersAndRoles.clear();

    for ( String name : getNames( fileInfo, USER_TYPE ) ) {
      //this was done to distinguish users and roles in case they are identical
      usersAndRolesList.addItem( name, name + "(user)" ); //$NON-NLS-1$
      existingUsersAndRoles.add( name );
    }
    for ( String name : getNames( fileInfo, ROLE_TYPE ) ) {
      usersAndRolesList.addItem( name, name + "(role)" ); //$NON-NLS-1$
      existingUsersAndRoles.add( name );
    }
    if ( usersAndRolesList.getItemCount() > 0 ) {
      usersAndRolesList.setSelectedIndex( 0 );
    }

    buildPermissionsTable( fileInfo );
  }

  /**
   *
   */
  public void init() {
    // not doing anything right now. GET moved to FilePropertiesDialog parent and
    // response set in PermissionsPanel.setAclResponse
  }

  // *********************
  // Document manipulation
  // *********************
  void removeRecipient( String recipient, String recipientType, Document fileInfo ) {
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    for ( int i = 0; i < aces.getLength(); i++ ) {
      Element ace = (Element) aces.item( i );
      if ( ace.getElementsByTagName( RECIPIENT_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipient ) &&  ace.getElementsByTagName( RECIPIENT_TYPE_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipientType ) ) {
        ace.getParentNode().removeChild( ace );
        break;
      }
    }
  }

  /**
   * @param name
   * @param type
   * @return
   */
  private Boolean isOwner( String name, Integer type, Document fileInfo ) {
    return name == getOwnerName( fileInfo ) && type == getOwnerType( fileInfo );
  }

  /**
   * @return
   */
  private String getOwnerName( Document fileInfo ) {
    return fileInfo.getElementsByTagName( OWNER_NAME_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue();
  }

  /**
   * @return
   */
  private Integer getOwnerType( Document fileInfo ) {
    return Integer.parseInt( fileInfo.getElementsByTagName( OWNER_TYPE_ELEMENT_NAME ).item( 0 ).getFirstChild()
        .getNodeValue() );
  }

  /**
   * @param recipientName
   * @param recipientType
   */
  void addRecipient( String recipientName, int recipientType, Document fileInfo ) {
    Element newAces = fileInfo.createElement( ACES_ELEMENT_NAME );
    Element newPermission = fileInfo.createElement( PERMISSIONS_ELEMENT_NAME );
    Element newRecipient = fileInfo.createElement( RECIPIENT_ELEMENT_NAME );
    Element newRecipientType = fileInfo.createElement( RECIPIENT_TYPE_ELEMENT_NAME );
    Element modifiableElementName = fileInfo.createElement( MODIFIABLE_ELEMENT_NAME );
    Text textNode = fileInfo.createTextNode( recipientName );
    newRecipient.appendChild( textNode );
    textNode = fileInfo.createTextNode( Integer.toString( recipientType ) );
    newRecipientType.appendChild( textNode );
    textNode = fileInfo.createTextNode( Boolean.toString( true ) );
    modifiableElementName.appendChild( textNode );
    newAces.appendChild( newPermission );
    newAces.appendChild( newRecipient );
    newAces.appendChild( newRecipientType );
    newAces.appendChild( modifiableElementName );

    fileInfo.getDocumentElement().appendChild( newAces );
    // Base recipient is created at this point.
    // Now give them the default perms.
    String strRecipientType = Integer.toString( recipientType );
    updatePermissionForUserOrRole( fileInfo, recipientName, strRecipientType, true, PERM_READ );
  }

  /**
   * @param recipient
   * @param permission
   */
  private void addPermission( String recipient, String recipientType, int permission, Document fileInfo ) {
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    for ( int i = 0; i < aces.getLength(); i++ ) {
      Element ace = (Element) aces.item( i );
      if ( ace.getElementsByTagName( RECIPIENT_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipient ) && ace.getElementsByTagName( RECIPIENT_TYPE_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipientType ) ) {
        Element newPerm = fileInfo.createElement( PERMISSIONS_ELEMENT_NAME );
        Text textNode = fileInfo.createTextNode( Integer.toString( permission ) );
        newPerm.appendChild( textNode );
        ace.appendChild( newPerm );
      }
    }
  }

  /**
   * @param type
   * @return list of names of given "type"
   */
  protected List<String> getNames( final Document fileInfo, int type ) {
    List<String> names = new ArrayList<String>();
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    for ( int i = 0; i < aces.getLength(); i++ ) {
      Element ace = (Element) aces.item( i );
      NodeList recipientTypeList = ace.getElementsByTagName( RECIPIENT_TYPE_ELEMENT_NAME );
      Node recipientNode = recipientTypeList.item( 0 );
      String nodeValue = recipientNode.getFirstChild().getNodeValue();
      int recipientType = Integer.parseInt( nodeValue );
      if ( recipientType == type ) {
        names.add( ace.getElementsByTagName( RECIPIENT_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue() );
      }
    }
    return names;
  }

  /**
   * @param recipient
   * @return
   */
  private List<Integer> getPermissionsForUserOrRole( Document fileInfo, String recipient, String recipientType ) {
    List<Integer> values = new ArrayList<Integer>();
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    for ( int i = 0; i < aces.getLength(); i++ ) {
      Element ace = (Element) aces.item( i );
      if ( ace.getElementsByTagName( RECIPIENT_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipient ) && ace.getElementsByTagName( RECIPIENT_TYPE_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipientType ) ) {
        NodeList permissions = ace.getElementsByTagName( PERMISSIONS_ELEMENT_NAME );
        for ( int j = 0; j < permissions.getLength(); j++ ) {
          if ( permissions.item( j ).getFirstChild() != null ) {
            values.add( new Integer( permissions.item( j ).getFirstChild().getNodeValue() ) );
          }
        }
        break;
      }
    }
    return values;
  }

  private Boolean isModifiableUserOrRole( Document fileInfo, String recipient, String recipientType ) {
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    for ( int i = 0; i < aces.getLength(); i++ ) {
      Element ace = (Element) aces.item( i );
      if ( ace.getElementsByTagName( RECIPIENT_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipient ) && ace.getElementsByTagName( RECIPIENT_TYPE_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipientType ) ) {
        NodeList modifiable = ace.getElementsByTagName( MODIFIABLE_ELEMENT_NAME );
        return modifiable.getLength() > 0 && modifiable.item( 0 ).getFirstChild().getNodeValue().equals( "true" );
      }
    }
    return false;
  }

  /**
   * @param recipient
   * @param grant     true = grant the Permission, false = deny the Permission (remove it if present)
   * @param perm      The integer value of the Permission as defined in <code>RepositoryFilePermissions</code>
   */
  private void updatePermissionForUserOrRole( Document fileInfo, String recipient, String recipientType, boolean grant, int perm ) {
    // first let's see if this node exists
    Node foundPermission = null;
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    for ( int i = 0; i < aces.getLength(); i++ ) {
      Element ace = (Element) aces.item( i );
      if ( ace.getElementsByTagName( RECIPIENT_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipient ) && ace.getElementsByTagName( RECIPIENT_TYPE_ELEMENT_NAME ).item( 0 ).getFirstChild().getNodeValue().equals(
          recipientType ) ) {
        NodeList permissions = ace.getElementsByTagName( PERMISSIONS_ELEMENT_NAME );
        for ( int j = 0; j < permissions.getLength(); j++ ) {
          Node testNode = permissions.item( j );
          if ( testNode.getFirstChild() != null && Integer.parseInt( testNode.getFirstChild()
              .getNodeValue() ) == perm ) {
            foundPermission = testNode;
            break;
          }
        }
        break;
      }
    }

    if ( grant ) {
      if ( foundPermission != null ) { // This permission already exists.
        return;
      }
      addPermission( recipient, recipientType, perm, fileInfo );
    } else {
      if ( foundPermission != null ) {
        foundPermission.getParentNode().removeChild( foundPermission );
      }
    }
  }

  /**
   *
   */
  private void removeAllAces( Document fileInfo ) {
    // Window.alert("removeAllAces() called with: \n" + fileInfo.toString());
    NodeList aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    while ( aces != null && aces.getLength() > 0 ) {
      for ( int i = 0; i < aces.getLength(); i++ ) {
        Node ace = aces.item( i );
        ace.getParentNode().removeChild( ace );
      }
      aces = fileInfo.getElementsByTagName( ACES_ELEMENT_NAME );
    }
  }

  private void replacePermissionsWithAll( Element ace, Document fileInfo ) {
    NodeList perms = ace.getElementsByTagName( PERMISSIONS_ELEMENT_NAME );
    int childCount = perms.getLength();
    for ( int i = 0; i < childCount; i++ ) {
      Node perm = perms.item( i );
      if ( perm != null ) {
        ace.removeChild( perm );
      }
    }
    Element newPerm = fileInfo.createElement( PERMISSIONS_ELEMENT_NAME );
    Text textNode = fileInfo.createTextNode( Integer.toString( PERM_ALL ) );
    newPerm.appendChild( textNode );
    ace.appendChild( newPerm );
  }

  /**
   * @return
   */
  Boolean isInheritsAcls( Document fileInfo ) {
    return Boolean.valueOf( fileInfo.getElementsByTagName( INHERITS_ELEMENT_NAME ).item( 0 ).getFirstChild()
        .getNodeValue() );
  }

  /**
   * @param inherits
   */
  void setInheritsAcls( Boolean inherits, Document fileInfo ) {
    fileInfo.getElementsByTagName( INHERITS_ELEMENT_NAME ).item( 0 )
        .getFirstChild().setNodeValue( inherits.toString() );
  }

  /**
   * Get owner name from acl response
   *
   * @param response
   */
  protected void setAclResponse( Response response ) {
    init( fileSummary, XMLParser.parse( response.getText() ) );
  }

  private String getRecipientTypeByValue( String userOrRoleString ) {
    String recipientType = "";
    if ( userOrRoleString.endsWith( "(user)" ) ) {
      recipientType = "0";
    } else if ( userOrRoleString.endsWith( "(role)" ) ) {
      recipientType = "1";
    }
    return recipientType;
  }

  private Boolean isRemovable() {

    if ( inheritsCheckBox.getValue() ) {
      return false;
    }

    if ( usersAndRolesList.getItemCount() == 0 ) {
      return false;
    }

    List<String> items = SelectUserOrRoleDialog.getSelectedItemsValue( usersAndRolesList );

    if ( !items.isEmpty() ) {
      for ( String userOrRoleString : items ) {
        String recipientType = getRecipientTypeByValue( userOrRoleString );
        String userOrRoleNameString = userOrRoleString.substring( 0, userOrRoleString.length() - 6 );
        if ( !isRemovableUserOrRole( userOrRoleNameString, recipientType ) ) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private Boolean isRemovableUserOrRole( String userOrRoleString, String recipientType ) {
    return !( isOwner( userOrRoleString, USER_TYPE, fileInfo ) || isOwner( userOrRoleString,
      ROLE_TYPE, fileInfo ) || !isModifiableUserOrRole( fileInfo, userOrRoleString, recipientType ) )
      && !inheritsCheckBox.getValue();
  }

  private void setAdmin() {
    try {
      final String url = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "accept", "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" ); //$NON-NLS-1$ //$NON-NLS-2$
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable caught ) {
          isAdmin = false;
        }

        public void onResponseReceived( Request request, Response response ) {
          isAdmin = "true".equalsIgnoreCase( response.getText() ); //$NON-NLS-1$
        }
      } );
    } catch ( RequestException e ) {
      isAdmin = false;
    }
  }

  private void updateVisibleItemsUserAndRolesList() {
    usersAndRolesList.setVisibleItemCount( Math.max( 4, Math.min( usersAndRolesList.getItemCount(), 6 ) ) );
  }
}
