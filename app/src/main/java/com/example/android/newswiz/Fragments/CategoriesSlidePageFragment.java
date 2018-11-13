package com.example.android.newswiz.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.newswiz.MainActivity;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Sources.SourcesInfo;

import me.grantland.widget.AutofitTextView;

public class CategoriesSlidePageFragment extends Fragment {

    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClickCategories(String item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnItemClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_categories_slide_page,
                container, false);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.recyclerView_categories);
        int numberOfColumns = Integer.parseInt(getContext().getResources().getString(R.string.no_of_cols));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        mRecyclerView.setHasFixedSize(true);

        CategoriesAdapter mAdapter = new CategoriesAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

   private class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesAdapterViewHolder>{

        private SourcesInfo mSourcesInfo;

       public CategoriesAdapter() {
           this.mSourcesInfo = MainActivity.getSourcesInfo();
       }

       @NonNull
       @Override
       public CategoriesAdapter.CategoriesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           Context context = parent.getContext();
           int layoutIdForItem = R.layout.categories_item;
           LayoutInflater inflater = LayoutInflater.from(context);
           View view = inflater.inflate(layoutIdForItem, parent, false);
           return new CategoriesAdapterViewHolder(view);
       }

       @Override
       public void onBindViewHolder(@NonNull CategoriesAdapter.CategoriesAdapterViewHolder holder, int position) {
                holder.mCategoryImageView.setImageResource(mThumbIds[position]);
                holder.mCategoryTextView.setText(mCategories[position]);
                holder.bind(mCategories[position], listener);
                if(mSourcesInfo.getmCategoriesSelected().contains(mCategories[position])){
                    holder.mCategoryConstraintLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    holder.mCategoryConstraintLayout.setBackgroundColor(Color.TRANSPARENT);
                }
       }

       @Override
       public int getItemCount() {
           return mThumbIds.length;
       }

       public class CategoriesAdapterViewHolder extends RecyclerView.ViewHolder {

           private final ConstraintLayout mCategoryConstraintLayout;
           private final ImageView mCategoryImageView;
           private final AutofitTextView mCategoryTextView;

           private CategoriesAdapterViewHolder(View itemView) {
               super(itemView);
               mCategoryConstraintLayout = itemView.findViewById(R.id.constraint_layout_categories_item);
               mCategoryImageView = itemView.findViewById(R.id.categories_icon);
               mCategoryTextView = itemView.findViewById(R.id.categories_desc);
           }

           public void bind(final String mCategory, final OnItemClickListener clickListener) {
               itemView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       clickListener.onItemClickCategories(mCategory);
                       mSourcesInfo = MainActivity.getSourcesInfo(); //The SourcesInfo would have changed now from MainActivity
                       notifyDataSetChanged();
                   }
               });
           }
       }
   }


    // reference to the images

    private Integer[] mThumbIds = {R.drawable.business, R.drawable.entertainment, R.drawable.health, R.drawable.science,
            R.drawable.sports, R.drawable.technology};

    private String[] mCategories = {"Business", "Entertainment", "Health", "Science",
                "Sports", "Technology"};
}
