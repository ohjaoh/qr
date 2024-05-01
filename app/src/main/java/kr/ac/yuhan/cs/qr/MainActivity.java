package kr.ac.yuhan.cs.qr;

// 기본적인 안드로이드 컴포넌트 및 AppCompatActivity 클래스를 임포트합니다.
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

// ZXing 라이브러리를 사용한 QR 코드 스캐너 기능을 위해 필요한 클래스들을 임포트합니다.
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

// MainActivity 클래스는 AppCompatActivity를 상속받아 앱의 주 화면 기능을 구현합니다.
public class MainActivity extends AppCompatActivity {

    // UI에서 사용될 버튼들을 선언합니다.
    private Button createQRBtn;
    private Button praddbtnj;
    private Button scanQRBtn;
    private Button selectdbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // activity_main 레이아웃을 화면에 설정합니다.

        // QR 코드 생성 버튼과 해당 버튼 클릭 이벤트 리스너를 설정합니다.
        createQRBtn = (Button) findViewById(R.id.createQR);
        createQRBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateQR.class);
                startActivity(intent); // CreateQR 액티비티를 시작합니다.
            }
        });

        // QR 코드 스캔 버튼과 이벤트 리스너를 설정합니다.
        scanQRBtn = (Button) findViewById(R.id.scanQR);
        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setBeepEnabled(false); // 스캔 시 소리 비활성화
                integrator.setCaptureActivity(ScanQR.class); // 스캔 활동으로 ScanQR 클래스 사용
                integrator.initiateScan(); // 스캔 시작
            }
        });

        // 데이터베이스 선택 버튼과 이벤트 리스너를 설정합니다.
        selectdbs = (Button) findViewById(R.id.selectdb);
        selectdbs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "서버와 연동으로 인해 약간의 딜레이가 발생할 수 있습니다.", Toast.LENGTH_SHORT).show();
                Intent select = new Intent(MainActivity.this, database_view.class);
                startActivity(select); // database_view 액티비티를 시작합니다.
            }
        });

        // 제품 추가 버튼과 이벤트 리스너를 설정합니다.
        praddbtnj = (Button) findViewById(R.id.praddbtn);
        praddbtnj.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pradds = new Intent(MainActivity.this, pr_add.class);
                startActivity(pradds); // pr_add 액티비티를 시작합니다.
            }
        });
    }
}
