package org.silkframework.runtime.plugin;

import java.lang.annotation.*;

/**
 * Annotation for configuration parameters of [[Plugin]] classes.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

  /**
   * The description for the annotated parameter.
   */
  String value();

  /**
   * Example value for the annotated parameter.
   */
  String example() default "";

  /**
   * True, if this is an advanced parameter that should not be shown by default.
   */
  boolean advanced() default false;
}
