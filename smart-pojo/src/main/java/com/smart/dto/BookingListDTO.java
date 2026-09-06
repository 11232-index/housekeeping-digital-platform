package com.smart.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BookingListDTO implements Serializable {

    private Long serviceItemId;
    private Long servicePackageId;
    private String requirement;

}
