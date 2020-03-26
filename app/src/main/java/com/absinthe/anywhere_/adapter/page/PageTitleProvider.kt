package com.absinthe.anywhere_.adapter.page

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import com.absinthe.anywhere_.AnywhereApplication
import com.absinthe.anywhere_.R
import com.absinthe.anywhere_.model.GlobalValues
import com.absinthe.anywhere_.model.PageEntity
import com.absinthe.anywhere_.utils.manager.ActivityStackManager
import com.absinthe.anywhere_.utils.manager.DialogManager
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class PageTitleProvider : BaseNodeProvider() {

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_page_title

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val node = data as PageTitleNode

        helper.setText(R.id.tv_title, node.title)
        val ivArrow = helper.getView<ImageView>(R.id.iv_arrow)
        if (node.isExpanded) {
            onExpansionToggled(ivArrow, true)
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        if (isEditMode) {
            return
        }
        getAdapter()?.expandOrCollapse(position)
        val node = data as PageTitleNode
        val ivArrow = helper.getView<ImageView>(R.id.iv_arrow)

        if (node.isExpanded) {
            onExpansionToggled(ivArrow, true)
        } else {
            onExpansionToggled(ivArrow, false)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onLongClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int): Boolean {
        if (isEditMode) {
            return false
        }
        val popup = PopupMenu(context, view)
        popup.menuInflater
                .inflate(R.menu.page_menu, popup.menu)
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
        }

        val node = data as PageTitleNode
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.rename_page -> DialogManager.showRenameDialog(ActivityStackManager.getInstance().topActivity, node.title)
                R.id.delete_page -> DialogManager.showDeletePageDialog(context, node.title, DialogInterface.OnClickListener { _, _ ->
                    getPageEntity(node.title)?.let { AnywhereApplication.sRepository.deletePage(it) }
                    val list = AnywhereApplication.sRepository.allPageEntities?.value

                    if (list != null) {
                        val title = list[0].title
                        val anywhereEntities = AnywhereApplication.sRepository.allAnywhereEntities?.value

                        if (anywhereEntities != null) {
                            for (ae in anywhereEntities) {
                                if (ae.category == node.title) {
                                    ae.category = title
                                    AnywhereApplication.sRepository.update(ae)
                                }
                            }
                        }
                        GlobalValues.setsCategory(title, 0)
                    }
                }, false)
                R.id.delete_page_and_item -> DialogManager.showDeletePageDialog(context, node.title, DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                    getPageEntity(node.title)?.let { AnywhereApplication.sRepository.deletePage(it) }
                    val list = AnywhereApplication.sRepository.allPageEntities?.value

                    if (list != null) {
                        val anywhereEntities = AnywhereApplication.sRepository.allAnywhereEntities?.value

                        if (anywhereEntities != null) {
                            for (ae in anywhereEntities) {
                                if (ae.category == node.title) {
                                    AnywhereApplication.sRepository.delete(ae)
                                }
                            }
                        }
                        GlobalValues.setsCategory(list[0].title, 0)
                    }
                }, true)
                else -> {
                }
            }
            true
        }
        popup.show()
        return super.onLongClick(helper, view, data, position)
    }

    private fun onExpansionToggled(arrow: ImageView, expanded: Boolean) {
        val start: Float
        val target: Float
        if (expanded) {
            start = 0f
            target = 90f
        } else {
            start = 90f
            target = 0f
        }
        val objectAnimator = ObjectAnimator.ofFloat(arrow, View.ROTATION, start, target)
        objectAnimator.duration = 200
        objectAnimator.start()
    }

    companion object {
        @JvmField
        var isEditMode = false

        private fun getPageEntity(title: String): PageEntity? {
            val list = AnywhereApplication.sRepository.allPageEntities?.value
            if (list != null) {
                for (pe in list) {
                    if (pe.title == title) {
                        return pe
                    }
                }
            }
            return null
        }

        @JvmStatic
        fun renameTitle(oldTitle: String, newTitle: String?) {
            val pe = getPageEntity(oldTitle)
            val list = AnywhereApplication.sRepository.allAnywhereEntities?.value
            if (list != null && pe != null) {
                for (ae in list) {
                    if (ae.category == pe.title) {
                        ae.category = newTitle!!
                        AnywhereApplication.sRepository.update(ae)
                    }
                }
                AnywhereApplication.sRepository.deletePage(pe)
                pe.title = newTitle!!
                AnywhereApplication.sRepository.insertPage(pe)
                GlobalValues.setsCategory(newTitle)
            }
        }
    }
}