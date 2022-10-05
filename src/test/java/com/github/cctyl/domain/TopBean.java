package com.github.cctyl.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
public class TopBean {


    @Column(name = "top_name")
    private  String topName;

}
