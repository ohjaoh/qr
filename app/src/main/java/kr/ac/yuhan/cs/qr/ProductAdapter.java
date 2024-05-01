package kr.ac.yuhan.cs.qr;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ProductAdapter extends ArrayAdapter<Product> {

    public ProductAdapter(Context context, ArrayList<Product> products) {
        super(context, 0, products);
    }

    // getView와 listview에 넣을 database_view_button.xml을 수정했으니 확인할 것 혹은 여기부터하려면  xml에서 내용을 임시로 지우고 진행할것하다
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.database_view_button, parent, false);
            holder = new ViewHolder();
            holder.textViewProductName = convertView.findViewById(R.id.textViewProductName);
            holder.textViewPrice = convertView.findViewById(R.id.textViewPrice);
            holder.textViewStock = convertView.findViewById(R.id.textViewStock);
            holder.textViewCategory = convertView.findViewById(R.id.textViewCategory);
            holder.imageViewProduct = convertView.findViewById(R.id.imageViewProduct);
            holder.buttonEdit = convertView.findViewById(R.id.buttonListItem1);
            holder.buttonDelete = convertView.findViewById(R.id.buttonListItem2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = getItem(position);


        holder.textViewProductName.setText(product.getName());
        holder.textViewCategory.setText(product.getCategory());
        holder.textViewPrice.setText(String.valueOf(product.getPrice()));
        holder.textViewStock.setText(String.valueOf(product.getStock()));
        // Glide 라이브러리를 사용하여 ImageView에 이미지 로드
        if (product.getImageo() != null && !product.getImageo().isEmpty()) {
            Glide.with(getContext())
                    .load(product.getImageo())
                    .placeholder(R.drawable.default_image) // 로딩 중 및 실패 시 표시할 기본 이미지
                    .into(holder.imageViewProduct);
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.default_image); // URL이 없을 경우 기본 이미지 표시
        }

        // Add click listeners for your buttons
        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 position에서 Product 객체 가져오기
                Product product = getItem(position);
                Log.d("DatabaseViewActivity", "포지션은 잘 오나?  position " + position);
                //포지션은 잘 넘어감
                // Intent를 사용하여 수정 액티비티로 Product 정보 전달
                Intent intent = new Intent(getContext(), pr_edit.class);
                intent.putExtra("productCode", product.getproductcode());
                intent.putExtra("productName", product.getName());
                intent.putExtra("productImage", product.getImageo());
                intent.putExtra("productPrice", product.getPrice());
                intent.putExtra("productStock", product.getStock());
                intent.putExtra("category", product.getCategory());
                getContext().startActivity(intent);
            }
        });

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 상품 삭제 로직 호출
                deleteProductByProductCode(product.getproductcode());

            }
        });

        return convertView;
    }

    // ViewHolder class
    static class ViewHolder {
        TextView textViewProductName;
        ImageView imageViewProduct;
        TextView textViewPrice;
        TextView textViewStock;
        TextView textViewCategory;
        Button buttonEdit;
        Button buttonDelete;
    }

    private void deleteProductByProductCode(int productCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // productCode를 기준으로 상품 문서를 조회
        db.collection("products")
                .whereEqualTo("productCode", productCode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 일치하는 각 문서(상품) 삭제
                                db.collection("products").document(document.getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext().getApplicationContext(), "상품이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                // 화면을 새로고침하는 코드
                                                removeProductByCode(productCode); // 여기서 리스트에서 제거
                                                notifyDataSetChanged(); // 변경사항 반영
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext().getApplicationContext(), "상품 삭제에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Log.d("deleteProduct", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void removeProductByCode(int productCode) {
        for (int i = 0; i < getCount(); i++) {
            Product p = getItem(i);
            if (p.getproductcode() == productCode) {
                remove(p); // 상품 제거
                break;
            }
        }
    }


}
