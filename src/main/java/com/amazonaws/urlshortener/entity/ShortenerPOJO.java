// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.urlshortener.entity;

import javax.persistence.*;
import java.util.Date;



/*
Mayur Comments
Please note the initial value which is 1 Cr to get 4 digit short URL
Please note allocation size, if we dont specify, after start and stop of application Hibernate will skip 50 increments
*/
@Entity
@NamedQuery(name = "ShortenerPOJO.findByUrl", query = "SELECT u FROM ShortenerPOJO u WHERE LOWER(u.longUrl) = LOWER(?1)")
@SequenceGenerator(name = "url_short_sequence", sequenceName = "url_short_sequence",  initialValue = 10000, allocationSize = 1)
@Table(name = "url_short_table")
public class ShortenerPOJO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "url_short_sequence")
    @Column(name = "id")
    private long id;

    @Column(nullable = false)
    private int randNum;

    @Column(nullable = false)
    private String longUrl;

    @Column(nullable = false)
    private Date createdDate;

    private Date expiresDate;


    public int getRandNum() {
        return randNum;
    }

    public void setRandNum(int randNum) {
        this.randNum = randNum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpiresDate() {
        return expiresDate;
    }

    public void setExpiresDate(Date expireDate) {
        this.expiresDate = expireDate;
    }
}