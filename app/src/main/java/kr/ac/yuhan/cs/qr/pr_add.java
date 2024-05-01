package kr.ac.yuhan.cs.qr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class pr_add extends AppCompatActivity {
    private FirebaseFirestore dbFirestore;

    private static final int PICK_FILE_REQUEST = 2; // 파일 선택을 위한 요청 코드
    private EditText editProductName, editProductPrice, editProductStock, editProductCategory;
    private Button buttonSaveProduct;
    private ImageView imageViewProduct;
    private Uri fileuri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pr_add);

        // UI 컴포넌트 초기화
        editProductName = findViewById(R.id.editProductName);
        editProductPrice = findViewById(R.id.editProductPrice);
        editProductStock = findViewById(R.id.editProductStock);
        editProductCategory = findViewById(R.id.editProductCategory);
        buttonSaveProduct = findViewById(R.id.buttonSaveProduct);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        dbFirestore = FirebaseFirestore.getInstance();

        imageViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileManager();
            }
        });
        // "Save" 버튼에 대한 클릭 리스너 설정
        buttonSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(pr_add.this, "서버와 연동으로 인해 약간의 딜레이가 발생할 수 있습니다.", Toast.LENGTH_SHORT).show();
                uploadFileAndSaveProductInfo();
            }

        });
    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // 모든 유형의 파일을 허용
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            fileuri = data.getData();
            // ImageView에 이미지 로드
            imageViewProduct.setImageURI(selectedFileUri);
        }
    }


    private void uploadFileAndSaveProductInfo() {
        String name = editProductName.getText().toString().trim();
        String priceStr = editProductPrice.getText().toString().trim();
        String stockStr = editProductStock.getText().toString().trim();
        String category = editProductCategory.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || fileuri == null || category.isEmpty()) {
            Toast.makeText(this, "모든 필드를 채워주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceStr);
        int stock = Integer.parseInt(stockStr);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("files/" + System.currentTimeMillis());

        UploadTask uploadTask = fileRef.putFile(fileuri);
        uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
            String fileUrl = uri.toString();

            // Firestore에서 productCounter 문서를 업데이트하고 새 productCode를 가져옵니다.
            // Firestore에서 counters 컬렉션을 만들고  productCounter 문서를 직접 생성한다.
            // lastProductCode 필드에 초기값(예: 0)을 설정합니다. 데이터타입은 number 이 문서가 없으면 프로그램이 진행 안됨
            // 만약에 상품컬렉션을 지웠으면 이거도 관리해서 0으로 만들것(수동임)
            DocumentReference counterRef = dbFirestore.collection("counters").document("productCounter");
            dbFirestore.runTransaction(transaction -> {
                DocumentSnapshot counterSnapshot = transaction.get(counterRef);
                Long lastProductCode = counterSnapshot.getLong("lastProductCode");
                if (lastProductCode == null) lastProductCode = 0L; // 초기값 설정
                Long newProductCode = lastProductCode + 1;
                transaction.update(counterRef, "lastProductCode", newProductCode);

                // 상품 정보와 파일 URL을 Firestore에 저장합니다.
                Map<String, Object> product = new HashMap<>();
                product.put("productName", name);
                product.put("imageUrl", fileUrl);
                product.put("price", price);
                product.put("stock", stock);
                product.put("category", category);
                product.put("productCode", newProductCode); // 새로운 productCode 사용

                dbFirestore.collection("products").add(product).addOnSuccessListener(documentReference -> {
                    Toast.makeText(pr_add.this, "상품 정보와 파일 URL 파이어베이스에 저장 성공", Toast.LENGTH_SHORT).show();
                    finishActivityWithResult();
                }).addOnFailureListener(e -> {
                    Toast.makeText(pr_add.this, "파이어베이스 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

                return null;
            }).addOnFailureListener(e -> {
                Toast.makeText(pr_add.this, "상품 코드 업데이트 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        })).addOnFailureListener(e -> {
            Toast.makeText(pr_add.this, "파일 업로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void finishActivityWithResult() {
        Intent intent = new Intent(pr_add.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }


}
