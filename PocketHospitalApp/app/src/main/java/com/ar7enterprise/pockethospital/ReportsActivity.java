package com.ar7enterprise.pockethospital;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ar7enterprise.pockethospital.model.DetailsTileAdapter;
import com.ar7enterprise.pockethospital.model.DetailsTileItem;
import com.ar7enterprise.pockethospital.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("pocket_hospital", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        if (user == null) {
            startActivity(new Intent(ReportsActivity.this, GuestActivity.class));
            finish();
        } else {

            Gson gson = new Gson();
            User userObj = gson.fromJson(user, User.class);
            ArrayList<DetailsTileItem> detailsTileItems = new ArrayList<>();

            findViewById(R.id.rpProfileImage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ReportsActivity.this, MainActivity.class));
                }
            });

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            TextView download = findViewById(R.id.rpDownloadButton);
            firestore.collection("report")
                    .where(
                            Filter.equalTo("mobile", userObj.getMobile())

                    )
                    .limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                            RecyclerView recyclerView = findViewById(R.id.rpResultsList);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ReportsActivity.this));

                            for (DocumentSnapshot documentSnapshot : documentSnapshotList) {
                                detailsTileItems.add(new DetailsTileItem(documentSnapshot.get("name").toString(),
                                        documentSnapshot.get("result").toString(),
                                        documentSnapshot.get("date").toString(), null));
                            }
                            recyclerView.setAdapter(new DetailsTileAdapter(detailsTileItems, false));

                            if (detailsTileItems.isEmpty()) {
                                download.setVisibility(View.GONE);
                            }
                        }
                    });

            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Animation animation = AnimationUtils.loadAnimation(ReportsActivity.this,R.anim.animation_x);
//                    download.setAnimation(animation);
//                    download.startAnimation(animation);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String fileName = "report_" + timeStamp + ".pdf";
                    File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName
                    );

                    try {
                        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
                        PdfDocument pdfDocument = new PdfDocument(writer);
                        Document document = new Document(pdfDocument);

                        Paragraph title = new Paragraph("Pocket Hospital Report Results")
                                .setBold()
                                .setFontSize(20)
                                .setTextAlignment(TextAlignment.CENTER);
                        document.add(title);

                        document.add(new Paragraph("\n"));

                        Table table = new Table(new float[]{4, 3, 3});
                        table.setWidth(UnitValue.createPercentValue(100));

                        table.addCell(new Cell().add(new Paragraph("Name").setBold()));
                        table.addCell(new Cell().add(new Paragraph("Result").setBold()));
                        table.addCell(new Cell().add(new Paragraph("Date").setBold()));

                        for (DetailsTileItem item : detailsTileItems) {
                            table.addCell(new Cell().add(new Paragraph(item.getTitle())));
                            table.addCell(new Cell().add(new Paragraph(item.getText1())));
                            table.addCell(new Cell().add(new Paragraph(item.getText2())));
                        }

                        document.add(table);
                        document.close();
                        Toast.makeText(ReportsActivity.this, "Report saved to Downloads!", Toast.LENGTH_LONG).show();

                        Uri pdfUri = FileProvider.getUriForFile(ReportsActivity.this, getPackageName() + ".provider", pdfFile);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(pdfUri, "application/pdf");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);

                    } catch (Exception e) {
                        Toast.makeText(ReportsActivity.this, "Failed to generate PDF", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }
}