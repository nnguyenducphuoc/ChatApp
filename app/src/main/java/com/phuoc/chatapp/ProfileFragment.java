package com.phuoc.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;
import com.phuoc.chatapp.model.UserModel;
import com.phuoc.chatapp.utils.AndroidUtil;
import com.phuoc.chatapp.utils.FirebaseUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfileFragment extends Fragment {
    ImageView profilePic;
    EditText usernameInput, phoneInput;
    Button updateProfileBtn;
    ProgressBar progressBar;
    TextView logoutBtn;
    UserModel currentUserModel;
    Uri selectedImageUri;

    ActivityResultLauncher<Intent> imagePickLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == Activity.RESULT_OK) {
                            Intent data = o.getData();
                            if (data != null && data.getData() != null) {
                                selectedImageUri = data.getData();
                                AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                            }
                        }
                    }
                });
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfileBtn = view.findViewById(R.id.profile_update_btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);

        getUserData();
        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBtnClick();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUtil.logout();
                        Intent intent = new Intent(getContext(), SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(getActivity()).cropSquare().compress(512).maxResultSize(512, 512)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePickLauncher.launch(intent);
                                return null;
                            }
                        });
            }
        });

        return view;
    }

    void updateBtnClick() {
        String newUsername = usernameInput.getText().toString();
        if (newUsername.isEmpty() || newUsername.length()<3) {
            usernameInput.setError("Username length should be at least 3 chars");
            return;
        }

        currentUserModel.setUsername(newUsername);
        setInProgress(true);

        if (selectedImageUri != null) {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            updateToFirestore();
                        }
                    });
        } else {
            updateToFirestore();
        }



    }

    void updateToFirestore() {
        FirebaseUtil.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()) {
                           setInProgress(false);
                           AndroidUtil.showToast(getContext(), "Update successfully");
                       } else {
                           AndroidUtil.showToast(getContext(), "Update failed");
                       }
                    }
                });
    }

    void getUserData() {
        setInProgress(true);

        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri uri = task.getResult();
                                    AndroidUtil.setProfilePic(getContext(), uri, profilePic);
                                }
                            }
                        });

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    setInProgress(false);
                    currentUserModel = task.getResult().toObject(UserModel.class);
                    usernameInput.setText(currentUserModel.getUsername());
                    phoneInput.setText(currentUserModel.getPhone());

                }
            }
        });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}