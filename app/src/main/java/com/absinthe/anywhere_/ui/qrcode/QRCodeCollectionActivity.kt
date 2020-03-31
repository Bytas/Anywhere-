package com.absinthe.anywhere_.ui.qrcode

import androidx.recyclerview.widget.RecyclerView
import com.absinthe.anywhere_.BaseActivity
import com.absinthe.anywhere_.adapter.card.QRCollectionAdapter
import com.absinthe.anywhere_.adapter.manager.WrapContentStaggeredGridLayoutManager
import com.absinthe.anywhere_.databinding.ActivityQrcodeCollectionBinding
import com.absinthe.anywhere_.databinding.CardQrCollectionTipBinding
import com.absinthe.anywhere_.model.OnceTag
import com.absinthe.anywhere_.model.QRCollection
import jonathanfinerty.once.Once
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QRCodeCollectionActivity : BaseActivity() {

    private lateinit var binding: ActivityQrcodeCollectionBinding
    private var mAdapter: QRCollectionAdapter = QRCollectionAdapter(this)

    init {
        isPaddingToolbar = true
    }

    override fun setViewBinding() {
        binding = ActivityQrcodeCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setToolbar() {
        mToolbar = binding.toolbar.toolbar
    }

    override fun initView() {
        super.initView()

        if (!Once.beenDone(Once.THIS_APP_INSTALL, OnceTag.QR_COLLECTION_TIP)) {
            val tipBinding = CardQrCollectionTipBinding.inflate(
                    layoutInflater, binding.llContainer, false)

            binding.llContainer.addView(tipBinding.root, 0)
            tipBinding.btnOk.setOnClickListener {
                binding.llContainer.removeView(tipBinding.root)
                Once.markDone(OnceTag.QR_COLLECTION_TIP)
            }
        }
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.layoutManager = WrapContentStaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        binding.srlQrCollection.isRefreshing = true

        GlobalScope.launch(Dispatchers.Main) {
            val collection = QRCollection.Singleton.INSTANCE.instance
            mAdapter.setItems(collection.list)

            binding.srlQrCollection.isRefreshing = false
            binding.srlQrCollection.isEnabled = false
        }
    }
}