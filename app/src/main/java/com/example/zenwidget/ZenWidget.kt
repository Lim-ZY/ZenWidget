package com.example.zenwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.layout.Column
import androidx.glance.layout.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import com.example.zenwidget.data.Repository
import com.example.zenwidget.layout.LongTextLayout
import com.example.zenwidget.layout.LongTextLayoutData
import com.example.zenwidget.utils.ActionUtils.actionStartDemoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ZenWidget : GlanceAppWidget() {
    companion object {
        val countKey = intPreferencesKey("count")
    }

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val repo = Repository.getRepo(id)

        val initialData = withContext(Dispatchers.Default) {
            repo.load()
        }

        provideContent {
            val data by repo.data().collectAsState(initial = initialData)

            GlanceTheme {
                LongTextAppWidgetContent(
                    data = data,
                    refreshDataAction = { repo.refresh() }
                )
            }
//            ZenContent()
        }
    }

//    @Composable
//    private fun ZenContent() {
//        val count = currentState(key = countKey) ?: 0
//        Column(
//            modifier = GlanceModifier
//                .fillMaxSize()
//                .background(Color.DarkGray),
//            verticalAlignment = Alignment.Vertical.CenterVertically,
//            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
//        ) {
//            Text(
//                text = count.toString(),
//                style = TextStyle(
//                    fontWeight = FontWeight.Medium,
//                    color = ColorProvider(Color.White, Color.White),
//                    fontSize = 26.sp
//                )
//            )
//            Button(
//                text = "^",
//                onClick = actionRunCallback(IncrementActionCallback::class.java)
//            )
//        }
//    }
    @Composable
    fun LongTextAppWidgetContent(
        data: LongTextLayoutData,
        refreshDataAction: () -> Unit,
    ) {
        val context = LocalContext.current

        LongTextLayout(
            title = context.getString(R.string.sample_long_text_app_widget_name),
            titleIconRes = R.drawable.sample_text_icon,
            titleBarActionIconRes = R.drawable.sample_refresh_icon,
            titleBarActionIconContentDescription = context.getString(
                R.string.sample_refresh_icon_button_label
            ),
            titleBarAction = refreshDataAction,
            data = data,
            action = actionStartDemoActivity(data.key),
        )
    }
}

class ZenReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZenWidget()
}
//
//object IncrementActionCallback: ActionCallback {
//    override suspend fun onAction(
//        context: Context,
//        glanceId: GlanceId,
//        parameters: ActionParameters
//    ) {
//        updateAppWidgetState(context, glanceId) { prefs ->
//            val currentCount = prefs[ZenWidget.countKey]
//            if (currentCount != null) {
//                prefs[ZenWidget.countKey] = currentCount + 1
//            } else {
//                prefs[ZenWidget.countKey] = 1
//            }
//        }
//        ZenWidget().update(context, glanceId)
//    }
//}
