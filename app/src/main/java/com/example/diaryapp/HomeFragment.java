package com.example.diaryapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaryapp.models.Diary;
import com.example.diaryapp.models.DiaryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;
    private List<Diary> diaryList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化 Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "請先登入", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 設置 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        diaryList = new ArrayList<>();
        diaryAdapter = new DiaryAdapter(diaryList, this::onDiaryClicked);
        recyclerView.setAdapter(diaryAdapter);

        // Firebase Realtime Database 參考
        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("diaries");

        // 從 Firebase 加載日記
        loadDiariesFromFirebase();

        // 新增日記按鈕
        Button btnAddDiary = view.findViewById(R.id.btn_add_diary);
        btnAddDiary.setOnClickListener(v -> {
            // 跳轉到 DiaryFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DiaryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadDiariesFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                diaryList.clear(); // 清空舊的列表

                for (DataSnapshot diarySnapshot : snapshot.getChildren()) {
                    Diary diary = diarySnapshot.getValue(Diary.class);
                    if (diary != null) {
                        diary.setDiaryId(diarySnapshot.getKey()); // 設置日記 ID
                        diaryList.add(diary);
                    }
                }

                diaryAdapter.notifyDataSetChanged(); // 通知 Adapter 資料已更新
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "無法加載日記：" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDiaryClicked(Diary diary) {
        Bundle args = new Bundle();
        args.putString("diaryId", diary.getDiaryId());
        args.putString("date", diary.getDate());
        args.putString("subject", diary.getSubject());
        args.putString("content", diary.getContent());

        DiaryFragment diaryFragment = new DiaryFragment();
        diaryFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, diaryFragment)
                .addToBackStack(null)
                .commit();
    }
}



