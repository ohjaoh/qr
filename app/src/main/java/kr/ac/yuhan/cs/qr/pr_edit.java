package kr.ac.yuhan.cs.qr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class pr_edit extends AppCompatActivity {
    private FirebaseFirestore dbFirestore;
    private EditText editProductName2, editProductPrice2, editProductStock2, editProductCategory2;
    private Button buttonUpdateProduct2;
    private static final int PICK_FILE_REQUEST = 2; // 파일 선택을 위한 요청 코드
    private ImageView imageViewProduct2;

    private Uri fileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pr_edit);

        // UI 컴포넌트 초기화
        editProductName2 = findViewById(R.id.editProductName2);
        editProductPrice2 = findViewById(R.id.editProductPrice2);
        editProductStock2 = findViewById(R.id.editProductStock2);
        editProductCategory2=findViewById(R.id.editProductCategory);
        buttonUpdateProduct2 = findViewById(R.id.buttonUpdateProduct2);
        imageViewProduct2 = findViewById(R.id.imageViewProduct2);
        dbFirestore = FirebaseFirestore.getInstance();

        // Intent에서 데이터 추출
        Intent intent = getIntent();

        // 추출한 데이터 사용가능하게 변수로 만들기
        int productCode = intent.getIntExtra("productCode", 0);
        String productName = intent.getStringExtra("productName");
        // 이미지는 특별한 처리가 필요
        String productImage = intent.getStringExtra("productImage");
        // 가격과 재고는 int, double 또는 다른 숫자 타입일 수 있습니다. 적절한 메서드 사용
        int productPrice = intent.getIntExtra("productPrice", 0); // 기본값을 설정
        int productStock = intent.getIntExtra("productStock", 0);
        String category = intent.getStringExtra("category");

        // 읽어온 값으로 세팅
        editProductName2.setText(productName);
        editProductPrice2.setText(String.valueOf(productPrice)); // int 값을 String으로 변환
        editProductStock2.setText(String.valueOf(productStock)); // int 값을 String으로 변환
        editProductCategory2.setText(category); // 카테고리 값 세팅

        // 이미지 세팅
        if (productImage != null && !productImage.isEmpty()) {
            imageViewProduct2.setImageURI(Uri.parse(productImage));
            Glide.with(this).load(productImage).into(imageViewProduct2);
        }

        //이미지를 누르면 파일선택기를 연다.
        imageViewProduct2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileManager();
            }
        });

        //업데이트버튼메서드
        buttonUpdateProduct2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // productCode를 이용하여 데이터베이스에 연동하고 업데이트하는 메서드
                updateProductByCode(productCode);

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
            fileUri = data.getData();
            // ImageView에 이미지 로드
            imageViewProduct2.setImageURI(selectedFileUri);
        }
    }


    private void updateProductInFirestore(String documentId) {
        String updatedName = editProductName2.getText().toString();
        String updatedPricestr = editProductPrice2.getText().toString();
        String updatedStockstr = editProductStock2.getText().toString();
        String updatedCategory = editProductCategory2.getText().toString();

        int updatedPrice = Integer.parseInt(updatedPricestr);
        int updatedStock = Integer.parseInt(updatedStockstr);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("files/" + System.currentTimeMillis());

        Map<String, Object> productUpdate = new HashMap<>();
        productUpdate.put("productName", updatedName); // 상품명 업데이트
        productUpdate.put("price", updatedPrice); // 가격 업데이트
        productUpdate.put("stock", updatedStock); // 재고 업데이트
        productUpdate.put("category", updatedCategory); // 카테고리 업데이트 추가


        // 파일이 선택되었는지 확인
        if (fileUri != null) {
            // 파일 업로드 로직
            fileRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // 파일 URL을 productUpdate 맵에 추가하고 문서를 업데이트
                            productUpdate.put("imageUrl", uri.toString());
                            updateDocument(documentId, productUpdate);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Upload File", "Error uploading file", e);
                    Toast.makeText(pr_edit.this, "파일 업로드 실패", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 파일 업로드 없이 문서 업데이트
            updateDocument(documentId, productUpdate);
        }

    }
    // Firestore 문서 업데이트를 처리하는 별도의 메소드
    private void updateDocument(String documentId, Map<String, Object> productUpdate) {
        dbFirestore.collection("products").document(documentId)
                .update(productUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Update Firestore", "DocumentSnapshot successfully updated.");
                        Toast.makeText(pr_edit.this, "상품 정보가 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Update Firestore", "Error updating document", e);
                        Toast.makeText(pr_edit.this, "상품 정보 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProductByCode(final int Code) {
        dbFirestore.collection("products")
                .whereEqualTo("productCode", Code)// 여기에 들어가는 변수의 타입과 파이어베이스의 필드의 타입이 일치해야함
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    Log.d("Firestore Success", "Document found with ID: " + document.getId());
                                    updateProductInFirestore(document.getId());
                                    Toast.makeText(pr_edit.this, "서버와 연동으로 인해 약간의 딜레이가 발생할 수 있습니다.", Toast.LENGTH_SHORT).show();
                                    finishActivityWithResult();
                                }
                            } else {
                                Log.d("Firestore Empty", "No documents found at all");
                            }
                        } else {
                            Log.d("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void finishActivityWithResult() {
        Intent database_viewIntent = new Intent(pr_edit.this, database_view.class);
        database_viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(database_viewIntent);
        finish();
        finish();
    }
}