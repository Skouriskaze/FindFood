package caden.foodapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CampusChooser extends AppCompatActivity {

    List<Campus> mCampuses;
    CampusArrayAdapter mAdapter;

    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_chooser);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/economica.otf");
        Typeface tf_i = Typeface.createFromAsset(getAssets(), "fonts/economica_italic.otf");
        TextView tv1 = (TextView) findViewById(R.id.appLbl);
        TextView tv2 = (TextView) findViewById(R.id.ffd);
        tv1.setTypeface(tf);
        tv2.setTypeface(tf_i);

        mCampuses = new ArrayList<>();
//        mCampuses.add(new Campus("Harvard University", "Cambridge, MA 02138"));
//        mCampuses.add(new Campus("Massachusetts Institute of Technology", "77 Massachusetts Ave, Cambridge, MA 02139"));
//        mCampuses.add(new Campus("Georgia Institute of Technology", "North Ave NW, Atlanta, GA 30332"));
        mAdapter = new CampusArrayAdapter(this, mCampuses);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Campuses");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataChangeHandler(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Cancelled Connection");
            }
        });

        ListView lvCampuses = (ListView) findViewById(R.id.lvCampuses);
        lvCampuses.setAdapter(mAdapter);
        final Context ctx = this;
        lvCampuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Campus campus= mCampuses.get(position);
                Intent intent = new Intent(ctx, Map.class);

                intent.putExtra("Name", campus.getName());
                intent.putExtra("Address", campus.getAddress());

                startActivity(intent);
            }
        });
    }

    public void dataChangeHandler(DataSnapshot dataSnapshot) {
        for (DataSnapshot d : dataSnapshot.getChildren()) {
            Campus c = d.getValue(Campus.class);
            mCampuses.add(c);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void addCampus(View view) {
        LayoutInflater lf = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View acView = lf.inflate(R.layout.add_campus_layout, null, false);
        final AutoCompleteTextView cName = (AutoCompleteTextView) acView.findViewById(R.id.cname);
        final AutoCompleteTextView cAddr = (AutoCompleteTextView) acView.findViewById(R.id.caddr);

        new AlertDialog.Builder(this).setView(acView)
                .setTitle("Add New Campus")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        TODO ADD TO LIST VIEW
                        Campus curr = new Campus(cName.getText().toString(), cAddr.getText().toString());
                        ref.child(cName.getText().toString()).setValue(curr);
                        mCampuses.add(curr);
                        mAdapter.notifyDataSetChanged();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    public class CampusArrayAdapter extends ArrayAdapter<Campus> {

        public CampusArrayAdapter(Context context, List<Campus> campuses) {
            super(context, -1, campuses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Campus curr = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_campus, parent, false);
            }

            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/tenor.ttf");

            TextView tvName = (TextView) convertView.findViewById(R.id.tvCampusName);
            TextView tvAddress = (TextView) convertView.findViewById(R.id.tvCampusAddress);

            tvName.setTypeface(tf);
            tvAddress.setTypeface(tf);

            tvName.setText(curr.getName());
            tvAddress.setText(curr.getAddress());

            return convertView;
        }
    }
}
