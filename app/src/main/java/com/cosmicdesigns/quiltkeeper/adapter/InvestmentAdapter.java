package com.cosmicdesigns.quiltkeeper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.model.InvestmentModel;

import java.util.ArrayList;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.InvestmentViewHolder>{

    private static final String TAG = "InvestmentAdapter";

    private Context mContext;
    private ArrayList<InvestmentModel> investmentModelArrayList;

    public InvestmentAdapter(Context mContext, ArrayList<InvestmentModel> investmentModelArrayList) {
        this.mContext = mContext;
        this.investmentModelArrayList = investmentModelArrayList;
    }

    @NonNull
    @Override
    public InvestmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_investment_item, parent, false);
        return new InvestmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvestmentViewHolder holder, int position) {
        holder.date_selected.setText(investmentModelArrayList.get(position).getDate_selected());
        holder.description.setText(investmentModelArrayList.get(position).getInvestment_description());
        holder.cost.setText(investmentModelArrayList.get(position).getInvestment_cost());
//        if (position %2 == 1){
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
//        }else{
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
//        }
    }

    @Override
    public int getItemCount() {
        return investmentModelArrayList.size();
    }

    class InvestmentViewHolder extends RecyclerView.ViewHolder {

        private TextView date_selected, description, cost;

        public InvestmentViewHolder(@NonNull View itemView) {
            super(itemView);
            date_selected = itemView.findViewById(R.id.item_date);
            description = itemView.findViewById(R.id.item_description);
            cost = itemView.findViewById(R.id.item_investment);
        }

    }
}
