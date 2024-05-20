package kr.ac.yuhan.cs.qr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;

public class ScanQR extends AppCompatActivity {
    private DecoratedBarcodeView barcodeScannerView;
    private CaptureManager capture;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        Log.d("ScanQR", "ListView set with dummy adapter");
        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.decorated_bar_code_view);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(this.getIntent(), savedInstanceState);
        capture.decode();
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                Log.d("ScanQR", "Scanned QR Data: " + result.getText());
                capture.onPause();  // Properly pause decoding
                processScannedData(result.getText());
            }
        });
        Button payButton = findViewById(R.id.paybutton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    // 밑에는 파이어베이스와 연결하여 전역변수로 설정한 2개의 변수 필드명, 데이터를 이용하여 조회하고 값을 가져온다.
    // 이후 재고를 수정하고 업데이트한다. 버튼을 눌러서 보낼 때 기존 값 -(선택수량)하게 한다.
    private void processScannedData(String scannedData) {
        if (scannedData != null && scannedData.contains(":")) {
            String[] keyValues = scannedData.split(":");
            if (keyValues.length == 2) {
                String field = keyValues[0];  // 이 부분은 상품 코드를 나타낼 필드, 예를 들어 "productCode"
                String value = keyValues[1];  // 실제 상품 코드
                Log.d("ScanQR", "Field: " + field + ", Value: " + value); // 필드와 값 로그

                fetchDataFromFirestore(field, value, new FirestoreCallback() {
                    @Override
                    public void onCallback(ArrayList<Product> products) {
                        ListView listView = findViewById(R.id.scannedlist);
                        ScanAdpter adapter = (ScanAdpter) listView.getAdapter();
                        if (adapter == null) {
                            adapter = new ScanAdpter(ScanQR.this, new ArrayList<>());
                            listView.setAdapter(adapter);
                        }

                        int parsedValue = Integer.parseInt(value);  // 파싱된 상품 코드
                        boolean isNewProduct = true;

                        for (int i = 0; i < adapter.getCount(); i++) {
                            Product existingProduct = adapter.getItem(i);
                            if (existingProduct.getproductcode() == parsedValue) {
                                existingProduct.setStock(existingProduct.getStock() + 1);
                                isNewProduct = false;
                                break;
                            }
                        }
                        //코드의 진행순서상 여기가 먼저고 뒤에서 매핑하는 바람에 product클래스에서 productCode를 함께 인식하도록   @PropertyName("productCode")를 추가함
                        //나중에 그냥 code를 productcode로 변경해야함
                        if (isNewProduct) {
                            for (Product newProduct : products) {
                                if (newProduct.getproductcode() == parsedValue) {
                                    newProduct.setStock(1);  // 새 상품의 초기 수량 설정
                                    adapter.add(newProduct);
                                    break;
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();  // 데이터 변경 알림
                        updateTotalPrice();  // 총 금액 업데이트
                    }


                });
            } else {
                Log.d("ScanQR", "Scanned data format is incorrect"); // 데이터 형식 오류 로그
            }
        } else {
            Log.d("ScanQR", "Scanned data is null or does not contain ':'"); // 스캔 데이터 문제 로그
        }

        capture.onResume();
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Product> products);
    }

    private void fetchDataFromFirestore(String field, String value, FirestoreCallback callback) {
        Log.d("ScanQR", "Starting to fetch data from Firestore. Field: " + field + ", Value: " + value);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<Product> products = new ArrayList<>(); // 여기서 선언

        long value1;
        try {
            value1 = Long.parseLong(value);
            Log.d("ScanQR", "Value parsed to long successfully.");
        } catch (NumberFormatException e) {
            Log.d("ScanQR", "Error parsing value to long: " + e.getMessage());
            callback.onCallback(new ArrayList<>());
            return;
        }

        Log.d("ScanQR", "Preparing to query Firestore...");

        db.collection("products").whereEqualTo(field, value1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            String productName = document.getString("productName");
                            String productImage = document.getString("imageUrl");
                            product.setName(productName);
                            product.setImage1(productImage);
                            products.add(product);
                            Log.d("ScanQR", "Product code: " + product.getproductcode() + ", Name: " + product.getName());
                        }
                    } else {
                        Log.d("ScanQR", "No products matched the query");
                    }

                    callback.onCallback(products);
                })
                .addOnFailureListener(e -> {
                    Log.e("ScanQR", "Error fetching data: " + e.getMessage());
                    callback.onCallback(null);
                });

        Log.d("ScanQR", "Firestore query dispatched.");
    }

    protected void updateTotalPrice() {
        ListView listView = findViewById(R.id.scannedlist);
        ScanAdpter adapter = (ScanAdpter) listView.getAdapter();
        if (adapter == null) {
            return; // 어댑터가 없으면 종료
        }

        double total = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            Product product = adapter.getItem(i);
            total += product.getPrice() * product.getStock();
        }

        TextView totalTextView = findViewById(R.id.itemtotal);
        totalTextView.setText(String.format("%,.0f", total));
    }

    private void processPayment() {
        ListView listView = findViewById(R.id.scannedlist);
        ScanAdpter adapter = (ScanAdpter) listView.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            Product product = adapter.getItem(i);
            updateProductStock(product);
        }
    }

    private void updateProductStock(Product product) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("ScanQR", "Querying product with code: " + String.valueOf(product.getproductcode()));

        db.collection("products")
                .whereEqualTo("productCode", product.getproductcode())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("ScanQR", "No products found with the code.");
                        return;
                    }
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DocumentReference docRef = document.getReference();
                        runTransactionToUpdateStock(db, docRef, product);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ScanQR", "Error fetching documents: " + e.getMessage());
                });
    }

    private void runTransactionToUpdateStock(FirebaseFirestore db, DocumentReference docRef, Product product) {
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("Product not found",
                        FirebaseFirestoreException.Code.ABORTED);
            }
            long newStock = snapshot.getLong("stock") - product.getStock();
            if (newStock < 0) {
                throw new FirebaseFirestoreException("Stock cannot be negative",
                        FirebaseFirestoreException.Code.ABORTED);
            }
            transaction.update(docRef, "stock", newStock);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ScanQR", "Stock updated successfully for product code: " + product.getproductcode());
        }).addOnFailureListener(e -> {
            Log.e("ScanQR", "Error updating stock: " + e.getMessage());
        });
    }





    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    public void readBarcode(String barcode) {
        Toast.makeText(getApplicationContext(), barcode, Toast.LENGTH_LONG).show();
    }
}
