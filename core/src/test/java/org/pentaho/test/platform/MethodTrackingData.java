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


package org.pentaho.test.platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class used to collect method call information during unit testing. It can be used with mock objects to
 * track which methods have been invoked and can hold information about the parameters provided. </p> Example:
 * <code>
 *   class MockClass extends ClassUnderTest {
 *      private List<MethodTrackingData> methods = new List<MethodTrackingData>();
 * 
 *      public void method(String arg) {
 *        methods.add(new MethodTrackingData("method").addParameter("arg", arg));
 *        ...
 *      }
 *   }
 * </code>
 */
public class MethodTrackingData {
  /**
   * The method that was invoked
   */
  private String methodName;

  /**
   * The parameters that were provided at the time of method invocation
   */
  private Map<String, Object> parameters = new HashMap<String, Object>();

  /**
   * Creates a new instance to track a method call
   * 
   * @param methodName
   *          the name of the method that was invoked
   */
  public MethodTrackingData( final String methodName ) {
    this.methodName = methodName;
  }

  /**
   * Adds parameter information about the method call. This method should be used once per parameter or other piece
   * of information that should be tracked.
   * 
   * @param paramName
   *          the name of the parameter at method invocation
   * @param paramValue
   *          the value of the parameter at method invocation
   * @return {@code this} so that the methods can be easily chained
   */
  public MethodTrackingData addParameter( final String paramName, final Object paramValue ) {
    parameters.put( paramName, paramValue );
    return this;
  }

  /**
   * @return the name of the method that was invoked
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return the map which contains the information about the parameters that were provided when the method was
   *         invoked
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }
}
