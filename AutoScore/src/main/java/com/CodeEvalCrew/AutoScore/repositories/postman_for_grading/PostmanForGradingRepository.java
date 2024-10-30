package com.CodeEvalCrew.AutoScore.repositories.postman_for_grading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;

@Repository
public interface PostmanForGradingRepository extends JpaRepository<Postman_For_Grading, Long>{ 
    
}
