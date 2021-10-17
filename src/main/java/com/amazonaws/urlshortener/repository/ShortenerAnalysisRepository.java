// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.urlshortener.repository;

import com.amazonaws.urlshortener.entity.ShortenerAnalysisPOJO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ShortenerAnalysisRepository extends JpaRepository<ShortenerAnalysisPOJO, Long> {
    
}