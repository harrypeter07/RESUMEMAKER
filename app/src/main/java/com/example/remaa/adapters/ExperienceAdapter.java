package com.example.remaa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.remaa.R;
import com.example.remaa.models.Experience;

import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder> {
    private List<Experience> experiences;

    public ExperienceAdapter(List<Experience> experiences) {
        this.experiences = experiences;
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experience, parent, false);
        return new ExperienceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        Experience experience = experiences.get(position);
        holder.bind(experience);
    }

    @Override
    public int getItemCount() {
        return experiences.size();
    }

    public void updateExperiences(List<Experience> newExperiences) {
        this.experiences = newExperiences;
        notifyDataSetChanged();
    }

    static class ExperienceViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView companyText;
        private final TextView dateText;
        private final TextView descriptionText;

        public ExperienceViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.experienceTitleText);
            companyText = itemView.findViewById(R.id.experienceCompanyText);
            dateText = itemView.findViewById(R.id.experienceDateText);
            descriptionText = itemView.findViewById(R.id.experienceDescriptionText);
        }

        public void bind(Experience experience) {
            titleText.setText(experience.getTitle());
            companyText.setText(experience.getCompany());
            String dateRange = experience.getStartDate() + " - " + 
                (experience.getEndDate().isEmpty() ? "Present" : experience.getEndDate());
            dateText.setText(dateRange);
            descriptionText.setText(experience.getDescription());
        }
    }
} 