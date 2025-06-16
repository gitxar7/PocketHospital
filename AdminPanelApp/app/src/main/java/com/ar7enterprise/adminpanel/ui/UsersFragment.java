package com.ar7enterprise.adminpanel.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ar7enterprise.adminpanel.R;
import com.ar7enterprise.adminpanel.model.AdminTileAdapter;
import com.ar7enterprise.adminpanel.model.AdminTileItem;
import com.ar7enterprise.adminpanel.model.UserTileAdapter;
import com.ar7enterprise.adminpanel.model.UsersTileItem;

import java.util.ArrayList;

public class UsersFragment extends Fragment {

    private ArrayList<UsersTileItem> tileItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        Button searchButton = view.findViewById(R.id.userSearchButton);


        loadUsers(view,null);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView search = view.findViewById(R.id.userSearch);
                String searchTxt = search.getText().toString();
                loadUsers(view,searchTxt);
            }
        });


//        RecyclerView recyclerView = view.findViewById(R.id.userList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        ArrayList<UsersTileItem> tileItems = new ArrayList<>();
//        tileItems.add(new UsersTileItem("Shala Grimes", "(310) 417-8976"));
//
//        recyclerView.setAdapter(new UserTileAdapter(tileItems));
        return view;
    }

    private void loadUsers(View view, String search){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext(), "pocket_hospital.db", null, 1);
                SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
                String query;
                String[] selectionArgs;
                tileItems.clear();
                if (search == null || search.trim().isEmpty()) {
                    query = "SELECT * FROM user";
                    selectionArgs = null;
                } else {
                    query = "SELECT * FROM user WHERE mobile LIKE ? OR name LIKE ?";
                    selectionArgs = new String[]{"%" + search + "%", "%" + search + "%"};
                }
                Cursor cursor = db.rawQuery(query, selectionArgs);

                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));

                    tileItems.add(new UsersTileItem(id, name, mobile));

                }

                db.close();

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView recyclerView = view.findViewById(R.id.userList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(new UserTileAdapter(tileItems));

                    }
                });
            }
        }).start();
    }
}