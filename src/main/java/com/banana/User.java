/*
 * PEARSON PROPRIETARY AND CONFIDENTIAL INFORMATION SUBJECT TO NDA
 * Copyright (c) 2017 Pearson Education, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Pearson Education, Inc. The intellectual and technical concepts contained
 * herein are proprietary to Pearson Education, Inc. and may be covered by U.S.
 * and Foreign Patents, patent applications, and are protected by trade secret
 * or copyright law. Dissemination of this information, reproduction of this
 * material, and copying or distribution of this software is strictly forbidden
 * unless prior written permission is obtained from Pearson Education, Inc.

 */
package com.banana;

import com.couchbase.client.java.repository.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;

import javax.validation.constraints.*;
import java.io.Serializable;

import static org.springframework.data.couchbase.core.mapping.id.GenerationStrategy.UNIQUE;

@Document(expiry = 3000)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = UNIQUE)
    @NotEmpty
    public String id;

    @NotNull
    public String firstName;

    @NotBlank
    public String lastName;

    @Min(5)
    @Max(20)
    public int activeMinutes;
}