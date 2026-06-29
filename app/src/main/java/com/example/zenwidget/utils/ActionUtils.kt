package com.example.zenwidget.utils

import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity

/**
 * Utility functions for creating [Action]s.
 */
object ActionUtils {
  /**
   * [Action] for launching the [ActionDemonstrationActivity] with the given message.
   */
  fun actionStartDemoActivity(message: String) =
    actionStartActivity<ActionDemonstrationActivity>(
      actionParametersOf(
        ActionSourceMessageKey to message
      )
    )
}