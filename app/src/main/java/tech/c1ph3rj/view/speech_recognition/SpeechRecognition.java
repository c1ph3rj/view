package tech.c1ph3rj.view.speech_recognition;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.IdentifiedLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import java.util.List;

import tech.c1ph3rj.view.R;

public class SpeechRecognition extends AppCompatActivity {


    // creating variables for our image view,
    // text view and two buttons.
    private EditText edtLanguage;
    private TextView languageCodeTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Speech Recognition");
                actionBar.setHomeAsUpIndicator(R.drawable.back_ic);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        init();
    }

    void init() {

        // on below line we are initializing our variables.
        edtLanguage = findViewById(R.id.idEdtLanguage);
        languageCodeTV = findViewById(R.id.idTVDetectedLanguageCode);
        Button detectLanguageBtn = findViewById(R.id.idBtnDetectLanguage);

        // adding on click listener for button
        detectLanguageBtn.setOnClickListener(v -> {
            // getting string from our edit text.
            String edt_string = edtLanguage.getText().toString();
            // calling method to detect language.
            detectLanguage(edt_string);
        });
    }

    private void detectLanguage(String string) {
        // initializing our firebase language detection.
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold(0.34f)
                        .build()
        );

        // adding method to detect language using identify language method.
        languageIdentifier.identifyLanguage(string).addOnSuccessListener(s -> {
            // below line we are setting our
            // language code to our text view.
            languageCodeTV.setText(s);
        }).addOnFailureListener(e -> {
            // handling error method and displaying a toast message.
            Toast.makeText(SpeechRecognition.this, "Fail to detect language : \n" + e, Toast.LENGTH_SHORT).show();
        });


        languageIdentifier =
                LanguageIdentification.getClient();
        // Model couldn’t be loaded or other internal error.
        // ...
        languageIdentifier.identifyPossibleLanguages(string)
                .addOnSuccessListener(identifiedLanguages -> {
                    for (IdentifiedLanguage identifiedLanguage : identifiedLanguages) {
                        String language = identifiedLanguage.getLanguageTag();
                        float confidence = identifiedLanguage.getConfidence();
                        Log.i(TAG, language + " (" + confidence + ")");
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}