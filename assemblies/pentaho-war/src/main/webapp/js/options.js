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


function runInBackground( url, target )
{
	var response = confirm( "Info:  Reports that prompt for parameters are not supported with this feature."
		+ "\nRun in Background may generate content with incorrect results." );

	if ( response )
	{
		url = url + "&background=true";
		if ( target.toLowerCase().indexOf( 'new' ) >= 0 )
		{
			pho.util.xss.open( url );
		}
		else
		{
			pho.util.xss.setLocation(window, url);
		}
	}
	return undefined;	// forces current page to remain unchanged when target=new
}

function getOptions( solution, path, filename, target, isFolder, furl, properties, hasModifyAcl  ) {

	var actions = "";
	if( filename.indexOf( '.url' ) == (filename.length - 4) ) {
		actions += "<a href=\""+furl+"\" target=\""+target+"\">Run</a>";
		return actions;
	}

	var actions = "";
	if( !isFolder ) {
		var url = "ViewAction?solution=" + solution
		  + "&path=" + path
		  + "&action=" + filename;
		actions += "<a href=\""+url+"\" target=\""+target+"\">Run</a>&nbsp;|&nbsp;";
		actions += "<a href='javascript:runInBackground(\"" + url + "\", \"" + target + "\");'>Background</a>";

    actions += getShareOption( solution, path, filename, target, furl, hasModifyAcl );
		
		if( properties.indexOf( "subscribable=true" ) != -1 ) {		
			actions += "<br/><a href='"+url+"&subscribepage=yes' target='"+target+"'>Subscribe</a>";
		}
	}

	return actions;
};

/**
 * replace single quotes with the hexadecimal character reference &apos;
 * @param str String string to be encoded
 */
function encodeSingleQuote( str )
{
	return str.replace( /'/g, "&apos;" );
};

function getProtocolHostPortContextParts( strUrl )
{
	var matched = strUrl.match( /(.*)\/ViewAction.*/ );	// get everything before "/ViewAction ..."
	return matched[ 1 ];
}

// properties is often "subscribable=false" (or true)
function getAdminOptions( solution, path, filename, target, isFolder, furl, properties) {

	var permUrl = "PropertiesEditor?path=/" + gRepositoryName
    + ( !StringUtils.isEmpty( solution ) ? "/" + solution : "" ) 
    + ( !StringUtils.isEmpty( path ) ? "/" + path : "" ) 
    + ( !StringUtils.isEmpty( filename ) ? "/" + filename : "" )  ;
	var actions = "<br/><br/><a href=\""+permUrl+"\" >Permissions</a>"
	return actions;
}

function getShareOption( solution, path, filename, target, furl, hasModifyAcl )
{
  if( hasModifyAcl )
  {		
    var option = "&nbsp;|&nbsp;<a href='javascript:void(0)'"
      + "onclick='javascript:showShareDialog( event,\"" 
      + encodeSingleQuote( solution ) + "\", \""
      + encodeSingleQuote( path ) + "\", \""
      + encodeSingleQuote( filename )
      + "\");" + "'>Share</a>";
    return option;
  }
  else
  {
    return "";
  }
}

var shareDialog = null;
var shareDialogController = null;
function showShareDialog( event, solution, path, filename )
{
  if ( null == shareDialog )
  {
  	shareDialog = new AclEditorDialog( "aclDialog.dialogId", "modalMaskId" );
    shareDialogController = new AclEditorController( shareDialog );
  }
  shareDialogController.loadPage( solution, path, filename );
  var position = UIUtil.getScrollCoords( { left: event.clientX, top: event.clientY } );
  shareDialog.setPosition( { left: position.left+ "px", top: position.top + "px" } );
  shareDialog.show();
}
