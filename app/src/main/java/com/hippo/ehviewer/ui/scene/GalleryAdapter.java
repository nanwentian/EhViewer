/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.ui.scene;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.drawable.TriangleDrawable;
import com.hippo.easyrecyclerview.MarginItemDecoration;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.client.EhCacheKeyFactory;
import com.hippo.ehviewer.client.EhUtils;
import com.hippo.ehviewer.client.data.GalleryInfo;
import com.hippo.ehviewer.widget.TileThumb;
import com.hippo.util.ApiHelper;
import com.hippo.widget.recyclerview.AutoGridLayoutManager;
import com.hippo.widget.recyclerview.AutoStaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

abstract class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {

    @IntDef({TYPE_LIST, TYPE_GRID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_LIST = 0;
    public static final int TYPE_GRID = 1;

    private final LayoutInflater mInflater;
    private final Resources mResources;
    private final RecyclerView mRecyclerView;
    private final AutoStaggeredGridLayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration mGirdDecoration;
    private int mType = TYPE_INVALID;

    public GalleryAdapter(@NonNull LayoutInflater inflater, @NonNull Resources resources,
            @NonNull RecyclerView recyclerView, int type) {
        mInflater = inflater;
        mResources = resources;
        mRecyclerView = recyclerView;
        mLayoutManager = new AutoStaggeredGridLayoutManager(0, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setAdapter(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        int paddingH = mResources.getDimensionPixelOffset(R.dimen.list_content_margin_h);
        int paddingV = mResources.getDimensionPixelOffset(R.dimen.list_content_margin_v);
        mRecyclerView.setPadding(paddingV, paddingH, paddingV, paddingH);
        setType(type);
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        if (type == mType) {
            return;
        }
        mType = type;

        switch (type) {
            default:
            case GalleryAdapter.TYPE_LIST: {
                int columnWidth = mResources.getDimensionPixelOffset(R.dimen.gallery_list_column_width);
                mLayoutManager.setColumnSize(columnWidth);
                mLayoutManager.setStrategy(AutoGridLayoutManager.STRATEGY_MIN_SIZE);
                if (null != mGirdDecoration) {
                    mRecyclerView.removeItemDecoration(mGirdDecoration);
                }
                notifyDataSetChanged();
                break;
            }
            case GalleryAdapter.TYPE_GRID: {
                int columnWidth = mResources.getDimensionPixelOffset(R.dimen.gallery_grid_column_width);
                mLayoutManager.setColumnSize(columnWidth);
                mLayoutManager.setStrategy(AutoGridLayoutManager.STRATEGY_SUITABLE_SIZE);
                if (null == mGirdDecoration) {
                    mGirdDecoration = new MarginItemDecoration(
                            mResources.getDimensionPixelOffset(R.dimen.gallery_grid_margin) / 2);
                }
                mRecyclerView.addItemDecoration(mGirdDecoration);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        switch (viewType) {
            default:
            case TYPE_LIST:
                layoutId = R.layout.item_gallery_list;
                break;
            case TYPE_GRID:
                layoutId = R.layout.item_gallery_grid;
                break;
        }
        return new GalleryHolder(mInflater.inflate(layoutId, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return mType;
    }

    @Nullable
    public abstract GalleryInfo getDataAt(int position);

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onBindViewHolder(GalleryHolder holder, int position) {
        GalleryInfo gi = getDataAt(position);
        if (null == gi) {
            return;
        }

        switch (mType) {
            default:
            case TYPE_LIST: {
                holder.thumb.load(EhCacheKeyFactory.getThumbKey(gi.gid), gi.thumb);
                holder.title.setText(EhUtils.getSuitableTitle(gi));
                holder.uploader.setText(gi.uploader);
                holder.rating.setRating(gi.rating);
                TextView category = holder.category;
                String newCategoryText = EhUtils.getCategory(gi.category);
                if (!newCategoryText.equals(category.getText())) {
                    category.setText(newCategoryText);
                    category.setBackgroundColor(EhUtils.getCategoryColor(gi.category));
                }
                holder.posted.setText(gi.posted);
                holder.simpleLanguage.setText(gi.simpleLanguage);
                break;
            }
            case TYPE_GRID: {
                ((TileThumb) holder.thumb).setThumbSize(gi.thumbWidth, gi.thumbHeight);
                holder.thumb.load(EhCacheKeyFactory.getThumbKey(gi.gid), gi.thumb);
                View category = holder.category;
                Drawable drawable = category.getBackground();
                int color = EhUtils.getCategoryColor(gi.category);
                if (!(drawable instanceof TriangleDrawable)) {
                    drawable = new TriangleDrawable(color);
                    category.setBackground(drawable);
                } else {
                    ((TriangleDrawable) drawable).setColor(color);
                }
                holder.simpleLanguage.setText(gi.simpleLanguage);
                break;
            }
        }

        // Update transition name
        if (ApiHelper.SUPPORT_TRANSITION) {
            long gid = gi.gid;
            holder.thumb.setTransitionName(TransitionNameFactory.getThumbTransitionName(gid));
        }
    }
}
