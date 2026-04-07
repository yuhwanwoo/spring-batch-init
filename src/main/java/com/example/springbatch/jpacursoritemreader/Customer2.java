package com.example.springbatch.jpacursoritemreader;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
public class Customer2 {

    @Id
    private long id;
    private String firstName;
    private String lastName;
    private String birthdate;
}