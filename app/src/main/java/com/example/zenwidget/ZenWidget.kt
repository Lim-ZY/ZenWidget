package com.example.zenwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import com.example.zenwidget.data.Repository
import com.example.zenwidget.layout.LongTextLayout
import com.example.zenwidget.layout.LongTextLayoutData
import com.example.zenwidget.utils.ActionUtils.actionStartDemoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ZenWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val repo = Repository.getRepo(id, context)

        val initialData = withContext(Dispatchers.Default) {
            repo.loadFromDb()
        }

        provideContent {
            val data by repo.data().collectAsState(initial = initialData)

            GlanceTheme {
                LongTextAppWidgetContent(
                    data = data,
                    refreshDataAction = actionRunCallback<RefreshActionCallback>(),
                    switchRepoAction = actionRunCallback<SwitchRepoActionCallback>()
                )
            }
        }
    }

    @Composable
    fun LongTextAppWidgetContent(
        data: LongTextLayoutData,
        refreshDataAction: Action,
        switchRepoAction: Action,
    ) {
        val context = LocalContext.current

        LongTextLayout(
            title = "ZenWidget",
            titleIconRes = R.drawable.sample_text_icon,
            titleBarActionIconRes = R.drawable.sample_refresh_icon,
            titleBarActionIconContentDescription = context.getString(
                R.string.sample_refresh_icon_button_label
            ),
            titleBarAction = refreshDataAction,
            switchRepoIconRes = R.drawable.sample_arrow_right_icon,
            switchRepoIconContentDescription = "Switch Repository",
            switchRepoAction = switchRepoAction,
            data = data,
            action = actionStartDemoActivity(data.key),
        )
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val repo = Repository.getRepo(glanceId, context)
        repo.refresh()
        ZenWidget().update(context, glanceId)
    }
}

class SwitchRepoActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val repo = Repository.getRepo(glanceId, context)
        repo.switchRepo()
        ZenWidget().update(context, glanceId)
    }
}

class ZenReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZenWidget()
}
