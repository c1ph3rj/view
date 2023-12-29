package tech.c1ph3rj.view.faq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;
import tech.c1ph3rj.view.ask_questions.AskQuestionsScreen;

public class FAQScreen extends AppCompatActivity {
    Services services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqscreen);

        try {
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                getSupportActionBar().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        services = new Services(this, null);

        init();
    }

    void init() {
        try {
//            findViewById(R.id.backBtn)
//                    .setOnClickListener(onClickBack -> getOnBackPressedDispatcher().onBackPressed());
            findViewById(R.id.askQuestionsBtn)
                    .setOnClickListener(onClickAskQuestions -> startActivity(new Intent(this, AskQuestionsScreen.class)));
            initExpandableListView();
        } catch (Exception e) {
            services.handleException(e);
        }
    }

    private void initExpandableListView() {
        try {
            HashMap<String, List<String>> expandableListData = new HashMap<>();
            ArrayList<String> listTitles = new ArrayList<>();

// Add group data (titles) and their children (content)
            listTitles.add("What is life insurance?");
            List<String> group1Items = new ArrayList<>();
            group1Items.add("Life insurance is a contract between an insurance company and an insured (customer). Life policies can either be for protection against the insured event; or can be an investment where the aim is to save and grow your money. The insured pays the premium and the insurer promises to pay a sum of money to an appointed beneficiary when the insured event occurs Depending on the type of policy one takes, events such as terminal or critical illness, death and disability can trigger payment.");
            expandableListData.put(listTitles.get(0), group1Items);

            listTitles.add("Why do I need life insurance?");
            List<String> group2Items = new ArrayList<>();
            group2Items.add("Life insurance protects the financial interests of your family. It can provide financial support in the event of critical illness, disability or sudden death. It can also act as a long-term investment that will help you meet your goals such as children's education, your children's marriage, building your dream home, a relaxed retirement life or any other goal. When you have life insurance, Kenya Revenue Authority allows a tax relief of 15% for a maximum of Ksh60,000 premium per year (Ksh5,000 per month).");
            expandableListData.put(listTitles.get(1), group2Items);

            listTitles.add("Which policy should I buy?");
            List<String> group3Items = new ArrayList<>();
            group3Items.add("There are different types of life insurance policies, its best that you choose the policy that best suits you, and your future needs. Some of the things you should understand are: The scope of the cover period, the various terms and conditions and the premium payable. It is important to contact the insurance company or agent for more details and guidance.");
            expandableListData.put(listTitles.get(2), group3Items);

            listTitles.add("What are the different types of Life Insurance covers?");
            List<String> group4Items = new ArrayList<>();
            group4Items.add("Term Life Insurance: Provides coverage for a set period (e.g., 10-30 years) with a death benefit if the insured person passes away during that time. No savings component.\n" +
                    "\n" +
                    "Whole Life Insurance: Offers lifetime coverage with a cash value component that grows over time and can be accessed for loans or withdrawals.\n" +
                    "\n" +
                    "Universal Life Insurance: Flexible policy allowing premium and death benefit adjustments, with a cash value that can be invested according to the policyholder's preferences.\n" +
                    "\n" +
                    "Variable Life Insurance: Combines life coverage with investment options like stocks and bonds, with the cash value and death benefit tied to investment performance.\n" +
                    "\n" +
                    "Variable Universal Life Insurance: A hybrid of universal and variable life insurance, offering premium and death benefit flexibility along with a range of investment choices, but with market risks.");
            expandableListData.put(listTitles.get(3), group4Items);

            ExpandableListView expandableListView = findViewById(R.id.expandableListView);
            CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(this, listTitles, expandableListData);
            expandableListView.setAdapter(adapter);

            expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
                // Handle group item click (e.g., expand/collapse)
                if (expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    expandableListView.expandGroup(groupPosition);
                }
                return true;
            });
        } catch (Exception e) {
            services.handleException(e);
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
////        getOnBackPressedDispatcher().onBackPressed();
//        return super.onOptionsItemSelected(item);
//    }
}
