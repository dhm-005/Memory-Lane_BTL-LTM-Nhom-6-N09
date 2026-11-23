
package com.mcserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionCard {
    public Long id;
    public String category;
    public String name;
    public String description;    
    public int price;
    
}

