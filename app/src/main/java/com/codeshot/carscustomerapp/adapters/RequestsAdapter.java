package com.codeshot.carscustomerapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codeshot.carscustomerapp.Common.Common;
import com.codeshot.carscustomerapp.Models.Request;
import com.codeshot.carscustomerapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class RequestsAdapter extends FirebaseRecyclerAdapter<Request,RequestsAdapter.RequestItem> {
    private Context context;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RequestsAdapter(@NonNull FirebaseRecyclerOptions<Request> options,Context context) {
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final RequestItem requestItem, int i, @NonNull Request request) {
        String requestKey=getRef(i).getKey();
        FirebaseDatabase.getInstance()
                .getReference(Common.drivers_tbl).child(request.getTo())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String driverName =dataSnapshot.child("userName").getValue().toString();
                            requestItem.tvRequestName.setText(driverName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DRIVERNAME",databaseError.getMessage());
                    }
                });
        if (request.getTime()!=null)
            requestItem.tvRequestTime.setText(request.getTime());
        if (request.getDate()!=null)
            requestItem.tvRequestDate.setText(request.getDate());

    }



    @NonNull
    @Override
    public RequestItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.item_request,
                parent,false);
        return new RequestItem(view);
    }

    public class RequestItem extends RecyclerView.ViewHolder {
        private TextView tvRequestName,tvRequestTime,tvRequestDate;
        public RequestItem(@NonNull View itemView) {
            super(itemView);
            tvRequestName=itemView.findViewById(R.id.tvRequestName);
            tvRequestTime=itemView.findViewById(R.id.tvRequestTime);
            tvRequestDate=itemView.findViewById(R.id.tvRequestDate);

        }
    }
}
