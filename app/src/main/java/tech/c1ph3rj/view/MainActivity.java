package tech.c1ph3rj.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import tech.c1ph3rj.library.CurvedBottomNavigation;
import tech.c1ph3rj.view.GetPremium.GetPremium;
import tech.c1ph3rj.view.ask_questions.AskQuestionsScreen;
import tech.c1ph3rj.view.audio_translate.AudioRecorder;
import tech.c1ph3rj.view.custom_forms.CustomForms;
import tech.c1ph3rj.view.faq.FAQScreen;
import tech.c1ph3rj.view.line_of_business.LineOfBusinessScreen;
import tech.c1ph3rj.view.products.ProductsScreen;
import tech.c1ph3rj.view.speech_recognition.SpeechRecognition;
import tech.c1ph3rj.view.user_information.UserInformationScreen;
import tech.c1ph3rj.view.video_kyc.VideoKYCScreen;


public class MainActivity extends AppCompatActivity {

    private static final int ID_HOME = 1;
    private static final int ID_EXPLORE = 2;
    private static final int ID_MESSAGE = 3;
    private static final int ID_NOTIFICATION = 4;
    private static final int ID_ACCOUNT = 5;

    //    private EditText etPageId;
//    private TextView tvSelected;
    private CurvedBottomNavigation bottomNavigation;
    private long lastClickTime = 0;
    CardView mainOptionsBtn;

    private int SELECTED_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initBottomNav();
    }

    void init() {
        try {
            mainOptionsBtn = findViewById(R.id.mainOptionsBtn);
            mainOptionsBtn.setOnClickListener(onClickMainOptions ->
                    startActivity(new Intent(this, LineOfBusinessScreen.class)));
//            startActivity(new Intent(this, AudioRecorder.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBottomNav() {
//        tvSelected = findViewById(R.id.tv_selected);
//        etPageId = findViewById(R.id.etPageId);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_HOME, R.drawable.ic_home, "home"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_MESSAGE, R.drawable.ic_message, "message"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore, "explore"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notification, "notification"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_account, "account"));

        bottomNavigation.setOnShowListener(model -> {
//            String name = getItemName(model.id);
//            tvSelected.setText(getString(R.string.selected_page, name));
        });

        bottomNavigation.setOnClickMenuListener(item -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 250) {
                // Process the click
                lastClickTime = currentTime;
                return;
            }
            SELECTED_ID = item.id;

            bottomNavigation.show(item.id);
        });

        bottomNavigation.setOnReselectListener(item -> Toast.makeText(getApplicationContext(), "item " + item.id + " is reselected.", Toast.LENGTH_LONG).show());

        bottomNavigation.show(ID_HOME);

//        findViewById(R.id.btShow).setOnClickListener(view -> {
//            int id;
//            try {
//                id = Integer.parseInt(etPageId.getText().toString());
//            } catch (Exception e) {
//                id = ID_HOME;
//            }
//
//            if (id >= ID_HOME && id <= ID_ACCOUNT) {
//                bottomNavigation.show(id);
//            }
//        });
    }

//    private String getItemName(int itemId) {
//        switch (itemId) {
//            case ID_HOME:
//                return "HOME";
//            case ID_EXPLORE:
//                return "EXPLORE";
//            case ID_MESSAGE:
//                return "MESSAGE";
//            case ID_NOTIFICATION:
//                return "NOTIFICATION";
//            case ID_ACCOUNT:
//                return "ACCOUNT";
//            default:
//                return "";
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        if(bottomNavigation != null && SELECTED_ID != 0) {
            bottomNavigation.show(SELECTED_ID);
        }
    }

}
