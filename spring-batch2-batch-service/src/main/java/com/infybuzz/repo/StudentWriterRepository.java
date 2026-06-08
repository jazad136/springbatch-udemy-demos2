package com.infybuzz.repo;

import org.springframework.data.repository.CrudRepository;

import com.infybuzz.mysql.entity.Student;

public interface StudentWriterRepository extends CrudRepository<Student, Long>{

}
