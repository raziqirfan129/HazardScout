package com.example.hazardscout;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.hazardscout.util.InsetUtils;

public class AboutActivity extends AppCompatActivity {
    private final int[] profileImages = {
            R.drawable.member_1,
            R.drawable.member_2,
            R.drawable.member_3,
            R.drawable.member_4,
            R.drawable.member_5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbarAbout);

        InsetUtils.applyToolbarInset(toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        TextView txtVersion = findViewById(R.id.txtVersion);
        TextView txtGithub = findViewById(R.id.txtGithub);
        LinearLayout memberContainer = findViewById(R.id.memberContainer);

        txtVersion.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
        makeLink(txtGithub, getString(R.string.github_url));

        String[] names = getResources().getStringArray(R.array.member_names);
        String[] studentNumbers = getResources().getStringArray(R.array.member_student_numbers);
        String[] programmes = getResources().getStringArray(R.array.member_programmes);
        String[] tasks = getResources().getStringArray(R.array.member_tasks);

        int count = Math.min(Math.min(names.length, studentNumbers.length), Math.min(programmes.length, tasks.length));
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < count; i++) {
            android.view.View item = inflater.inflate(R.layout.item_member, memberContainer, false);
            ImageView image = item.findViewById(R.id.imgProfile);
            TextView name = item.findViewById(R.id.txtMemberName);
            TextView details = item.findViewById(R.id.txtMemberDetails);
            TextView task = item.findViewById(R.id.txtMemberTask);

            image.setImageResource(profileImages[Math.min(i, profileImages.length - 1)]);
            name.setText(names[i]);
            details.setText(studentNumbers[i] + "\n" + programmes[i]);
            task.setText(getString(R.string.task_format, tasks[i]));
            memberContainer.addView(item);
        }
    }

    private void makeLink(TextView textView, String url) {
        SpannableString text = new SpannableString(url);
        text.setSpan(new URLSpan(url), 0, url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
