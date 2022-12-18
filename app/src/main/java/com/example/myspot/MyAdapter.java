package com.example.myspot;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    //private String[] mDataset;
    //private ArrayList<String> list;
    private ArrayList<String> titlesList=new ArrayList<>();
    private ArrayList<String> bodiesList=new ArrayList<>();
    private ArrayList<String> timesList=new ArrayList<>();
    private Context context;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView body;
        private TextView time;
        RelativeLayout parentLayout;

        public MyViewHolder(View view) {
            super(view);
            title=view.findViewById(R.id.title);
            body=view.findViewById(R.id.body);
            time=view.findViewById(R.id.time);
            parentLayout=view.findViewById(R.id.parent_layout);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context1,ArrayList<String> titles,ArrayList<String> bodies,ArrayList<String> times) {
        context=context1;
        titlesList=titles;
        bodiesList=bodies;
        timesList=times;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        // create a new view
        View v=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.d(TAG,"onBindViewHolder: called.");

        holder.title.setText(titlesList.get(position));
        holder.body.setText(bodiesList.get(position));
        holder.time.setText(timesList.get(position));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: clicked on: "+timesList.get(position));
                //Toast.makeText(context,timesList.get(position),Toast.LENGTH_SHORT).show();
            }
        });
        //holder.textView.setText(mDataset[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return titlesList.size();
    }
}