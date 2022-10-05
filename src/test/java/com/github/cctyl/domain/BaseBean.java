package com.github.cctyl.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

//@MappedSuperclass
@Data
public class BaseBean extends  TopBean {


    @Column(name = "test_name")
    private String testName;

    @Transient
    private String testProperty;

}
