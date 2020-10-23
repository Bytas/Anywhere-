package com.absinthe.anywhere_.services

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.JobIntentService
import com.absinthe.anywhere_.constants.AnywhereType
import com.absinthe.anywhere_.model.database.AnywhereEntity
import com.absinthe.anywhere_.model.viewholder.FlowStepBean
import com.absinthe.anywhere_.ui.editor.EXTRA_ENTITY
import com.absinthe.anywhere_.utils.NotifyUtils
import com.absinthe.anywhere_.utils.handler.Opener
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.NumberFormatException

class WorkflowIntentService : JobIntentService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        NotifyUtils.createWorkflowNotification(this)
    }

    override fun onHandleWork(intent: Intent) {
        NotifyUtils.createWorkflowNotification(this)

        val entity: AnywhereEntity? = intent.getParcelableExtra(EXTRA_ENTITY)

        entity?.let { ett ->
            val flowStepList: List<FlowStepBean>? = try {
                Gson().fromJson(ett.param1, object : TypeToken<List<FlowStepBean>>() {}.type)
            } catch (e: JsonSyntaxException) {
                null
            }

            flowStepList?.let { list ->
                list.forEach {
                    if (it.entity != null) {
                        val a11yDelay = if (it.entity!!.type == AnywhereType.Card.ACCESSIBILITY) {
                            try {
                                it.entity!!.param2.toInt()
                            } catch (e: NumberFormatException) {
                                0
                            }
                        } else {
                            0
                        }

                        handler.postDelayed({
                            Opener.with(this@WorkflowIntentService).load(it.entity!!).open()
                        }, it.delay + a11yDelay)
                    }
                }
            }
        }
    }

    companion object {

        private const val JOB_ID = 1

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, WorkflowIntentService::class.java, JOB_ID, work)
        }
    }
}