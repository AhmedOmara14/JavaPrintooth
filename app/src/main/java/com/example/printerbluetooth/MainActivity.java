package com.example.printerbluetooth;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity {
    private Printing printing;
    private Button btnPairUnpair, btnPrintText, btnPrintImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Printooth.INSTANCE.init(MainActivity.this);

        if (Printooth.INSTANCE.hasPairedPrinter()) {
            printing = Printooth.INSTANCE.printer();
        }

        btnPairUnpair = findViewById(R.id.btnPairUnpair);
        btnPrintText = findViewById(R.id.btnPrintText);
        btnPrintImage = findViewById(R.id.btnPrintImage);
        initViews();
        initListener();
    }

    private void initViews() {
        if (Printooth.INSTANCE.hasPairedPrinter()) {
            btnPairUnpair.setText(new StringBuilder("Unpair ").append(Printooth.INSTANCE.getPairedPrinter().getName()).toString());
        } else {
            btnPairUnpair.setText(new StringBuilder("Pair With Printer"));
        }
    }

    private void initListener() {
        btnPairUnpair.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Printooth.INSTANCE.hasPairedPrinter()) {
                        Printooth.INSTANCE.removeCurrentPrinter();
                    } else {
                        startActivityForResult(new Intent(MainActivity.this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                        initViews();
                    }
                } catch (Exception e) {
                    Log.w("btnUnpair", e.toString());
                }
            }
        });

        btnPrintText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!Printooth.INSTANCE.hasPairedPrinter()) {
                        startActivityForResult(new Intent(MainActivity.this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                    } else {
                        printSomeText();
                    }
                } catch (Exception e) {
                    Log.w("btnPrintText", e.toString());
                }

            }
        });

        if (printing != null) {
            printing.setPrintingCallback(new PrintingCallback() {
                @Override
                public void connectingWithPrinter() {
                    Toast.makeText(MainActivity.this, "Connecting with printer", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void printingOrderSentSuccessfully() {
                    Toast.makeText(MainActivity.this, "Data Sent To printer", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void connectionFailed(String s) {
                    Toast.makeText(MainActivity.this, "Connection fail : " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String s) {
                    Toast.makeText(MainActivity.this, "Error : " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onMessage(String s) {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnPrintImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Printooth.INSTANCE.hasPairedPrinter())
                    startActivityForResult(new Intent(MainActivity.this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                else
                    printImages();
            }
        });
    }

    private void printSomeText() {
        try {

            String BILL = "";
            BILL = BILL
                    + "     *********      ";
            BILL = BILL+"\n***Invoice No*** \n" +
                    "Muslim Movers\n"
                    +"14/55"+"\n";

            BILL = BILL + "\n\n";
            BILL = BILL + "Tickets:" + "      " + String.valueOf("100")+"\n";
            BILL = BILL + "Price:        "+String.valueOf("200")+"\n"+
                    "1"+"\n";
            BILL = BILL
                    + "     *********     \n";
            ////textsize and allignment
            byte[] format = { 33, 33, 33 };
            byte[] arrayOfByte1 = { 27, 3, 0 };
            format[2] = ((byte)(0x10 | arrayOfByte1[2]));
            format[2] = ((byte) (0x8 | arrayOfByte1[2]));

            ArrayList<Printable> printables = new ArrayList<>();
                printables.add(new TextPrintable.Builder()
                        .setText(BILL)
                        .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                        .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                        .setFontSize(format[2])
                        .setNewLinesAfter(1)
                        .build());



            printing.print(printables);
        } catch (Exception e) {
            Log.w("PrintSomeText() ", e.getMessage());
        }
    }

    private void printImages() {
        ArrayList<Printable> printables = new ArrayList<>();


        //Load image from internet
        Picasso.get()
                .load("Link of image")
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        printables.add(new ImagePrintable.Builder(bitmap).build());
                        printables.add(new ImagePrintable.Builder(bitmap).build());


                        printing.print(printables);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            printSomeText();
            printImages();

            initViews();
        }
    }
}