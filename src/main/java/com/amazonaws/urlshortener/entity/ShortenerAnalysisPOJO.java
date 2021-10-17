// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.urlshortener.entity;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "url_analysis_table")
public class ShortenerAnalysisPOJO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "url_analysis_sequence")
    @Column(name = "id")
    private long id;

    
    @Column(nullable = false)
    private long lurl;
    
    @Column(nullable = false)
    private Date viewedDate;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getLurl() {
        return lurl;
    }

    public void setLurl(long lurl) {
        this.lurl = lurl;
    }


    public Date getViewedDate() {
        return viewedDate;
    }

    public void setViewedDate(Date viewedDate) {
        this.viewedDate = viewedDate;
    }

}