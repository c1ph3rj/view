package tech.c1ph3rj.view;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import tech.c1ph3rj.library.CurvedBottomNavigation;


public class MainActivity extends AppCompatActivity {

    private static final int ID_HOME = 1;
    private static final int ID_EXPLORE = 2;
    private static final int ID_MESSAGE = 3;
    private static final int ID_NOTIFICATION = 4;
    private static final int ID_ACCOUNT = 5;

    private TextView tvSelected;
    private CurvedBottomNavigation bottomNavigation;
    private EditText etPageId;
    private long lastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBottomNav();
    }

    private void initBottomNav() {
        tvSelected = findViewById(R.id.tv_selected);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        etPageId = findViewById(R.id.etPageId);

        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_HOME, R.drawable.ic_home, "home"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_MESSAGE, R.drawable.ic_message, "message"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore, "explore"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notification, "notification"));
        bottomNavigation.add(new CurvedBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_account , "account"));

        bottomNavigation.setOnShowListener(model -> {
            String name = getItemName(model.id);
            tvSelected.setText(getString(R.string.selected_page, name));
        });

        bottomNavigation.setOnClickMenuListener(item -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > 250) {
                // Process the click
                lastClickTime = currentTime;
                return;
            }
            bottomNavigation.show(item.id);
        });

        bottomNavigation.setOnReselectListener(item -> Toast.makeText(getApplicationContext(), "item " + item.id + " is reselected.", Toast.LENGTH_LONG).show());

        bottomNavigation.show(ID_HOME);

        findViewById(R.id.btShow).setOnClickListener(view -> {
            int id;
            try {
                id = Integer.parseInt(etPageId.getText().toString());
            } catch (Exception e) {
                id = ID_HOME;
            }

            if (id >= ID_HOME && id <= ID_ACCOUNT) {
                bottomNavigation.show(id);
            }
        });
    }

    private String getItemName(int itemId) {
        switch (itemId) {
            case ID_HOME:
                return "HOME";
            case ID_EXPLORE:
                return "EXPLORE";
            case ID_MESSAGE:
                return "MESSAGE";
            case ID_NOTIFICATION:
                return "NOTIFICATION";
            case ID_ACCOUNT:
                return "ACCOUNT";
            default:
                return "";
        }
    }
}
