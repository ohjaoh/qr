package kr.ac.yuhan.cs.qr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ScanAdpter extends ArrayAdapter<Product> {
    private ArrayList<Product> products;  // 내부에서 관리하는 상품 리스트

    public ScanAdpter(Context context, ArrayList<Product> products) {
        super(context, 0, products);
    }
    // 현재 어댑터가 관리하는 상품 리스트를 반환
    public ArrayList<Product> getItems() {
        if (products == null) {
            products = new ArrayList<>();  // 이렇게 하면 null 반환을 막을 수 있습니다.
        }
        return products;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder 패턴을 사용하여 뷰 성능 개선
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.scanitem, parent, false);
            holder = new ViewHolder();
            holder.imageViewProduct = convertView.findViewById(R.id.scan_imageViewProduct);
            holder.textViewProductName = convertView.findViewById(R.id.scan_textViewProductName);
            holder.textViewPrice = convertView.findViewById(R.id.scan_textViewPrice);
            holder.textViewStock = convertView.findViewById(R.id.scan_textViewStock);
            holder.textViewCategory = convertView.findViewById(R.id.scan_textViewCategory);
            holder.buttonAdd = convertView.findViewById(R.id.scan_stockAdd);
            holder.buttonMinus = convertView.findViewById(R.id.scan_stockMinus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = getItem(position);

        // 상품 정보 설정
        holder.textViewProductName.setText(product.getName());
        holder.textViewPrice.setText(String.format("%d", product.getPrice()));
        holder.textViewStock.setText(String.format("%d", product.getStock()));
        holder.textViewCategory.setText(product.getCategory());

        // Glide 라이브러리를 사용하여 이미지 로드
        Glide.with(getContext()).load(product.getImageo()).into(holder.imageViewProduct);

        // '추가' 버튼 이벤트 처리
        holder.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 재고 증가 로직
                int currentStock = product.getStock();
                product.setStock(currentStock + 1); // 상품 재고 증가
                notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
                if (getContext() instanceof ScanQR) {
                    ((ScanQR)getContext()).updateTotalPrice();  // 총 금액 업데이트
                }
            }
        });

        holder.buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentStock = product.getStock();
                if (currentStock > 1) {
                    product.setStock(currentStock - 1);
                    notifyDataSetChanged(); // 데이터 변경을 알림
                    ((ScanQR)getContext()).updateTotalPrice(); // 총 금액 업데이트
                } else if (currentStock == 1) {
                    // 재고가 1이면 다이얼로그 표시
                    showRemoveItemDialog(product, position, ScanAdpter.this);
                }
            }
        });

        return convertView;
    }
    private void showRemoveItemDialog(final Product product, final int position, final ArrayAdapter<Product> adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("이 아이템을 삭제하시겠습니까?");
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapter.remove(product);
                adapter.notifyDataSetChanged(); // 리스트 뷰 업데이트
                ((ScanQR)getContext()).updateTotalPrice(); // 총 금액 업데이트
            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean contains(int productCode) {
        for (int i = 0; i < getCount(); i++) {
            Product existingProduct = getItem(i);
            if (existingProduct != null && existingProduct.getproductcode() == productCode) {
                return true;
            }
        }
        return false;
    }

    public int getIndex(int productCode) {
        for (int i = 0; i < getCount(); i++) {
            Product existingProduct = getItem(i);
            if (existingProduct != null && existingProduct.getproductcode() == productCode) {
                return i;
            }
        }
        return -1;  // 상품이 리스트에 없는 경우 -1 반환
    }


    // ViewHolder 패턴을 사용하여 뷰 재사용 효율성 향상
    static class ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewPrice;
        TextView textViewStock;
        TextView textViewCategory;
        Button buttonAdd;
        Button buttonMinus;
    }
}
