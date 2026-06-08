package com.infybuzz.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.infybuzz.postgresql.entity.Student;

@Repository
public interface StudentReaderRepository extends CrudRepository<Student, Long>{

}
