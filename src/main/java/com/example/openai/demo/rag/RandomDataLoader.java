package com.example.openai.demo.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

//@Component
public class RandomDataLoader {

  private final VectorStore vectorStore;

  public RandomDataLoader(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  @PostConstruct
  public void loadSentencesIntoVectorStore() {
    List<String> sentences = List.of(
            // Category 1: Time Off & Leave Policies (Cluster A)
            "Employees accrue 1.5 days of paid time off (PTO) at the end of every completed calendar month.",
            "Maternity leave provides up to 16 weeks of fully paid leave, while paternity leave offers 4 weeks.",
            "Unused vacation days up to a maximum of 5 days can be rolled over into the next calendar year.",
            "Sick leave requires a valid medical certificate if the absence exceeds three consecutive workdays.",

            // Category 2: Benefits & Compensation (Cluster B)
            "Health insurance coverage begins on the first day of the month following 30 days of employment.",
            "Annual performance reviews take place every November, determining merit-based salary adjustments.",
            "The company matches 401(k) retirement contributions up to 4 percent of an employee's annual salary.",

            // Category 3: Onboarding & Equipment (Cluster C)
            "New hires must complete all mandatory compliance training modules within their first two weeks.",
            "Company laptops and accessories are issued by IT operations during day one orientation.",

            // Category 4: Negation & Policy Restrictions (Testing Edge Cases)
            "Employees are permitted to work remotely up to two days per week with manager approval.",
            "Remote work from international locations is strictly prohibited due to tax and compliance regulations.",

            // Category 5: Out-of-Scope / Noise (Negative Testing)
            "The annual company retreat this year will take place at the lakefront resort in July.",
            "Coffee and snack inventory in the main kitchen is restocked every Monday morning."
    );
     List<Document> documentList = sentences.stream().map(Document::new).toList();
     vectorStore.add(documentList);
  }

}
