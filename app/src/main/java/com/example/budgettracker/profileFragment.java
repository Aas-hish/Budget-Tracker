package com.example.budgettracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class profileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView chooseImageButton;
    private ImageView profileImageView;
    private TextView profileNameTextView;
    private TextView profileEmailTextView;
    private Button logOutButton;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final String TAG = "ProfileFragment";

    public profileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate called");

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        Log.d(TAG, "Firebase components initialized");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        profileImageView = view.findViewById(R.id.profile_image);
        chooseImageButton = view.findViewById(R.id.choose_image_button);
        profileNameTextView = view.findViewById(R.id.profile_name);
        profileEmailTextView = view.findViewById(R.id.profile_email);
        logOutButton = view.findViewById(R.id.logout_button);

        Log.d(TAG, "UI components initialized");

        // Set up button click listeners
        chooseImageButton.setOnClickListener(v -> openImagePicker());

        logOutButton.setOnClickListener(v -> logout());

        // Load user profile information
        loadUserProfile();

        return view;
    }

    private void logout() {
        Log.d(TAG, "Logout clicked");

        if (firebaseAuth != null) {
            // Sign out the user
            firebaseAuth.signOut();
            Log.d("sign", "User signed out");
        }

        if (isAdded() && getActivity() != null) {
            // Get the activity context
            Activity activity = getActivity();

            // Navigate to the login activity
            Intent intent = new Intent(activity, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Finish the current activity to prevent returning back to it
            activity.finish();
            Log.d("navi", "Navigated to login activity and finished current activity");
        } else {
            // Handle the case where the activity is null (which shouldn't normally happen)
            Toast.makeText(getContext(), "Error: Unable to log out", Toast.LENGTH_SHORT).show();
            Log.e("null", "Activity is null during logout");
        }
    }

    private void loadUserProfile() {
        Log.d(TAG, "Loading user profile");

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Current user is null");
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    String name = task.getResult().child("name").getValue(String.class);
                    String email = task.getResult().child("email").getValue(String.class);
                    String profileImageUrl = task.getResult().child("profileImageUrl").getValue(String.class);

                    profileNameTextView.setText("Hi, " + name);
                    profileEmailTextView.setText(email);

                    // Load the profile image using Glide if the URL is available
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(getContext()).load(profileImageUrl).into(profileImageView);
                    }
                    Log.d(TAG, "User profile loaded successfully");
                }
            } else {
                Toast.makeText(getContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load user profile", task.getException());
            }
        });
    }

    private void openImagePicker() {
        Log.d(TAG, "Opening image picker");

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Log.d(TAG, "Image selected: " + imageUri.toString());
            // Upload the image to Firebase
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's UID
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Create a reference to the profile_images directory in Firebase Storage
        StorageReference imageRef = storageReference.child(userId + "/profile_image.jpg");

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();

                        // Update the user's profile with the new image URL
                        databaseReference.child(userId).child("profileImageUrl").setValue(profileImageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Profile image URL updated in database");
                                    loadUserProfile(); // Refresh profile to show updated image
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to update profile image", e);
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to upload image", e);
                });
    }
}
