package org.impulsewellness.biobeatslite.ui.emusicg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.impulsewellness.biobeatslite.R;
import org.impulsewellness.biobeatslite.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.processingComponentRecyclerView;
        ImageButton addButton = binding.addButton;
        addButton.setOnClickListener(v -> showAddDialog());
        return root;
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.AddDialogTheme);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add, null);
        builder.setView(dialogView);

        // Set up the input Spinner
        Spinner inputSpinner = dialogView.findViewById(R.id.input_spinner);
        ArrayList<String> inputs = new ArrayList<>();
        inputs.add("Input 1");
        inputs.add("Input 2");
        inputs.add("Input 3");
        ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, inputs);
        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputSpinner.setAdapter(inputAdapter);

        // Set up the type Spinner
        Spinner typeSpinner = dialogView.findViewById(R.id.type_spinner);
        ArrayList<String> types = new ArrayList<>();
        types.add("Type 1");
        types.add("Type 2");
        types.add("Type 3");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle the Create button click
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle the Cancel button click and dismiss the dialog
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}