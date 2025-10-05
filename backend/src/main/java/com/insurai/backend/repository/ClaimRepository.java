package com.insurai.backend.repository;
import com.insurai.backend.entity.Claim;
import com.insurai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.insurai.backend.entity.Policy;
import com.insurai.backend.entity.Employee;


public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByEmployee(Employee employee);
    List<Claim> findByAssignedAgent(User agent);
    List<Claim> findByPolicy(Policy policy);

}
