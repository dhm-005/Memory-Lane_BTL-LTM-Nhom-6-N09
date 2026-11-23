
package com.mcserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionCard {
    private Long id;
    private String code;
    private String category;
    private String name;
    private String description;    
    private int price;
}

