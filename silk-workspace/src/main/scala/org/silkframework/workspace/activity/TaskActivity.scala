package org.silkframework.workspace.activity

import org.silkframework.runtime.activity.{Activity, ActivityControl}
import org.silkframework.runtime.plugin.PluginDescription
import org.silkframework.workspace.Task

import scala.reflect.ClassTag

/**
  * Holds an activity that is part of an task.
  *
  * @param task The task this activity belongs to.
  * @param initialFactory The initial activity factory for generating the activity.
  * @tparam DataType The type of the task.
  */
class TaskActivity[DataType: ClassTag](val task: Task[DataType], initialFactory: TaskActivityFactory[DataType, _, _]) {

  @volatile
  private var currentControl: ActivityControl[_] = Activity(initialFactory(task))

  @volatile
  private var currentFactory = initialFactory

  def name = currentControl.name

  def autoRun = currentFactory.autoRun

  def control = currentControl

  def factory = currentFactory

  def config: Map[String, String] = PluginDescription(currentFactory.getClass).parameterValues(currentFactory)

  def update(config: Map[String, String]) = {
    currentFactory = PluginDescription(currentFactory.getClass)(config)
    currentControl = Activity(currentFactory(task))
  }

  def activityType = currentFactory.activityType
}