package com.oibsip.library.repository;

import com.oibsip.library.model.IssueRecord;
import com.oibsip.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findByReturnDateIsNull();

    List<IssueRecord> findByUser(User user);

    List<IssueRecord> findByUserAndReturnDateIsNull(User user);
}