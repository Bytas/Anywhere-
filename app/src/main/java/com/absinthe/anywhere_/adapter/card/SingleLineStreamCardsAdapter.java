package com.absinthe.anywhere_.adapter.card;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.absinthe.anywhere_.R;
import com.absinthe.anywhere_.adapter.BaseAdapter;
import com.absinthe.anywhere_.adapter.ItemTouchCallBack;
import com.absinthe.anywhere_.databinding.ItemStreamCardSingleLineBinding;
import com.absinthe.anywhere_.model.AnywhereEntity;
import com.absinthe.anywhere_.model.AnywhereType;
import com.absinthe.anywhere_.model.Const;
import com.absinthe.anywhere_.model.GlobalValues;
import com.absinthe.anywhere_.utils.manager.Logger;
import com.absinthe.anywhere_.utils.UiUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.catchingnow.icebox.sdk_client.IceBox;

import java.util.ArrayList;

public class SingleLineStreamCardsAdapter extends BaseAdapter<SingleLineStreamCardsAdapter.ItemViewHolder> implements ItemTouchCallBack.OnItemTouchListener {

    public SingleLineStreamCardsAdapter(Context context) {
        super(context);
        this.mContext = context;
        this.mItems = new ArrayList<>();
        this.mode = ADAPTER_MODE_NORMAL;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemStreamCardSingleLineBinding binding = ItemStreamCardSingleLineBinding.inflate(inflater, parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        AnywhereEntity item = mItems.get(position);
        viewHolder.bind(item);

    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private ItemStreamCardSingleLineBinding binding;

        ItemViewHolder(ItemStreamCardSingleLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AnywhereEntity item) {
            binding.executePendingBindings();

            String pkgName;
            if (item.getAnywhereType() == AnywhereType.URL_SCHEME) {
                pkgName = item.getParam2();
            } else {
                pkgName = item.getParam1();
            }
            try {
                if (IceBox.getAppEnabledSetting(mContext, pkgName) != 0) {
                    binding.setAppName(item.getAppName() + "\u2744");
                } else {
                    binding.setAppName(item.getAppName());
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Logger.e(e.getMessage());
                binding.setAppName(item.getAppName());
            }

            Glide.with(mContext)
                    .load(UiUtils.getAppIconByPackageName(mContext, item))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.ivAppIcon);
            if (GlobalValues.sCardBackgroundMode.equals(Const.CARD_BG_MODE_PURE)) {
                UiUtils.setCardUseIconColor(binding.ivCardBg,
                        UiUtils.getAppIconByPackageName(mContext, item),
                        color -> {
                            if (color != 0) {
                                binding.tvAppName.setTextColor(UiUtils.isLightColor(color) ? Color.BLACK : Color.WHITE);
                            }
                        });
            } else if (GlobalValues.sCardBackgroundMode.equals(Const.CARD_BG_MODE_GRADIENT)) {
                UiUtils.setCardUseIconColor(binding.ivCardBg, UiUtils.getAppIconByPackageName(mContext, item));
            }

            if (item.getShortcutType() == AnywhereType.SHORTCUTS) {
                binding.ivBadge.setImageResource(R.drawable.ic_add_shortcut);
                binding.ivBadge.setColorFilter(mContext.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                binding.ivBadge.setVisibility(View.VISIBLE);
            } else if (item.getExportedType() == AnywhereType.EXPORTED) {
                binding.ivBadge.setImageResource(R.drawable.ic_exported);
                binding.ivBadge.setColorFilter(mContext.getResources().getColor(R.color.exported_tint), PorterDuff.Mode.SRC_IN);
                binding.ivBadge.setVisibility(View.VISIBLE);
            } else {
                binding.ivBadge.setVisibility(View.GONE);
            }

        }

    }

}