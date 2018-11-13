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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.newswiz.MainActivity;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Sources.SourcesInfo;


public class PublishersSlidePageFragment extends Fragment {

    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClickPublishers(String item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnItemClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_publishers_slide_page,
                container, false);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.recyclerView_publishers);
        //int numberOfColumns = Integer.parseInt(getContext().getResources().getString(R.string.no_of_cols));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), getNumberofColumns()));
        mRecyclerView.setHasFixedSize(true);

        PublishersAdapter mAdapter = new PublishersAdapter();
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    private int getNumberofColumns(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if(nColumns < 2) return 2;
        return nColumns;
    }


    public  class PublishersAdapter extends RecyclerView.Adapter<PublishersAdapter.PublishersAdapterViewHolder>{

        private SourcesInfo mSourcesInfo;

        PublishersAdapter(){
            this.mSourcesInfo = MainActivity.getSourcesInfo();

        }

        @NonNull
        @Override
        public PublishersAdapter.PublishersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.publisher_item, parent, false);
            return new PublishersAdapterViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull PublishersAdapter.PublishersAdapterViewHolder holder, int position) {
                holder.mPublisherImageView.setImageResource(mThumbIds[position]);
                holder.mPublisherTextView.setText(mPublishers[position]);
                holder.bind(mPublishers[position], listener, position);
                if(mSourcesInfo.getmPublishersSelected().contains(mPublishers[position])){
                    //source has been selected
                    holder.mPublisherConstraintLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    holder.mPublisherConstraintLayout.setBackgroundColor(Color.TRANSPARENT);
                }
        }

        @Override
        public int getItemCount() {
            return mThumbIds.length;
        }

        public class PublishersAdapterViewHolder extends RecyclerView.ViewHolder {

            private final ConstraintLayout mPublisherConstraintLayout;
            private final ImageView mPublisherImageView;
            private final TextView mPublisherTextView;

            public PublishersAdapterViewHolder(View itemView) {
                super(itemView);
                mPublisherConstraintLayout = itemView.findViewById(R.id.constraint_layout_publisher_item);
                mPublisherImageView = itemView.findViewById(R.id.publisher_icon);
                mPublisherTextView = itemView.findViewById(R.id.publisher_desc);
            }

            public void bind(final String mPublisher, final OnItemClickListener clickListener, final int position) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickListener.onItemClickPublishers(mPublisher);
                        mSourcesInfo = MainActivity.getSourcesInfo(); //The SourcesInfo would have changed now from MainActivity
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

        // references to the images
        private Integer[] mThumbIds = {
                R.drawable.abc_news, R.drawable.abc_news_au,
                R.drawable.aftenposten, R.drawable.aljazeera_english,
                R.drawable.bbc, R.drawable.cbs_news,
                R.drawable.cnn_news, R.drawable.entertainment_weekly,
                R.drawable.espn, R.drawable.financial_post,
                R.drawable.financial_times, R.drawable.fox_news,
                R.drawable.fox_sports, R.drawable.ign,
                R.drawable.independent, R.drawable.lequipe,
                R.drawable.metro, R.drawable.msnbc,
                R.drawable.mtvnews, R.drawable.nat_geo,
                R.drawable.nbc_news, R.drawable.new_scientist,
                R.drawable.new_york_magazine, R.drawable.talk_sport,
                R.drawable.techradar, R.drawable.the_guardian,
                R.drawable.the_nyt, R.drawable.wsj
        };


        private String[] mPublishers = {"ABC News", "ABC News (AU)", "Aftenposten", "AlJazeera (ENG)"
                , "BBC", "CBS News", "CNN News", "Entertainment Weekly", "ESPN", "Financial Post",
                "Financial Times", "Fox News", "Fox Sports", "IGN", "Independent", "L'Equipe",
                "Metro", "MSNBC", "MTV News", "Nat. Geo.", "NBC News", "New Scientist",
                "NY Magazine", "Talk Sport", "TechRadar", "The Guardian", "NYT",
                "Wall Street Journal"};
    }

