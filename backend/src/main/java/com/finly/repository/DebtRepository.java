package com.finly.repository;

import com.finly.model.Debt;
import com.finly.model.DebtType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByUserIdAndReferenceMonthAndReferenceYear(
        Long userId, Integer month, Integer year
    );

    List<Debt> findByUserIdAndIsFixedTemplateTrue(Long userId);

    List<Debt> findByUserIdAndType(Long userId, DebtType type);

    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId " +
           "AND d.referenceYear = :year " +
           "AND d.referenceMonth = :month " +
           "AND d.parentDebt IS NULL")
    List<Debt> findMainDebtsByUserAndMonthYear(
        @Param("userId") Long userId,
        @Param("month") Integer month,
        @Param("year") Integer year
    );

    @Query("SELECT d FROM Debt d WHERE d.parentDebt.id = :parentId ORDER BY d.currentInstallment")
    List<Debt> findInstallmentsByParentDebt(@Param("parentId") Long parentId);

    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId " +
           "AND d.referenceYear = :year " +
           "AND d.referenceMonth = :month " +
           "AND (d.parentDebt IS NOT NULL OR SIZE(d.installments) = 0)")
    List<Debt> findActiveDebtsByUserAndMonthYear(
        @Param("userId") Long userId,
        @Param("month") Integer month,
        @Param("year") Integer year
    );
}
