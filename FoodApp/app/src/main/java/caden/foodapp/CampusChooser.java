package caden.foodapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CampusChooser extends AppCompatActivity {

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

        final List<Campus> campuses = new ArrayList<>();
        campuses.add(new Campus("Harvard University", "Cambridge, MA 02138"));
        campuses.add(new Campus("Massachusetts Institute of Technology", "77 Massachusetts Ave, Cambridge, MA 02139"));
        CampusArrayAdapter adapter = new CampusArrayAdapter(this, campuses);

        ListView lvCampuses = (ListView) findViewById(R.id.lvCampuses);
        lvCampuses.setAdapter(adapter);
        final Context ctx = this;
        lvCampuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Campus campus= campuses.get(position);
                Intent intent = new Intent(ctx, Map.class);

                intent.putExtra("Name", campus.getName());
                intent.putExtra("Address", campus.getAddress());

                startActivity(intent);
            }
        });
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

            TextView tvName = (TextView) convertView.findViewById(R.id.tvCampusName);
            TextView tvAddress = (TextView) convertView.findViewById(R.id.tvCampusAddress);

            tvName.setText(curr.getName());
            tvAddress.setText(curr.getAddress());

            return convertView;
        }
    }
}
