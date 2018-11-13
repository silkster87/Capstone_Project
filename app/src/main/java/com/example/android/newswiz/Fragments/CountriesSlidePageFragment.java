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


public class CountriesSlidePageFragment extends Fragment {

    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClickCountries(String item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnItemClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_countries_slide_page, container, false);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.recyclerView_countries);
        int numberOfColumns = Integer.parseInt(getContext().getResources().getString(R.string.no_of_cols));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        mRecyclerView.setHasFixedSize(true);

        CountriesAdapter mAdapter = new CountriesAdapter();
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    private class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.CountriesAdapterViewHolder>{

        private SourcesInfo mSourcesInfo;

        public CountriesAdapter() {
            this.mSourcesInfo = MainActivity.getSourcesInfo();
        }

        @NonNull
        @Override
        public CountriesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            int layoutIdForItem = R.layout.countries_item;
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(layoutIdForItem, parent, false);
            return new CountriesAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CountriesAdapterViewHolder holder, int position) {
            holder.mCountryImageView.setImageResource(mThumbIds[position]);
            holder.mCountryTextView.setText(mCountries[position]);
            holder.bind(mCountries[position], listener);
            if(mSourcesInfo.getmCountriesSelected().contains(mCountries[position])){
                holder.mCountryConstraintLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                holder.mCountryConstraintLayout.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public int getItemCount() {
            return mThumbIds.length;
        }

        public class CountriesAdapterViewHolder extends RecyclerView.ViewHolder {

            private final ConstraintLayout mCountryConstraintLayout;
            private final ImageView mCountryImageView;
            private final TextView mCountryTextView;

            public CountriesAdapterViewHolder(View itemView) {
                super(itemView);

                mCountryConstraintLayout = itemView.findViewById(R.id.constraint_layout_countries_item);
                mCountryImageView = itemView.findViewById(R.id.countries_icon);
                mCountryTextView = itemView.findViewById(R.id.countries_desc);
            }

            public void bind(final String mCountry, final OnItemClickListener clickListener) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickListener.onItemClickCountries(mCountry);
                        mSourcesInfo = MainActivity.getSourcesInfo(); //The SourcesInfo would have changed now from MainActivity
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }


        private Integer[] mThumbIds = {R.drawable.argentina, R.drawable.australia, R.drawable.austria, R.drawable.belgium,
                R.drawable.brazil, R.drawable.bulgaria, R.drawable.canada, R.drawable.china, R.drawable.colombia,
                R.drawable.cuba, R.drawable.czech_republic, R.drawable.egypt, R.drawable.france, R.drawable.germany,
                R.drawable.greece, R.drawable.hong_kong, R.drawable.hungary, R.drawable.india, R.drawable.indonesia,
                R.drawable.ireland, R.drawable.israel, R.drawable.italy, R.drawable.japan, R.drawable.latvia,
                R.drawable.lithuania, R.drawable.malaysia, R.drawable.mexico, R.drawable.morocco,
                R.drawable.netherlands, R.drawable.new_zealand, R.drawable.nigeria, R.drawable.norway, R.drawable.philippines,
                R.drawable.poland, R.drawable.portugal, R.drawable.romania, R.drawable.russia,
                R.drawable.saudi_arabia, R.drawable.serbia, R.drawable.singapore, R.drawable.slovakia, R.drawable.slovenia,
                R.drawable.southafrica, R.drawable.southkorea, R.drawable.sweden, R.drawable.switzerland,
                R.drawable.taiwan, R.drawable.thailand, R.drawable.turkey, R.drawable.ukraine,
                R.drawable.united_arab_emirates, R.drawable.united_kingdom, R.drawable.united_states, R.drawable.venezuela};

        private String[] mCountries = {"Argentina", "Australia", "Austria", "Belgium", "Brazil", "Bulgaria", "Canada", "China",
                "Colombia", "Cuba", "Czech Rep.", "Egypt", "France", "Germany", "Greece", "Hong Kong", "Hungary", "India",
                "Indonesia", "Ireland", "Israel", "Italy", "Japan", "Latvia", "Lithuania", "Malaysia", "Mexico", "Morocco",
                "Netherlands", "New Zealand", "Nigeria", "Norway", "Philippines",
                "Poland", "Portugal", "Romania", "Russia", "Saudi Arabia", "Serbia", "Singapore", "Slovakia", "Slovenia",
                "South Africa", "South Korea", "Sweden", "Switzerland", "Taiwan", "Thailand", "Turkey", "Ukraine",
                "U.A.E", "U.K", "U.S.A", "Venezuela"};



}
