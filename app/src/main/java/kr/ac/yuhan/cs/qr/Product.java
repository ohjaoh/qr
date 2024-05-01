package kr.ac.yuhan.cs.qr;

import com.google.firebase.firestore.PropertyName;

// 여기서 카테고리는 준비완료
public class Product {
    private int productcode;
    private String name;
    private String image1; // 상품 이미지 또는 3D 파일 데이터
    private int price; // 가격
    private int stock; // 재고량
    private String category;//카테고리

    // 생성자
    public Product(int productcode, String name, String category, String image1, int price, int stock) {
        this.productcode = productcode;
        this.name = name;
        this.category = category;
        this.image1 = image1;
        this.price = price;
        this.stock = stock;
    }

    // 기본 생성자 추가
    public Product() {
        // Firestore 역직렬화를 위해 필요 파이어베이스에서 읽은 정보를 임시로 담아두는 역할을 함
    }

    @PropertyName("productCode")
    public void setproductcode(int productcode) {
        this.productcode = productcode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setcategory(String category) {this.category = category;}

    // 게터 메서드
    @PropertyName("productCode")
    public int getproductcode() {
        return productcode;
    }

    public String getName() {
        return name;
    }

    public String getImageo() {
        return image1;
    } // 메서드 이름 변경

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
    @PropertyName("category") // Firestore 필드 이름과 일치하도록 설정
    public String getCategory() {
        return category;
    }
}
