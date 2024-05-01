package kr.ac.yuhan.cs.qr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

// AppCompatActivity를 상속받는 database_view 클래스 정의
public class database_view extends AppCompatActivity {
    private ListView listView;
    private ProductAdapter adapter;
    private ArrayList<Product> productList = new ArrayList<>(); // 상품 정보를 담을 리스트

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loadItemsFromFirestore(); // 파이어베이스에서 데이터를 불러오는 메서드 호출
            handler.postDelayed(this, 5000); // 5초 후에 다시 실행하도록 스케줄링
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_view); // 레이아웃 설정

        listView = findViewById(R.id.listViewProducts); // 레이아웃에서 리스트 뷰 연결
        Log.d("DatabaseViewActivity", "onCreate: listView 연결 완료");
        adapter = new ProductAdapter(this, productList);
        Log.d("DatabaseViewActivity", "onCreate: Adapter 설정 완료");
        listView.setAdapter(adapter); // 리스트 뷰에 어댑터 설정

        loadItemsFromFirestore(); // 초기 데이터 로딩
        handler.postDelayed(runnable, 5000); // 5초마다 데이터를 새로 고침

        Log.d("DatabaseViewActivity", "onCreate: 파이어베이스에서 아이템 로드 완료");
    }

    // 파이어베이스에서 제품 데이터를 불러오는 메서드
    private void loadItemsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // 파이어베이스 인스턴스 생성
        db.collection("products").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    productList.clear(); // 기존의 리스트를 클리어
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        int code = document.getLong("productCode").intValue();
                        String productName = document.getString("productName");
                        String category = document.getString("category");
                        String imageUrl = document.getString("imageUrl");
                        int price = document.getLong("price").intValue();
                        int stock = document.getLong("stock").intValue();

                        if (imageUrl == null || imageUrl.isEmpty()) {
                            imageUrl = getDefaultImageUrl(); // 기본 이미지 URL 사용
                        }

                        Log.d("DatabaseViewActivity", "Loaded imageUrl: " + imageUrl);
                        productList.add(new Product(code, productName, category, imageUrl, price, stock)); // 리스트에 제품 추가
                    }
                    adapter.notifyDataSetChanged(); // 데이터 변경을 어댑터에 알림
                } else {
                    Log.e("DatabaseViewActivity", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // 화면이 종료될 때 주기적인 새로고침 중지
    }

    private String getDefaultImageUrl() {
        // 이 메서드에서는 리소스 ID를 문자열로 반환합니다.
        return "R.drawable.default_image";
    }

}
