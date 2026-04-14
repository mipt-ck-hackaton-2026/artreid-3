package com.ck.hackaton.artreid_3.artreid3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataLoadResponseDTO {
    private int loaded;
    private int updated;
    private int skipped;
    private int errors;
}
